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
import scala.collection.mutable.HashMap;

import org.appdapter.core.log.{BasicDebugger, Loggable};
import org.appdapter.core.name.{Ident, FreeIdent};
import org.appdapter.core.item.{Item};
import org.appdapter.help.repo.{RepoClient}

import org.cogchar.api.perform.{Media, PerfChannel};
import org.cogchar.impl.perform.{DummyTextChan, FancyTime, ChannelNames};
import org.cogchar.platform.trigger.{CogcharScreenBox, CogcharActionTrigger, CogcharActionBinding, CogcharEventActionBinder};

/**  A theater is an execution context for scene-based behavior.
 * @author Stu B. <www.texpedient.com>
 */

class Theater(val myDebugCharID : Ident) extends CogcharScreenBox {
	private val	myBM = new BehaviorModulator();
	// private val myChanSet = new java.util.HashSet[Channel[_ <: Media, FancyTime]]();
	private val myChanSet = new java.util.HashSet[PerfChannel]();
	// var myBinder : DummyBinder = null;
	private var	mySceneBook : SceneBook = null;
	
	private var myWorkThread : Thread = null;
	private var myStopFlag : Boolean = false;
	
	def registerChannel (c : PerfChannel) {
	// def registerChannel (c : Channel[_ <: Media, FancyTime]) {
		getLogger().info("Registering channel [{}] in theater for char-theater {}", c, myDebugCharID);
		myChanSet.add(c);
	}
	def getSceneBook = mySceneBook;
	def registerSceneBook(sb : SceneBook) {
		mySceneBook = sb;
	}
	def makeSceneFromBook(sceneID: Ident) : BScene = {
		getLogger().info("MakeSceneFromBook for SceneID={}, CharacterID={}", sceneID, myDebugCharID);
		val sceneSpec = mySceneBook.findSceneSpec(sceneID);
		val scene = new BScene(sceneSpec);
		scene.wireSubChannels(myChanSet);
		scene;
	}
	def activateScene(scene: BScene) {
		// TODO:  Ensure previous scene is complete, and modulator is idle or fresh or something.
		getLogger().info("Activating scene with spec[{}] for char-theater {}", scene.mySceneSpec, myDebugCharID);
		myBM.setSceneContext(scene);
		scene.attachBehaviorsToModulator(myBM);
	}
	def loadSceneBookFromFile(triplesFlexPath : String, optCL : ClassLoader, clearCachesFirst : Boolean ) {
		if (clearCachesFirst) {
			// Issue: this is a global operation, but we are loading one Theater/SceneBook per character.
			SceneBook.clearBuilderCaches();
		}
		val sceneBook = SceneBook.readSceneBookFromFile(triplesFlexPath, optCL);
		registerSceneBook(sceneBook);		
	}
	def loadSceneBookFromRepo(repoClient : RepoClient, chanGraphID : Ident, behavGraphID : Ident, clearCachesFirst : Boolean ) {
		if (clearCachesFirst) {
			// Issue: this is a global operation, but we are loading one Theater/SceneBook per character.
			SceneBook.clearBuilderCaches();
		}
		val sceneBook = SceneBook.readSceneBookFromRepo(repoClient, chanGraphID, behavGraphID);
		registerSceneBook(sceneBook);		
	}	
	def stopAllScenes() {
		myBM.stopAllModules();
	}
	def stopThread() { 
		myStopFlag = true;
	}
	def killThread() { 
		if (myWorkThread != null) {
			logInfo("Theater.killThread is interrupting its own thread");
			// It's possible (and has happened) that the thread goes null upon normal completion in the run loop
			// between the check above and the interrupt call. This try block makes killThread more resistant to that problem.
			// Question:  Why wouldn't we just synchronize on either the Theater object or the Thread object?
			try {
			  myWorkThread.interrupt();
			} catch {
			  case e: NullPointerException => logInfo(
				  "Theater.killThread encountered a null pointer exception trying to interrupt its thread, probably it just completed");
			}
			myWorkThread = null;
		}
	}
	def startThread() {
		val sleepTimeMsec = 100
		// TODO: Consider how this thread communication might be better done with Scala actors, 
		// following experimental pattern in BehaviorTrial.scala.
		// 
		if (myWorkThread != null) {
			val tstate = myWorkThread.getState;
			if (tstate == Thread.State.TERMINATED) {
				myWorkThread = null;
			} else {
				throw new RuntimeException("Cannot start new Theater thread, old thread still exists in state: " + tstate + ", " + myWorkThread);
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
				logInfo("Theater behavior work thread stopped");
				if (myWorkThread != null) {
					logInfo("marking Theater behavior work thread null as part of clean exit.");
					myWorkThread = null;
				} else {
					logWarning("End of run method found theater thread already null.  Boo!");
				}
			}
		}
		myWorkThread = new Thread(r);
		myWorkThread.start();
	}
	// Negative value avoids thread kill.
	// 0 forces thread kill immediately.
	// positive value waits (in *calling* thead) that many millsec before killing thread.
	def fullyStop(waitMsecThenForce : Int) {
		stopAllScenes();
		stopThread();
		if (waitMsecThenForce >= 0) {
			if (waitMsecThenForce > 0) {
				Thread.sleep(waitMsecThenForce);
			}
			killThread();
		}
	}
	

}
object Theater extends BasicDebugger {

	def main(args: Array[String])  : Unit = {
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);		
		test();
	}
	
	def loadTestSceneBook() : SceneBook = {
		val triplesFlexPath = "org/cogchar/test/assembly/demo_scenes_A.ttl";
		val sb = new SceneBook()
		// sb.loadSceneSpecsFromFile(triplesFlexPath, optResourceClassLoader);
		sb
		// val sb = 		thtr.loadSceneBookFromFile(triplesFlexPath, null, true);		
	} 
	
	def test() : Unit = {
		val dbgCharName="PhonyChar";
		val dbgCharID = new FreeIdent(ChannelNames.NS_ccScnInst + dbgCharName, dbgCharName);
		val		thtr = new Theater(dbgCharID);

		// logInfo("Tricky: " + ChannelNames.getNumericChannelName("hmm", 22, 4));
		
		val dummySpeechChanID = ChannelNames.getOutChanIdent_SpeechMain();
		val dtc = new DummyTextChan(dummySpeechChanID);
		thtr.registerChannel(dtc);

		// val triplesFlexPath = "org/cogchar/test/assembly/ca_test.ttl";
		// val triplesFlexPath = "../org.cogchar.bundle.render.resources/src/main/resources/behavior/bhv_nugget_02.ttl";
		val triplesFlexPath = "org/cogchar/test/assembly/demo_scenes_A.ttl";
		thtr.loadSceneBookFromFile(triplesFlexPath, null, true);
		
		val sceneBook : SceneBook = thtr.getSceneBook;
		val ruledTestSceneName = "scn_004";
		val ruledTestSceneID : Ident = new FreeIdent(ChannelNames.NS_ccScnInst + ruledTestSceneName, ruledTestSceneName)
		val ruledTestSS : SceneSpec = sceneBook.findSceneSpec(ruledTestSceneID);
		
		val aSceneSpec : SceneSpec = sceneBook.allSceneSpecs().head;
		logInfo("Found first SceneSpec to build a trigger for: " + aSceneSpec);
		
		val trigHead : CogcharActionTrigger = org.cogchar.impl.trigger.FancyTriggerFacade.makeTriggerForScene(aSceneSpec);
	
		logInfo("Found ruled SceneSpec " + ruledTestSceneName + " to build a trigger for: " + ruledTestSS);
		
		val trigRuled : CogcharActionTrigger = org.cogchar.impl.trigger.FancyTriggerFacade.makeTriggerForScene(ruledTestSS);
		
		thtr.startThread();
		
		Thread.sleep(2000);
		trigHead.fire(thtr);
		// thtr.myBM.setDebugImportanceThreshold(Loggable.IMPO_LOLO);
		// thtr.myBM.runUntilDone(100);
		// 
		// 
		Thread.sleep(4000);
		logInfo("=======================================\nStarting ruled scene test");
		trigRuled.fire(thtr);
		Thread.sleep(4000);
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