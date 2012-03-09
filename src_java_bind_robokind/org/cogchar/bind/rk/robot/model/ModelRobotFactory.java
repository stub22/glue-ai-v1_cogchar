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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.cogchar.avrogen.bind.robokind.BoneRotationConfig;
import org.cogchar.avrogen.bind.robokind.BoneRotationRangeConfig;
import org.cogchar.avrogen.bind.robokind.BonyJointConfig;
import org.cogchar.avrogen.bind.robokind.BonyRobotConfig;
import org.cogchar.avrogen.bind.robokind.RotationAxis;
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
 */
public class ModelRobotFactory {

	static Logger theLogger = LoggerFactory.getLogger(ModelRobotFactory.class);

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

	public static ModelRobot buildRobot(BonyRobotConfig config) {
		ModelRobot robot =
				new ModelRobot(new Robot.Id(config.robotId.toString()));
		for (BonyJointConfig jc : config.jointConfigs) {
			robot.registerBonyJoint(buildJoint(jc));
		}
		List<BoneRotationConfig> rotConfigs = config.initialBonePositions;
		List<ModelBoneRotation> rotations =
				new ArrayList<ModelBoneRotation>(rotConfigs.size());
		for (BoneRotationConfig conf : rotConfigs) {
			rotations.add(new ModelBoneRotation(
					conf.boneName.toString(),
					conf.rotationAxis,
					conf.rotationRadians));
		}
		robot.setInitialBoneRotations(rotations);
		return robot;
	}

	private static ModelJoint buildJoint(BonyJointConfig config) {
		Joint.Id jointId = new Joint.Id(config.jointId);
		double defVal = Utils.bound(config.normalizedDefaultPosition, 0.0, 1.0);
		NormalizedDouble def = new NormalizedDouble(defVal);
		List<BoneRotationRangeConfig> rotConfigs = config.boneRotations;
		List<ModelBoneRotRange> ranges = new ArrayList<ModelBoneRotRange>(rotConfigs.size());
		for (BoneRotationRangeConfig c : rotConfigs) {
			ranges.add(new ModelBoneRotRange(
					c.boneName.toString(),
					c.rotationAxis,
					c.minPosition,
					c.maxPosition));
		}
		return new ModelJoint(jointId, config.name.toString(), ranges, def);
	}
}
