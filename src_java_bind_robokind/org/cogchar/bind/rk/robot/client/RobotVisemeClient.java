/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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

import org.osgi.framework.BundleContext;
import org.robokind.api.common.lifecycle.utils.ManagedServiceFactory;
import org.robokind.api.common.osgi.lifecycle.OSGiComponentFactory;
import org.robokind.api.common.utils.RKConstants;
import org.robokind.api.motion.Robot;
import org.robokind.integration.motion_speech.VisemeMotionUtils;
import org.appdapter.core.name.Ident;


/**
 * @author Stu B. <www.texpedient.com>
 */

public class RobotVisemeClient {
	public static void startPumpingVisemeAnimation(BundleContext bunCtx, Ident speechOutChanID, Ident charBodyChanID) { 
		// As of 2012-11-23, the code below is based on sample code shown in comments of:
		// org.robokind.integration.motion_speech.Activator
		ManagedServiceFactory fact = new OSGiComponentFactory(bunCtx);
		String speechServiceId = RKConstants.DEFAULT_SPEECH_ID;
		String path = "path/to/VisemeConf.json";

		VisemeMotionUtils.startVisemeFrameSourceGroup(fact, new Robot.Id(RKConstants.VIRTUAL_R50_ID), speechServiceId, path);
	}
}
