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
package org.cogchar.bind.robokind.joint;

import java.io.File;
import org.osgi.framework.BundleContext;
import org.robokind.api.motion.Robot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BlendingBonyRobotContext extends BlendingRobotContext<BonyRobot> {
	static Logger theLogger = LoggerFactory.getLogger(BlendingBonyRobotContext.class);
	public static String	HARDCODED_DUMMY_ROBOT_ID = "myDevice1";
	public BlendingBonyRobotContext(BundleContext bundleCtx) {
		super(bundleCtx);
	}
	
	public void makeBonyRobotWithBlenderAndFrameSource(File jointBindingConfigFile) throws Exception {
		String bindingFilePath = jointBindingConfigFile.getAbsolutePath();
		theLogger.info("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& START makeBonyRobot__, using file: " + bindingFilePath);
		//Create your Robot and register it
		BonyRobot br = BonyRobotFactory.buildFromFile(jointBindingConfigFile);
        if(br == null){
            theLogger.warn("Error building Robot from file: " + bindingFilePath);
            return;
        }
		registerAndStart(br);
		theLogger.info("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& END makeBonyRobotWithBlenderAndFrameSource ");
	}	
    public void registerDummyBlendingRobot() throws Exception {
		theLogger.info("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& START registerDummyBlendingRobot");
		//Create your Robot and register it
		Robot.Id hbID = new Robot.Id(HARDCODED_DUMMY_ROBOT_ID);
		BonyRobot br = new BonyRobot(hbID);
		//BonyRobotUtils.makeBonyJointForRobot(myBonyRobot, 22, "JTwentyTwo", 0.5, 0.2);
		//BonyRobotUtils.makeBonyJointForRobot(myBonyRobot, 22, "JNinetyNine", 0.8, 0.9);
		//registerAndStart(br)
		theLogger.info("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& END registerDummyBlendingRobot");
	}		
}
