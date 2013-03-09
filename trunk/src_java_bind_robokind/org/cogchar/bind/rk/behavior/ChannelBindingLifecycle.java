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
import org.cogchar.api.perform.Channel;
import org.cogchar.api.perform.Media;
import org.robokind.api.animation.player.AnimationPlayer;
import org.robokind.api.common.lifecycle.AbstractLifecycleProvider;
import org.robokind.api.common.lifecycle.utils.DescriptorListBuilder;
import org.robokind.api.speech.SpeechService;

/**
 *
 * @author Matthew Stevenson <www.robokind.org>
 */
public class ChannelBindingLifecycle<M extends Media, Time> extends AbstractLifecycleProvider<Channel, Channel<M,Time>> {
    private ChannelBindingConfig myBindingConfig;
    
    public ChannelBindingLifecycle(ChannelBindingConfig conf){
        super(new DescriptorListBuilder()
                //The name "service" is used only within the lifecycle
                .dependency("service", conf.myChannelType.getServiceClass()).with(conf.myOSGiFilterString)
                .getDescriptors());
        myRegistrationProperties = new Properties();
        myRegistrationProperties.put("URI", conf.myChannelURI);
    }
    
    @Override
    protected Channel<M, Time> create(Map<String, Object> dependencies) {
        Object service = dependencies.get("service");
        switch(myBindingConfig.myChannelType){
            case SPEECH:
                return createSpeechChannel((SpeechService)service);
            case ANIMATION:
                return createAnimationChannel((AnimationPlayer)service);
        }
        return null;
    }
    
    private Channel createSpeechChannel(SpeechService service){
        return null;
    }
    
    private Channel createAnimationChannel(AnimationPlayer service){
        return null;
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
