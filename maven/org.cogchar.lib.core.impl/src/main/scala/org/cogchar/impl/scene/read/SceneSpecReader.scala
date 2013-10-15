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

package org.cogchar.impl.scene.read
import org.appdapter.core.name.{Ident, FreeIdent}
import org.appdapter.core.store.{Repo, InitialBinding }
import org.appdapter.help.repo.{RepoClient, RepoClientImpl, InitialBindingImpl} 
import org.appdapter.impl.store.{FancyRepo};
import com.hp.hpl.jena.query.{QuerySolution} // Query, QueryFactory, QueryExecution, QueryExecutionFactory, , QuerySolutionMap, Syntax};
import com.hp.hpl.jena.rdf.model.{Model}
import org.cogchar.impl.perform.{PerfChannelNames};
import org.cogchar.name.behavior.{MasterDemoNames};
import org.cogchar.impl.channel.{FancyChannelSpec};
import org.cogchar.impl.scene.{SceneSpec, SceneBook};
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.platform.util.ClassLoaderUtils;
import org.osgi.framework.BundleContext;
import org.appdapter.core.matdat.{SheetRepo, RepoSpec, OnlineSheetRepoSpec, DatabaseRepoSpec, DirectDerivedGraph, DerivedGraphSpec, _}

/**
 * @author Stu B. <www.texpedient.com>
 */

class SceneSpecReader  extends BasicDebugger {
	def readChannelSpecs(repoClient : RepoClient, chanGraphQN : String) : java.util.Set[FancyChannelSpec] = {
		val specSet = new java.util.HashSet[FancyChannelSpec]();
		val objectsFound : java.util.Set[Object] = repoClient.assembleRootsFromNamedModel(chanGraphQN);
		if (objectsFound != null) {
			import scala.collection.JavaConversions._;
			for (o <- objectsFound) {
				o match {
					case cspec : FancyChannelSpec => specSet.add(cspec)
					case _ => getLogger().warn("Unexpected object found in {} = {}", Array[Object]( chanGraphQN, o));
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
		println(EQBAR + "\nReading indirect graph " + derivedBehavGraphID + " using " + pqs + "thru repoClient [" + srcRepoCli + "]");
				
		val bmp =  ModelProviderFactory.makeOneDerivedModelProvider (srcRepoCli, pqs, derivedBehavGraphID)
		readSceneSpecsFromBMP(bmp)
	}
	def readSceneSpecsFromBMP(bmp : BoundModelProvider) : java.util.List[SceneSpec] = {
		val allObjects : java.util.Set[Object] = bmp.assembleModelRoots()
		val sceneSpecList : java.util.List[SceneSpec] = SceneBook.filterSceneSpecs(allObjects);
		sceneSpecList
	}
}
