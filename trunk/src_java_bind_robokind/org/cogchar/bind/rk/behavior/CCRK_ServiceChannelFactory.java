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
import java.util.Map;
import java.util.Properties;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.api.channel.Channel;
import org.cogchar.api.perform.PerfChannel;
import org.cogchar.api.perform.Media;
import org.cogchar.bind.rk.robot.client.RobotAnimContext;
import org.cogchar.bind.rk.speech.client.SpeechOutputClient;
import org.cogchar.blob.emit.BehaviorConfigEmitter;
import org.cogchar.platform.util.ClassLoaderUtils;
import org.osgi.framework.BundleContext;
import org.robokind.api.animation.player.AnimationPlayer;
import org.robokind.api.common.lifecycle.AbstractLifecycleProvider;
import org.robokind.api.common.lifecycle.utils.DescriptorListBuilder;
import org.robokind.api.speech.SpeechService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.framework.FrameworkUtil;

/**
 * @author Stub22
 * @author matt
 */


public class CCRK_ServiceChannelFactory extends BasicDebugger {
	
	public Channel makeServiceChannel(ChannelBindingConfig cbc, Object service) {
		ChannelBindingConfig.ChannelType chanTypeConstant = cbc.getChannelType();
		Ident chanID = cbc.getChannelIdent();
        switch(chanTypeConstant){
            case SPEECH:
                return createSpeechChannel(chanID, (SpeechService)service);
            case ANIMATION:
                return createAnimationChannel(chanID, (AnimationPlayer)service);
			default:
				getLogger().error("Cannot resolve service channel type: {}", chanTypeConstant);
        }
		return null;
	}
    private PerfChannel createSpeechChannel(Ident chanID, SpeechService speechSvc){
		getLogger().info("Creating SpeechOutChan at [{}] for [{}]", chanID, speechSvc);
		return new SpeechOutputClient(speechSvc, chanID);
    }
    
    private PerfChannel createAnimationChannel(Ident chanID, AnimationPlayer animPlayerSvc){
		getLogger().info("Creating AnimPlayChan at [{}] for [{}]", chanID, animPlayerSvc);
		// If we wind up keeping this emitter thing, it can be added as a dependency.
		BehaviorConfigEmitter behavCE = new BehaviorConfigEmitter();
		/* charIdent - so far, used only for log messages
		 * behavCE  - only used to resolve local files, in case animResURL does not resolve within classpath.
		 */
		RobotAnimContext	roboAnimContext = new RobotAnimContext(chanID, behavCE);
		roboAnimContext.initConnForAnimPlayer(animPlayerSvc);
		// This list of classloaders to be used to look for animation resources could also be defined as a dependency.
		// But right now, what we need is a BundleContext!
		BundleContext ctx = FrameworkUtil.getBundle(ChannelBindingLifecycle.class).getBundleContext();
		List<ClassLoader> clsForRKConf = ClassLoaderUtils.getFileResourceClassLoaders(ctx, ClassLoaderUtils.ALL_RESOURCE_CLASSLOADER_TYPES);
		roboAnimContext.setResourceClassLoaders(clsForRKConf);
		return roboAnimContext.getTriggeringChannel();
    }	
}
