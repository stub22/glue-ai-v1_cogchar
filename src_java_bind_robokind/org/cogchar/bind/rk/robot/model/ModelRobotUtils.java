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

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Connection;
import org.apache.qpid.client.AMQQueue;

import org.robokind.impl.messaging.ConnectionManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.robokind.api.common.services.ServiceConnectionDirectory;
import org.robokind.api.motion.Robot;
import org.robokind.api.motion.Robot.RobotPositionHashMap;
import org.robokind.api.motion.Robot.RobotPositionMap;

import org.robokind.api.motion.utils.RobotFrameSource;
import org.robokind.api.motion.utils.RobotUtils;
import org.robokind.api.motion.jointgroup.JointGroup;
import org.robokind.api.motion.jointgroup.RobotJointGroup;

import org.robokind.impl.motion.jointgroup.RobotJointGroupConfigXMLReader;
import org.robokind.impl.motion.messaging.JMSMotionFrameReceiver;
import org.robokind.impl.motion.messaging.MoveFrameListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class ModelRobotUtils {

	static Logger theLogger = LoggerFactory.getLogger(ModelRobotUtils.class);

	public static void registerRobotAndAttachBlender(Robot robot, BundleContext bundleCtx) throws Exception {
		robot.connect();
		ServiceRegistration reg = RobotUtils.registerRobot(bundleCtx, robot, null);
		if (reg == null) {
			throw new Exception("Error Registering Robot");
		}
		//Starts a blender
		ServiceRegistration[] blenderRegs = RobotUtils.startDefaultBlender(
				bundleCtx, robot.getRobotId(), RobotUtils.DEFAULT_BLENDER_INTERVAL);
		//create and register the MotionTargetFrameSource,
		RobotFrameSource robotFS = new RobotFrameSource(bundleCtx, robot.getRobotId());
		RobotUtils.registerFrameSource(bundleCtx, robot.getRobotId(), robotFS);
		RobotPositionMap positions = new RobotPositionHashMap();
		//... add positions

		//moves to the positions over 1.5 seconds
		robotFS.move(positions, 1500);
	}

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
