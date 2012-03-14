/*
 * Copyright 2011 The Cogchar Project (www.cogchar.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cogchar.bind.rk.robot.model;

import org.cogchar.bind.rk.robot.config.BoneProjectionRange;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import org.cogchar.avrogen.bind.robokind.BonyRobotConfig;
import org.cogchar.bind.rk.robot.config.BoneJointConfig;
import org.cogchar.bind.rk.robot.config.BoneRobotConfig;
import org.robokind.api.common.position.NormalizedDouble;
import org.robokind.api.common.utils.Utils;
import org.robokind.api.motion.Joint;
import org.robokind.api.motion.Robot;
import org.robokind.bind.apache_avro.AvroUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matthew Stevenson <www.robokind.org>
 * @author Stu B. <www.texpedient.com>
 */
public class ModelRobotFactory {

	static Logger theLogger = LoggerFactory.getLogger(ModelRobotFactory.class);
/*
	public static ModelRobot buildFromFile(File configFile) {
		String streamTitle = configFile.getAbsolutePath();
		try {
			InputStream inStream = new FileInputStream(configFile);
			return buildFromStream(inStream, streamTitle);
		} catch (IOException ex) {
			theLogger.warn(
					"Error reading ModelRobot config from  file: " + streamTitle, ex);
		}
		return null;
	}

	public static ModelRobot buildFromStream(InputStream configInputStream, String streamTitle) {
		ModelRobot builtMR = null;
		try { 
			BonyRobotConfig config = AvroUtils.readFromStream(
					BonyRobotConfig.class,
					null,
					BonyRobotConfig.SCHEMA$,
					configInputStream,
					true);
			builtMR = buildRobot(config);
		} catch (Throwable t) {
			theLogger.warn(
					"Error reading ModelRobot config from  stream: " + streamTitle, t);
		}
		return builtMR;
	}
*/
	public static ModelRobot buildRobot(BoneRobotConfig config) {
		Robot.Id robotID = new Robot.Id(config.myRobotName);
		ModelRobot robot = new ModelRobot(robotID);
		theLogger.info("Robot.Id=" + robotID);
		for (BoneJointConfig bjc : config.myBJCs) {
			Joint.Id jointId = new Joint.Id(bjc.myJointNum);
			ModelJoint mj = new ModelJoint(jointId, bjc);
			robot.registerBonyJoint(mj);
		}
		theLogger.info("Built robot " + robot + " with ID=" + robot.getRobotId());
		return robot;
	}
		/*		
		 * The joints have a default position, so this info is superfluous.
		List<BoneRotationConfig> rotConfigs = config.initialBonePositions;

		List<BoneProjectionPosition> rotations =
				new ArrayList<BoneProjectionPosition>(rotConfigs.size());
		for (BoneRotationConfig conf : rotConfigs) {
			rotations.add(new BoneProjectionPosition(
					conf.boneName.toString(),
					conf.rotationAxis,
					conf.rotationRadians));
		}
		robot.setInitialBoneRotations(rotations);
		 * 
		 */
		
	
/*
	private static ModelJoint buildJoint(BoneJointConfig config) {
		
		double defVal = Utils.bound(config.myNormalDefaultPos, 0.0, 1.0);
		NormalizedDouble defPosNorm = new NormalizedDouble(defVal);
		List<BoneProjectionRange> bprConfigs = config.myProjectionRanges;
		return new ModelJoint(jointId, config.myBoneName, bprConfigs, defPosNorm);
	}
	 * 
	 */
}
