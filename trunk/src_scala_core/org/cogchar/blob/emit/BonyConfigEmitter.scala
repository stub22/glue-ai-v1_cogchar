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

class BonyConfigEmitter extends DemoConfigEmitter {
	// This skeleton + mesh model is delivered by the jar from o.c.bundle.render.opengl.
	val PATH_HUMANOID_MESH = "jme3dat/models_20110917/sinbad/Sinbad.mesh.xml";	

	// val EXTRA_ROBOT_MESH_PATH = "zeno_model-ogre/zeno_model-ogre.mesh.xml";
	val EXTRA_ROBOT_MESH_PATH = "zenobot_04_20120302/ZenoBot04.ma.mesh.xml";	
	
	val WINGED_OBELISK_SCENE = "leo_hanson_tests/test3/test3.scene";
	val WOS_SCALE = 0.5f;
	
	val PATH_HUMANOID_JOINT_CONFIG = "bonyRobotConfig.json";
	val NB_PATH_HUMANOID_JOINT_CFG = "org_cogchar_nbui_render/bonyRobotConfig.json";
	
	val NB_BONY_ROBOT_ID = "COGCHAR_NB_ROBOT";
	val DUMMY_ROBOT_ID = "DummyRobot22";
	
	val DEFAULT_MAIN_CHAR_URI = "NO_CHARACTER_AT_ALL";
	
	var	myMainCharURI : String = DEFAULT_MAIN_CHAR_URI;
	
	def setMainCharURI(uri: String) : Unit = {
		myMainCharURI = uri;
	}

//				DEFAULT_PATH_HUMANOID_MESH = "Models/Sinbad/Sinbad.mesh.xml",	// Default path in JME test setup
//			PATH_UNSHADED_MAT =  "Common/MatDefs/Misc/Unshaded.j3md",
	def getJointConfigFileForChar() : java.io.File = {
		println("**************\n***************");
		println("Scala BonyConfigEmitter generating jointConfigFile for charURI[" + myMainCharURI + "]");
		println("**************\n***************");
		val path = if (myMainCharURI.startsWith("NBURI")) NB_PATH_HUMANOID_JOINT_CFG  else PATH_HUMANOID_JOINT_CONFIG;
		return new java.io.File(path);
	}
	
	def getHumanoidMeshPath : String = PATH_HUMANOID_MESH;
	def getExtraRobotMeshPath : String = EXTRA_ROBOT_MESH_PATH;
	
	def getStickFigureScenePath : String = WINGED_OBELISK_SCENE;
	def getStickFigureSceneScale : Float = 0.5f;

	def getRobokindRobotID(robotURI : String) = {
		NB_BONY_ROBOT_ID; // or DUMMY_ROBOT_ID, ...
	}
	
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
}
