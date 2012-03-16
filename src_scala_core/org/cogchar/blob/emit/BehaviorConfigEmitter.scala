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

package org.cogchar.blob.emit

/**
 * @author Stu B. <www.texpedient.com>
 */

class BehaviorConfigEmitter {
	
	val DEFAULT_SYS_CONTEXT_URI = "urn:org.cogchar/syscontext/default";
	
	val RK_BIND_PERM = "rk_bind_config";
	val RK_BIND_TEMP = "rk_bind_temp";
	
	val MOTION = "motion";

	val BEHAVIOR_PERM = "behavior";
	val BEHAVIOR_TEMP = "behavior_temp";
	
	val LOGGING_TEMP = "logging_temp";
	
	val BONY_ROBOT_CONFIG_PREFIX = "bonyRobot_";
	val BONY_ROBOT_CONFIG_SUFFIX = ".ttl";
	
	val RK_JOINT_GROUP_PREFIX = "jointGroup_";
	val RK_JOINT_GROUP_SUFFIX = ".xml";	
	
	var	mySystemContextURI : String = DEFAULT_SYS_CONTEXT_URI;
	var myLocalFileRootDir : String = ".";
	
	def setSystemContextURI(uri: String) : Unit = {
		mySystemContextURI = uri;
	}
	def setLocalFileRootDir(dir: String) : Unit = {
		myLocalFileRootDir = dir;
	}
	def getSystemContextURI() : String = mySystemContextURI;
	
	/* "Temp" paths accessed from local file system, for easy end-user tweaking */
	def getLocalConfigFilePath (pathTail: String) : String = {
		myLocalFileRootDir + "/config/cogchar/" + pathTail;
	}
	def getRKBindTempFilePath (pathTail: String) : String = {
		getLocalConfigFilePath(RK_BIND_TEMP + "/" + pathTail);
	}
	def getRKMotionTempFilePath (pathTail: String) : String = {
		getRKBindTempFilePath(MOTION + "/" + pathTail);
	}
	
	/* Permanent paths */
	def getPermPath(pathTail: String) : String = {
		pathTail;
	}
	def getRKBindPermPath (pathTail: String) : String = {
		getPermPath(RK_BIND_PERM + "/" + pathTail);
	}
	def getRKMotionPermPath (pathTail: String) : String = {
		getRKBindPermPath(MOTION + "/" + pathTail);
	}

	/* Feature-specific X Character-specific path tails */
	def getBonyRobotConfigPathTail (chrShortName : String) : String = {
		BONY_ROBOT_CONFIG_PREFIX + chrShortName + BONY_ROBOT_CONFIG_SUFFIX;
	}	
	def getJointGroupConfigPathTail (chrShortName : String) : String = {
		RK_JOINT_GROUP_PREFIX + chrShortName + RK_JOINT_GROUP_SUFFIX;
	}	
	
}
