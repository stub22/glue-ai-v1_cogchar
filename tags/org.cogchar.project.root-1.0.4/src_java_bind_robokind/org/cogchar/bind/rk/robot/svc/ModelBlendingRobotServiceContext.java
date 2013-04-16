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
package org.cogchar.bind.rk.robot.svc;

import org.cogchar.api.skeleton.config.BoneRobotConfig;
import org.cogchar.bind.rk.robot.model.ModelRobot;
import org.robokind.api.motion.Robot;
import org.osgi.framework.BundleContext;


/**
 * @author Stu B. <www.texpedient.com>
 */
public class ModelBlendingRobotServiceContext extends BlendingRobotServiceContext<ModelRobot> {
	

	public ModelBlendingRobotServiceContext(BundleContext bundleCtx) {
		super(bundleCtx);
	}

	public void makeModelRobotWithBlenderAndFrameSource(BoneRobotConfig config) throws Throwable {
		logInfo("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& START makeBonyRobot__, using config: " + config);
		//Create your Robot and register it
		ModelRobot br = ModelRobot.buildRobot(config);
        if(br == null){
            logWarning("Error building ModelRobot from config: " + config);
            return;
        }
		registerAndStart(br);
		logInfo("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& END makeBonyRobotWithBlenderAndFrameSource ");
	}	
	

	// Old test method, currently unused.
	public void registerDummyModelRobot() throws Throwable {
		logInfo("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& START registerDummyBlendingRobot");
		//Create your Robot and register it
		Robot.Id hbID = new Robot.Id("temp"); // HARDCODED_DUMMY_ROBOT_ID);
		ModelRobot br = new ModelRobot(hbID);
		//BonyRobotUtils.makeBonyJointForRobot(myBonyRobot, 22, "JTwentyTwo", 0.5, 0.2);
		//BonyRobotUtils.makeBonyJointForRobot(myBonyRobot, 22, "JNinetyNine", 0.8, 0.9);
		registerAndStart(br);
		logInfo("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& END registerDummyBlendingRobot");
	}	

}