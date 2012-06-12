/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.blob.emit

import org.appdapter.core.item.Ident;
import org.appdapter.core.item.FreeIdent;

import org.cogchar.api.humanoid.{HumanoidFigureConfig, HumanoidBoneConfig, HumanoidBoneDesc};

/**
 * @author Stu B. <www.texpedient.com>
 */
case class NVParam(val name: String, val value: String) {
	def urlEncoding : String = {
		name + "=" + value;
	}
}

class BonyConfigEmitter extends DemoConfigEmitter {

	val COGCHAR_URN_PREFIX = "urn:ftd:cogchar.org:2012:";
	
	val	COGCHAR_CHAR_URN_PREFIX = COGCHAR_URN_PREFIX + "char#";

	
	// Commercial "Extra Robot" model, not loadable without a license.
	// Cogchar shoud ignore this "ExtraRobot" when it is not loadable, and still
	// function correctly for Sinbad + other characters.
	val EXTRA_ROBOT_MESH_PATH = "model3D/char/zeno/zeno_R50_06_20120323/zenobot_06_20120323.mesh.xml";		
	
	// val EXTRA_ROBOT_MESH_PATH = "zenobot_05_20120309/ZenoBot05.ma.mesh.xml";		
	//		String dualCharURI = "urn:org.cogchar/platform/nb701?char=HRK_Zeno_R50&version=20120302";   
	//		
	//			
	val HRK_URN_PREFIX = "urn:ftd:hrkind.com:2012:chars#";
	val ZENO_R50_NICKNAME = "ZenoR50";
	val	ZENO_R50_CHAR_URI = COGCHAR_CHAR_URN_PREFIX + ZENO_R50_NICKNAME;
	val	ZENO_R50_CHAR_IDENT = new FreeIdent(ZENO_R50_CHAR_URI, ZENO_R50_NICKNAME)
	
	
	
	
	val FANCY_PANEL_CLASSNAME = "org.cogchar.render.gui.bony.FancyCharPanel";
	val SLIM_PANEL_CLASSNAME = "org.cogchar.render.gui.bony.VirtCharPanel";
	
	// Skeleton + mesh model ias delivered from o.c.b.render.opengl.resources
	val SINBAD_MESH_PATH = "jme3dat/models_20110917/sinbad/Sinbad.mesh.xml";	




	val WINGED_OBELISK_SCENE = "leo_hanson_tests/test3/test3.scene";
	val WOS_SCALE = 0.5f;
	
	// val NB_BONY_ROBOT_ID = "COGCHAR_NB_ROBOT";
	// val DUMMY_ROBOT_ID = "DummyRobot22";
	
	val SINBAD_NICKNAME = "Sinbad";
	
	

	
	val	SINBAD_CHAR_URI = COGCHAR_CHAR_URN_PREFIX + SINBAD_NICKNAME;
	
	val	SINBAD_CHAR_IDENT = new FreeIdent(SINBAD_CHAR_URI, SINBAD_NICKNAME)
	
	//val SINBAD_JOINT_PATH = "rk_bind_config/motion/bonyRobotConfig_Sinbad.json";
	//val ZENO_JOINT_PATH = "rk_bind_config/motion/bonyRobotConfig_ZenoR50.json";
	
	val NB_PLATFORM_CURRENT = "nb701";
	

	
	def isZenoHome() : Boolean = {	true;	}
	
	def isMinimalSim() : Boolean = {	getSystemContextURI.startsWith("NB");	}
	
	def makeParamString(bindingList : List[NVParam]) : String = {
		val len = bindingList.length;
		if (len == 0) {
			return "";
		} else { 
			val firstPairString = bindingList.head.urlEncoding;
			if (len == 1) {
				return firstPairString;
			} else {
				return firstPairString + "&" + makeParamString(bindingList.tail);
			}
		}
	}
	
	def makeCogcharURN(item : String, bindingList : List[NVParam]) : String = {
		val paramsEncoded = makeParamString(bindingList);
		val marker = if (paramsEncoded.length() > 0) "?" else "";
		COGCHAR_URN_PREFIX + item + marker + paramsEncoded;
	}
	
	def getStickFigureScenePath : String = {
		if (isMinimalSim()) null else WINGED_OBELISK_SCENE;
	}
	def getStickFigureSceneScale : Float = 0.5f;

	/*
	def getRobokindRobotID(robotURI : String) = {
		NB_BONY_ROBOT_ID; // or DUMMY_ROBOT_ID, ...
	}
	*/
   
	def getVCPanelClassName(kind : String) : String = {
		kind match {
			case "FULL" => FANCY_PANEL_CLASSNAME;
			case "SLIM" => SLIM_PANEL_CLASSNAME;
			case _ => null
		}
	}
	
	def getNamedFloatVector(vectorURI : String) : Array[Float] = {
		val third = 0.33333f;
		val res = new Array[Float](3); // third, third, third);
		res(0) = third;
		res(1) = third;
		res(2) = third;
		val res2 : Array[Float] = Array(third, third, third);
		res2;
	}
			
	def getActiveBonyCharIdents() : java.util.List[Ident] = {
		val res = new java.util.ArrayList[Ident]();
		if (isZenoHome()) {res.add(ZENO_R50_CHAR_IDENT)};
		if (!isMinimalSim()) {res.add(SINBAD_CHAR_IDENT);}
		res;
	}
	
	def getNicknameForChar(charIdent : Ident) : String = getNicknameForChar(charIdent.getAbsUriString())
	def getNicknameForChar(charURI : String) : String = {		
		charURI match {
			case SINBAD_CHAR_URI => SINBAD_NICKNAME
			case ZENO_R50_CHAR_URI => ZENO_R50_NICKNAME
		}
	}
	
	def getBonyConfigPathTailForChar(charIdent: Ident) : String = getBonyConfigPathTailForChar(charIdent.getAbsUriString())
	def getBonyConfigPathTailForChar(charURI : String) : String = {
		val chrShortName = getNicknameForChar(charURI);
		getBehaviorConfigEmitter().getBonyRobotConfigPathTail(chrShortName);
	}
	
	def getJointGroupPathTailForChar(charIdent: Ident) : String = getJointGroupPathTailForChar(charIdent.getAbsUriString())
	def getJointGroupPathTailForChar(charURI : String) : String = {
		val chrShortName = getNicknameForChar(charURI);
		getBehaviorConfigEmitter().getJointGroupConfigPathTail(chrShortName);
	}	

	def getMeshPathForChar(charIdent : Ident) : String = getMeshPathForChar(charIdent.getAbsUriString())
	def getMeshPathForChar(charURI : String) : String = {	
		charURI match {
			case SINBAD_CHAR_URI => SINBAD_MESH_PATH
			case ZENO_R50_CHAR_URI => EXTRA_ROBOT_MESH_PATH
		}
	}
	
	private def buildBaseHumanoidFigureConfigForChar(charIdent : Ident) : HumanoidFigureConfig = {
		val hfc = new HumanoidFigureConfig();
		hfc.myCharIdent = charIdent;
		hfc.myNickname = getNicknameForChar(charIdent);
		hfc.myMeshPath = getMeshPathForChar(charIdent);
		hfc.myDebugSkelMatPath = getMaterialPath;
		hfc.myBoneConfig = new HumanoidBoneConfig();
		hfc
	}
	private def getSinbadFigureConfig()  : HumanoidFigureConfig = {
		val hfc = buildBaseHumanoidFigureConfigForChar(SINBAD_CHAR_IDENT);
		hfc.myBoneConfig.addSinbadDefaultBoneDescs();
		hfc.myInitX = 30.0f;
		hfc.myInitY = 0.0f;
		hfc.myInitZ = -30.0f;
		hfc.myPhysicsFlag = true;
		hfc
	}
	private def getZenoFigureConfig() : HumanoidFigureConfig =  {
		val hfc = buildBaseHumanoidFigureConfigForChar(ZENO_R50_CHAR_IDENT);
		hfc.myBoneConfig.addZenoDefaultBoneDescs();
		hfc.myInitX = 0.0f;
		hfc.myInitY = -5.0f;
		hfc.myInitZ = 0.0f;
		hfc.myPhysicsFlag = false;
		hfc
	}
	def getHumanoidFigureConfigForChar(charIdent : Ident) : HumanoidFigureConfig = {
		if (charIdent.equals(SINBAD_CHAR_IDENT)) {
			getSinbadFigureConfig()
		} else if (charIdent.equals(ZENO_R50_CHAR_IDENT)) {
			getZenoFigureConfig();
		} else {
			buildBaseHumanoidFigureConfigForChar(charIdent);
		}
	}
	
}
