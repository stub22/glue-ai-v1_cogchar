/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

/**
 * @author Stu B. <www.texpedient.com>
 */
public class RobotAnimClient {
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
		System.out.println("cpSource=" + cpSource);
		Map<Integer, String> chanNames = cpSource.getChannelNames();
		System.out.println("channelNames=" + chanNames);
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
