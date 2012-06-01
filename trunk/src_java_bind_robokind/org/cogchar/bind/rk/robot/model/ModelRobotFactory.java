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
package org.cogchar.bind.rk.robot.model;

import org.cogchar.api.skeleton.config.BoneJointConfig;
import org.cogchar.api.skeleton.config.BoneRobotConfig;
import org.robokind.api.common.config.VersionProperty;
import org.robokind.api.common.services.ServiceFactory;
import org.robokind.api.motion.Joint;
import org.robokind.api.motion.Robot;
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
		Robot.Id robotID = new Robot.Id(config.myRobotName);
		ModelRobot robot = new ModelRobot(robotID);
		theLogger.info("Robot.Id=" + robotID);
		for (BoneJointConfig bjc : config.myBJCs) {
			theLogger.info("Building Joint for config: " + bjc);
			Integer jointNum = bjc.myJointNum;
			if (jointNum != null) {
				Joint.Id jointId = new Joint.Id(bjc.myJointNum);
				ModelJoint mj = new ModelJoint(jointId, bjc);
				robot.registerBonyJoint(mj);
			} else {
				theLogger.warn("Found null jointNum at: " + bjc);
			}
		}
		theLogger.info("Built robot " + robot + " with ID=" + robot.getRobotId());
		return robot;
	}	
}
