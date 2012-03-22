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

package org.cogchar.impl.perform

import  org.cogchar.api.perform.{Channel, TextChannel, Performance, BasicPerformance}

import org.appdapter.api.module.Module.State;
import org.appdapter.core.log.{BasicDebugger};
import org.appdapter.core.item.{Ident, Item, FreeIdent};
import org.appdapter.gui.box.KnownComponentImpl;
import org.appdapter.gui.assembly.DynamicCachingComponentAssembler;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;



/**
 * @author Stu B. <www.texpedient.com>
 */

class FancyChan(val myIdent: Ident) extends BasicDebugger with Channel {
	private var	myStatus : Channel.Status = Channel.Status.INIT;
	
	def getIdent() : Ident = {myIdent}
	override def getStatus() : Channel.Status = {myStatus}
	override def getName() : String = {myIdent.getLocalName();}
}
abstract class FancyTextChan(id: Ident) extends FancyChan(id) with TextChannel {
	@throws(classOf[Throwable])
	def startTextPerformance (txt: String) : Unit;
	
	def  performText(txt: String) : Performance[TextChannel] = {		
		val perf = new BasicPerformance[TextChannel](this);
		try {
			perf.updateResultState(State.IN_START);
			startTextPerformance(txt);
			perf.updateResultState(State.IN_RUN);
		} catch {
			case t => {
				perf.updateResultState(State.FAILED_STARTUP);
				logError("Error on [" + this + "] performing text[" + txt + "]", t);
			}
		}
		perf;
	}
}
class DummyTextChan(id: Ident) extends FancyTextChan(id) {
	@throws(classOf[Throwable])	override def startTextPerformance (txt: String) : Unit = {
		logInfo("************* START DUMMY TEXT PERFORMANCE on [" + getName() + "] of [" + txt + "]");
	}
}

class ChannelSpec extends KnownComponentImpl {
	var		myDetails : String = "EMPTY";
	override def getFieldSummary() : String = {
		return super.getFieldSummary() + ", details=" + myDetails;
	}	
}
class ChannelSpecBuilder(builderConfRes : Resource) extends DynamicCachingComponentAssembler[ChannelSpec](builderConfRes) {

	override protected def initExtendedFieldsAndLinks(cs: ChannelSpec, configItem : Item, assmblr : Assembler , mode: Mode ) {
		logInfo("ChannelSpecBuilder.initExtendedFieldsAndLinks");	
		cs.myDetails = "StayTuned!";
		//val linkedBehaviorSpecs : java.util.List[Object] = findOrMakeLinkedObjects(configItem, SceneFieldNames.P_behavior, assmblr, mode, null);
		//logInfo("Scene found linkedBehaviorSpecs: " + linkedBehaviorSpecs)
	}
}

object ChannelNames extends org.appdapter.gui.assembly.AssemblyNames {
	val		NS_ccScn =	"http://www.cogchar.org/schema/scene#";
	val		NS_ccScnInst = "http://www.cogchar.org/schema/scene/instance#";

	val		N_PRE_speechOut =  "speechOut";
	
	val		SPEECH_CHANNEL_NUM_DIGITS = 3;
	val		SPEECH_MAIN_CHANNEL_NUM = 100;
	
	
	def getChannelIdent(localName : String) : Ident = { 
		val absURI = NS_ccScnInst + localName;
		new FreeIdent(absURI, localName);
	}
	def getNumericChannelName(prefix : String, chanNum  : Int, chanWidth : Int) : String = { 
		val fmtString = "%s_%0" + chanWidth.toString + "d";
		fmtString.format(prefix, chanNum);
	}
	
	def getSpeechOutChannelIdent(chanNum  : Int) : Ident = { 
		val chanName = getNumericChannelName(N_PRE_speechOut, chanNum, SPEECH_CHANNEL_NUM_DIGITS);
		getChannelIdent(chanName);
	}
	def getMainSpeechOutChannelIdent() = getSpeechOutChannelIdent(SPEECH_MAIN_CHANNEL_NUM); 

}