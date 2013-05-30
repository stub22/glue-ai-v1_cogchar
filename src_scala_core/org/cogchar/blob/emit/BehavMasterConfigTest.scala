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
import org.cogchar.impl.perform.{PerfChannelNames};
import org.cogchar.name.behavior.{MasterDemoNames};
import org.cogchar.impl.channel.{FancyChannelSpec};
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
	
	// These constants are used to test the ChanBinding model found in "GluePuma_BehavMasterDemo"
	//   https://docs.google.com/spreadsheet/ccc?key=0AlpQRNQ-L8QUdFh5YWswSzdYZFJMb1N6aEhJVWwtR3c
	// When exported to Disk
    final val BMC_WORKBOOK_PATH = "GluePuma_BehavMasterDemo.xlsx"
	final val BMC_NAMESPACE_SHEET_NAME = "Nspc"
    final val BMC_DIRECTORY_SHEET_NAME = "Dir"
	
	
	final val TGT_GRAPH_SPARQL_VAR = RepoSpecDefaultNames.DFLT_TGT_GRAPH_SPARQL_VAR; // "qGraph"

	
	val QUERY_SOURCE_GRAPH_QN = MasterDemoNames.QUERY_SOURCE_GRAPH_QN;
	val CHAN_BIND_GRAPH_QN = MasterDemoNames.CHAN_BIND_GRAPH_QN;
	val BEHAV_STEP_GRAPH_QN = MasterDemoNames.BEHAV_STEP_GRAPH_QN;
	val BEHAV_SCENE_GRAPH_QN = MasterDemoNames.BEHAV_SCENE_GRAPH_QN;
	val DERIVED_BEHAV_GRAPH_QN = MasterDemoNames.DERIVED_BEHAV_GRAPH_QN;

	val PIPELINE_GRAPH_QN = MasterDemoNames.PIPELINE_GRAPH_QN;
	val PIPE_ATTR_QQN = MasterDemoNames.PIPE_QUERY_QN; 
	val PIPE_SOURCE_QQN = MasterDemoNames.PIPE_SOURCE_QUERY_QN;
	
	

	def makeBMC_RepoSpec(ctx : BundleContext) : OnlineSheetRepoSpec = { 				
		val fileResModelCLs : java.util.List[ClassLoader] = 
				ClassLoaderUtils.getFileResourceClassLoaders(ctx, ClassLoaderUtils.ALL_RESOURCE_CLASSLOADER_TYPES);
		makeBMC_RepoSpec(fileResModelCLs);
	}
	def makeBMC_RepoSpec(fileResModelCLs : java.util.List[ClassLoader]) : OnlineSheetRepoSpec = { 				
		new OnlineSheetRepoSpec(BMC_SHEET_KEY, BMC_NAMESPACE_SHEET_NUM, BMC_DIRECTORY_SHEET_NUM, fileResModelCLs);
	}
    
  
  	def makeBMC_OfflineRepoSpec(ctx : BundleContext) : OfflineXlsSheetRepoSpec = { 				
		val fileResModelCLs : java.util.List[ClassLoader] = 
				ClassLoaderUtils.getFileResourceClassLoaders(ctx, ClassLoaderUtils.ALL_RESOURCE_CLASSLOADER_TYPES);
		makeBMC_OfflineRepoSpec(fileResModelCLs);
	}
	def makeBMC_OfflineRepoSpec(fileResModelCLs : java.util.List[ClassLoader]) : OfflineXlsSheetRepoSpec = { 				
		new OfflineXlsSheetRepoSpec(BMC_WORKBOOK_PATH, BMC_NAMESPACE_SHEET_NAME, BMC_DIRECTORY_SHEET_NAME, fileResModelCLs);
	}
  
	def readChannelSpecs(repoClient : RepoClient, chanGraphQN : String) : java.util.Set[FancyChannelSpec] = {
		val specSet = new java.util.HashSet[FancyChannelSpec]();
		val objectsFound : java.util.Set[Object] = repoClient.assembleRootsFromNamedModel(chanGraphQN);
		if (objectsFound != null) {
			import scala.collection.JavaConversions._;
			for (o <- objectsFound) {
				o match {
					case cspec : FancyChannelSpec => specSet.add(cspec)
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
	val EQBAR = "=========================================================================================="
	
	def readSceneSpecsFromDerivedRepo(srcRepoCli : RepoClient, pqs : PipelineQuerySpec, derivedBehavGraphID : Ident) : java.util.List[SceneSpec] = {
		println(EQBAR + "\nReading derived graph specs using " + pqs + "thru repoClient [" + srcRepoCli + "]");
				// queri		[attr=" + pplnAttrQueryQN + ", sources=" + pplnSrcQueryQN + "] on graph [" 	+ pplnGraphQN +  "] 
				
				
		 val dgSpecSet : Set[DerivedGraphSpec] = DerivedGraphSpecReader.queryDerivedGraphSpecs(srcRepoCli, pqs)
		 for (dg <- dgSpecSet) {
			 println("Got derived graph spec: " + dg)
			 // ps.makeRepo().loadSheetModelsIntoMainDataset();
		 }
		 val srcRepo = srcRepoCli.getRepo
		 val derivedRepoSpec  = new DerivedRepoSpec(dgSpecSet, srcRepo)
		 
		val derivedRepo = derivedRepoSpec.makeRepo;
		
		val derivedGraphStats = derivedRepo.getGraphStats.toArray;
		for (dgStat <- derivedGraphStats) {
			println("Got derived-graph-stat: " + dgStat)
		}
		
		val derivedBehavSpecs = derivedRepo.assembleRootsFromNamedModel(derivedBehavGraphID);
		val derivedSceneSpecList = SceneBook.filterSceneSpecs(derivedBehavSpecs);	
		derivedSceneSpecList
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
		getLogger().info("Found chanSpecs in " + CHAN_BIND_GRAPH_QN + " : " + chanSpecs)
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
		 
		// Create a new repo of kind  "derived"
		
		
		val	derivedBehavGraphID = bmcRepoCli.makeIdentForQName(DERIVED_BEHAV_GRAPH_QN);
		val pqs = new PipelineQuerySpec(PIPE_ATTR_QQN, PIPE_SOURCE_QQN, PIPELINE_GRAPH_QN);
		val derivedSceneSpecList = readSceneSpecsFromDerivedRepo(bmcRepoCli, pqs, derivedBehavGraphID);		
		println ( EQBAR + "\nGot derived scene specs : " + derivedSceneSpecList)

	}
	
}

