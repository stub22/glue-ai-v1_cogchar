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
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.robokind.api.motion.Robot;
import org.robokind.api.motion.Robot.RobotPositionHashMap;
import org.robokind.api.motion.Robot.RobotPositionMap;
import org.robokind.api.motion.utils.RobotFrameSource;
import org.robokind.api.motion.utils.RobotMoverFrameSource;
import org.robokind.api.motion.utils.RobotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class BlendingRobotServiceContext<R extends Robot> extends RobotServiceContext<R> {
	static Logger theLogger = LoggerFactory.getLogger(BlendingRobotServiceContext.class);
	
	private	ServiceRegistration[]		myBlenderRegs;
	private	RobotMoverFrameSource		myFrameSource;
	
	public BlendingRobotServiceContext(BundleContext bundleCtx) {
		 super(bundleCtx);
	}

	protected void startDefaultBlender() {
		R robot = getRobot();
		Robot.Id robotID = robot.getRobotId();
        theLogger.info("Starting default blender for robotID: " + robotID);
        myBlenderRegs = RobotUtils.startDefaultBlender(
                myBundleCtx, robotID, RobotUtils.DEFAULT_BLENDER_INTERVAL);
	}
	protected void  registerFrameSource() { 
		R robot = getRobot();
		Robot.Id robotID = robot.getRobotId();		
		//create and register the MotionTargetFrameSource,
        myFrameSource = new RobotMoverFrameSource(robot);
		theLogger.info("Registering FrameSource for robotID: " + robotID);
		RobotUtils.registerFrameSource(myBundleCtx, robot.getRobotId(), myFrameSource);
	}
    protected void testPositionMove() { 
		RobotPositionMap positions = new RobotPositionHashMap();
		//... add positions
        
        //moves to the positions over 1.5 seconds
        myFrameSource.move(positions, 1500);
	}	

	public void registerAndStart(R robot) throws Throwable {
		super.registerAndStart(robot);
		startDefaultBlender();
		registerFrameSource();
		testPositionMove();
	}
}
