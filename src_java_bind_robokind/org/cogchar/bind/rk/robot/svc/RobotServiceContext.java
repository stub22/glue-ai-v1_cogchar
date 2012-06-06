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

import java.io.File;
import org.appdapter.core.log.BasicDebugger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.robokind.api.motion.Robot;
import org.robokind.api.motion.utils.RobotUtils;
import org.robokind.impl.motion.lifecycle.RemoteRobotHostServiceGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Stu B. <www.texpedient.com>
 */
public class RobotServiceContext<R extends Robot> extends BasicDebugger {
	static Logger theLogger = LoggerFactory.getLogger(RobotServiceContext.class);

	protected BundleContext			myBundleCtx;
	private R						myRobot;
	private	ServiceRegistration		myRobotReg;

	public RobotServiceContext(BundleContext bundleCtx) {
		myBundleCtx = bundleCtx;
	}
	public BundleContext getBundleContext() { 
		return myBundleCtx;
	}
	public R getRobot() { 
        if(myRobot == null){
            theLogger.warn("No Robot Set");
        }
		return myRobot;
	}
	protected void registerRobot(R robot) throws Exception {
		myRobot = robot;
		myRobot.connect();
		theLogger.info("Calling RobotUtils.registerRobot()");
		myRobotReg = RobotUtils.registerRobot(myBundleCtx, robot, null);
		theLogger.info("RobotUtils.registerRobot() returned: " + myRobotReg);
		if(myRobotReg == null){
			 throw new Exception("Error Registering Robot: " + robot);
		}
    }
    protected void launchRemoteHost(String connectionConfigId){
        new RemoteRobotHostServiceGroup(
                myBundleCtx, myRobot.getRobotId(), 
                "host", "client", connectionConfigId, null).start();
    }
	public void registerAndStart(
            R robot, String connectionConfigId) throws Throwable {
		registerRobot(robot);
        launchRemoteHost(connectionConfigId);
	}
	public void startJointGroup(File jointGroupConfigXML_file) { 
		RobotServiceFuncs.startJointGroup(
                myBundleCtx, myRobot, jointGroupConfigXML_file);
	}
}
