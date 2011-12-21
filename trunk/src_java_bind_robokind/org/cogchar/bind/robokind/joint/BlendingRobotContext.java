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
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.robokind.api.motion.Robot;
import org.robokind.api.motion.Robot.RobotPositionHashMap;
import org.robokind.api.motion.Robot.RobotPositionMap;
import org.robokind.api.motion.utils.RobotFrameSource;
import org.robokind.api.motion.utils.RobotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class BlendingRobotContext<R extends Robot> {
	static Logger theLogger = LoggerFactory.getLogger(BlendingRobotContext.class);
	private BundleContext			myBundleCtx;
	private R						myRobot;
	private	ServiceRegistration		myRobotReg;
	private	ServiceRegistration[]	myBlenderRegs;
	private	RobotFrameSource		myFrameSource;
	
	public BlendingRobotContext(BundleContext bundleCtx) {
		myBundleCtx = bundleCtx;
	}
	protected void registerRobot(R robot) throws Exception {
		myRobot = robot;
		myRobot.connect();
		myRobotReg = RobotUtils.registerRobot(myBundleCtx, robot, null);
		if(myRobotReg == null){
			 throw new Exception("Error Registering Robot: " + robot);
		}
	}
	protected void startDefaultBlender() {
        //Starts a blender
        myBlenderRegs = RobotUtils.startDefaultBlender(
                myBundleCtx, myRobot.getRobotId(), RobotUtils.DEFAULT_BLENDER_INTERVAL);
	}
	protected void  registerFrameSource() { 
		//create and register the MotionTargetFrameSource,
        myFrameSource = new RobotFrameSource(myBundleCtx, myRobot.getRobotId());
		RobotUtils.registerFrameSource(myBundleCtx, myRobot.getRobotId(), myFrameSource);
	}
    protected void testPositionMove() { 
		RobotPositionMap positions = new RobotPositionHashMap();
		//... add positions
        
        //moves to the positions over 1.5 seconds
        myFrameSource.move(positions, 1500);
	}	

	public R getRobot() { 
		return myRobot;
	}
	public void registerAndStart(R robot) throws Exception {
		registerRobot(robot);
		startDefaultBlender();
		registerFrameSource();
		testPositionMove();
	}
}
