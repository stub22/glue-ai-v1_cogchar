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
import java.io.InputStream;
import java.util.*;
import org.appdapter.core.log.BasicDebugger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.utils.RobotManager;
import org.mechio.api.motion.utils.RobotUtils;
import org.mechio.impl.motion.lifecycle.RemoteRobotHostServiceGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Stu B. <www.texpedient.com>
 */
public class RobotServiceContext<R extends Robot> extends BasicDebugger {
	static Logger theLogger = LoggerFactory.getLogger(RobotServiceContext.class);
	
	// A static map of all the Robots connected via registerRobot
	// We'll need this to disconnect 'em all on "mode" change
	// The map points to the bundle context used to register the robot so we can unregister it with that context, since
	// myBundleCtx is not here static. A bit of a dirty work around; eventually much of the RK binding might need to be 
	// rewritten to allow character disconnects without this sort of monkey business.
	private static Map<Robot, BundleContext> startedRobotsContextMap = new HashMap<Robot, BundleContext>();
	// A static list of all the robot services registered via registerRobot
	// We'll need this to unregister 'em all on "mode" change
	private static List<ServiceRegistration> registeredRobots = new ArrayList<ServiceRegistration>();
	// A static list of all the RemoteRobotHostServiceGroups registered via launchRemoteHost
	// We'll need this to stop 'em all on "mode" change
	private static List<RemoteRobotHostServiceGroup> launchedHosts = new ArrayList<RemoteRobotHostServiceGroup>();

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
            theLogger.warn("getRobot() is returning null!");
        }
		return myRobot;
	}
	protected void registerRobot(R robot) throws Exception {
		myRobot = robot;
		myRobot.connect();
		startedRobotsContextMap.put(myRobot, myBundleCtx);
		theLogger.info("Calling RobotUtils.registerRobot()");
		myRobotReg = RobotUtils.registerRobot(myBundleCtx, robot, null);
		theLogger.info("RobotUtils.registerRobot() returned: " + myRobotReg);
		registeredRobots.add(myRobotReg);
		if(myRobotReg == null){
			 throw new Exception("Error Registering Robot: " + robot);
		}
    }
    protected void launchRemoteHost(String connectionConfigId) {
        RemoteRobotHostServiceGroup newHost = new RemoteRobotHostServiceGroup(
                myBundleCtx, myRobot.getRobotId(), 
                "host", "client", connectionConfigId, null);
		launchedHosts.add(newHost);
		newHost.start();
    }
	public void registerAndStart(R robot, String connectionConfigId) throws Throwable {
		registerRobot(robot);
        launchRemoteHost(connectionConfigId);
	}
	public void startJointGroup(File jointGroupConfigXML_file) { 
		RobotServiceFuncs.startJointGroup(
                myBundleCtx, myRobot, jointGroupConfigXML_file, File.class);
	}
	public void startJointGroup(InputStream jointGroupConfigXML_stream) { 
		RobotServiceFuncs.startJointGroup(
                myBundleCtx, myRobot, jointGroupConfigXML_stream, InputStream.class);
	}
	
	public static void clearRobots() {
		for (RemoteRobotHostServiceGroup oneHost : launchedHosts) {
			oneHost.stop();
		}
		launchedHosts.clear();
		for (ServiceRegistration oneRobotReg : registeredRobots) {
			oneRobotReg.unregister();
		}
		registeredRobots.clear();
		Set<Robot> startedRobots = startedRobotsContextMap.keySet();
		for (Robot oneRobot : startedRobots) {
			RobotManager manager = RobotUtils.getRobotManager(startedRobotsContextMap.get(oneRobot));
			manager.removeRobot(oneRobot.getRobotId());
			oneRobot.disconnect();
		}
		startedRobots.clear();
	}
}
