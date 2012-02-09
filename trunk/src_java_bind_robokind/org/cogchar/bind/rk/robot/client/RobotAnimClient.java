/*
 *  Copyright 2011-2 by The Cogchar Project (www.cogchar.org).
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

import java.util.Map;
import java.util.Map.Entry;
import org.osgi.framework.BundleContext;
import org.robokind.api.animation.Animation;
import org.robokind.api.animation.Channel;
import org.robokind.api.animation.MotionPath;
import org.robokind.api.animation.utils.AnimationUtils;
import org.robokind.api.animation.utils.ChannelsParameterSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class RobotAnimClient {
	static Logger theLogger = LoggerFactory.getLogger(RobotAnimClient.class);
	BundleContext	myBundleCtx;
	public RobotAnimClient(BundleContext bundleCtx) throws Exception {
		myBundleCtx = bundleCtx;
	}
	public void createAndPlayTestAnim() throws Exception {
		/* This is how the animation editor gets the available channels.  This
		 * uses a Robot and maps the JointIds to the Integers which are used as
		 * channel ids.  Right now, it just uses the Integer from the JointId.
		 * So an animation will only work for a single Robot.
		 * I need to make ids and position in the animations explicit, but it
		 * isn't as critical as fixing the motion was.
		 */

		ChannelsParameterSource cpSource = AnimationUtils.getChannelsParameterSource();
		theLogger.trace("channelParamSource=" + cpSource);
		Map<Integer, String> chanNames = cpSource.getChannelNames();
		theLogger.info("Test animation channelNames=" + chanNames);
		Animation anim = new Animation();
		//Create your channels and add points
		for (Entry<Integer, String> e : chanNames.entrySet()) {
			Channel chan = new Channel(e.getKey(), e.getValue());
			//default path interpolation is a CSpline
			MotionPath path = new MotionPath();
			//time in millisec, position [0,1]
			path.addPoint(0, 0.5);
			path.addPoint(1000, 1.0);
			path.addPoint(3000, 0.0);
			path.addPoint(4000, 0.5);
			chan.addPath(path);
			anim.addChannel(chan);
		}
        
        //null should be RobotUtils.getRobotFilter(robotId)
        AnimationUtils.playAnimation(myBundleCtx, null, anim);
	}
}
