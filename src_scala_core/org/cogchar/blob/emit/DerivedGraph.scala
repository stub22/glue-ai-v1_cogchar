/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.cogchar.blob.emit
import org.appdapter.core.name.Ident

import org.appdapter.core.log.{BasicDebugger};
import org.appdapter.core.name.{Ident, FreeIdent};
import org.appdapter.core.store.{Repo, InitialBinding }
import org.appdapter.help.repo.{RepoClient, RepoClientImpl, InitialBindingImpl, SolutionList} 
import org.appdapter.impl.store.{FancyRepo};
import org.appdapter.core.matdat.{SheetRepo}

import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory

/**
 * @author Stu B. <www.texpedient.com>
 *
 */

import com.hp.hpl.jena.rdf.model.Resource;
import org.appdapter.core.item.{Item, JenaResourceItem}
trait TypedResrc extends Ident with Item {
	def hasTypeMark(typeID : Ident) : Boolean = false
}
class JenaTR(r : Resource, val myTypes : Set[Ident]) extends JenaResourceItem(r) with TypedResrc {
	override def hasTypeMark(typeID : Ident) : Boolean = myTypes.contains(typeID)
}
/*
object JTR_Factory {
	 def wacky() : Unit { }
}
*/
case class PipelineQuerySpec(val pplnAttrQueryQN : String, val pplnSrcQueryQN : String, val pplnGraphQN : String) 

import org.cogchar.name.dir.NamespaceDir;

object DerivedGraphNames {
	// val		OPCODE_UNION = "UNION";
	val		V_pipeID = "pipeID"
	val		V_typeID = "typeID"
	
	/* opTypeID is one of ccrt:UnionModel, 
	 * useTypeID is one of ccrt:BehaviorModel, 	 */

	val		T_union = new FreeIdent(NamespaceDir.NS_CCRT_RT + "UnionModel");
	val		P_sourceModel = new FreeIdent(NamespaceDir.NS_CCRT_RT + "sourceModel");
}
class DerivedGraphSpec(val myTargetGraphTR : TypedResrc,  var myInGraphIDs : Set[Ident]) extends BasicDebugger {
	override def toString() : String = {
		"DerivedGraphSpec[targetTR=" + myTargetGraphTR + ", inGraphs=" + myInGraphIDs + "]";
	}
	def isUnion() : Boolean = myTargetGraphTR.hasTypeMark(DerivedGraphNames.T_union)
	def getStructureTypeID() : Ident = {
		if (isUnion()) {
			DerivedGraphNames.T_union
		} else  {
			DerivedGraphNames.T_union
		}
	}
	def makeDerivedModel(sourceRepo : Repo) : Model = {
		getStructureTypeID() match { 
			case DerivedGraphNames.T_union => {
				var cumUnionModel = ModelFactory.createDefaultModel();
				for (srcGraphID <- myInGraphIDs) {
					val srcGraph = sourceRepo.getNamedModel(srcGraphID)
					// TODO : when upgrading (to Jena v2.8?) use  ModelFactory.createUnion();
					cumUnionModel = cumUnionModel.union(srcGraph)
				}
				cumUnionModel
			}
			case x => {
				getLogger().warn("Unknown structure type {}", x)
				ModelFactory.createDefaultModel()
			}
		}
	}
}

class DerivedGraph extends BasicDebugger  {

}


object DerivedGraphSpecReader extends BasicDebugger {

    def queryDerivedGraphSpecs (rc : RepoClient, pqs : PipelineQuerySpec) : Set[DerivedGraphSpec] = {
		
		var pipeAttrSL : SolutionList = null;
		try {
			pipeAttrSL = rc.queryIndirectForAllSolutions(pqs.pplnAttrQueryQN, pqs.pplnGraphQN)		
		} catch {
			case t: Throwable =>  {
				getLogger().error("Problem executing querySpec {} on repoClient {} ", pqs, rc)
				getLogger().error("Stack trace: ", t)
				return Set[DerivedGraphSpec]()
			}
		}
		
		val pipeTypeSetsByID = new scala.collection.mutable.HashMap[Ident, Set[Ident]]() 
		import scala.collection.JavaConversions._
		val pjl = pipeAttrSL.javaList
		getLogger().info("Got pipeAttribute list : {}", pjl)
		pjl foreach (psp  => {
			// A pipe is the result of a single operation applied to a (poss. ordered by query) set of sources
			val pipeID = psp.getIdentResultVar(DerivedGraphNames.V_pipeID)
			// Each pipe-spec will have one or more types, which may be viewed as classifiers of both how the pipe
			// is constructed (the type of pipe structure) and how its output is used (the type of pipe-outut contents).
			// For now we assume that it is viable to recognize the opTypeID and useTypeID during this read process.
			val aTypeID = psp.getIdentResultVar(DerivedGraphNames.V_typeID)
			val pipeTypeSet : Set[Ident] = if (pipeTypeSetsByID.contains(pipeID)) {
				pipeTypeSetsByID.get(pipeID).get + aTypeID
			} else {
				Set(aTypeID)
			}
			pipeTypeSetsByID.put(pipeID, pipeTypeSet)
		})
		val pipesByID = new scala.collection.mutable.HashMap[Ident, DerivedGraphSpec]()
		val pipeGraphID = rc.getRepo.makeIdentForQName(pqs.pplnGraphQN)
		val pipeModel = rc.getRepo.getNamedModel(pipeGraphID)
		var dgSpecSet = Set[DerivedGraphSpec]()
		for ((pipeKeyID, typeSet) <- pipeTypeSetsByID) {
			val pipeGraphRes = pipeModel.getResource(pipeKeyID.getAbsUriString())
			val typedRes = new JenaTR(pipeGraphRes, typeSet)
			val linkedPipeSrcItems = typedRes.getLinkedItemSet(DerivedGraphNames.P_sourceModel);
			// Note JavaConverters is not the same as JavaConversions
			import scala.collection.JavaConverters._
			val scalaSet : Set[Ident] = linkedPipeSrcItems.asScala.map(_.asInstanceOf[Ident]).toSet
			val dgSpec = new DerivedGraphSpec(typedRes, scalaSet)
			dgSpecSet = dgSpecSet + dgSpec
		}
		dgSpecSet
	}		
}