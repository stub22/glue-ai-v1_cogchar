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

package org.cogchar.impl.scene

import org.appdapter.core.log.{BasicDebugger};
import org.appdapter.core.name.{Ident, FreeIdent};
import org.appdapter.core.item.{Item};
import org.appdapter.bind.rdf.jena.model.{JenaFileManagerUtils};

import org.appdapter.fancy.rclient.{RepoClient}

import org.cogchar.impl.perform.{ChannelSpecBuilder};
import scala.collection.mutable.HashMap;
import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;	

/**
 * @author Stu B. <www.texpedient.com>
 */

class SceneBook extends BasicDebugger {
	val		mySceneSpecs = new HashMap[Ident,SceneSpec]();


	def addSceneSpec(ss: SceneSpec) {
		mySceneSpecs.put(ss.getIdent(), ss);
	}
	
	
	def registerSceneSpecs (lss : List[SceneSpec] ) : Unit = {
		for (ss <- lss) {
			addSceneSpec(ss);
		}
	}
	def findSceneSpec(sceneID : Ident) : SceneSpec = {
		mySceneSpecs.getOrElse(sceneID, null);
	}
	def allSceneSpecs() : Iterable[SceneSpec] = mySceneSpecs.values ;
	
}
object SceneBook extends BasicDebugger {
	def loadSceneSpecsFromRepo(repoClient : RepoClient, chanGraphID : Ident, behavGraphID : Ident) : List[SceneSpec] = { 
		getLogger.info("Loading Channel Specs from: {}", chanGraphID);
		val chanSet : java.util.Set[Object] = repoClient.assembleRootsFromNamedModel(chanGraphID)
		getLogger.debug("Loaded chan objects {} ", chanSet);
		// Channels are now available for wiring via the global builder cache - messy!
		getLogger.info("Loading Scene/Behavior Specs from: {}", behavGraphID);
		val behvSet : java.util.Set[Object] = repoClient.assembleRootsFromNamedModel(behavGraphID)
		getLogger.debug("Loaded behav objects {} ", behvSet);	
		yieldSceneSpecs(behvSet)
	}
	def loadSceneSpecsFromFile(rdfConfigFlexPath : String , optResourceClassLoader : ClassLoader ) : List[SceneSpec] = { 
		if (optResourceClassLoader != null) {
			getLogger.debug("Ensuring registration of classLoader: {}", optResourceClassLoader);
			JenaFileManagerUtils.ensureClassLoaderRegisteredWithDefaultJenaFM(optResourceClassLoader);
		}
		getLogger.debug("Loading SceneSpecs from: {}", rdfConfigFlexPath);
		val loadedStuff = AssemblerUtils.buildAllObjectsInRdfFile(rdfConfigFlexPath);
		getLogger.debug("Loaded {} objects", loadedStuff.size());
		yieldSceneSpecs(loadedStuff)
	}	
	def yieldSceneSpecs(loadedStuff : java.util.Set[Object]) : List[SceneSpec] = { 		

		var sceneSpecList = List[SceneSpec]()
		if (loadedStuff != null) {
			val si = loadedStuff.iterator();		
			while (si.hasNext()) {
				val obj = si.next();
				if (obj.isInstanceOf[SceneSpec]) {
					sceneSpecList = sceneSpecList :+ obj.asInstanceOf[SceneSpec]
				}
			}
		} else {
			getLogger.warn("SceneSpecLoad FAILED, yielding empty list.")
		}
		getLogger.debug("===========================================================================================")
		getLogger.debug("Loaded SceneSpecList: {}", sceneSpecList);
		getLogger.debug("===========================================================================================")
		sceneSpecList;
	}	
	def filterSceneSpecs (stuff : java.util.Set[Object]) : java.util.List[SceneSpec] = {
		import scala.collection.JavaConversions._;
		yieldSceneSpecs(stuff);
	}
	def  readSceneBookFromFile(triplesFlexPath : String, optCL : ClassLoader ) : SceneBook = {
		val sb = new SceneBook();
		val sceneSpecList : List[SceneSpec] = loadSceneSpecsFromFile(triplesFlexPath, optCL);
		sb.registerSceneSpecs(sceneSpecList);
		sb;
	}
	def  readSceneBookFromRepo(repoClient : RepoClient, chanGraphID : Ident, behavGraphID : Ident) : SceneBook = {
		val sb = new SceneBook();
		val sceneSpecList : List[SceneSpec] = loadSceneSpecsFromRepo(repoClient, chanGraphID, behavGraphID);
		sb.registerSceneSpecs(sceneSpecList);
		sb;
	}
	
	import org.appdapter.bind.rdf.jena.assembly.CachingComponentAssembler;
	
	def  clearBuilderCaches() {
        AssemblerUtils.clearCacheForAssemblerSubclassForSession(classOf[SceneSpecBuilder], AssemblerUtils.getDefaultSession());
		AssemblerUtils.clearCacheForAssemblerSubclassForSession(classOf[BehaviorSpecBuilder], AssemblerUtils.getDefaultSession());
		AssemblerUtils.clearCacheForAssemblerSubclassForSession(classOf[ChannelSpecBuilder], AssemblerUtils.getDefaultSession());
	}

}