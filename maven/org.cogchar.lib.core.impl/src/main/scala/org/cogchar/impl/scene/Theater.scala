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
import org.appdapter.fancy.rclient.{RepoClient}
import org.appdapter.api.module.Module
import org.cogchar.api.channel.{GraphChannel}
import org.cogchar.api.perform.{Media, PerfChannel};
import org.cogchar.impl.perform.{DummyTextChan, FancyTime, PerfChannelNames};
import org.cogchar.impl.chan.fancy.{GraphChannelHub};
import org.cogchar.platform.trigger.{CogcharScreenBox, CogcharActionTrigger, CogcharActionBinding, CogcharEventActionBinder};

import org.cogchar.api.scene.Behavior

/**  A theater is an execution context for scene-based behavior.
 * @author Stu B. <www.texpedient.com>
 */

class Theater(val myIdent : Ident) extends CogcharScreenBox {
	private var	myBM = new BehaviorModulator();
	// private val myChanSet = new java.util.HashSet[Channel[_ <: Media, FancyTime]]();
	private val myPerfChanSet = new java.util.HashSet[PerfChannel]();
	// var myBinder : DummyBinder = null;
	
	// This SceneBook is *not* used when we are wired up using Scene+Channel lifecycles (except in a minor role as stateless filtering delegate)
	private var	mySceneBook : SceneBook = null;
	
	private var myWorkThread : Thread = null;
	private var myStopFlag : Boolean = false;
	
	// The behaviorModulator we use currently needs to treat a single scene as its context.
	// However role could be refactored out, so we are leaving the door open to a multi-scene theater.
	private val	myUnfinishedScenes = scala.collection.mutable.HashSet[BScene]()
	
	private var myGraphChanHub : GraphChannelHub = null;
	
	// Called from Theater wiringDemo
	def setGraphChanHub(gch : GraphChannelHub) {  myGraphChanHub = gch	}
	
	def registerPerfChannel (c : PerfChannel) {
	// def registerChannel (c : Channel[_ <: Media, FancyTime]) {
		getLogger.info("Registering perf-channel [{}] in behavior-theater {}", Array[Object]( c, myIdent));
		myPerfChanSet.add(c);
	}
	def getSceneBook = mySceneBook;
	def registerSceneBook(sb : SceneBook) {
		mySceneBook = sb;
	}
	// Called from FancyTrigger.makeTriggerForScene(ss : SceneSpec).fire()
	def makeSceneFromBook(sceneID: Ident) : BScene = {
		getLogger.info("MakeSceneFromBook for SceneID={}, char-theater ={}", Array[Object]( sceneID, myIdent));
		val sceneSpec = mySceneBook.findSceneSpec(sceneID);
		val scene = new FancyBScene(sceneSpec); // new BScene(sceneSpec);
		scene.wirePerfChannels(myPerfChanSet);
		safelyWireGraphChannels(scene);
		scene;
	}
  
	// called during makeSceneFromBook()-above and also during exclusiveActivateScene()-below
    private def safelyWireGraphChannels(scene: BScene){
        if (myGraphChanHub != null) {
            //we'll give the scene a copy of the hub's full map
			val graphChans = new java.util.HashSet[GraphChannel]()
            for(gc <- myGraphChanHub.myGraphChans.values){
              graphChans.add(gc);
            }
			scene.wireGraphChannels(graphChans)
		}
    }
  
	// Called from trigger.fire() when for trigs constructed by FancyTrigger.makeTriggerForScene(ss : SceneSpec),
	// which does this on each fire() {scn =
	// 			t.stopAllScenesAndModules(cancelOutJobs)
	//			val scn : BScene = t.makeSceneFromBook(freeSceneID);
	//			t.exclusiveActivateScene(scn, cancelOutJobs);
	def exclusiveActivateScene(scene: BScene, cancelPrevJobs : Boolean) {	
		// This rq-stops all their modules, and asks them each to forget/reset, but does not "forget" them at theater or BM level.
		deactivateAllScenes(cancelPrevJobs)
		activateScene(scene)
	}
	// Called from within this package, and also from MasterDemo (and now from Milo)
	// 2014-10-21 - separated prior "activateScene" into two steps, so we can get at the scene's behaviors after
	// they are constructed, but before it has been added to the running set.  
	def activateScene(scene: BScene) : Unit = {
		val behavs : List[Behavior[BScene]] = initializeSceneAndMakeBehaviors(scene)
		activateInitializedScene(scene, behavs)
	}
	def initializeSceneAndMakeBehaviors(scene: BScene) : List[Behavior[BScene]] = {
//		Currently we do not try to wirePerfChannels because...we expect that to be done by lifecycle-poppin.  Right?
//		Hmmm.   Stu buys that wiring up channels to exist with simple-lifecycles is good.  The wiring of those to actually
//		make a scene run may be an area where we need more control, must look more at JFlux APIs.
//		scene.wirePerfChannels(myPerfChanSet);
//		Graph channels throughout the scene-impl (down through behavs, guards, actions) should connect to appropriate 
//		graphs in this step.  This makes more sense than popping lifecycles around beyond what is needed.
//		Steps and Guards should not have their own lifecycles.
		
        safelyWireGraphChannels(scene);		
		// See comments about multi-scene above.  For now we expect to be used in a single-active-scene approach.
		// IF we are strict single-scene, then we SHOULD ensure previous scene is complete, and modulator is idle.
		val prevModuleCnt = myBM.getAttachedModuleCount
		if (prevModuleCnt > 1) {
			getLogger.warn("activateScene() called but prevModuleCount={}", prevModuleCnt)
		}
		getLogger.info("Activating scene with spec[{}] for char-theater {}", Array[Object]( scene.mySceneSpec.getIdent, myIdent) : _*);
		// TODO: Check if the scene still has any previously cached modules.
		// That would suggest we are re-using the scene instance, which is not a common practice.  
		// A possible scenario would be a scene interrupting itself with a re-activation, but we've never tried that.
		scene.forgetAllModules()
		// Here is the single-active-scene contraint currently enforced by BehaviorModulator.
		myBM.setSceneContext(scene);
		scene.attachSceneToModulator(myBM);
		val behavs : List[Behavior[BScene]] = scene.makeBehaviorsFromSpecs
		behavs
	}
	def activateInitializedScene(scene : BScene, behavsToActivate : List[Behavior[BScene]]) : Unit =  {
		scene.attachBehaviorModules(behavsToActivate)
		myUnfinishedScenes.add(scene);
	}
	def deactivateScene(scene: BScene, cancelPerfJobs : Boolean) {
		if (cancelPerfJobs) {
			// We want any output playback to be canceled.
			scene.cancelAllPerfJobs()
		}
		// We want all modules (i.e. Behavior + Perf-Monitor) to stop running
		scene.requestStopAllModules()
		// AND we want the scene to forget them, so that any monitoring it was doing (e.g. in FancyBScene) 
		// is now cleared and reset for future re-use of the scene.   This approach allows us to re-use scene
		// objects piped in through the OSGi theater, although that generally is kind of a messy idea.
		// So, would probably be better to dispose of this scene object, and reconstitute from sceneSpec
		// when desired.  
		scene.forgetAllModules(); // Note this is called again during the later forgetting of the scene itself.
		// TODO : Consider disposing of the scene object, preventing it from being reused.
		// REFINE:
		// Note that we cannot yet assume the scene's modules have actually finished executing.
		// So *THIS* scene will probably not get forgotten yet, but we do take this opportunity to
		// clean out any other finished scenes in the theater that are accumulating dust.  (This task would 
		// ideally be done at "user" level, i.e. within an admin coroutine module, rather than here
		// in the outer-"kernel")
		doPendingCleanup()
	}
	def doPendingCleanup() {
 
		forgetFinishedScenes()
		
	}
	def deactivateAllScenes(cancelPerfJobs : Boolean) {
		// copy toArray to avoid interference with delete ops.  myActiveScenes.toArray.apply{}
		for (sc <- myUnfinishedScenes.toArray) {
			deactivateScene(sc, cancelPerfJobs)
		}
	}
	
	protected def forgetFinishedScene(scene: BScene) {
		// This would have happened already if the scene was "deactivated", but NOT if it merely "expired" by having
		// all its modules stopped.
		scene.forgetAllModules();
		// TODO:  dbl-check that it is really "finished"
		if (myUnfinishedScenes.contains(scene)) {
			myUnfinishedScenes.remove(scene)
		} else {
			getLogger.warn("Asked to forget a scene we don't even remember: {}", scene)
		}
	}
	// Currently this is called in thread below when the BehaviorModulator takes a break, 
	// and also in (?) 
	protected def forgetFinishedScenes() {
		for (sc <- myUnfinishedScenes.toArray) {
			if (!sc.hasUnfinishedModules) {
				getLogger.warn("Found 'expired' scene to forget: {}", sc.mySceneSpec.getIdent)
				forgetFinishedScene(sc)
			}			
		}
	}	
	def attachIndependentModule(aModule : Module[BScene]) {
		myBM.attachModule(aModule)
	}
	// This is a very heavy stop, since it basically presumes the theater has no utility modules (that need to keep running).
	def stopAllScenesAndModules(cancelOutputJobs : Boolean) {
		deactivateAllScenes(cancelOutputJobs)
		// We can't know if this is necessary. 
		requestStopAllModules();
	}
	// Stops *ALL* modules, not just the soft ones in the scene.  Use with care!
	private def requestStopAllModules() {
		getLogger.warn("requestStopAllModules will stop independent modules as well as scene-associated modules")
		myBM.requestStopOnAllModules();
	}
	protected def replaceBehaviorModulator() { 
		getLogger.info("#$^%#$^#$^#$^%#$^% Replacing a BehaviorModulator - wow!  Is this necessary?")
		myBM = new BehaviorModulator();
	}
	private def stopThread() { 
		myStopFlag = true;
	}
	private def killThread() { 
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
		startThread(200)
	}
	def startThread(sleepTimeMsec : Int) {
		
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
					
					forgetFinishedScenes()
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
	def fullyStop(waitMsecThenForce : Int, cancelOutputJobs : Boolean) {
		stopAllScenesAndModules(cancelOutputJobs);
		stopThread();
		if (waitMsecThenForce >= 0) {
			if (waitMsecThenForce > 0) {
				Thread.sleep(waitMsecThenForce);
			}
			killThread();
		}
	}
}
