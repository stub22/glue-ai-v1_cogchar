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
import org.cogchar.api.perform.{Channel, Media, BasicChannel, Performance};
import org.cogchar.impl.perform.{FancyTime, ChannelSpec, ChannelNames};

import org.cogchar.api.scene.{Scene, SceneBuilder};

import scala.collection.mutable.HashMap;
/**
 * @author Stu B. <www.texpedient.com>
 */

class BSceneRootChan (id : Ident, val scn: BScene) extends BasicChannel[Media, FancyTime](id) {
	override protected def attemptMediaStartNow(m : Media ) : Unit = {
	}
	override def makePerformanceForMedia(media : Media ) : Performance[Media, FancyTime] = {
		null;
	}
	override def getMaxAllowedPerformances() : Int = 1;
}
// trait SubChan extends Channel[Media, FancyTime] {}

class BScene(val mySceneSpec: SceneSpec) extends BasicDebugger with Scene[FancyTime, BSceneRootChan] {
	val rootyID = new FreeIdent(SceneFieldNames.I_rooty, SceneFieldNames.N_rooty);
	val myRootChan = new BSceneRootChan(rootyID, this);
	val		myWiredChannels  = new HashMap[Ident,Channel[_ <: Media, FancyTime]]();
	
	override def getRootChannel() : BSceneRootChan = {	myRootChan	}
	import scala.collection.JavaConversions._;
	override def wireSubChannels(chans : java.util.Collection[Channel[_ <: Media, FancyTime]]) : Unit = {
		// TODO:  reconcile the actually wired channels with the ones in the SceneSpecs.		
		for (val c <- chans) {
			getLogger().info("Wiring scene[{}] to channel: {}", mySceneSpec.getIdent.getLocalName, c);
			myWiredChannels.put(c.getIdent, c)
		}
	}
	def attachBehaviorsToModulator(bm : BehaviorModulator) {
		for (val bs : BehaviorSpec <- mySceneSpec.myBehaviorSpecs.values) {
			val b = bs.makeBehavior();
			bm.attachModule(b);
		}
	}
	def getChannel(id : Ident) : Channel[_ <: Media, FancyTime] = {
		return myWiredChannels.getOrElse(id, null);
	}
	override def toString() : String = {
		"BScene[id=" + rootyID + ", chanMap=" + myWiredChannels + "]";
	}
}



