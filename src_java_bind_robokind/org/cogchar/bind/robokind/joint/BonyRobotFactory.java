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
package org.cogchar.bind.robokind.joint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.cogchar.avrogen.bind.robokind.BoneRotationConfig;
import org.cogchar.avrogen.bind.robokind.BonyJointConfig;
import org.cogchar.avrogen.bind.robokind.BonyRobotConfig;
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
public class BonyRobotFactory {
	static Logger theLogger = LoggerFactory.getLogger(BonyRobotFactory.class);
    public static BonyRobot buildFromFile(File configFile){
        try{
            BonyRobotConfig config = AvroUtils.readFromFile(
                    BonyRobotConfig.class, 
                    null, 
                    BonyRobotConfig.SCHEMA$, 
                    configFile, 
                    true);
            return buildRobot(config);
        }catch(IOException ex){
            theLogger.warn(
                    "Error loading file: " + configFile.getAbsolutePath(), ex);
        }
        return null;
    }
    
    public static BonyRobot buildRobot(BonyRobotConfig config){
        BonyRobot robot = 
                new BonyRobot(new Robot.Id(config.robotId.toString()));
        for(BonyJointConfig jc : config.jointConfigs){
            robot.registerBonyJoint(buildJoint(jc));
        }
        return robot;
    }
    
    private static BonyJoint buildJoint(BonyJointConfig config){
        Joint.Id jointId = new Joint.Id(config.jointId);
        double defVal = Utils.bound(config.normalizedDefaultPosition, 0.0, 1.0);
        NormalizedDouble def = new NormalizedDouble(defVal);
        List<BoneRotationConfig> rotConfigs = config.boneRotations;
        List<BoneRotationRange> ranges = new ArrayList<BoneRotationRange>(rotConfigs.size());
        for(BoneRotationConfig c : rotConfigs){
            ranges.add(new BoneRotationRange(
                    c.boneName.toString(), 
                    c.rotationAxis,
                    c.minPosition,
                    c.maxPosition));
        }
        return new BonyJoint(jointId, config.name.toString(), ranges, def);
    }
}
