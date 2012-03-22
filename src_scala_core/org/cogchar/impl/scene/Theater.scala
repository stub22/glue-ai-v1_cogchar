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
	
	var myThread : Thread = null;
	var myStopFlag : Boolean = false;
	
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
		logInfo("Activating scene with spec[" + scene.mySceneSpec + "]");
		myBM.setSceneContext(scene);
		scene.attachBehaviorsToModulator(myBM);
	}
	def loadSceneBook(triplesFlexPath : String, optCL : ClassLoader, clearCachesFirst : Boolean ) {
		if (clearCachesFirst) {
			SceneBook.clearBuilderCaches();
		}
		val sceneBook = SceneBook.readSceneBook(triplesFlexPath, optCL);
		registerSceneBook(sceneBook);		
	}
	def stopAllScenes() {
		myBM.stopAllModules();
	}
	def stopThread() { 
		myStopFlag = true;
	}
	def killThread() { 
		if (myThread != null) {
			myThread.interrupt();
			myThread = null;
		}
	}
	def startThread() {
		val sleepTimeMsec = 100
		// TODO: Consider how this can be better done with agents.
		if (myThread != null) {
			val tstate = myThread.getState;
			if (tstate == Thread.State.TERMINATED) {
				myThread = null;
			} else {
				throw new RuntimeException("Cannot start new Theater thread, old thread still exists in state: " + tstate + ", " + myThread);
			}
		}
		myStopFlag = false;
		val r : Runnable = new Runnable() {
			override def run() {
				while (!myStopFlag) {
					myBM.runUntilDone(sleepTimeMsec);
					// logInfo("Theater behavior module is 'done', detaching all finished modules");
					// We turned auto-detach back on, so we don't currently need to do:   myBM.detachAllFinishedModules();
					// logInfo("Sleeping for " + sleepTimeMsec + "msec");
					Thread.sleep(sleepTimeMsec);
				}
				logInfo("Theater behavior thread stopped.");
			}
		}
		myThread = new Thread(r);
		myThread.start();
	}
	
	def fullyStop(waitMsecThenForce : Int) {
		stopAllScenes();
		stopThread();
		if (waitMsecThenForce >= 0) {
			if (waitMsecThenForce > 0) {
				Thread.sleep(waitMsecThenForce);
				killThread();
			}
		}
	}

}
object Theater extends BasicDebugger {
	

	def main(args: Array[String]) {
		
		val		thtr = new Theater();

		logInfo("Tricky: " + ChannelNames.getNumericChannelName("hmm", 22, 4));
		
		val dummySpeechChanID = ChannelNames.getMainSpeechOutChannelIdent();
		val dtc = new DummyTextChan(dummySpeechChanID);
		thtr.registerChannel(dtc);

		val triplesFlexPath = "org/cogchar/test/assembly/ca_test.ttl";
		
		thtr.loadSceneBook(triplesFlexPath, null, true);
		
		val aSceneSpec : SceneSpec = thtr.mySceneBook.mySceneSpecs.values.head;
		logInfo("Found a SceneSpec: " + aSceneSpec);
		
		val trig : DummyTrigger = org.cogchar.impl.trigger.FancyTrigger.makeTrigger(aSceneSpec);
		
	
		thtr.startThread();
		
		Thread.sleep(2000);
		trig.fire(thtr);
		// thtr.myBM.setDebugImportanceThreshold(Loggable.IMPO_LOLO);
		// thtr.myBM.runUntilDone(100);
		// 
		Thread.sleep(8000);
		logInfo("********************** stopping thread");
		thtr.fullyStop(500);
		Thread.sleep(2000);
		
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