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

import org.appdapter.bind.rdf.jena.assembly.CachingComponentAssembler;
import org.cogchar.impl.perform.{ChannelSpecBuilder};
import scala.collection.mutable.HashMap;

/**
 * @author Stu B. <www.texpedient.com>
 */

class SceneBook extends BasicDebugger {
	val		mySceneSpecs = new HashMap[Ident,SceneSpec]();
	import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;	

	def addSceneSpec(ss: SceneSpec) {
		mySceneSpecs.put(ss.getIdent(), ss);
	}
	
	def registerSceneSpecs (lss : List[SceneSpec] ) : Unit = {
		for (val ss <- lss) {
			addSceneSpec(ss);
		}
	}
	def findSceneSpec(sceneID : Ident) : SceneSpec = {
		mySceneSpecs.getOrElse(sceneID, null);
	}
	def allSceneSpecs() : Iterable[SceneSpec] = mySceneSpecs.values ;
	
	def loadSceneSpecs(rdfConfigFlexPath : String , optResourceClassLoader : ClassLoader ) : List[SceneSpec] = { 
		if (optResourceClassLoader != null) {
			getLogger.debug("Ensuring registration of classLoader: {}", optResourceClassLoader);
			AssemblerUtils.ensureClassLoaderRegisteredWithJenaFM(optResourceClassLoader);
		}
		getLogger.debug("Loading SceneSpecs from: {}", rdfConfigFlexPath);
		val loadedStuff = AssemblerUtils.buildAllObjectsInRdfFile(rdfConfigFlexPath);
		getLogger.debug("Loaded {} objects", loadedStuff.size());
	//	logInfo("Stuff: " + loadedStuff);
		val si = loadedStuff.iterator();
		var sceneSpecList = List[SceneSpec]()
		while (si.hasNext()) {
			val obj = si.next();
			if (obj.isInstanceOf[SceneSpec]) {
				sceneSpecList = sceneSpecList :+ obj.asInstanceOf[SceneSpec]
			}
		}
		getLogger.debug("===========================================================================================")
		getLogger.debug("Loaded SceneSpecList: {}", sceneSpecList);
		getLogger.debug("===========================================================================================")
		sceneSpecList;
		// for (Object o : loadedStuff) {
	}
}
object SceneBook extends BasicDebugger {
	
	def  readSceneBook(triplesFlexPath : String, optCL : ClassLoader ) : SceneBook = {
		val sb = new SceneBook();
		val sceneSpecList : List[SceneSpec] = sb.loadSceneSpecs(triplesFlexPath, optCL);
		sb.registerSceneSpecs(sceneSpecList);
		sb;
	}
	def  clearBuilderCaches() {
		CachingComponentAssembler.clearCacheForAssemblerSubclass(classOf[SceneSpecBuilder]);
		CachingComponentAssembler.clearCacheForAssemblerSubclass(classOf[BehaviorSpecBuilder]);
		CachingComponentAssembler.clearCacheForAssemblerSubclass(classOf[ChannelSpecBuilder]);
	}

}