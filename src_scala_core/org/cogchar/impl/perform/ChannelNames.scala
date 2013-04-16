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
import  org.cogchar.api.perform.{Media, PerfChannel, Performance};
//BasicTextChannel, BasicFramedChannel, BasicTextPerformance, BasicFramedPerformance};

import org.appdapter.api.module.Module.State;
import org.appdapter.core.log.{BasicDebugger};
import org.appdapter.core.name.{Ident, FreeIdent};
import org.appdapter.core.item.{Item};
import org.appdapter.core.component.KnownComponentImpl;
import org.appdapter.bind.rdf.jena.assembly.DynamicCachingComponentAssembler;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Resource;

import org.cogchar.name.dir.NamespaceDir;

object ChannelNames extends org.appdapter.api.trigger.BoxAssemblyNames {
	val		NS_ccScn =	NamespaceDir.NS_ccScn // "http://www.cogchar.org /schema/scene#";
	val		NS_ccScnInst = NamespaceDir.NS_ccScnInst // "http://www.cogchar.org /schema/scene/instance#";

	val		P_details = NS_ccScn + "details";
	val		P_osgiFilterString = NamespaceDir.NS_CCRT_RT + "OSGiFilter";
	val		P_channelType = NamespaceDir.NS_CCRT_RT + "chanType";
	
	val		N_PRE_verbalOut =   "speechOut";
	val		N_PRE_animOut	=	"animOut";
	val		N_PRE_blendOut	=	"blendOut";
	
	val		N_PRE_verbalIn	=	"verbalIn";
	val		N_PRE_spatialIn	=	"spatialIn"
	val		N_PRE_triggerIn	=	"triggerIn";
	
	// The apparent complexity here is due to a lack of *deep* assumption that all
	// channels can or should fit in a uniform numbering space.  There is a shallow
	// assumption, since the ranges are kept distinct and all 3 digits, but that
	// assumption can be directly relaxed.
	// 
	// Output = 000 to 499
	val		VERBAL_OUT_CHANNEL_NUM_DIGITS			= 3;
	val		CHN_OUT_SPEECH_BEST						= 100;
	
	val		ANIM_OUT_CHANNEL_NUM_DIGITS				= 3;
	val		CHN_OUT_ANIM_RK_BEST					= 200;
	val		CHN_OUT_ANIM_RK_PERM					= 210;
	val		CHN_OUT_ANIM_RK_TEMP					= 220;
	
	val		BLEND_OUT_CHANNEL_NUM_DIGITS				= 3;
	
	val		CHN_OUT_BLEND_RK_BEST					= 250;
	
	// Input = 500 to 999
	val		VERBAL_IN_CHANNEL_NUM_DIGITS			= 3;
	val		CHN_IN_VERBAL_HEARD_BEST				= 500;
	val		CHN_IN_VERBAL_CHAT_REPLY_BEST			= 550;
	val		CHN_IN_VERBAL_CHAT_REPLY_COGBOT			= 555;
	val		CHN_IN_VERBAL_CHAT_REPLY_OTHER			= 557;

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
	val		CHN_IN_TRIGGER_ANIMATION				= 730;
	

	
	private def getChannelIdent(localName : String) : Ident = { 
		val absURI = NS_ccScnInst + localName;
		new FreeIdent(absURI, localName);
	}
	private def getNumericChannelName(prefix : String, chanNum  : Int, chanWidth : Int) : String = { 
		val fmtString = "%s_%0" + chanWidth.toString + "d";
		fmtString.format(prefix, chanNum);
	}

	// The (public) defs below link a set of compiled symbols (the method names) to a set of 
	// RDF symbols (the idents constructed by the methods).  If these idents need to be accessed
	// a lot (which seems somewhat likely), we can set up some vals or a cache.
	
	private def getOutChanIdent_Verbal(chanNum  : Int) : Ident = { 
		val chanName = getNumericChannelName(N_PRE_verbalOut, chanNum, VERBAL_OUT_CHANNEL_NUM_DIGITS);
		getChannelIdent(chanName);
	}
	def getOutChanIdent_SpeechMain() = getOutChanIdent_Verbal(CHN_OUT_SPEECH_BEST); 

	
	
	private def getOutChanIdent_Anim(chanNum  : Int) : Ident = { 
		val chanName = getNumericChannelName(N_PRE_animOut, chanNum, ANIM_OUT_CHANNEL_NUM_DIGITS);
		getChannelIdent(chanName);
	}	
	
	def getOutChanIdent_AnimBest() = getOutChanIdent_Anim(CHN_OUT_ANIM_RK_BEST);
	def getOutChanIdent_AnimPerm() = getOutChanIdent_Anim(CHN_OUT_ANIM_RK_PERM);
	def getOutChanIdent_AnimTemp() = getOutChanIdent_Anim(CHN_OUT_ANIM_RK_TEMP);
	
	private def getOutChanIdent_Blend(chanNum  : Int) : Ident = { 
		val chanName = getNumericChannelName(N_PRE_blendOut, chanNum, ANIM_OUT_CHANNEL_NUM_DIGITS);
		getChannelIdent(chanName);
	}
	
	def getOutChanIdent_BlendBest() = getOutChanIdent_Blend(CHN_OUT_BLEND_RK_BEST);
	
	private def getInChanIdent_Verbal(chanNum  : Int) : Ident = { 
		val chanName = getNumericChannelName(N_PRE_verbalIn, chanNum, VERBAL_IN_CHANNEL_NUM_DIGITS);
		getChannelIdent(chanName);
	}
	
	def getInChanIdent_VerbalHeardBest() = getInChanIdent_Verbal(CHN_IN_VERBAL_HEARD_BEST); 
	def getInChanIdent_VerbalChatReplyBest() = getInChanIdent_Verbal(CHN_IN_VERBAL_CHAT_REPLY_BEST); 
	def getInChanIdent_VerbalChatReplyCogbot() = getInChanIdent_Verbal(CHN_IN_VERBAL_CHAT_REPLY_COGBOT);
	def getInChanIdent_VerbalChatReplyOther() = getInChanIdent_Verbal(CHN_IN_VERBAL_CHAT_REPLY_OTHER);

	private def getInChanIdent_Trigger(chanNum  : Int) : Ident = { 
		val chanName = getNumericChannelName(N_PRE_triggerIn, chanNum, TRIGGER_IN_CHANNEL_NUM_DIGITS);
		getChannelIdent(chanName);
	}
	
	def getInChanIdent_TriggerScene() = getInChanIdent_Trigger(CHN_IN_TRIGGER_SCENE);
	def getInChanIdent_TriggerMode() = getInChanIdent_Trigger(CHN_IN_TRIGGER_MODE);
	def getInChanIdent_TriggerAction() = getInChanIdent_Trigger(CHN_IN_TRIGGER_ACTION);
	def getInChanIdent_TriggerAnimation() = getInChanIdent_Trigger(CHN_IN_TRIGGER_ANIMATION);
	
}