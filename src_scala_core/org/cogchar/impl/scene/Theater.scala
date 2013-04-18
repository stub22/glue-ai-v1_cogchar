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
