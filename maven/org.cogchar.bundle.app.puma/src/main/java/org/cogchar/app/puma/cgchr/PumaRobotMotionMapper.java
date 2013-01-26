/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.app.puma.cgchr;

import java.util.List;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.Ident;
import org.cogchar.api.skeleton.config.BoneRobotConfig;
import org.cogchar.bind.rk.robot.client.RobotAnimClient;
import org.cogchar.bind.rk.robot.client.RobotAnimContext;
import org.cogchar.bind.rk.robot.svc.RobotServiceContext;
import org.cogchar.blob.emit.BehaviorConfigEmitter;
import org.cogchar.impl.perform.FancyTextChan;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class PumaRobotMotionMapper extends BasicDebugger {
	private	Ident				myCharIdent;
	private	RobotServiceContext	myRSC;
	private	RobotAnimContext	myRAC;
	
	public PumaRobotMotionMapper (Ident charID, BehaviorConfigEmitter behavCE, List<ClassLoader> clsForRKConf) throws Throwable {
		myCharIdent = charID;
		
		if (behavCE != null) {
			// This gives us an animation triggering context, connecting behavior system to animation system.
			myRAC = new RobotAnimContext(myCharIdent, behavCE);
			if (clsForRKConf != null) {
				// Setup classLoaders used to load animations
				myRAC.setResourceClassLoaders(clsForRKConf);
			}
		} else {
			getLogger().warn("Cannot init with behavCE == null");
		}
	}
	public void connectRobotSC (RobotServiceContext robotSvcContext) {
		myRSC = robotSvcContext;
		// Connect the triggering RobotAnimContext to the running model robot.
		myRAC.initConn(robotSvcContext);		
	}
	protected FancyTextChan getBestAnimOutChan() { 
		return myRAC.getTriggeringChannel();
	}
	protected void stopAndReset() {
		if (myRAC != null) {
			myRAC.stopAndReset();
		}else {
			getLogger().warn("stopAndReset() ignored because RobotAnimContext = null for {}", myCharIdent);
		}
	}
	protected void playBuiltinAnimNow(RobotAnimClient.BuiltinAnimKind baKind) {
		if (myRAC != null) {
			myRAC.playBuiltinAnimNow(baKind);
		} else {
			getLogger().warn("playDangerYogaTestAnim() ignored because RobotAnimContext = null for {}", myCharIdent);
		}
	}	
}
