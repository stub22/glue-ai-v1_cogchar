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

import org.appdapter.core.log.{BasicDebugger, Loggable};
import org.appdapter.core.item.{Ident, Item, FreeIdent};
import scala.collection.mutable.HashMap;

import org.cogchar.api.perform.{Channel};
import org.cogchar.impl.perform.{DummyTextChan, ChannelNames};

import org.cogchar.platform.trigger.{DummyBox, DummyTrigger, DummyBinding, DummyBinder};

/**
 * @author Stu B. <www.texpedient.com>
 */

class Theater extends BasicDebugger with DummyBox {
	val	myBM = new BehaviorModulator();
	val myChanSet = new java.util.HashSet[Channel]();
	var myBinder : DummyBinder = null;
	var	mySceneBook : SceneBook = null;
	
	def registerChannel(c : Channel) {
		myChanSet.add(c);
	}
	def getSceneBook = mySceneBook;
	def registerSceneBook(sb : SceneBook) {
		mySceneBook = sb;
	}
	def makeSceneFromBook(sceneID: Ident) : BScene = {
		val sceneSpec = mySceneBook.findSceneSpec(sceneID);
		val scene = new BScene(sceneSpec);
		scene.wirePerformanceChannels(myChanSet);
		scene;
	}
	def activateScene(scene: BScene) {
		// TODO:  Ensure previous scene is complete, and modulator is idle or fresh or something.
		myBM.setSceneContext(scene);
		scene.attachBehaviorsToModulator(myBM);
	}
	def loadSceneBook(triplesFlexPath : String, optCL : ClassLoader ) {
		val sceneBook = SceneBook.readSceneBook(triplesFlexPath, optCL);
		registerSceneBook(sceneBook);		
	}
	def setBinder(db : DummyBinder) {
		myBinder = db;
	}
	
}
object Theater extends BasicDebugger {
	

	def main(args: Array[String]) {
		
		val		thtr = new Theater();
		
		val dummySpeechChanID = ChannelNames.getMainSpeechOutChannelIdent();
		val dtc = new DummyTextChan(dummySpeechChanID);
		thtr.registerChannel(dtc);

		val triplesFlexPath = "org/cogchar/test/assembly/ca_test.ttl";
		
		thtr.loadSceneBook(triplesFlexPath, null);
		
		
		val aSceneSpec : SceneSpec = thtr.mySceneBook.mySceneSpecs.values.head;
		logInfo("Found a SceneSpec: " + aSceneSpec);
		
		val trig : DummyTrigger = org.cogchar.impl.trigger.FancyTrigger.makeTrigger(aSceneSpec);
		trig.fire(thtr);
	
		thtr.myBM.setDebugImportanceThreshold(Loggable.IMPO_LOLO);
		thtr.myBM.runUntilDone(100);
		
		logInfo("************************  BehaviorModulator Test #1 Finished ***************************************");
	}  
}

/*		val testSceneName = "scn_001";
//		val testSceneURI = 	SceneFieldNames.NS_ccScnInst + testSceneName;
//		val testSceneID =  new FreeIdent(testSceneURI, testSceneName);
		
		val testSceneSpec = mySceneBook.findSceneSpec(testSceneID);
		
		val scene = thtr.makeSceneFromBook(testSceneID);
		
		thtr.activateScene(scene);
*/	