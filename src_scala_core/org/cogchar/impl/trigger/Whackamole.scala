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

package org.cogchar.impl.trigger

import org.appdapter.api.registry.VerySimpleRegistry;
import org.appdapter.osgi.registry.RegistryServiceFuncs;

import org.appdapter.api.trigger.{Box, BoxContext, BoxImpl, MutableBox, Trigger, TriggerImpl};
import org.appdapter.gui.demo.{DemoBrowser, DemoNavigatorCtrl};

import org.appdapter.scafun.{Boxy, GoFish}

import org.appdapter.core.log.{BasicDebugger, Loggable};

import org.appdapter.core.name.{Ident, FreeIdent};
import org.appdapter.core.item.{Item};


import scala.collection.mutable.HashMap;

import org.cogchar.api.perform.{Media, Channel};
import org.cogchar.impl.perform.{DummyTextChan, FancyTime, ChannelNames};

import org.cogchar.platform.trigger.{DummyBox, DummyTrigger, DummyBinding, DummyBinder};

import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import scala.collection.JavaConversions;

/**
 *	Designed to work with or without OSGi context.
 * @author Stu B. <www.texpedient.com>
 */

object Whackamole extends BasicDebugger {
	def getVerySimpleRegistry() : VerySimpleRegistry = {
		RegistryServiceFuncs.getTheWellKnownRegistry(this.getClass());
	}
	// Started with code from Appdapter GoFish.makeTNC
	def makeTNC(args: Array[String]) : DemoNavigatorCtrl = {
		val tnc = DemoBrowser.makeDemoNavigatorCtrl(args);
		val box1 = Boxy.boxItUp();
		tnc.addBoxToRoot(box1, false);
		tnc;
	} 

	// Started with code from Appdapter demo BridgeTriggers
	def getAssembledObjsFromFile(triplesURL : String) : Set[Object] = {
		val cl = getClass().getClassLoader();
		AssemblerUtils.ensureClassLoaderRegisteredWithJenaFM(cl);
		logInfo("Loading triples from URL: " + triplesURL);
		
		val loadedStuff : java.util.Set[Object] = AssemblerUtils.buildAllObjectsInRdfFile(triplesURL);
		logInfo("Loaded " + loadedStuff.size() + " objects");		
		val mutSet : scala.collection.mutable.Set[Object] = JavaConversions.asScalaSet[Object](loadedStuff);
		mutSet.toSet[Object]
	}
	def makeStuff(triplesURL : String) : MutableBox[DummyTrigger] = {
		logInfo(toString() + ".fire()");
		val loadedStuff : Set[Object] = getAssembledObjsFromFile(triplesURL);
		var winner : MutableBox[DummyTrigger] = null;
		for (x <- loadedStuff) {
			println("Got Thing[" + x + "]")
		}
		/*
		 for (Object o : loadedStuff) {
		 o match {
		 case 
		 }
		 if (o instanceof MutableBox) {
		 loadedMutableBox : MutableBox = (MutableBox) o;
		 bc.contextualizeAndAttachChildBox(targetBox, loadedMutableBox);
		 logInfo("Loaded mutable box: " + loadedMutableBox);
		 } else {
		 logInfo("Loaded object which is not a mutable box: " + o);
		 }
		 }
		 */
		winner;
	}
	def makeFunTrig() : DummyTrigger  = {
	//		val sceneID : Ident = ss.getIdent();
	//	val freeSceneID : FreeIdent = new FreeIdent(sceneID);
		val ndt = new DummyTrigger() { 
			// Note that we use *argument* theater, not enclosing one. 
			// So, this trigger can be used on any theater, but of course,
			// the matching sceneID must be found in the book of that theater!
			override def fire(db : DummyBox) : Unit = {
				
				println("Firing [" + toString + " on " + db);
	//			val t : Theater = db.asInstanceOf[Theater];
	//			t.stopAllScenes();
	//			val scn : BScene = t.makeSceneFromBook(freeSceneID);
	//			t.activateScene(scn);
			}
		}
		ndt;
	}
	def main(args: Array[String]) :Unit = {
		println(this.getClass.getCanonicalName() + ".main(" + args + ")-BEGIN");
		println("Whack-em-ole, Vacquera!");
		val time = java.lang.System.currentTimeMillis();
		println("El tiempo es: " + time);
		val tnc : DemoNavigatorCtrl = makeTNC(args);
		
		// tnc.launchFrame("Whackamole");
		val moreBoxesModelURL : String = "org/cogchar/test/assembly/whackam.ttl";
		val moreBoxesRoot : MutableBox[DummyTrigger] = makeStuff(moreBoxesModelURL)
		val reloadFlag : Boolean = true;
		// tnc.addBoxToRoot(moreBoxesRoot, reloadFlag)
		println(this.getClass.getCanonicalName() + ".main()-END");
	}	
}
