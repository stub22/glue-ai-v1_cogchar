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

import  org.cogchar.api.event.{Event}
import  org.cogchar.api.perform.{Media, Channel, Performance, BasicTextChannel, BasicFramedChannel, BasicTextPerformance, BasicFramedPerformance};

import org.appdapter.api.module.Module.State;
import org.appdapter.core.log.{BasicDebugger};
import org.appdapter.core.item.{Ident, Item, FreeIdent};
import org.appdapter.core.component.KnownComponentImpl;
import org.appdapter.bind.rdf.jena.assembly.DynamicCachingComponentAssembler;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;



/**
 * @author Stu B. <www.texpedient.com>
 */

class FancyTime (val myStampMsec : Long) {
}

trait FancyChanStuff {
}

abstract class FancyTextChan(id: Ident) extends BasicTextChannel[FancyTime](id) {}
abstract class FancyFramedChan[F](id: Ident) extends BasicFramedChannel[FancyTime,F](id) {}	
	

class FancyTextPerf(media : Media.Text, chan: Channel.Text[FancyTime]) 
		extends  BasicTextPerformance[FancyTime, FancyTextPerf, Event[FancyTextPerf, FancyTime]](media, chan) {
}
class FancyFramedPerf[F](media : Media.Framed[F], chan: Channel.Framed[FancyTime,F]) 
		extends  BasicFramedPerformance[FancyTime, F, FancyFramedPerf[F], Event[FancyFramedPerf[F], FancyTime]](media, chan) {
}

class DummyTextChan(id: Ident) extends FancyTextChan(id) {
	@throws(classOf[Throwable])	
	override protected def attemptMediaStartNow(m : Media.Text) {
		val textString = m.getFullText();
		logInfo("************* START DUMMY TEXT PERFORMANCE on [" + getName() + "] of [" + textString + "]");
	}
	override def  makePerformanceForMedia(media : Media.Text) : Performance[Media.Text, FancyTime] =  {
		return new FancyTextPerf(media, this);
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

object ChannelNames extends org.appdapter.api.trigger.BoxAssemblyNames {
	val		NS_ccScn =	"http://www.cogchar.org/schema/scene#";
	val		NS_ccScnInst = "http://www.cogchar.org/schema/scene/instance#";

	val		N_PRE_speechOut =  "speechOut";
	val		N_PRE_animOut =		"animOut";
	
	val		N_PRE_speechIn	=	"speechIn";
	val		N_PRE_visionIn	=	"visionIn"
	val		N_PRE_triggerIn	=	"triggerIn";
	
	// Output = 000 to 499
	val		VERBAL_OUT_CHANNEL_NUM_DIGITS			= 3;
	val		CHN_OUT_SPEECH_BEST						= 100;
	
	val		ANIM_OUT_CHANNEL_NUM_DIGITS				= 3;
	val		CHN_OUT_ANIM_RK_BEST					= 200;
	val		CHN_OUT_ANIM_RK_PERM					= 210;
	val		CHN_OUT_ANIM_RK_TEMP					= 220;
	
	val		CHN_OUT_BLEND_RK_BEST					= 250;
	
	// Input = 500 to 999
	val		VERBAL_IN_CHANNEL_NUM_DIGITS			= 3;
	val		CHN_IN_VERBAL_HEARD						= 500;
	val		CHN_IN_CHAT_REPLY						= 510;

	val		SPATIAL_IN_CHANNEL_NUM_DIGITS			= 3;
	val		CHN_IN_SPATIAL_FACE						= 600;
	val		CHN_IN_SPATIAL_MOTION					= 610;
	val		CHN_IN_SPATIAL_SALIENCE					= 620;
	val		CHN_IN_SPATIAL_IN_SKELETON				= 630;
	val		CHN_IN_SPATIAL_IN_SOUND_LOC				= 640;
	

	val		TRIGGER_IN_CHANNEL_NUM_DIGITS			= 3;
	val		CHN_IN_TRIGGER_SCENE					= 700;
	val		CHN_IN_TRIGGER_MODE						= 710;
	val		CHN_IN_TRIGGER_ACTION					= 720;
	val		CHN_IN_TRIGGER_ANIM						= 730;
	

	
	def getChannelIdent(localName : String) : Ident = { 
		val absURI = NS_ccScnInst + localName;
		new FreeIdent(absURI, localName);
	}
	def getNumericChannelName(prefix : String, chanNum  : Int, chanWidth : Int) : String = { 
		val fmtString = "%s_%0" + chanWidth.toString + "d";
		fmtString.format(prefix, chanNum);
	}
	
	def getSpeechOutChannelIdent(chanNum  : Int) : Ident = { 
		val chanName = getNumericChannelName(N_PRE_speechOut, chanNum, VERBAL_OUT_CHANNEL_NUM_DIGITS);
		getChannelIdent(chanName);
	}
	def getMainSpeechOutChannelIdent() = getSpeechOutChannelIdent(CHN_OUT_SPEECH_BEST); 

	def getAnimChannelIdent(chanNum  : Int) : Ident = { 
		val chanName = getNumericChannelName(N_PRE_animOut, chanNum, ANIM_OUT_CHANNEL_NUM_DIGITS);
		getChannelIdent(chanName);
	}	
	
	def getAnimBestChannelIdent() = getAnimChannelIdent(CHN_OUT_ANIM_RK_BEST);
	def getAnimPermChannelIdent() = getAnimChannelIdent(CHN_OUT_ANIM_RK_PERM);
	def getAnimTempChannelIdent() = getAnimChannelIdent(CHN_OUT_ANIM_RK_TEMP);
}