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
import org.appdapter.core.log.BasicDebugger;

object BehavMasterConfigTest extends BasicDebugger {
	//   https://docs.google.com/spreadsheet/ccc?key=0AlpQRNQ-L8QUdFh5YWswSzdYZFJMb1N6aEhJVWwtR3c
	
	final val BMC_SHEET_KEY = "0AlpQRNQ-L8QUdFh5YWswSzdYZFJMb1N6aEhJVWwtR3c"
	
	final val BMC_NAMESPACE_SHEET_NUM = 4
	
	final val BMC_DIRECTORY_SHEET_NUM = 3
	
	// val chanBindQueryQN = AssumedQueryDir.LIGHT_QUERY_URI // "ccrt:find_lights_99" // The QName of a query in the "Queries" model/tab
	val CHAN_BIND_GRAPH_QN = "hrk:chan_sheet_77"
	
	def makeBMC_RepoSpec() : OnlineSheetRepoSpec = { 
		val fileResModelCLs = new java.util.ArrayList[ClassLoader]();
		new OnlineSheetRepoSpec(BMC_SHEET_KEY, BMC_NAMESPACE_SHEET_NUM, 
											BMC_DIRECTORY_SHEET_NUM, fileResModelCLs);
	}
	def readChannelSpecs(repoClient : RepoClient, chanGraphQN : String) : java.util.Set[ChannelSpec] = {
		val specSet = new java.util.HashSet[ChannelSpec]();
		val objectsFound : java.util.Set[Object] = repoClient.assembleRootsFromNamedModel(chanGraphQN);
		import scala.collection.JavaConversions._;
		for (o <- objectsFound) {
			o match {
				case cspec : ChannelSpec => objectsFound.add(cspec)
				case _ => getLogger().warn("Unexpected object found in {} = {}", chanGraphQN, o);
			}
		}
		specSet;
	} 
	def main(args: Array[String]) : Unit = {
		// Must enable "compile" or "provided" scope for Log4J dep in order to compile this code.
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);

		val bmcRepoSpec = makeBMC_RepoSpec();
		
		val bmcMemoryRepoHandle = bmcRepoSpec.makeRepo();	
		println("OK to ignore error above about metadata/behavior/zeno_demo_scenes_A.ttl")
		
		val bmcRepoCli = bmcRepoSpec.makeRepoClient(bmcMemoryRepoHandle);
		
		val chanSpecs = readChannelSpecs(bmcRepoCli, CHAN_BIND_GRAPH_QN);
		println("Found chanSpecs: " + chanSpecs)
		import scala.collection.JavaConversions._;
		for (c <- chanSpecs) {
			val chanID = c.getChannelID();
			val chanTypeID = c.getChannelTypeID();
			val chanOSGiFilter = c.getOSGiFilterString();
			println("Channel id=" + chanID + ", type=" + chanTypeID + ", filter=" + chanOSGiFilter)
		}
	
		// println ("AnimOut-Best=" + ChannelNames.getOutChanIdent_AnimBest)
		// println("SpeechOut-Best=" + ChannelNames.getOutChanIdent_SpeechMain)
	}
}
