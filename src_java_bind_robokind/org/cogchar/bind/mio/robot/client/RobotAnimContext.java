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
package org.cogchar.bind.mio.robot.client;

import com.hp.hpl.jena.rdf.model.Model;
import java.util.ArrayList;
import java.util.List;

import org.appdapter.core.name.Ident;
import org.appdapter.core.log.BasicDebugger;

import org.cogchar.blob.emit.BehaviorConfigEmitter;

import org.mechio.api.animation.Animation;
import org.mechio.api.animation.player.AnimationJob;

import org.cogchar.bind.mio.robot.client.RobotAnimClient.BuiltinAnimKind;
import org.cogchar.bind.mio.robot.model.ModelRobot;
import java.net.URL;
import org.cogchar.impl.channel.AnimFileSpecReader;
import org.cogchar.platform.util.ClassLoaderUtils;
import org.mechio.api.animation.player.AnimationPlayer;

import org.cogchar.impl.perform.MediaResolverFactory;
import org.cogchar.impl.perform.UrlSearcher;
import org.cogchar.impl.perform.MediaPathFinder;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class RobotAnimContext extends BasicDebugger {

	enum AnimChannel {

		RK_XML_BEST,
		RK_XML_PERM,
		RK_XML_TEMP
	}
	protected Ident				myAnimOutChanID;
	private AnimOutTrigChan		myTriggeringChannel;
	
	protected RobotAnimClient		myAnimClient;
	private List<AnimationJob>	myJobsInStartOrder = new ArrayList<AnimationJob>();

	protected BehaviorConfigEmitter myBehaviorCE;
	
	protected	List<ClassLoader>	myResourceCLs = new ArrayList<ClassLoader>();

	/**
	 * 
	 * @param animOutChanID - so far, used only for log messages
	 * @param behavCE  - only used to resolve local files, in case animResURL does not resolve within classpath. 
	 */
	public RobotAnimContext(Ident animOutChanID, BehaviorConfigEmitter behavCE) {
		myAnimOutChanID = animOutChanID;
		myBehaviorCE = behavCE;
	}
	public void setResourceClassLoaders(List<ClassLoader>  resCLs) {
		myResourceCLs = resCLs;
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

	public synchronized AnimationJob startFullAnimationNow(Animation anim) {
		AnimationJob aj = myAnimClient.playFullAnimationNow(anim);
		if (aj != null) {
			myJobsInStartOrder.add(aj);
			getLogger().info("Started AnimationJob: [{}]", aj);
		} else {
			getLogger().warn("********************* Could not start animation[" + anim + "]");
		}
		return aj;
	}
	protected ModelRobot getModelRobot() { 
		return null;
	}
	public void playBuiltinAnimNow(BuiltinAnimKind baKind) {
		if (myAnimClient == null) {
			getLogger().warn("Cannot play builtin anim-kind {}, because myAnimClient == null, for chan: {} ", baKind, myAnimOutChanID);
			return;
		}
		ModelRobot modelRobot = getModelRobot();
		if (modelRobot == null) {
			getLogger().warn("Cannot play builtin anim-kind {}, because modelRobot== null, for chan: {} ", baKind, myAnimOutChanID);
			return;			
		}
		Animation builtinAnim = null;
		// TODO: check cache
		try {
			builtinAnim = myAnimClient.makeBuiltinAnim(baKind, modelRobot);
		} catch (Throwable t) {
			getLogger().error("Problem creating builtin anim: {} ", baKind, t);
			return;
		}
		if (builtinAnim != null) {
			startFullAnimationNow(builtinAnim);
		}
		getLogger().info("Started builtin anim {} on robot {} ", builtinAnim, myAnimOutChanID);			
	}

	public AnimOutTrigChan getTriggeringChannel() {
		if (myTriggeringChannel == null) {
			Ident id = myAnimOutChanID; 
			getLogger().warn("Manually creating media cache and triggering channel for ID: " + id);
			AnimMediaHandle.Cache mediaCache = makeMediaHandleCache();
			myTriggeringChannel = new AnimOutTrigChan(id, this, mediaCache);
		}
		return myTriggeringChannel;
	}
	private AnimMediaHandle.Cache makeMediaHandleCache() {
		// This behavCE was set up by either:    PumaBehaviorManager   or    CCRK_ServiceChannelFactory
		BehaviorConfigEmitter behavCE = myBehaviorCE;
		// If there is not enough info in the behavCE, the returned list will be empty 
		scala.collection.immutable.List animFancyFileSpecs = AnimFileSpecReader.findAnimFileSpecs(behavCE);
		MediaPathFinder pathFinder = MediaResolverFactory.makeFancyFileSpecMediaPathFinder(animFancyFileSpecs);
		UrlSearcher searcher = MediaResolverFactory.makeClasspathUrlSearcher(myResourceCLs);
		return new AnimMediaHandle.Cache(myAnimClient, pathFinder, searcher); //  pathModel, pathPropID, myResourceCLs);
	}	
}
