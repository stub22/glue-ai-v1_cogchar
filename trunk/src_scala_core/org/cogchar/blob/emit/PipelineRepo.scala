/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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

import com.hp.hpl.jena.query.DataSource
import com.hp.hpl.jena.query.Dataset
import com.hp.hpl.jena.query.DatasetFactory
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.rdf.model.ModelFactory
import java.lang.Override
import org.appdapter.core.name.Ident
import org.appdapter.core.store.Repo
import org.appdapter.impl.store.DirectRepo
import scala.collection.JavaConversions.asScalaSet

/**
 * @author LogicMOO <www.logicmoo.org>
 */

// FIXME:  The srcRepo should really not be given to the RepoSpec, because it
// is not serializable specData.
class PipelineRepoSpec(val myPipeSpecs : Set[DerivedGraphSpec], val mySrcRepo : Repo.WithDirectory) extends RepoSpec {
	override def toString(): String = {
		"PipelineRepoSpec[pipeSpecs= " + myPipeSpecs + "]";
	}
	override def makeRepo(): PipelineRepo = {
		val emptyDirModel = ModelFactory.createDefaultModel();			
		new PipelineRepo(emptyDirModel, this) 
		
	}
}

// @TODO to be moved to org.appdapter.lib.core
class PipelineRepo(emptyDirModel : Model, val myRepoSpec : PipelineRepoSpec) extends DirectRepo(emptyDirModel) {
	/*  Don't need this right away
	@Override def getDfltQrySrcGraphQName(): String = {
		mySrcPipeGraphID.getAbsUriString()
	}
	*/
	/*  We inherit this from DirectRepo
	 override def makeMainQueryDataset(): Dataset = {
	 // this is lazy as we can get
	 val mainDset: Dataset = DatasetFactory.create() // becomes   createMem() in later Jena versions.
	 val mainDsource: DataSource = mainDset.asInstanceOf[DataSource];
	 populateFromSourceSet(mainDsource, mySourceIdSet)
	 mainDset;
	 }
	 */
	// TODO:  Move this method up to Appdapter.DirectRepo
	def replaceNamedModel(modelID : Ident, jenaModel : Model) {
		val	repoDset : Dataset = getMainQueryDataset
		val repoDsource : DataSource = repoDset.asInstanceOf[DataSource];
		repoDsource.replaceNamedModel(modelID.getAbsUriString, jenaModel);
	}
  
	def loadSheetModelsIntoMainDataset() = {
		// this calls our 
		getMainQueryDataset; // calls makeMainQueryDataset
	}
	protected def populateUnionGraphFromSourceRepo(tgtUnionGraphID : Ident, srcGraphIDs: java.util.Set[Ident]) = {
		val sourceRepo = myRepoSpec.mySrcRepo;
		var cumUnionModel = ModelFactory.createDefaultModel();
		for (srcGraphID <- srcGraphIDs.toList) {
			val srcGraph = sourceRepo.getNamedModel(srcGraphID)
			cumUnionModel = cumUnionModel.union(srcGraph)
		}
		replaceNamedModel(tgtUnionGraphID, cumUnionModel)
	}
	/*
	import scala.collection.JavaConversions._;
	protected def populateFromSourceSet(mainDset: DataSource, aSourceIdSet: java.util.Set[Ident]) = {
		val pipeUri = mySrcPipeGraphID.getAbsUriString;
		for (srcModelID <- aSourceIdSet.toList) {
			try {
				val srcModel: Model = dirRepo.getNamedModel(srcModelID);
				if (srcModel == null)
					throw new RuntimeException("PipelineRepo: no named repo called " + name + " findable by " + myName)
				var destModel: Model = mainDset.getNamedModel(pipeUri);
				if (destModel == null) {
					destModel = ModelFactory.createDefaultModel();
				}
				val resultModel = destModel.union(srcModel);
				mainDset.replaceNamedModel(pipeUri, resultModel);
				getLogger.debug("Loaded: " + name + " s=" + srcModel.size + " d=" + destModel.size + " r=" + resultModel.size + " into " + myName)
			} catch {
				case except => {
						println(" ex " + except);
						getLogger.error("PipelineRepo: error loading " + name + " into " + myName, except)
					}
			}

		}
	}
	*/

	def showDebugInfo() = {
		loadSheetModelsIntoMainDataset()
		//findAllSolutions(JenaArqQueryFuncs.parseQueryText(""))
	}

}
