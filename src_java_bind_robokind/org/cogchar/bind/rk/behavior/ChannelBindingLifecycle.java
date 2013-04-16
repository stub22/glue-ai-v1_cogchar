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
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.api.perform.Channel;
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
 *
 * @author Matthew Stevenson <www.robokind.org>
 */
// public class ChannelBindingLifecycle<M extends Media, Time> extends AbstractLifecycleProvider<Channel, Channel<M,Time>> {
public class ChannelBindingLifecycle extends AbstractLifecycleProvider<Channel, Channel> {
	private static Logger theLogger =  LoggerFactory.getLogger(ChannelBindingLifecycle.class);
    private ChannelBindingConfig myBindingConfig;
    
    public ChannelBindingLifecycle(ChannelBindingConfig conf){
        super(new DescriptorListBuilder()
                //The name "service" is used only within the lifecycle
                .dependency("service", conf.getChannelType().getServiceClass()).with(conf.getOSGiFilterString())
                .getDescriptors());
        myBindingConfig = conf;
        myRegistrationProperties = new Properties();
        myRegistrationProperties.put("URI", conf.getChannelURI());
    }
    
    @Override protected Channel create(Map<String, Object> dependencies) {
        Object service = dependencies.get("service");
        switch(myBindingConfig.getChannelType()){
            case SPEECH:
                return createSpeechChannel((SpeechService)service);
            case ANIMATION:
                return createAnimationChannel((AnimationPlayer)service);
        }
        return null;
    }
    protected Ident getChannelIdent() { 
		return new FreeIdent(myBindingConfig.getChannelURI());
	}
    private Channel createSpeechChannel(SpeechService speechSvc){
		Ident chanIdent = getChannelIdent();
		theLogger.warn("Creating SpeechOutChan at [{}] for [{}]", chanIdent, speechSvc);
		return new SpeechOutputClient(speechSvc, chanIdent);
    }
    
    private Channel createAnimationChannel(AnimationPlayer animPlayerSvc){
		Ident chanIdent = getChannelIdent();
		theLogger.warn("Creating AnimPlayChan at [{}] for [{}]", chanIdent, animPlayerSvc);
		// If we wind up keeping this emitter thing, it can be added as a dependency.
		BehaviorConfigEmitter behavCE = new BehaviorConfigEmitter();
		/* charIdent - so far, used only for log messages
		 * behavCE  - only used to resolve local files, in case animResURL does not resolve within classpath.
		 */
		RobotAnimContext	roboAnimContext = new RobotAnimContext(chanIdent, behavCE);
		roboAnimContext.initConnForAnimPlayer(animPlayerSvc);
		// This list of classloaders to be used to look for animation resources could also be defined as a dependency.
		// But right now, what we need is a BundleContext!
		BundleContext ctx = FrameworkUtil.getBundle(ChannelBindingLifecycle.class).getBundleContext();
		List<ClassLoader> clsForRKConf = ClassLoaderUtils.getFileResourceClassLoaders(ctx, ClassLoaderUtils.ALL_RESOURCE_CLASSLOADER_TYPES);
		roboAnimContext.setResourceClassLoaders(clsForRKConf);
		return roboAnimContext.getTriggeringChannel();
    }

    @Override
    protected void handleChange(String dependencyKey, Object dependency, Map<String, Object> availableDependencies) {
        myService = isSatisfied() ? create(availableDependencies) : null;
    }

    @Override
    protected Class<Channel> getServiceClass() {
        return Channel.class;
    }
    
}
