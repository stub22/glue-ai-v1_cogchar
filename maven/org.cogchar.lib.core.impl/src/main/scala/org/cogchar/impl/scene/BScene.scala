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

import org.appdapter.core.name.{Ident};
import org.appdapter.core.log.{BasicDebugger};

// import org.cogchar.name.behavior.{SceneFieldNames}
import org.cogchar.api.perform.{PerfChannel, Media, Performance, FancyPerformance};
import org.cogchar.impl.perform.basic.{BasicPerfChan, BasicPerformance};
import org.cogchar.impl.perform.{FancyTime, FancyTextPerf};

import org.cogchar.api.channel.{GraphChannel};
import org.cogchar.api.scene.{Scene};
import org.cogchar.api.perform.{PerfChannel, Media, Performance, FancyPerformance};
import org.cogchar.impl.perform.{FancyTime, FancyTextPerf}

import scala.collection.mutable.HashMap;
import org.appdapter.api.module.{Module}


/**
 * @author Stu B. <www.texpedient.com>
 */

class BSceneRootCursor() {
}
abstract class BSceneRootMedia() extends Media[BSceneRootCursor] {

}
class BSceneRootChan (id : Ident, val scn: BScene) extends BasicPerfChan(id){
	override protected def fastCueAndPlay[Cur, M <: Media[Cur], Time] (m : M, c : Cur,perf: BasicPerformance[Cur, M, Time]) {
		// Match on BSceneRootMedia or throw a fit!
	}


	override def getMaxAllowedPerformances() : Int = 1;
}


// BScene stands for BehaviorScene, which is constructed from a SceneSpec.
// // The "___Spec" Layer is considered immutable and reusable.
// In theory, a BScene could be "played" more than once.
// However, we want extensions to be able to define mutable variables.

/**
 * A BScene is a BehaviorScene, which is used as the app-context for a group of cooperative/competitive Behaviors(Modules).
 * The idea of the scene having a "rootChannel" is not fully implemented.
 */

abstract class BScene (val mySceneSpec: SceneSpec) extends BasicDebugger with Scene[FancyTime, BSceneRootChan] {
	val rootyID = mySceneSpec.getIdent() // new FreeIdent(SceneFieldNames.I_rooty, SceneFieldNames.N_rooty);
	val myRootChan = new BSceneRootChan(rootyID, this);
	val myWiredPerfChannels  = new HashMap[Ident,PerfChannel]();

	val myCachedModules = new scala.collection.mutable.HashSet[Module[BScene]]()
	var myCachedModulator : BehaviorModulator = null


	override def getRootChannel() : BSceneRootChan = {	myRootChan	}
	import scala.collection.JavaConversions._;
	
	override def wirePerfChannels(perfChans : java.util.Collection[PerfChannel]) : Unit = {
		// Currently, all we do is copy references to all chans into our wired channel map.
		// TODO:  reconcile the actually wired channels with the ones in the SceneSpecs.
		// 
		// Open question:  What does it mean to "re-wire" a BScene?
		if (myWiredPerfChannels.size > 0) {
			throw new RuntimeException("Wiring new perfChannels [" + perfChans + "] into a scene with existing channels: " + myWiredPerfChannels)
		}
		for (c <- perfChans) {
			getLogger.debug("Wiring scene[{}] to channel: {}", Array[Object]( mySceneSpec.getIdent.getLocalName, c));
			myWiredPerfChannels.put(c.getIdent, c)
		}
	}
	// If the modulator has "autoDetachOnFinish" set to true, then the modules will be auto-detached.
	def attachBehaviorsToModulator(bm : BehaviorModulator) {
		updateModuleCaches(bm);
		makeAndAttachBehavsFromSpecs();
	}

	/**
	 * HERE our BScene is acting like a tidy immutable behavior-factory, which is nice.
	 * However, we are invokind attachModule, which is making use of a BehaviorModulator
	 * that might already have copies of these fresh Behaviors we are making.  Also, the
	 * running scene may cache information by stepSpec-ID at present (rather than by say,
	 * step-EXEC-ID, which would be generated at runtime, thus safer but harder to find).
	 * That cache in FancyBScene below is how GuardedBehaviors check their guard-perfs.
	 * A BScene might choose
	 */
	protected def makeAndAttachBehavsFromSpecs() {
		for (bs : BehaviorSpec <- mySceneSpec.myBehaviorSpecs.values) {
			val b = bs.makeBehavior();
			attachModule(b);
		}
	}

	protected def updateModuleCaches(bm : BehaviorModulator) {
		// We are intercepting this method as a signal to treat this bm as our new cached modulator.
		if (myCachedModulator != bm) {
			if (myCachedModulator != null) {
				getLogger.warn("Whoah!  Scene {} is being attached to a different modulator than before!", mySceneSpec.getIdent)
				if (myCachedModules.nonEmpty) {
					// Indicates the scene was not properly stopped before and is now being reused.  Ick.
					getLogger.error("Double-Woah!  Scene {} has cached modules and is now moving to a different modulator!"
										+ "Now stopping the old modules, and hoping for the best.", mySceneSpec.getIdent)
					requestStopAllModules
					forgetAllModules
				}
			}
		}
		myCachedModulator = bm;
		if (myCachedModules.nonEmpty) {
			getLogger.warn("#############  Hey, we already have some cached modules in this scene: {}", myCachedModules)
		}
	}

	// Direct approach to attaching perf-monitor-modules
	protected def attachModule(aModule : Module[BScene]) {
		if (myCachedModulator != null) {
			// If the modulator has "autoDetachOnFinish" set to true, then the modules will be auto-detached.
			myCachedModulator.attachModule(aModule)
			myCachedModules.add(aModule)
		}
	}
	def requestStopAllModules() {
		for (mod <- myCachedModules) {
			mod.markStopRequested
		}
	}
	def forgetAllModules() {
		myCachedModules.clear
	}
	// If we want a module to be detached from a modulator without waiting for auto-detach-on-finish, then
	// we must use something like this.  However, we cannot detach a module which is currently in an action method.
	// (doRun, stop, start) - we will instead get an exception.  To proceed without blocking or async requests,
	// this method must catch those exceptions.
	def attemptImmediateDetachAllModules() {
	}
	def getUnfinishedModules() : Set[Module[BScene]] = {
		if (myCachedModulator != null) {
			myCachedModulator.findUnfinishedModules(myCachedModules.toSet)
		} else {
			Set[Module[BScene]]()
		}
	}
	def hasUnfinishedModules() : Boolean = getUnfinishedModules.nonEmpty

	override def getPerfChannel(id : Ident) : PerfChannel = {
		return myWiredPerfChannels.getOrElse(id, null);
	}


	override def toString() : String = {
		"BScene[id=" + rootyID + ", chanMap=" + myWiredPerfChannels + ", modules=" + myCachedModules + "]";
	}
	def cancelAllPerfJobs()

	override def  getDiagnosticInfo() : java.lang.Object = {
		return "Default Diagnostics for [" + this + "]";
	}
}

import scala.collection.mutable.Map

/**
 * Implements the features beyond BScene that we need to make behavior decisions.
 *    1) Tracks performances of its own steps for others to guard on
 *    2) [TODO] - Tracks GraphChannels supplying useful input+state data
 *
 *    These two features above should be separated into traits, which are then
 *    mixed in by FancyBScene.  Consider having those traits extend Scene interface.
 *
 */
class FancyBScene(ss: SceneSpec) extends BScene(ss) {
	val		myPerfMonModsByStepSpecID  = new HashMap[Ident, FancyPerfMonitorModule]()
	val		myWiredGraphChannels  = new HashMap[Ident,GraphChannel]();

	override def wireGraphChannels(graphChans : java.util.Collection[GraphChannel]) : Unit = {
		import scala.collection.JavaConversions._
		for (gc : GraphChannel <- graphChans)  {
			val chanID = gc.getIdent
			// TODO:  Check the scene spec to see whether we want this channel, and if so, how it should be wired.
			// In particular, do we want to apply a thingAction-viewed filter onto it?
			// But also, orthogonally, do we want to interact more directly with the GraphChannel hub to pull out
			// what we want?  Do we assume that has already happened before this wire() method is called?
			myWiredGraphChannels.put(chanID, gc)
		}
	}
	// This is used by the step-actions to find a graphChannel of interest.
	// It depends on wireGraphChannels having been called earlier with a matching channel.
	override def getGraphChannel(id : Ident) : GraphChannel = {
		return myWiredGraphChannels.getOrElse(id, null);
	}
	override def cancelAllPerfJobs() {
		for (knownPerfMod  <- myPerfMonModsByStepSpecID.values) {
			val knownPerf  = knownPerfMod.myPerf
			knownPerf.requestOutputJobCancel
		}
	}
	override def forgetAllModules() {
		getLogger.info("FancyBScene {} is forgetting all modules (and stepSpec-perf mappings!)", mySceneSpec.getIdent)
		super.forgetAllModules()
		myPerfMonModsByStepSpecID.clear
	}
	def registerPerfForStep(stepSpecID : Ident, perf : FancyPerformance) {
		perf match {
			case ftp : FancyTextPerf => {
				val perfMonMod = new FancyPerfMonitorModule(ftp)
				// TODO:  if already a module at that ID, print warning, tell module to stop, replace it.
				myPerfMonModsByStepSpecID.put(stepSpecID, perfMonMod)
				attachModule(perfMonMod)
			}
			case  _ => {
				getLogger().warn("************* Cannot yet register a non-text perf = {} ", perf);
			}
		}
	}
	def getPerfStatusForStep(stepSpecID : Ident) : Performance.State = {
		val optPMM = myPerfMonModsByStepSpecID.get(stepSpecID)
		optPMM match {
			case Some(monitorModule: FancyPerfMonitorModule) => {
				monitorModule.getPerfState
			}
			case None => {
				// We treat a performance not created "yet" as INITING, which cleanly allows check against the
				// stepSpecID at anytime.
				Performance.State.INITING
			}
			case  Some(x) => {
				getLogger().error("*************    Found weird performanceMonitorModule = {} ", x);
				// Treat an error/weirdness as INITING too, for now.  Performance is not a public API.  Use channels!
				Performance.State.INITING
			}
			// Plus an (unnecessary?) catchall case as tutorial + experiment:
			// Seems Scala 2.8.1 compiler cannot recognize "all cases are covered already", so it allows this pattern.
			case _ => {
				getLogger().error("**************   How did we avoid all the cases above?  optPMM={} ", optPMM);
				Performance.State.INITING
			}
		}
	}
}


