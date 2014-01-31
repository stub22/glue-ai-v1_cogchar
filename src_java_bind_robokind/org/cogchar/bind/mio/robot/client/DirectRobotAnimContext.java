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

package org.cogchar.bind.rk.robot.client;

import org.cogchar.bind.rk.robot.client.RobotAnimClient;
import org.cogchar.bind.rk.robot.client.RobotAnimContext;
import org.cogchar.bind.rk.robot.svc.RobotServiceContext;
import org.osgi.framework.BundleContext;
import org.mechio.api.motion.Robot;
import org.mechio.api.motion.utils.RobotUtils;

import org.cogchar.bind.rk.robot.client.RobotAnimClient.BuiltinAnimKind;
import org.cogchar.bind.rk.robot.model.ModelRobot;
import java.net.URL;
import org.appdapter.core.name.Ident;
import org.cogchar.blob.emit.BehaviorConfigEmitter;
import org.cogchar.platform.util.ClassLoaderUtils;
import org.mechio.api.animation.player.AnimationPlayer;

/**
 * @author Stu B. <www.texpedient.com>
 */


public class DirectRobotAnimContext extends RobotAnimContext {
	
	private Robot				myTargetRobot;

	public DirectRobotAnimContext(Ident animOutChanID, BehaviorConfigEmitter behavCE, RobotServiceContext robotSvcContext) {
		super(animOutChanID, behavCE);
		if (!initConnForTargetRobot(robotSvcContext)) {
			throw new RuntimeException("Cannot connect to local target robot for animation on chan=" + animOutChanID);
		}
	}
	@Override protected ModelRobot getModelRobot() {
		return (ModelRobot) myTargetRobot;
	}
	
	private boolean initConnForTargetRobot(RobotServiceContext robotSvcContext) {
		try {
			BundleContext osgiBundleCtx = robotSvcContext.getBundleContext();
			myTargetRobot = robotSvcContext.getRobot();
			if (myTargetRobot == null) {
				getLogger().warn("initConn() aborting due to missing target robot, for charIdent: " + myAnimOutChanID);
				return false;
			}
			Robot.Id robotId = myTargetRobot.getRobotId();
			getLogger().info("***************************** Using robotId: " + robotId);
			String osgiFilterStringForAnimPlayer = RobotUtils.getRobotFilter(robotId);
			getLogger().info("***************************** Using osgiFilterStringForAnimPlayer: " + osgiFilterStringForAnimPlayer);
			myAnimClient = new RobotAnimClient(osgiBundleCtx, osgiFilterStringForAnimPlayer);
			return true;
		} catch (Throwable t) {
			getLogger().error("Cannot init RobotAnimClient for char[" + myAnimOutChanID + "]", t);
			return false;
		}
	}


	
}
