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

import org.appdapter.core.name.{Ident, FreeIdent};
import org.appdapter.core.item.{Item};
import org.appdapter.core.log.{BasicDebugger};

import org.appdapter.core.component.KnownComponentImpl;
import org.appdapter.bind.rdf.jena.assembly.DynamicCachingComponentAssembler;

import com.hp.hpl.jena.assembler.{Assembler, Mode}

import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;

import org.cogchar.name.behavior.{SceneFieldNames}
import org.cogchar.api.channel.{Channel, BasicChannel}
import org.cogchar.api.perform.{PerfChannel, Media, BasicPerfChan, Performance, BasicPerformance};
import org.cogchar.impl.perform.{FancyTime, ChannelSpec, ChannelNames, FancyTextPerf, FancyPerformance};

import org.cogchar.api.scene.{Scene};

import scala.collection.mutable.HashMap;
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
 * A BScene is a BehaviorScene, which is used as the app-context for the Behavior(Modules)
 */

abstract class BScene (val mySceneSpec: SceneSpec) extends BasicDebugger with Scene[FancyTime, BSceneRootChan] {
	val rootyID = new FreeIdent(SceneFieldNames.I_rooty, SceneFieldNames.N_rooty);
	val myRootChan = new BSceneRootChan(rootyID, this);
	val		myWiredChannels  = new HashMap[Ident,PerfChannel]();
	
	override def getRootChannel() : BSceneRootChan = {	myRootChan	}
	import scala.collection.JavaConversions._;
	override def wireSubChannels(chans : java.util.Collection[PerfChannel]) : Unit = {
		// Currently, all we do is copy references to all chans into our wired channel map.
		// TODO:  reconcile the actually wired channels with the ones in the SceneSpecs.
		// Open question:  What would it mean to "re-wire" a BScene?
		for (val c <- chans) {
			getLogger().info("Wiring scene[{}] to channel: {}", mySceneSpec.getIdent.getLocalName, c);
			myWiredChannels.put(c.getIdent, c)
		}
	}
	var myCachedModulator : BehaviorModulator = null
	// If the modulator has "autoDetachOnFinish" set to true, then the modules will be auto-detached.
	def attachBehaviorsToModulator(bm : BehaviorModulator) {
		myCachedModulator = bm;
		for (val bs : BehaviorSpec <- mySceneSpec.myBehaviorSpecs.values) {
			val b = bs.makeBehavior();
			bm.attachModule(b);
		}
	}
	import org.appdapter.api.module.{Module}
	
	// Temporary approach to attaching perf-monitor-modules
	def attachModule(aModule : Module[BScene]) {
		if (myCachedModulator != null) {
			myCachedModulator.attachModule(aModule)
		}
	}
	def getChannel(id : Ident) : PerfChannel = {
		return myWiredChannels.getOrElse(id, null);
	}
	override def toString() : String = {
		"BScene[id=" + rootyID + ", chanMap=" + myWiredChannels + "]";
	}
}

class LocalGraph(graphQN : String)

import scala.collection.mutable.Map
class FancyBScene(ss: SceneSpec) extends BScene(ss) {
	val		myPerfMonModsByStepSpecID : Map[Ident, FancyPerfMonitorModule] = Map()
	val		myLocGraphsByID : Map[Ident, LocalGraph] = Map()
	
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


