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

/**
 * @author Stu B. <www.texpedient.com>
 */
case class NVParam(val name: String, val value: String) {
	def urlEncoding : String = {
		name + "=" + value;
	}
}

class BonyConfigEmitter extends DemoConfigEmitter {
	// This skeleton + mesh model is delivered by the jar from o.c.bundle.render.opengl.resources
	val SINBAD_MESH_PATH = "jme3dat/models_20110917/sinbad/Sinbad.mesh.xml";	

	// Commercial "Extra Robot" model, not loadable without a license, ignored in those cases.
	val EXTRA_ROBOT_MESH_PATH = "zenobot_05_20120309/ZenoBot05.ma.mesh.xml";		
	// val EXTRA_ROBOT_MESH_PATH = "zenobot_04_20120302/ZenoBot04.ma.mesh.xml";		
	
	//		String dualCharURI = "urn:org.cogchar/platform/nb701?char=HRK_Zeno_R50&version=20120302";   
	val WINGED_OBELISK_SCENE = "leo_hanson_tests/test3/test3.scene";
	val WOS_SCALE = 0.5f;
	
	// val NB_BONY_ROBOT_ID = "COGCHAR_NB_ROBOT";
	// val DUMMY_ROBOT_ID = "DummyRobot22";
	
	val DEFAULT_SYS_CONTEXT_URI = "urn:org.cogchar/syscontext/default";
	
	val	SINBAD_CHAR_URI = "urn:org.cogchar/char/Sinbad"
	val	ZENO_CHAR_URI = "urn:org.cogchar/char/Zeno"
	
	val SINBAD_JOINT_PATH = "rk_bind_config/motion/bonyRobotConfig_Sinbad.json";
	val ZENO_JOINT_PATH = "rk_bind_config/motion/bonyRobotConfig_ZenoR50.json";
	
	var	mySystemContextURI : String = DEFAULT_SYS_CONTEXT_URI;
	
	def setSystemContextURI(uri: String) : Unit = {
		mySystemContextURI = uri;
	}
	def getSystemContextURI = mySystemContextURI;
	
	val COGCHAR_URN_PRE = "urn:org.cogchar/";
	
	val NB_PLATFORM_CURRENT = "nb701";
	
	val x=12;
	

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
		COGCHAR_URN_PRE + item + marker + paramsEncoded;
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
			case "FULL" => "org.cogchar.render.opengl.bony.gui.FancyCharPanel";
			case "SLIM" => "org.cogchar.render.opengl.bony.gui.VirtCharPanel";
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
	
	def isMinimalSim() : Boolean = {
		mySystemContextURI.startsWith("NB");
	}
	
	def getBonyCharURIs() : java.util.List[String] = {
		val res = new java.util.ArrayList[String]();
		res.add(ZENO_CHAR_URI);		
		res.add(SINBAD_CHAR_URI);
		res;
	}
	def getJointConfigAssetNameForChar(charURI : String) : String = {
		charURI match {
			case SINBAD_CHAR_URI => SINBAD_JOINT_PATH
			case ZENO_CHAR_URI => ZENO_JOINT_PATH
		}
		
	}	
	def getMeshPathForChar(charURI : String) : String = {	
		charURI match {
			case SINBAD_CHAR_URI => SINBAD_MESH_PATH
			case ZENO_CHAR_URI => EXTRA_ROBOT_MESH_PATH
		}
	}
	def getNicknameForChar(charURI : String) : String = {		
		charURI match {
			case SINBAD_CHAR_URI => "Sinbad"
			case ZENO_CHAR_URI => "Zeno"
		}
	}
}
