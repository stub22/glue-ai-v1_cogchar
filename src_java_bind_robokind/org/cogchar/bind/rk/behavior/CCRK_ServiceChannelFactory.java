/*
 * Copyright 2013 The Cogchar Project (www.cogchar.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cogchar.bind.rk.behavior;

import java.util.List;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.api.channel.Channel;
import org.cogchar.api.perform.PerfChannel;
import org.cogchar.bind.rk.robot.client.RobotAnimContext;
import org.cogchar.bind.rk.speech.client.SpeechOutputClient;
import org.cogchar.blob.emit.BehaviorConfigEmitter;
import org.cogchar.platform.util.ClassLoaderUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.robokind.api.animation.player.AnimationPlayer;
import org.robokind.api.speech.SpeechService;

import org.osgi.framework.FrameworkUtil;

/**
 * @author Stub22
 * @author matt
 */

public class CCRK_ServiceChannelFactory extends BasicDebugger {

	// See extensive comments in ChannelBindingLifecycle about the current need for this workaround.
	// Used to construct a BehavConfigEmitter which an AnimChannel may use to look up animation metadata.
	protected RepoClient myWorkaroundRepoClient;

	// Another cruddy workaround is the whole enum-binding thing, discussed in the ChannelBindingConfig.java file.
	public Channel makeServiceChannel(ChannelBindingConfig cbc, Object serviceDepObj) {
		ChannelBindingConfig.ChannelType chanTypeConstant = cbc.getChannelType();
		Ident chanID = cbc.getChannelIdent();
		switch (chanTypeConstant) {
		case SPEECH_BLOCK_OUT:
			return createSpeechChannel(chanID, (SpeechService) serviceDepObj);
		case ANIMATION_PLAYER:
			return createAnimationChannel(chanID, (AnimationPlayer) serviceDepObj);
		default:
			getLogger().error("Cannot resolve service channel type: {}", chanTypeConstant);
		}
		return null;
	}

	private PerfChannel createSpeechChannel(Ident chanID, SpeechService speechSvc) {
		getLogger().info("Creating SpeechOutChan at [{}] for [{}]", chanID, speechSvc);
		return new SpeechOutputClient(speechSvc, chanID);
	}

	private PerfChannel createAnimationChannel(Ident chanID, AnimationPlayer animPlayerSvc) {
		getLogger().info("Creating AnimPlayChan at [{}] for [{}]", chanID, animPlayerSvc);
		// If we wind up keeping this emitter thing, it can be added as a dependency.
		Ident animPathModelID = null; // Not currently used, as we are relying on hardcodes in the AnimFileReader
		BehaviorConfigEmitter behavCE = new BehaviorConfigEmitter(myWorkaroundRepoClient, animPathModelID);
		/* charIdent - so far, used only for log messages
		 * behavCE  - only used to resolve local files, in case animResURL does not resolve within classpath.
		 */
		RobotAnimContext roboAnimContext = new RobotAnimContext(chanID, behavCE);
		roboAnimContext.initConnForAnimPlayer(animPlayerSvc);
		// This list of classloaders to be used to look for animation resources could also be defined as a dependency.
		// But right now, what we need is a BundleContext!

		BundleContext ctx = null;
		Bundle bndle = FrameworkUtil.getBundle(ChannelBindingLifecycle.class);
		if (bndle != null) {
			ctx = bndle.getBundleContext();
		}
		List<ClassLoader> clsForRKConf = ClassLoaderUtils.getFileResourceClassLoaders(ctx, ClassLoaderUtils.ALL_RESOURCE_CLASSLOADER_TYPES);
		roboAnimContext.setResourceClassLoaders(clsForRKConf);
		return roboAnimContext.getTriggeringChannel();
	}
}
