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
import org.appdapter.core.item.{Item, ItemFuncs};
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;
import org.cogchar.name.behavior.{SceneFieldNames};

import org.appdapter.core.log.{BasicDebugger, Loggable};

import  org.cogchar.api.perform.{Media, PerfChannel, Performance}
import  org.cogchar.impl.perform.basic.{BasicPerformance}
import org.cogchar.impl.channel.{FancyChannelSpec};
import org.cogchar.impl.perform.{FancyTime, FancyTextMedia, FancyTextPerf, FancyTextCursor, FancyPerformance, FancyTextPerfChan, FancyTextInstruction};

/**
 * @author Stu B. <www.texpedient.com>
 */

trait BehaviorActionExec {
	// Must execute very quickly, as it is blocking the whole coroutine system!
	// Returns a list of all ongoing tasks started by this perform-ance.
	def perform(s: BScene) : List[FancyPerformance]
}

abstract class BehaviorActionSpec extends BasicDebugger {
	// Usually there will be just one channel ID attached to each Action.
	var	myChannelIdents : List[Ident] = List();
	def addChannelIdent(id  : Ident) {
		myChannelIdents = myChannelIdents :+ id;
		getLogger().debug("************ appended {} so list is now: {} ", id, myChannelIdents);
	}
	def makeActionExec() : BehaviorActionExec
	
	def wireChannelSpecs(parentItem : Item, reader : ItemAssemblyReader, assmblr : Assembler , mode: Mode) {
		val chanPropName = SceneFieldNames.P_channel
		val actionChannelSpecs = reader.findOrMakeLinkedObjects(parentItem, chanPropName, assmblr, mode, null);
		getLogger().debug("Got action-channel specs: {} ", actionChannelSpecs);
		// Having more than one channel on an action will lead to exceptions later on
		// (because we are not set-up to monitor that situation).  		
		if (actionChannelSpecs.size != 1) {
			getLogger().warn("Unexpected action-channel-specs size (!=1) : {}", actionChannelSpecs.size)
		}
		for (val actChanSpec <- actionChannelSpecs.toArray) {
			actChanSpec match {
				case fcs: FancyChannelSpec => {
					wireFancyChannelSpec(fcs)
				}
				case _ => getLogger().warn("Unexpected object found in step at {} = {}", chanPropName, actChanSpec);
			}
		}		
	}
	protected def wireFancyChannelSpec(fcs : FancyChannelSpec) {
		val chanId = fcs.getIdent();
		// What does this freeing accomplish?  Are we trying to ensure the source model can be garbage collected?
		val freeChanIdent = new FreeIdent(chanId);
		addChannelIdent(freeChanIdent);		
	}
}

class TextActionSpec(val myActionText : String) extends BehaviorActionSpec() { 
	override def makeActionExec() : BehaviorActionExec = {
		new TextActionExec(this)
	}
	override def toString() : String = {
		"TextActionSpec[actionTxt=" + myActionText + ", channelIds=" + myChannelIdents + "]";
	}		
}
class TextActionExec(val mySpec : TextActionSpec) extends BasicDebugger with BehaviorActionExec {
	override def perform(s: BScene) : List[FancyPerformance] = {
		var perfListReverseOrder : List[FancyPerformance] = Nil
		val media = new FancyTextMedia(mySpec.myActionText);
		for (val chanId : Ident <- mySpec.myChannelIdents) {
			getLogger().debug("Looking for channel[{}] in scene [{}]", chanId, s);
			val chan : PerfChannel = s.getChannel(chanId);
			getLogger().debug("Found channel {}", chan);
			if (chan != null) {
				chan match {
					case txtChan : FancyTextPerfChan[_] => { 
							val perf  = txtChan.makePerfAndPlayAtBeginNow(media)
							// prepending to the list is fastest, hence the "reverseOrder" approach.
							perfListReverseOrder = perf :: perfListReverseOrder
		
						
						}
					case  _ => {
							getLogger().warn("************* TextAction cannot perform on non Text-Channel: " + chan);
						}
				}
			} else {
				getLogger().warn("******************* Could not locate channel for: " + chanId);
			}
		}
		perfListReverseOrder.reverse  // we presume that Nil.reverse == Nil !
	}
	override def toString() : String = {
		"TextActionExec[spec=" + mySpec + "]";
	}	
}

import org.cogchar.impl.channel.{FancyChannel}
import org.cogchar.api.thing.{ThingActionSpec, ThingActionFilter, WantsThingAction}
/*
trait WantsThingAction {
	def consumeSpec(inTASpec : ThingActionSpec) : Unit 
}
*/
/*
 * Consumes filtered ThingActions from an input channel, and uses them to write to some output channel.
 * But - that can be accomplished as a pure-graph operation, so there would be no reason to deserialize for that case.
 * So - 
 */
class UseThingActionExec(val mySpec : UseThingActionSpec) extends BasicDebugger with BehaviorActionExec {
	def perform(s: BScene) : List[FancyPerformance] = {
		// Find and "take" the most obvious input ThingAction, by marking a seen-it bag for this agent.
		Nil
	}
	def useIt(inTASpec : ThingActionSpec, outChan : FancyChannel) {
		// By default, we look for an obvious way to pass taSpec to outChan.
		outChan match {
			case wtaChan : WantsThingAction => {
				val consumpStatus = wtaChan.consumeAction(inTASpec, outChan.getIdent)
			}
		}
	}
}
class UseThingActionSpec (val myInChanID : Ident, val myFilterID : Ident, val myOutChanID : Ident) extends BehaviorActionSpec() { 
	// This object supplies information 
	// Where should perform() look for the input ThingAction?  
	// What filter should we use to *take* one/all of the the input-TAs before then passing them on?
	// Where is the implied "seen-it" bag for this agent on this channel, used to mark ? 
	// When did this agent "start"?
	// Where should perform() send the output ThingAction
	
	override def makeActionExec() : BehaviorActionExec = {
		new UseThingActionExec(this)
	}
	override def toString() : String = {
		"PassThingActionSpec[actionTxt= + myActionText + , channelIds=" + myChannelIdents + "]";
	}		
}

