/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.bind.mio.remote;

import java.util.Properties;
import org.jflux.api.core.Listener;
import org.jflux.api.core.config.Configuration;
import org.jflux.impl.messaging.rk.config.DependentLifecycle;
import org.jflux.impl.messaging.rk.config.MessagingLifecycleGroupConfigUtils;
import org.jflux.impl.messaging.rk.config.RKMessagingConfigUtils;
import org.osgi.framework.BundleContext;
import org.mechio.api.animation.lifecycle.AnimationPlayerClientLifecycle;
import org.mechio.api.animation.protocol.AnimationEvent;
import org.mechio.api.animation.protocol.AnimationSignal;
import org.jflux.impl.services.rk.lifecycle.utils.ManagedServiceFactory;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponent;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponentFactory;
import static org.jflux.impl.messaging.rk.config.MessagingLifecycleGroupConfigUtils.*;
import static org.jflux.impl.services.rk.lifecycle.config.RKManagedGroupConfigUtils.*;
import org.jflux.impl.services.rk.lifecycle.utils.SimpleLifecycle;
import org.mechio.impl.animation.messaging.PortableAnimationEvent;

/**
 *
 * @author matt
 */
public class AnimationConnector {
    private final static String REQUEST_DEST_CONFIG_ID = "animationRequestDestConfig";
    private final static String SIGNAL_DEST_CONFIG_ID = "animationSignalDestConfig";
    private final static String REQUEST_SERIALIZE_CONFIG_ID = AnimationEvent.class.toString();
    private final static String SIGNAL_SERIALIZE_CONFIG_ID = AnimationSignal.class.toString();
    private final static String REQUEST_DEST_NAME = "animationRequest";
    private final static String SIGNAL_DEST_NAME = "animationSignal";
    private final static String REQUEST_SENDER_ID = "animReqSndr";
    private final static String SIGNAL_RECEIVER_ID = "animSigRecvr";
    public final static String GROUP_PREFIX = "RKAnimGroup";
    
    public static void connect(BundleContext context, 
            String animPlayerId, String destPrefix, String connectionConfigId) {
        if(context == null 
                || animPlayerId ==  null || connectionConfigId == null){
            throw new NullPointerException();
        }
        ManagedServiceFactory fact = new OSGiComponentFactory(context);
        registerDestConfigs(animPlayerId, destPrefix, fact); 
        launchComponents(animPlayerId, connectionConfigId, null, fact);
        
        launchRemoteAnimClient(context, animPlayerId, 
                animPlayerId, REQUEST_SENDER_ID, SIGNAL_RECEIVER_ID);
    }
    
    private static void registerDestConfigs(String groupId, String destPrefix, ManagedServiceFactory fact){
        String idBase =  groupId + "/" + GROUP_PREFIX;
        String destBase = destPrefix; //groupId + GROUP_PREFIX;
        RKMessagingConfigUtils.registerTopicConfig(
                idBase + "/" + REQUEST_DEST_CONFIG_ID, 
                destBase + REQUEST_DEST_NAME,  null, fact);
        RKMessagingConfigUtils.registerTopicConfig(
                idBase + "/" + SIGNAL_DEST_CONFIG_ID, 
                destBase + SIGNAL_DEST_NAME,  null, fact);
    }
    
    private static void launchComponents(
            String groupId, String connectionConfigId,
            Properties props, ManagedServiceFactory fact){
        String idBase = groupId + "/" + GROUP_PREFIX;
        launchComponent(idBase + "/" + REQUEST_SENDER_ID, props, REMOTE_NOTIFIER, 
                idBase + "/" + REQUEST_DEST_CONFIG_ID, connectionConfigId, 
                REQUEST_SERIALIZE_CONFIG_ID, fact);
        launchComponent(idBase + "/" + SIGNAL_RECEIVER_ID, props, REMOTE_LISTENER, 
                idBase + "/" + SIGNAL_DEST_CONFIG_ID, connectionConfigId, 
                SIGNAL_SERIALIZE_CONFIG_ID, fact);
    }
    
    private static String launchComponent(
            final String groupId,
            final Properties props, 
            final int componentType, 
            final String destinationConfigId, 
            final String connectionConfigId,
            String serializeConfigId, 
            ManagedServiceFactory fact){
        final ManagedGroupFactory mgf = new ManagedGroupFactory(fact); 
        DependentLifecycle.createDependencyListener(
                RKMessagingConfigUtils.SERIALIZATION_CONFIG, 
                serializeConfigId, Configuration.class, 
                new Listener<Configuration<String>>() {
                    @Override
                    public void handleEvent(Configuration<String> event) {
                        mgf.adapt(buildMessagingComponentLifecycleGroupConfig(
                                groupId, props, componentType, event, 
                                destinationConfigId, connectionConfigId));
                    }
                }, fact);
        return groupId(groupId, groupId, groupId);
    }
    
    private static void launchRemoteAnimClient(
            BundleContext context, String animClientId, String animHostId,
            String animationSenderId, String signalReceiverId){
        String idBase = animClientId + "/" + GROUP_PREFIX;
        AnimationPlayerClientLifecycle lifecycle =
                new AnimationPlayerClientLifecycle(
                        animClientId, animHostId, 
                        groupId(idBase, animationSenderId, NOTIFIER_COMPONENT),
                        groupId(idBase, signalReceiverId, LISTENER_COMPONENT),
                        context);
        OSGiComponent speechComp = new OSGiComponent(context, lifecycle);
        speechComp.start();
    }
    private static String groupId(String groupId, String suffix, String component){
        return MessagingLifecycleGroupConfigUtils.childId(groupId + "/" + suffix, component);   
    } 
	
	public static void launchPortableAnimEventFactory(BundleContext context) { 
        new OSGiComponent(context, 
                new SimpleLifecycle(
                        new PortableAnimationEvent.Factory(), 
                        AnimationEvent.AnimationEventFactory.class)).start();		
	}
}
