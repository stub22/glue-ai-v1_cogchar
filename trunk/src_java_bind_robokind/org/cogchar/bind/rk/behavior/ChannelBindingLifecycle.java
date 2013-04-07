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

import java.util.Map;
import java.util.Properties;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.api.perform.Channel;
import org.cogchar.api.perform.Media;
import org.cogchar.bind.rk.robot.client.RobotAnimContext;
import org.cogchar.bind.rk.speech.client.SpeechOutputClient;
import org.cogchar.blob.emit.BehaviorConfigEmitter;
import org.osgi.framework.BundleContext;
import org.robokind.api.animation.player.AnimationPlayer;
import org.robokind.api.common.lifecycle.AbstractLifecycleProvider;
import org.robokind.api.common.lifecycle.utils.DescriptorListBuilder;
import org.robokind.api.speech.SpeechService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matthew Stevenson <www.robokind.org>
 */
public class ChannelBindingLifecycle<M extends Media, Time> extends AbstractLifecycleProvider<Channel, Channel<M,Time>> {
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
    
    @Override
    protected Channel<M, Time> create(Map<String, Object> dependencies) {
        Object service = dependencies.get("service");
        switch(myBindingConfig.getChannelType()){
            case SPEECH:
                return createSpeechChannel((SpeechService)service);
            case ANIMATION:
                return createAnimationChannel((AnimationPlayer)service);
        }
        return null;
    }
    
    private Channel createSpeechChannel(SpeechService speechSvc){
		theLogger.warn("Creating speechChannel for[{}]", speechSvc);
		BundleContext bundleCtx = null;
		Ident chanIdent = new FreeIdent(myBindingConfig.getChannelURI());
		return new SpeechOutputClient(speechSvc, chanIdent);
    }
    
    private Channel createAnimationChannel(AnimationPlayer animPlayerSvc){
		theLogger.warn("Creating speechChannel for[{}]", animPlayerSvc);
		Ident charIdent = null;
		BehaviorConfigEmitter behavCE = null;
		/* charIdent - so far, used only for log messages
		 * behavCE  - only used to resolve local files, in case animResURL does not resolve within classpath.
		 */
		RobotAnimContext	roboAnimContext = new RobotAnimContext(charIdent, behavCE);
		roboAnimContext.initConnForAnimPlayer(animPlayerSvc);
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
