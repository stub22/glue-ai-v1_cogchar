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

package org.cogchar.bind.rk.robot.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class ModelRobotUtils {

	static Logger theLogger = LoggerFactory.getLogger(ModelRobotUtils.class);

	private static void appendBoneRotation(Map<String, List<ModelBoneRotation>> rotListMap, ModelBoneRotation rot) {
		String bone = rot.getBoneName();
		List<ModelBoneRotation> rotList = rotListMap.get(bone);
		if (rotList == null) {
			rotList = new ArrayList<ModelBoneRotation>();
			rotListMap.put(bone, rotList);
		}
		rotList.add(rot);
	}

	public static Map<String, List<ModelBoneRotation>> getGoalAnglesAsRotations(ModelRobot robot) {
		Map<String, List<ModelBoneRotation>> rotListMap = new HashMap();
		List<ModelJoint> joints = new ArrayList(robot.getJointList());
		for (ModelJoint j : joints) {
			for (ModelBoneRotation rot : j.getRotationListForCurrentGoal()) {
				appendBoneRotation(rotListMap, rot);
			}
		}
		return rotListMap;
	}

	public static Map<String, List<ModelBoneRotation>> getInitialRotationMap(ModelRobot robot) {
		Map<String, List<ModelBoneRotation>> rotListMap = new HashMap();
		List<ModelBoneRotation> initRots = robot.getInitialBoneRotations();
		if (initRots == null) {
			return rotListMap;
		}
		for (ModelBoneRotation rot : initRots) {
			appendBoneRotation(rotListMap, rot);
		}
		return rotListMap;
	}

}
