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


package org.cogchar.outer.behav.demo;

import java.util.Properties;
import org.jflux.api.core.Listener;
import org.jflux.api.core.config.Configuration;
import org.jflux.impl.messaging.rk.config.DependentLifecycle;
import org.jflux.impl.messaging.rk.config.MessagingLifecycleGroupConfigUtils;
import org.jflux.impl.messaging.rk.config.RKMessagingConfigUtils;
import org.jflux.impl.services.rk.lifecycle.ManagedService;
import org.jflux.impl.services.rk.lifecycle.utils.ManagedServiceFactory;
import org.robokind.api.messaging.services.ServiceCommand;
import org.robokind.api.messaging.services.ServiceError;
import org.robokind.api.speech.SpeechConfig;
import org.robokind.api.speech.SpeechEventList;
import org.robokind.api.speech.SpeechRequest;
import org.robokind.api.speech.lifecycle.RemoteSpeechServiceClientLifecycle;
import static org.jflux.impl.messaging.rk.config.MessagingLifecycleGroupConfigUtils.*;
import static org.jflux.impl.services.rk.lifecycle.config.RKManagedGroupConfigUtils.*;

/**
 *
 * @author matt
 */
public class SpeechConnector {
    private final static String COMMAND_DEST_CONFIG_ID = "CommandDestConfig";
    private final static String CONFIG_DEST_CONFIG_ID = "ConfigDestConfig";
    private final static String ERROR_DEST_CONFIG_ID = "ErrorDestConfig";
    private final static String REQUEST_DEST_CONFIG_ID = "RequestDestConfig";
    private final static String EVENT_DEST_CONFIG_ID = "EventDestConfig";
    
    private final static String COMMAND_SERIALIZE_CONFIG_ID = ServiceCommand.class.toString();
    private final static String CONFIG_SERIALIZE_CONFIG_ID = SpeechConfig.class.toString();
    private final static String ERROR_SERIALIZE_CONFIG_ID = ServiceError.class.toString();
    private final static String REQUEST_SERIALIZE_CONFIG_ID = SpeechRequest.class.toString();
    private final static String EVENT_SERIALIZE_CONFIG_ID = SpeechEventList.class.toString();
    
    private final static String COMMAND_DEST_NAME = "Command";
    private final static String CONFIG_DEST_NAME = "Command";
    private final static String ERROR_DEST_NAME = "Error";
    private final static String REQUEST_DEST_NAME = "Request";
    private final static String EVENT_DEST_NAME = "Event";
    
    private final static String COMMAND_SENDER_ID = "Command";
    private final static String CONFIG_SENDER_ID = "Config";
    private final static String ERROR_RECEIVER_ID = "Error";
    private final static String REQUEST_SENDER_ID = "Request";
    private final static String EVENT_RECEIVER_ID = "Event";
        
    public final static String GROUP_PREFIX = "RKSpeechGroup";
    
    public static void connect(ManagedServiceFactory fact, 
            String speechGroupId, String speechPrefix, String connectionConfigId) {
        if(fact == null 
                || speechGroupId ==  null || connectionConfigId == null){
            throw new NullPointerException();
        }
        registerDestConfigs(speechGroupId, speechPrefix, fact);
        launchComponents(speechGroupId, connectionConfigId, speechPrefix, null, fact);
        
        launchRemoteSpeechClient(fact, speechGroupId, 
                speechGroupId,  
                speechPrefix + COMMAND_SENDER_ID, 
                speechPrefix + CONFIG_SENDER_ID, 
                speechPrefix + ERROR_RECEIVER_ID, 
                speechPrefix + REQUEST_SENDER_ID, 
                speechPrefix + EVENT_RECEIVER_ID);
    }
    
    private static void registerDestConfigs(String groupId, String speechPrefix, ManagedServiceFactory fact){
        String idBase =  groupId + "/" + GROUP_PREFIX;
        String destBase = ""; //groupId + GROUP_PREFIX;
        RKMessagingConfigUtils.registerQueueConfig(
                idBase + "/" + speechPrefix + COMMAND_DEST_CONFIG_ID, 
                destBase + speechPrefix + COMMAND_DEST_NAME,  null, fact);
        RKMessagingConfigUtils.registerQueueConfig(
                idBase + "/" + speechPrefix + CONFIG_DEST_CONFIG_ID, 
                destBase + speechPrefix + CONFIG_DEST_NAME,  null, fact);
        RKMessagingConfigUtils.registerTopicConfig(
                idBase + "/" + speechPrefix + ERROR_DEST_CONFIG_ID, 
                destBase + speechPrefix + ERROR_DEST_NAME,  null, fact);
        RKMessagingConfigUtils.registerQueueConfig(
                idBase + "/" + speechPrefix + REQUEST_DEST_CONFIG_ID, 
                destBase + speechPrefix + REQUEST_DEST_NAME,  null, fact);
        RKMessagingConfigUtils.registerTopicConfig(
                idBase + "/" + speechPrefix + EVENT_DEST_CONFIG_ID, 
                destBase + speechPrefix + EVENT_DEST_NAME,  null, fact);
    }
    
    private static void launchComponents(
            String groupId, String connectionConfigId, String speechPrefix,
            Properties props, ManagedServiceFactory fact){
        String idBase = groupId + "/" + GROUP_PREFIX;
        launchComponent(
                idBase + "/" + speechPrefix + COMMAND_SENDER_ID, props, REMOTE_NOTIFIER, 
                idBase + "/" + speechPrefix + COMMAND_DEST_CONFIG_ID, connectionConfigId,
                COMMAND_SERIALIZE_CONFIG_ID, fact);
        launchComponent(
                idBase + "/" + speechPrefix + CONFIG_SENDER_ID, props, REMOTE_NOTIFIER, 
                idBase + "/" + speechPrefix + CONFIG_DEST_CONFIG_ID, connectionConfigId,
                CONFIG_SERIALIZE_CONFIG_ID, fact);
        launchComponent(
                idBase + "/" + speechPrefix + ERROR_RECEIVER_ID, props, REMOTE_LISTENER, 
                idBase + "/" + speechPrefix + ERROR_DEST_CONFIG_ID, connectionConfigId, 
                ERROR_SERIALIZE_CONFIG_ID, fact);
        launchComponent(
                idBase + "/" + speechPrefix + REQUEST_SENDER_ID, props, REMOTE_NOTIFIER, 
                idBase + "/" + speechPrefix + REQUEST_DEST_CONFIG_ID, connectionConfigId, 
                REQUEST_SERIALIZE_CONFIG_ID, fact);
        launchComponent(
                idBase + "/" + speechPrefix + EVENT_RECEIVER_ID, props, REMOTE_LISTENER, 
                idBase + "/" + speechPrefix + EVENT_DEST_CONFIG_ID, connectionConfigId, 
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
            ManagedServiceFactory fact,
            String speechClientId, String speechHostId,
            String commandSenderId, String configSenderId, 
            String errorReceiverId, String speechRequestSenderId,
            String speechEventsReceiverId){
        String idBase = speechClientId + "/" + GROUP_PREFIX;
        RemoteSpeechServiceClientLifecycle lifecycle =
                new RemoteSpeechServiceClientLifecycle(
                        speechClientId, speechHostId, 
                        groupId(idBase, commandSenderId, NOTIFIER_COMPONENT), 
                        groupId(idBase, configSenderId, NOTIFIER_COMPONENT), 
                        groupId(idBase, errorReceiverId, LISTENER_COMPONENT), 
                        groupId(idBase, speechRequestSenderId, NOTIFIER_COMPONENT), 
                        groupId(idBase, speechEventsReceiverId, LISTENER_COMPONENT));
        ManagedService speechComp = fact.createService(lifecycle, null);
        speechComp.start();
    }
    private static String groupId(String groupId, String suffix, String component){
        return MessagingLifecycleGroupConfigUtils.childId(groupId + "/" + suffix, component);   
    }    
}
