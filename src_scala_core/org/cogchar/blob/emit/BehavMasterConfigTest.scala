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

import org.appdapter.core.name.{Ident, FreeIdent}
import org.appdapter.core.store.{Repo, InitialBinding }
import org.appdapter.help.repo.{RepoClient, RepoClientImpl, InitialBindingImpl} 
import org.appdapter.impl.store.{FancyRepo};
import org.appdapter.core.matdat.{SheetRepo}
import com.hp.hpl.jena.query.{QuerySolution} // Query, QueryFactory, QueryExecution, QueryExecutionFactory, , QuerySolutionMap, Syntax};
import com.hp.hpl.jena.rdf.model.{Model}
import org.cogchar.impl.perform.{ChannelSpec, ChannelNames};
import org.cogchar.impl.scene.{SceneSpec, SceneBook};
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.platform.util.ClassLoaderUtils;
import org.osgi.framework.BundleContext;

object BehavMasterConfigTest extends BasicDebugger {
	// These constants are used to test the ChanBinding model found in "GluePuma_BehavMasterDemo"
	//   https://docs.google.com/spreadsheet/ccc?key=0AlpQRNQ-L8QUdFh5YWswSzdYZFJMb1N6aEhJVWwtR3c
	final val BMC_SHEET_KEY = "0AlpQRNQ-L8QUdFh5YWswSzdYZFJMb1N6aEhJVWwtR3c"
	final val BMC_NAMESPACE_SHEET_NUM = 4
	final val BMC_DIRECTORY_SHEET_NUM = 3
	final val QUERY_SOURCE_GRAPH_QN = "ccrt:qry_sheet_77";
	final val TGT_GRAPH_SPARQL_VAR = RepoSpecDefaultNames.DFLT_TGT_GRAPH_SPARQL_VAR; // "qGraph"
	final val CHAN_BIND_GRAPH_QN = "hrk:chan_sheet_77"
	final val BEHAV_STEP_GRAPH_QN = "hrk:behavStep_sheet_77"
	final val BEHAV_SCENE_GRAPH_QN = "hrk:behavScene_sheet_77"
	final val PIPELINE_GRAPH_QN = "hrk:pipeline_sheet_77"
	

	def makeBMC_RepoSpec(ctx : BundleContext) : OnlineSheetRepoSpec = { 				
		val fileResModelCLs : java.util.List[ClassLoader] = 
				ClassLoaderUtils.getFileResourceClassLoaders(ctx, ClassLoaderUtils.ALL_RESOURCE_CLASSLOADER_TYPES);
		makeBMC_RepoSpec(fileResModelCLs);
	}
	def makeBMC_RepoSpec(fileResModelCLs : java.util.List[ClassLoader]) : OnlineSheetRepoSpec = { 				
		new OnlineSheetRepoSpec(BMC_SHEET_KEY, BMC_NAMESPACE_SHEET_NUM, BMC_DIRECTORY_SHEET_NUM, fileResModelCLs);
	}
	def readChannelSpecs(repoClient : RepoClient, chanGraphQN : String) : java.util.Set[ChannelSpec] = {
		val specSet = new java.util.HashSet[ChannelSpec]();
		val objectsFound : java.util.Set[Object] = repoClient.assembleRootsFromNamedModel(chanGraphQN);
		if (objectsFound != null) {
			import scala.collection.JavaConversions._;
			for (o <- objectsFound) {
				o match {
					case cspec : ChannelSpec => specSet.add(cspec)
					case _ => getLogger().warn("Unexpected object found in {} = {}", chanGraphQN, o);
				}
			}
		} else {
			getLogger().error("Channel root assemble returned null for graph {}", chanGraphQN);
		}
		specSet;
	} 
	def readSceneSpecs(repoClient : RepoClient, behavGraphQN : String) : java.util.List[SceneSpec] = {
		val	behavGraphID = repoClient.makeIdentForQName(behavGraphQN);
		
		val allBehavSpecs = repoClient.assembleRootsFromNamedModel(behavGraphID);
		val ssList = SceneBook.filterSceneSpecs(allBehavSpecs);
		getLogger().info("Loaded SceneSpecs: " + ssList);
		
		ssList
	}
	def queryPipelineSpecs (rc : RepoClient) : java.util.Collection[PipelineSpec] = {
		
		val pplnQueryQN = "ccrt:find_pipes_77" // The QName of a query in the "Queries" model/tab
		val pplnGraphQN = "hrk:pipeline_sheet_77" // The QName of a graph = model = tab, as given by directory model.
		val solList = rc.queryIndirectForAllSolutions(pplnQueryQN, pplnGraphQN)
		
		val resultJMap = new java.util.HashMap[Ident, PipelineSpec]();
		import scala.collection.JavaConversions._
		val solJList = solList.javaList
		getLogger().info("Got pipeSpec-piece solJList: {}", solJList)
		solJList foreach (psp  => {
				val pipeID = psp.getIdentResultVar("pipeID")
				val sourceID = psp.getIdentResultVar("sourceID")
				var pipeSpec = resultJMap.get(pipeID);
				if (pipeSpec == null) {
					pipeSpec = new PipelineSpec(pipeID);
					resultJMap.put(pipeID, pipeSpec)
				}
				pipeSpec.mySourceIdSet.add(sourceID)
			})
		resultJMap.values

	}	
	def main(args: Array[String]) : Unit = {
		// Must enable "compile" or "provided" scope for Log4J dep in order to compile this code.
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);

		val fileResModelCLs =  new java.util.ArrayList[ClassLoader]();
		val bmcRepoSpec = makeBMC_RepoSpec(fileResModelCLs);
		
		val bmcMemoryRepoHandle = bmcRepoSpec.makeRepo();	
		
		println("Loaded Repo: " + bmcMemoryRepoHandle)
		
		val graphStats = bmcMemoryRepoHandle.getGraphStats();
		println("Got graphStats: " + graphStats)
		import scala.collection.JavaConversions._
		for (gs <- graphStats) {
			println("GraphStat: " + gs)
		}
	
		// val bmcRepoCli = bmcRepoSpec.makeRepoClient(bmcMemoryRepoHandle);
		// ..just does:
		// new RepoClientImpl(repo, getDfltTgtGraphSparqlVarName, getDfltQrySrcGraphQName);
		val bmcRepoCli = new RepoClientImpl(bmcMemoryRepoHandle, TGT_GRAPH_SPARQL_VAR, QUERY_SOURCE_GRAPH_QN)
		
		println("Repo Client: " + bmcRepoCli)
		// Use an arbitrarily assumed name for the ChannelBinding Graph (as set in the "Dir" model of the source repo).
		val chanSpecs = readChannelSpecs(bmcRepoCli, CHAN_BIND_GRAPH_QN);
		getLogger().info("Found chanSpecs: " + chanSpecs)
		import scala.collection.JavaConversions._;
		for (c <- chanSpecs) {
			val chanID = c.getChannelID();
			val chanTypeID = c.getChannelTypeID();
			val chanOSGiFilter = c.getOSGiFilterString();
			println("Channel id=" + chanID + ", type=" + chanTypeID + ", filter=" + chanOSGiFilter)
		}
		// Dump out some channel-type constants
		// println ("AnimOut-Best=" + ChannelNames.getOutChanIdent_AnimBest)
		// println("SpeechOut-Best=" + ChannelNames.getOutChanIdent_SpeechMain)
		 
		// Create a new repo of kind "computed" or "derived"
		 val pipeSpecs = queryPipelineSpecs(bmcRepoCli)
		 for (ps <- pipeSpecs) {
			 println("Got PipeSpec: " + ps)
		 }
		 
		val sceneSpecs = readSceneSpecs(bmcRepoCli, BEHAV_SCENE_GRAPH_QN)
	}
	
}
class PipelineSpec(val myPipeID : Ident) {
	val mySourceIdSet = new java.util.HashSet[Ident]();
	override def toString() : String = {
		"PipelineSpec[pipeID=" + myPipeID + ", sourceIDS=" + mySourceIdSet + "]";
	}
}

