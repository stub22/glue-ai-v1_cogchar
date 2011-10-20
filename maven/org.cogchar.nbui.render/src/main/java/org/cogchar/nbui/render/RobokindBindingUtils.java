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

package org.cogchar.nbui.render;

import org.cogchar.bind.robokind.joint.BonyRobot;
import org.osgi.framework.BundleContext;
import org.robokind.api.motion.Robot;
import org.cogchar.bind.robokind.joint.BonyRobotUtils;
import org.cogchar.bind.robokind.joint.BonyAnimUtils;
import org.cogchar.render.opengl.bony.app.BonyVirtualCharApp;
import org.cogchar.render.opengl.bony.model.DemoBonyWireframeRagdoll;
import org.cogchar.render.opengl.bony.app.BonyRagdollApp;
import org.cogchar.render.opengl.bony.sys.BonyContext;

/**
 * @author Stu B. <www.texpedient.com>
 * @author Matthew Stevenson <www.robokind.org>
 */
public class RobokindBindingUtils {
	private	static BonyRobot		myBonyRobot;
	private	static BundleContext	myBundleCtx;
	
	public static String	HARDCODED_ROBOT_ID = "hey I'm a Robot.Id!";
	
	public static void createAndRegisterRobot(BundleContext bundleCtx) throws Exception {
		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& carr start");
		//Create your Robot and register it
		Robot.Id hbID = new Robot.Id(HARDCODED_ROBOT_ID);
		myBonyRobot  = new BonyRobot(hbID);
		BonyRobotUtils.makeBonyJointForRobot(myBonyRobot, 22, "JTwentyTwo", 0.5, 0.2);
		BonyRobotUtils.makeBonyJointForRobot(myBonyRobot, 22, "JNinetyNine", 0.8, 0.9);
		BonyRobotUtils.registerRobokindRobot(myBonyRobot, bundleCtx);

		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& carr end");
	}
    
	public static class DanceDoer implements DemoBonyWireframeRagdoll.Doer {
		
		public void doIt() { 
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&  Doing dance ");
			try {
				BonyAnimUtils.createAndPlayAnim(myBundleCtx);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
    
	public static void connectToVirtualChar(BundleContext bundleCtx) throws Exception {
		myBundleCtx = bundleCtx;
		BonyContext bc = RenderUtils.getBonyContext(bundleCtx);
		BonyVirtualCharApp app = bc.getApp();
		BonyRagdollApp bra = (BonyRagdollApp) app;
		DemoBonyWireframeRagdoll dbwr = bra.getRagdoll();
		DanceDoer dd = new DanceDoer();
		dbwr.setDanceDoer(dd);
	}
		
}
