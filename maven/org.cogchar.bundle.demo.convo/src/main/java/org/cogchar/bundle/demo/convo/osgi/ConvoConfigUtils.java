/*
 * Copyright 2012 The Cogchar Project (www.cogchar.org).
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
package org.cogchar.bundle.demo.convo.osgi;

import org.jflux.api.core.Listener;
import org.jflux.api.core.Source;
import org.jflux.api.core.config.Configuration;
import org.jflux.api.core.config.DefaultConfiguration;

/**
 *
 * @author Matthew Stevenson
 */
public class ConvoConfigUtils {
    public static String CONF_SPREC_BROKER_IP = "convoSpRecBrokerIp";
    public static String CONF_SPREC_BROKER_PORT = "convoSpRecBrokerPort";
    public static String CONF_SPREC_BROKER_USERNAME = "convoSpRecBrokerUser";
    public static String CONF_SPREC_BROKER_PASSWORD = "convoSpRecBrokerPassword";
    public static String CONF_SPREC_BROKER_CLIENT_NAME = "convoSpRecBrokerClientName";
    public static String CONF_SPREC_BROKER_VIRTUAL_HOST = "convoSpRecBrokerVirtualHost";
    public static String CONF_SPREC_DESTINATION = "convoSpRecDestinationStr";
    public static String CONF_TTS_BROKER_IP = "convoTTSBrokerIp";
    public static String CONF_TTS_BROKER_PORT = "convoTTSBrokerPort";
    public static String CONF_TTS_BROKER_USERNAME = "convoTTSBrokerUser";
    public static String CONF_TTS_BROKER_PASSWORD = "convoTTSBrokerPassword";
    public static String CONF_TTS_BROKER_CLIENT_NAME = "convoTTSBrokerClientName";
    public static String CONF_TTS_BROKER_VIRTUAL_HOST = "convoTTSBrokerVirtualHost";
    public static String CONF_TTS_DESTINATION = "convoTTSDestinationStr";
    public static String CONF_COGBOT_IP = "convoCogbotIp";
    public static String CONF_COGBOT_POLL_INTERVAL = "convoCogbotPollInterval";
    public static String CONF_COGBOT_POLL_MESSAGE = "convoCogbotPollMessage";
    
    private static Configuration<String> theConfiguration;
    
    public static synchronized Configuration<String> defaultConfiguration(){
        if(theConfiguration == null){
            theConfiguration = buildDefaultConfig();
        }
        return theConfiguration;
    }
    
    static synchronized void setDefaultConfiguration(
            Configuration<String> config){
        theConfiguration = config;
    }
    
    private static Configuration<String> buildDefaultConfig(){
        DefaultConfiguration<String> conf = new DefaultConfiguration<String>();
        
        conf.addProperty(String.class, CONF_SPREC_BROKER_IP, "127.0.0.1");
        conf.addProperty(String.class, CONF_SPREC_BROKER_PORT, "5672");
        conf.addProperty(String.class, CONF_SPREC_BROKER_USERNAME, "admin");
        conf.addProperty(String.class, CONF_SPREC_BROKER_PASSWORD, "admin");
        conf.addProperty(String.class, CONF_SPREC_BROKER_CLIENT_NAME, "client1");
        conf.addProperty(String.class, CONF_SPREC_BROKER_VIRTUAL_HOST, "test");
        conf.addProperty(String.class, CONF_SPREC_DESTINATION, 
                "speechRecEvent; {create:always, node:{type:topic}}");
        
        conf.addProperty(String.class, CONF_TTS_BROKER_IP, "127.0.0.1");
        conf.addProperty(String.class, CONF_TTS_BROKER_PORT, "5672");
        conf.addProperty(String.class, CONF_TTS_BROKER_USERNAME, "admin");
        conf.addProperty(String.class, CONF_TTS_BROKER_PASSWORD, "admin");
        conf.addProperty(String.class, CONF_TTS_BROKER_CLIENT_NAME, "client1");
        conf.addProperty(String.class, CONF_TTS_BROKER_VIRTUAL_HOST, "test");
        conf.addProperty(String.class, CONF_TTS_DESTINATION, 
                "speechRequest; {create:always, node:{type:queue}}");
        
        conf.addProperty(String.class, CONF_COGBOT_IP, "127.0.0.1");
        conf.addProperty(String.class, CONF_COGBOT_POLL_MESSAGE, "");
        conf.addProperty(Long.class, CONF_COGBOT_POLL_INTERVAL, 1000L);
        
        return conf;
    }
    
    public static <T> Source<T> getSource(Class<T> clazz, String key){
        return defaultConfiguration().getPropertySource(clazz, key);
    }
    
    public static <T> Listener<T> getSetter(Class<T> clazz, String key){
        return defaultConfiguration().getPropertySetter(clazz, key);
    }
}
