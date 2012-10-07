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

import org.appdapter.scafun.{Boxy, GoFish, FullBox, FullTrigger}

import org.appdapter.core.log.{BasicDebugger, Loggable};

import org.appdapter.core.name.{Ident, FreeIdent};
import org.appdapter.core.item.{Item};


import scala.collection.mutable.HashMap;

import org.cogchar.api.perform.{Media, Channel};
import org.cogchar.impl.perform.{DummyTextChan, FancyTime, ChannelNames};

import org.cogchar.platform.trigger.{CogcharScreenBox, CogcharActionTrigger, CogcharActionBinding, CogcharEventActionBinder};


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
	def getAssembledObjsFromModelAtURL(triplesURL : String) : Set[Object] = {
		// Make sure that the classpath of this app class is known to Jena, in case the URL refers to a resource of
		// this module (e.g. OSGi bundle).  If we happen to be outside a container, this classloader-add has no
		// practical effect.
		val cl = getClass().getClassLoader();
		logDebug("Ensuring classloader registered for RDF-model resource URL resolution: " + cl);
		AssemblerUtils.ensureClassLoaderRegisteredWithJenaFM(cl);
		logInfo("Loading RDF triples directly from resource URL: " + triplesURL);
		
		val loadedStuff : java.util.Set[Object] = AssemblerUtils.buildAllObjectsInRdfFile(triplesURL);
		logInfo("Loaded " + loadedStuff.size() + " objects of various types.");		
		val mutSet : scala.collection.mutable.Set[Object] = JavaConversions.asScalaSet[Object](loadedStuff);
		mutSet.toSet[Object]
	}
	def loadBoxesFromModelAtURL(triplesURL : String) : List[MutableBox[CogcharActionTrigger]] = {
		val loadedStuff : Set[Object] = getAssembledObjsFromModelAtURL(triplesURL);
		// Filter the loaded objects down to a list of the MutableBoxes found, and return that.
		var winnerList = List [MutableBox[CogcharActionTrigger]]()
		for (x <- loadedStuff) {
			logInfo("Got Thing[" + x + "]")
			x match {
				// Scala "match" may be ignoring the [ParameterType]
				case mb : MutableBox[CogcharActionTrigger] => { winnerList = mb :: winnerList }
				case _ => logInfo("Ignoring.")
			}
		}
		winnerList;
	}
	def makeFunTrig() : CogcharActionTrigger  = {
	//		val sceneID : Ident = ss.getIdent();
	//	val freeSceneID : FreeIdent = new FreeIdent(sceneID);
		val ndt = new CogcharActionTrigger() { 
			// Note that we use *argument* theater, not enclosing one. 
			// So, this trigger can be used on any theater, but of course,
			// the matching sceneID must be found in the book of that theater!
			override def fire(db : CogcharScreenBox) : Unit = {
				
				logInfo("Firing [" + toString + " on " + db);
	//			val t : Theater = db.asInstanceOf[Theater];
	//			t.stopAllScenes();
	//			val scn : BScene = t.makeSceneFromBook(freeSceneID);
	//			t.activateScene(scn);
			}
		}
		ndt;
	}
	def main(args: Array[String]) :Unit = {
		logInfo(this.getClass.getCanonicalName() + ".main(" + args + ")-BEGIN");
		logInfo("Whack-em-ole, Vaquera!");
		val time = java.lang.System.currentTimeMillis();
		logInfo("El tiempo es: " + time);
		val tnc : DemoNavigatorCtrl = makeTNC(args);
		
		tnc.launchFrame("Whackamole");

		//  This test model is found in the resource path of this module (o.c.lib.core),
		//  which typically is mapped to our src_resources_core directory.
		//  In applications, the software can use models from another form like DB or spreadsheet.
		//  This assembler-style model defines bindings to Java factory classes and specific config objects.
		//  Some config objects are bound to WhackBox and WhackTrig below, via some factory classes
		//  found in the Appdapter project.  
		val moreBoxesModelURL : String = "org/cogchar/test/assembly/whackam.ttl";
		
		val moreBoxes : List[MutableBox[CogcharActionTrigger]] = loadBoxesFromModelAtURL(moreBoxesModelURL)
		logInfo("Got MoreBoxes: " + moreBoxes)
		val reloadFlag : Boolean = true;
		for (mb <- moreBoxes) {
			 tnc.addBoxToRoot(mb, reloadFlag)
		}
		logInfo(this.getClass.getCanonicalName() + ".main()-END");
	}	
}

class WhackBox extends FullBox[WhackTrig] {
	
}

class WhackTrig extends TriggerImpl[WhackBox] with FullTrigger[WhackBox] {
	override def fire(wb : WhackBox) : Unit = {
		logInfo(this.toString() + " firing on " + wb.toString());
	}
}
