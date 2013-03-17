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

import java.util.ArrayList;
import java.util.List;

import org.appdapter.core.name.Ident;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.api.perform.Media;
import org.cogchar.api.perform.Performance;
import org.cogchar.bind.rk.robot.svc.RobotServiceContext;

import org.cogchar.blob.emit.BehaviorConfigEmitter;

import org.cogchar.impl.perform.ChannelNames;
import org.cogchar.impl.perform.FancyTextChan;
import org.cogchar.impl.perform.FancyTextPerf;
import org.cogchar.impl.perform.FancyTime;

import org.osgi.framework.BundleContext;
import org.robokind.api.animation.Animation;
import org.robokind.api.animation.player.AnimationJob;
import org.robokind.api.motion.Robot;
import org.robokind.api.motion.utils.RobotUtils;

import org.cogchar.bind.rk.robot.client.RobotAnimClient.BuiltinAnimKind;
import org.cogchar.bind.rk.robot.model.ModelRobot;
import java.net.URL;
import org.cogchar.platform.util.ClassLoaderUtils;
import org.robokind.api.animation.player.AnimationPlayer;
/**
 * @author Stu B. <www.texpedient.com>
 */
	public class RobotAnimContext extends BasicDebugger {

	enum AnimChannel {

		RK_XML_BEST,
		RK_XML_PERM,
		RK_XML_TEMP
	}
	private Ident myCharIdent;
	private Robot myTargetRobot;
	private RobotAnimClient myAnimClient;
	private List<AnimationJob> myJobsInStartOrder = new ArrayList<AnimationJob>();
	private TriggeringChannel myTriggeringChannel;
	private BehaviorConfigEmitter myBehaviorCE;
	
	private	List<ClassLoader>	myResourceCLs = new ArrayList<ClassLoader>();

	/**
	 * 
	 * @param charIdent - so far, used only for log messages
	 * @param behavCE  - only used to resolve local files, in case animResURL does not resolve within classpath. 
	 */
	public RobotAnimContext(Ident charIdent, BehaviorConfigEmitter behavCE) {
		myCharIdent = charIdent;
		myBehaviorCE = behavCE;
	}
	public void setResourceClassLoaders(List<ClassLoader>  resCLs) {
		myResourceCLs = resCLs;
	}
	public boolean initConnForTargetRobot(RobotServiceContext robotSvcContext) {
		try {
			BundleContext osgiBundleCtx = robotSvcContext.getBundleContext();
			myTargetRobot = robotSvcContext.getRobot();
			if (myTargetRobot == null) {
				getLogger().warn("initConn() aborting due to missing target robot, for charIdent: " + myCharIdent);
				return false;
			}
			Robot.Id robotId = myTargetRobot.getRobotId();
			getLogger().info("***************************** Using robotId: " + robotId);
			String osgiFilterStringForAnimPlayer = RobotUtils.getRobotFilter(robotId);
			getLogger().info("***************************** Using osgiFilterStringForAnimPlayer: " + osgiFilterStringForAnimPlayer);
			myAnimClient = new RobotAnimClient(osgiBundleCtx, osgiFilterStringForAnimPlayer);
			return true;
		} catch (Throwable t) {
			getLogger().error("Cannot init RobotAnimClient for char[" + myCharIdent + "]", t);
			return false;
		}
	}
	public boolean initConnForAnimPlayer(AnimationPlayer player) {
		myAnimClient = new RobotAnimClient(player);
		return true;
	}

	public void stopAndReset() {
		endAndClearKnownAnimationJobs();
	}

	public void endAndClearKnownAnimationJobs() {
		// We could use this instead:
		//		getAllCurrentAnimationsForPlayer
		// which would stop/clear all animations, regardless of whether launched by this AnimClient
		// TODO:  Let's print the one's we didn't stop and clear!

		List<AnimationJob> notCleared = new ArrayList<AnimationJob>();
		int clearedCount = 0, unclearedCount = 0;
		for (AnimationJob aj : myJobsInStartOrder) {
			if (myAnimClient.endAndClearAnimationJob(aj)) {
				clearedCount++;
				getLogger().info("endAndClear success #" + clearedCount + " on AnimationJob " + aj);
			} else {
				notCleared.add(aj);
				unclearedCount++;
				getLogger().warn("****** endAndClear FAILURE #" + unclearedCount + " on AnimationJob " + aj);
			}
		}
		myJobsInStartOrder = notCleared;
		getLogger().info("endAndClear complete, succeededCount=" + clearedCount + ", failedCount=" + unclearedCount);
	}

	public synchronized void startFullAnimationNow(Animation anim) {
		AnimationJob aj = myAnimClient.playFullAnimationNow(anim);
		if (aj != null) {
			myJobsInStartOrder.add(aj);
			getLogger().info("Started AnimationJob: [{}]", aj);
		} else {
			getLogger().warn("********************* Could not start animation[" + anim + "]");
		}
	}

	public void playBuiltinAnimNow(BuiltinAnimKind baKind) {
		if (myAnimClient != null) {
			Animation builtinAnim = null;
			// TODO: check cache
			try {
				builtinAnim = myAnimClient.makeBuiltinAnim(baKind, (ModelRobot) myTargetRobot);

			} catch (Throwable t) {
				getLogger().error("Problem creating builtin anim: {} ", baKind, t);
				return;
			}
			if (builtinAnim != null) {
				startFullAnimationNow(builtinAnim);
			}
			getLogger().info("Started builtin anim {} on robot {} ", builtinAnim, myCharIdent);
		} else {
			getLogger().warn("Cannot play builtin anim {}, because myAnimClient == null, on robot: {} ", baKind, myCharIdent);
		}
	}

	public TriggeringChannel getTriggeringChannel() {
		if (myTriggeringChannel == null) {
			Ident id = ChannelNames.getOutChanIdent_AnimBest();
			getLogger().info("Creating triggering channel with ident=" + id);
			myTriggeringChannel = new TriggeringChannel(id);
		}
		return myTriggeringChannel;
	}

	public class TriggeringChannel extends FancyTextChan {

		private boolean myUseTempAnimsFlag = false;

		public TriggeringChannel(Ident id) {
			super(id);
		}

		public void setUseTempAnims(boolean flag) {
			myUseTempAnimsFlag = flag;
		}

		@Override protected void attemptMediaStartNow(Media.Text m) throws Throwable {
			String animPathStr = m.getFullText();
			Animation anim = null;
			URL animResURL = ClassLoaderUtils.findResourceURL(animPathStr, myResourceCLs);
			if (animResURL != null) {
				getLogger().warn("Found Animation Resource URL: " + animResURL);
				String aruString = animResURL.toExternalForm();
				anim = myAnimClient.readAnimationFromURL(aruString);
			} else {
				String fullPath = null;
				// Temporarily we always use the temp path, because it's just a file and we don't have to turn
				// the resource lookup into a URL.
				//if (myUseTempAnimsFlag) {
				fullPath = myBehaviorCE.getRKAnimationTempFilePath(animPathStr);
				//} else {
				//	fullPath = myBehaviorCE.getRKAnimationPermPath(animPathStr);
				//}
				getLogger().info("Attempting to start animation at relative path[" + fullPath + "]");
				anim = myAnimClient.readAnimationFromFile(fullPath);
			}
			if (anim != null) {
				startFullAnimationNow(anim);
			}
		}

		@Override public Performance<Media.Text, FancyTime> makePerformanceForMedia(Media.Text m) {
			return new FancyTextPerf(m, this);
		}
	}
}
