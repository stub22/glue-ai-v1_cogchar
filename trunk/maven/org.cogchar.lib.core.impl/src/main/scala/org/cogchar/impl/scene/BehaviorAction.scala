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
import  org.cogchar.api.perform.BehaviorActionExec
import  org.cogchar.impl.perform.basic.{BasicPerformance}
import org.cogchar.impl.channel.{FancyChannelSpec};
import org.cogchar.api.perform.{PerfChannel, Media, Performance, FancyPerformance};
import org.cogchar.impl.perform.{FancyTime, FancyTextMedia, FancyTextPerf, FancyTextCursor, FancyTextPerfChan, FancyTextInstruction};

import org.cogchar.api.channel.{GraphChannel}
import org.cogchar.impl.channel.{ThingActionGraphChan}
import org.cogchar.api.thing.{ThingActionSpec, ThingActionFilter, WantsThingAction}
import org.cogchar.api.fancy.{FancyThingModelWriter}
import org.apache.http.impl.client.{DefaultHttpClient}

import org.cogchar.impl.thing.basic.BasicThingActionSpecBuilderTempFunctions
import  org.cogchar.api.perform.BehaviorActionExec

import java.util.Random;

import org.cogchar.api.scene._
import scala.collection.JavaConverters._


/**
 * @author Stu B. <www.texpedient.com>
 */

trait BehaviorActionExec_Scala_goto_api_Java {
	// Must execute very quickly, as it is blocking the whole coroutine system!
	// Returns a list of all ongoing tasks started by this perform-ance.
	//def perform(s: org.cogchar.api.scene.Scene) : List[FancyPerformance]
}

abstract class BehaviorActionSpec extends BasicDebugger {
	// Usually there will be just one channel ID attached to each Action.
	var	myChannelIdents : List[Ident] = List();
	def addChannelIdent(id  : Ident) {
		myChannelIdents = myChannelIdents :+ id;
		getLogger().debug("************ appended {} so list is now: {} ", Array[Object]( id, myChannelIdents));
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
		for (actChanSpec <- actionChannelSpecs.toArray) {
			actChanSpec match {
				case fcs: FancyChannelSpec => {
					wireFancyChannelSpec(fcs)
				}
				case _ => getLogger().warn("Unexpected object found in step at {} = {}", Array[Object]( chanPropName, actChanSpec));
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
	override def performExec(s: Scene[_,_]) : java.util.List[org.cogchar.api.perform.FancyPerformance] = {
		var perfListReverseOrder : List[FancyPerformance] = Nil // new java.util.ArrayList[org.cogchar.api.perform.FancyPerformance](foo.toList)
		val media = new FancyTextMedia(mySpec.myActionText);
		// We don't yet have a practical use case where there is really more than one outChan here.
		for (chanId : Ident <- mySpec.myChannelIdents) {
			getLogger().debug("Looking for channel[{}] in scene [{}]", Array[Object]( chanId, s));
			val chan : PerfChannel = s.getPerfChannel(chanId);
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
		new java.util.ArrayList[org.cogchar.api.perform.FancyPerformance](perfListReverseOrder.reverse.asJava)  // we presume that Nil.reverse == Nil !
	}
	override def toString() : String = {
		"TextActionExec[spec=" + mySpec + "]";
	}
}

/*
trait WantsThingAction {
	def consumeSpec(inTASpec : ThingActionSpec) : Unit
}
*/
/*
 * Consumes filtered ThingActions from an input channel, and uses them to write to some output channel.
 * For an output *graph*-channel, this passthru can be accomplished as a pure-graph operation, so
 * there would be no reason to deserialize for that case.
 * However, when the output channel downstream is a more concrete kind of Java perf-channel, then the
 * deserialization of the ThingActionSpec into this perform method is useful.
 */
class UseThingActionExec(val mySpec : UseThingActionSpec) extends BasicDebugger with BehaviorActionExec {
	override def performExec(s: Scene[_,_]) : java.util.List[org.cogchar.api.perform.FancyPerformance] = {
		// Find and "take" the most obvious input ThingAction, by marking a seen-it bag for this agent.
		var perfListReverseOrder : List[FancyPerformance] = Nil // new java.util.ArrayList[org.cogchar.api.perform.FancyPerformance](foo.toList)
		val inChanID : Ident = mySpec.myInChanID;
		getLogger().info("UseThingActionExec.perform inChanID=", inChanID)

		val graphChanHub = Nil
		val inTASpec = Nil
		if (mySpec.myChannelIdents.size == 1) {
			val outChanID = mySpec.myChannelIdents.head
			val outChan : PerfChannel = s.getPerfChannel(outChanID);
			val inChan : GraphChannel = s.getGraphChannel(mySpec.myInChanID)

      //Sending unseen ThingActions to outChan
      /*
       * We will want to control how many ThingActions get processed per execution.
       * TAGraphChane views and marks all unseen ThingActions at once, that will
       * need to be changed to give us finer control.
       */
      inChan match {
        case taChan :ThingActionGraphChan => {
            val tasList : java.util.List[ThingActionSpec] = taChan.seeThingActions
            for(tas : ThingActionSpec <- tasList.asScala){
              val perf = useIt(tas, outChan)
              if(perf.isInstanceOf[FancyPerformance]){
                perfListReverseOrder = perf.asInstanceOf[FancyPerformance] :: perfListReverseOrder
              }
            }
        }
      }
		}
      new java.util.ArrayList[org.cogchar.api.perform.FancyPerformance](    perfListReverseOrder.asJava)
	}

	def useIt(inTASpec : ThingActionSpec, outChan : PerfChannel) : Option[FancyPerformance] = {
		// By default, we look for an obvious way to pass taSpec to outChan.
		// We also sometimes need a way to capture + return a FancyPerformance for monitoring.
		outChan match {
			// Experiment:  We try matching the Channel against a broader ThingAction-Consumer object
			// (not specific to our Cogchar Behavior system, per se).  This should work, but cannot yet yield us a perf.
			case wtaChan : WantsThingAction => {
				val consumpStatus = wtaChan.consumeAction(inTASpec, outChan.getIdent)
			}
		}
		None
	}
}
class UseThingActionSpec (val myInChanID : Ident, val myOptFilterID : Option[Ident]) extends BehaviorActionSpec() {
	// This object needs to configure a number of decisions:
	// Where should perform() look for the input ThingAction?
	// What filter should we use to *take* one/all of the the input-TAs before then passing them on?
	// Where is the implied "seen-it" bag for this agent on this channel, used to mark ?
	// When did this agent "start"?  (For input ThingAction filtering purposes)
	// Where should perform() send the output ThingAction?

	override def makeActionExec() : BehaviorActionExec = {
		new UseThingActionExec(this)
	}
	override def toString() : String = {
		"UseThingActionSpec[inChanID= " + myInChanID + ", optFilterID=" + myOptFilterID + ", outChanIDs=" + myChannelIdents + "]";
	}
}

class FireThingActionExec( val mySpec : FireThingActionSpec) extends BasicDebugger with BehaviorActionExec {
  val theTargetGraphQN = "ccrt:thing_sheet_22"

  override def performExec(s: Scene[_,_]) : java.util.List[org.cogchar.api.perform.FancyPerformance] = {
    
    var perfs = new java.util.ArrayList[org.cogchar.api.perform.FancyPerformance]();
    val taList = mySpec.myThingActionSpecList
    for ( ta : ThingActionSpec <- taList ) {
      val fmw : FancyThingModelWriter = new FancyThingModelWriter()
      val updateTextToAddTA : String = fmw.writeTASpecToString(ta, theTargetGraphQN, new Random())

      val debugFlag : Boolean = false;
      val fixme_functions : BasicThingActionSpecBuilderTempFunctions = new BasicThingActionSpecBuilderTempFunctions()
      fixme_functions.execRemoteSparqlUpdate("", updateTextToAddTA, debugFlag)

    }
    perfs;
  }
}
class FireThingActionSpec (val myThingActionSpecList: List[ThingActionSpec], val myOutputTAGraph: Ident ) extends BehaviorActionSpec() {

  override def makeActionExec() : BehaviorActionExec = {
		new FireThingActionExec(this)
	}

	override def toString() : String = {
      "FireThingActionSpec[firesThingActions= " + myThingActionSpecList.toString + ", outputTAGraph = " + myOutputTAGraph.toString;
	}
}