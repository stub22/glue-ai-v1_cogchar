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


package org.cogchar.app.puma.net.demo;

import java.util.Properties;
import org.jflux.api.core.Listener;
import org.jflux.api.core.config.Configuration;
import org.osgi.framework.BundleContext;
import org.robokind.api.common.lifecycle.utils.ManagedServiceFactory;
import org.robokind.api.common.osgi.lifecycle.OSGiComponent;
import org.robokind.api.common.osgi.lifecycle.OSGiComponentFactory;
import org.robokind.api.messaging.services.ServiceCommand;
import org.robokind.api.messaging.services.ServiceError;
import org.robokind.api.speech.SpeechConfig;
import org.robokind.api.speech.SpeechEventList;
import org.robokind.api.speech.SpeechRequest;
import org.robokind.api.speech.lifecycle.RemoteSpeechServiceLifecycle;
import org.robokind.impl.messaging.config.DependentLifecycle;
import org.robokind.impl.messaging.config.MessagingLifecycleGroupConfigUtils;
import org.robokind.impl.messaging.config.RKMessagingConfigUtils;
import static org.robokind.impl.messaging.config.MessagingLifecycleGroupConfigUtils.*;
import static org.robokind.api.common.lifecycle.config.RKManagedGroupConfigUtils.*;

/**
 *
 * @author matt
 */
public class SpeechConnector {
    private final static String COMMAND_DEST_CONFIG_ID = "speechCommandDestConfig";
    private final static String CONFIG_DEST_CONFIG_ID = "speechConfigDestConfig";
    private final static String ERROR_DEST_CONFIG_ID = "speechErrorDestConfig";
    private final static String REQUEST_DEST_CONFIG_ID = "speechRequestDestConfig";
    private final static String EVENT_DEST_CONFIG_ID = "speechEventDestConfig";
    
    private final static String COMMAND_SERIALIZE_CONFIG_ID = ServiceCommand.class.toString();
    private final static String CONFIG_SERIALIZE_CONFIG_ID = SpeechConfig.class.toString();
    private final static String ERROR_SERIALIZE_CONFIG_ID = ServiceError.class.toString();
    private final static String REQUEST_SERIALIZE_CONFIG_ID = SpeechRequest.class.toString();
    private final static String EVENT_SERIALIZE_CONFIG_ID = SpeechEventList.class.toString();
    
    private final static String COMMAND_DEST_NAME = "speechCommand";
    private final static String CONFIG_DEST_NAME = "speechCommand";
    private final static String ERROR_DEST_NAME = "speechError";
    private final static String REQUEST_DEST_NAME = "speechRequest";
    private final static String EVENT_DEST_NAME = "speechEvent";
    
    private final static String COMMAND_SENDER_ID = "speechCommand";
    private final static String CONFIG_SENDER_ID = "speechConfig";
    private final static String ERROR_RECEIVER_ID = "speechError";
    private final static String REQUEST_SENDER_ID = "speechRequest";
    private final static String EVENT_RECEIVER_ID = "speechEvent";
        
    public final static String GROUP_PREFIX = "RKSpeechGroup";
    
    public static void connect(BundleContext context, 
            String speechGroupId, String destPrefix, String connectionConfigId) {
        if(context == null 
                || speechGroupId ==  null || connectionConfigId == null){
            throw new NullPointerException();
        }
        ManagedServiceFactory fact = new OSGiComponentFactory(context);
        registerDestConfigs(speechGroupId, destPrefix, fact);
        launchComponents(speechGroupId, connectionConfigId, null, fact);
        
        launchRemoteSpeechClient(context, speechGroupId, 
                speechGroupId,  COMMAND_SENDER_ID, CONFIG_SENDER_ID, 
                ERROR_RECEIVER_ID, REQUEST_SENDER_ID, EVENT_RECEIVER_ID);
    }
    
    private static void registerDestConfigs(String groupId, String destPrefix, ManagedServiceFactory fact){
        String idBase =  groupId + "/" + GROUP_PREFIX;
        String destBase = destPrefix; //groupId + GROUP_PREFIX;
        RKMessagingConfigUtils.registerQueueConfig(
                idBase + "/" + COMMAND_DEST_CONFIG_ID, destBase + COMMAND_DEST_NAME,  null, fact);
        RKMessagingConfigUtils.registerQueueConfig(
                idBase + "/" + CONFIG_DEST_CONFIG_ID, destBase + CONFIG_DEST_NAME,  null, fact);
        RKMessagingConfigUtils.registerTopicConfig(
                idBase + "/" + ERROR_DEST_CONFIG_ID, destBase + ERROR_DEST_NAME,  null, fact);
        RKMessagingConfigUtils.registerQueueConfig(
                idBase + "/" + REQUEST_DEST_CONFIG_ID, destBase + REQUEST_DEST_NAME,  null, fact);
        RKMessagingConfigUtils.registerTopicConfig(
                idBase + "/" + EVENT_DEST_CONFIG_ID, destBase + EVENT_DEST_NAME,  null, fact);
    }
    
    private static void launchComponents(
            String groupId, String connectionConfigId,
            Properties props, ManagedServiceFactory fact){
        String idBase = groupId + "/" + GROUP_PREFIX;
        launchComponent(idBase + "/" + COMMAND_SENDER_ID, props, REMOTE_NOTIFIER, 
                idBase + "/" + COMMAND_DEST_CONFIG_ID, connectionConfigId,
                COMMAND_SERIALIZE_CONFIG_ID, fact);
        launchComponent(idBase + "/" + CONFIG_SENDER_ID, props, REMOTE_NOTIFIER, 
                idBase + "/" + CONFIG_DEST_CONFIG_ID, connectionConfigId,
                CONFIG_SERIALIZE_CONFIG_ID, fact);
        launchComponent(idBase + "/" + ERROR_RECEIVER_ID, props, REMOTE_LISTENER, 
                idBase + "/" + ERROR_DEST_CONFIG_ID, connectionConfigId, 
                ERROR_SERIALIZE_CONFIG_ID, fact);
        launchComponent(idBase + "/" + REQUEST_SENDER_ID, props, REMOTE_NOTIFIER, 
                idBase + "/" + REQUEST_DEST_CONFIG_ID, connectionConfigId, 
                REQUEST_SERIALIZE_CONFIG_ID, fact);
        launchComponent(idBase + "/" + EVENT_RECEIVER_ID, props, REMOTE_LISTENER, 
                idBase + "/" + EVENT_DEST_CONFIG_ID, connectionConfigId, 
                EVENT_SERIALIZE_CONFIG_ID, fact);
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
    
    private static void launchRemoteSpeechClient(
            BundleContext context,
            String speechClientId, String speechHostId,
            String commandSenderId, String configSenderId, 
            String errorReceiverId, String speechRequestSenderId,
            String speechEventsReceiverId){
        String idBase = speechClientId + "/" + GROUP_PREFIX;
        RemoteSpeechServiceLifecycle lifecycle =
                new RemoteSpeechServiceLifecycle(
                        speechClientId, speechHostId, 
                        groupId(idBase, commandSenderId, NOTIFIER_COMPONENT), 
                        groupId(idBase, configSenderId, NOTIFIER_COMPONENT), 
                        groupId(idBase, errorReceiverId, LISTENER_COMPONENT), 
                        groupId(idBase, speechRequestSenderId, NOTIFIER_COMPONENT), 
                        groupId(idBase, speechEventsReceiverId, LISTENER_COMPONENT));
        OSGiComponent speechComp = new OSGiComponent(context, lifecycle);
        speechComp.start();
    }
    private static String groupId(String groupId, String suffix, String component){
        return MessagingLifecycleGroupConfigUtils.childId(groupId + "/" + suffix, component);   
    }    
}
