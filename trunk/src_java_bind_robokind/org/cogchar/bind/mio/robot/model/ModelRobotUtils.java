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

package org.cogchar.bind.mio.robot.model;

import org.cogchar.api.skeleton.config.BoneProjectionPosition;
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

	private static void appendBoneRotation(Map<String, List<BoneProjectionPosition>> rotListMap, BoneProjectionPosition rot) {
		String bone = rot.getBoneName();
		List<BoneProjectionPosition> rotList = rotListMap.get(bone);
		if (rotList == null) {
			rotList = new ArrayList<BoneProjectionPosition>();
			rotListMap.put(bone, rotList);
		}
		rotList.add(rot);
	}

	public static Map<String, List<BoneProjectionPosition>> getGoalAnglesAsRotations(ModelRobot robot) {
		Map<String, List<BoneProjectionPosition>> rotListMap = new HashMap();
		List<ModelJoint> joints = new ArrayList(robot.getJointList());
		for (ModelJoint j : joints) {
			for (BoneProjectionPosition rot : j.getRotationListForCurrentGoal()) {
				appendBoneRotation(rotListMap, rot);
			}
		}
		return rotListMap;
	}

}
