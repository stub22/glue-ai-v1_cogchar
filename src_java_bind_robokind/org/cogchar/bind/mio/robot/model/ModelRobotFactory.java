/*
 * Copyright 2012 The Cogchar Project (www.cogchar.org).
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
package org.cogchar.bind.mio.robot.model;

import org.cogchar.api.skeleton.config.BoneJointConfig;
import org.cogchar.api.skeleton.config.BoneRobotConfig;
import org.jflux.api.common.rk.config.VersionProperty;
import org.jflux.api.common.rk.services.ServiceFactory;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.Robot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matthew Stevenson
 */
public class ModelRobotFactory implements ServiceFactory<Robot, BoneRobotConfig> {
	static Logger theLogger = LoggerFactory.getLogger(ModelRobotFactory.class);

    @Override
    public VersionProperty getServiceVersion() {
        return ModelRobot.VERSION;
    }

    @Override
    public Robot build(BoneRobotConfig config) throws Exception {
        return buildRobot(config);
    }

    @Override
    public Class<Robot> getServiceClass() {
        return Robot.class;
    }

    @Override
    public Class<BoneRobotConfig> getConfigurationClass() {
        return BoneRobotConfig.class;
    }
    
    public static ModelRobot buildRobot(BoneRobotConfig config) {
		String robotName = config.myRobotName;
		if (robotName == null) {
			theLogger.warn("robotName is null, aborting robot build for config {}", config);
			return null;
		}
		Robot.Id robotID = new Robot.Id(robotName);
		ModelRobot robot = new ModelRobot(robotID);
		theLogger.info("Robot.Id=" + robotID);
		for (BoneJointConfig bjc : config.myBJCs) {
			theLogger.debug("Building Joint for config {} ", bjc);
			Integer jointNum = bjc.myJointNum;
			if (jointNum != null) {
				Joint.Id jointId = new Joint.Id(bjc.myJointNum);
				ModelJoint mj = new ModelJoint(jointId, bjc);
				robot.registerBonyJoint(mj);
			} else {
				theLogger.warn("Found null jointNum for config {} " + bjc);
			}
		}
		theLogger.info("Built robot {} with ID={}", robot, robot.getRobotId());
		return robot;
	}	
}
