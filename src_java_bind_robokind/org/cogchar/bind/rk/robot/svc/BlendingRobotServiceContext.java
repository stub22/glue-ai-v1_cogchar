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
import org.jflux.api.core.config.Configuration;
import org.osgi.framework.BundleContext;
import org.robokind.api.motion.Robot;
import org.robokind.api.motion.Robot.RobotPositionMap;
import org.robokind.api.motion.lifecycle.DefaultBlenderServiceGroup;
import org.robokind.api.motion.utils.RobotMoverFrameSource;
import org.robokind.api.motion.utils.RobotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class BlendingRobotServiceContext<R extends Robot> extends RobotServiceContext<R> {
	static Logger theLogger = LoggerFactory.getLogger(BlendingRobotServiceContext.class);
	
	private	DefaultBlenderServiceGroup  myBlenderGroup;
	private	RobotMoverFrameSource		myFrameSource;
	
	public BlendingRobotServiceContext(BundleContext bundleCtx) {
		 super(bundleCtx);
	}

	protected void startDefaultBlender() {
		R robot = getRobot();
        if(robot == null){
            return;
        }
		Robot.Id robotID = robot.getRobotId();
        theLogger.info("Starting default blender for robotID: " + robotID);
        myBlenderGroup = new DefaultBlenderServiceGroup(
                myBundleCtx, robotID, 
                RobotUtils.DEFAULT_BLENDER_INTERVAL, null);
        myBlenderGroup.start();
	}
    
	protected void  registerFrameSource() { 
		R robot = getRobot();
        if(robot == null){
            return;
        }
		Robot.Id robotID = robot.getRobotId();		
		//create and register the MotionTargetFrameSource,
        myFrameSource = new RobotMoverFrameSource(robot);
		theLogger.info("Registering FrameSource for robotID: " + robotID);
		RobotUtils.registerFrameSource(myBundleCtx, robot.getRobotId(), myFrameSource);
	}
    protected void testPositionMove() { 
		R robot = getRobot();
        if(robot == null){
            return;
        }
		RobotPositionMap positions = robot.getDefaultPositions();        
        //moves to the positions over 1.5 seconds
        myFrameSource.move(positions, 1500);
	}	

    @Override
	public void registerAndStart(
            R robot, String connectionConfig) throws Throwable {
		Robot.Id robotID = robot.getRobotId();
		theLogger.info("super.registerAndStart(robotID=" + robotID + ")");
		super.registerAndStart(robot, connectionConfig);
		theLogger.info("startDefaultBlender(robotID=" + robotID + ")");
		startDefaultBlender();
		theLogger.info("registerFrameSource(robotID=" + robotID + ")");
		registerFrameSource();
		theLogger.info("testPositionMove(robotID=" + robotID + ")");
		testPositionMove();
		theLogger.info("registerAndStart COMPLETE for robotID=" + robotID);
	}
}
