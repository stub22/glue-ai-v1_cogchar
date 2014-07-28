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
package org.cogchar.bundle.demo.dictation.osgi;

import org.jflux.api.core.config.Configuration;
import org.jflux.api.core.config.DefaultConfiguration;

/**
 *
 * @author Matthew Stevenson
 */
public class DictationConfigUtils {
    public static String CONF_BROKER_IP = "dictBrokerIp";
    public static String CONF_BROKER_PORT = "dictBrokerPort";
    public static String CONF_BROKER_USERNAME = "dictBrokerUser";
    public static String CONF_BROKER_PASSWORD = "dictBrokerPassword";
    public static String CONF_BROKER_CLIENT_NAME = "dictBrokerClientName";
    public static String CONF_BROKER_VIRTUAL_HOST = "dictBrokerVirtualHost";
    public static String CONF_DESTINATION = "dictDestinationStr";
    
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
        
        conf.addProperty(String.class, CONF_BROKER_IP, "127.0.0.1");
        conf.addProperty(String.class, CONF_BROKER_PORT, "5672");
        conf.addProperty(String.class, CONF_BROKER_USERNAME, "admin");
        conf.addProperty(String.class, CONF_BROKER_PASSWORD, "admin");
        conf.addProperty(String.class, CONF_BROKER_CLIENT_NAME, "client1");
        conf.addProperty(String.class, CONF_BROKER_VIRTUAL_HOST, "test");
        conf.addProperty(String.class, CONF_DESTINATION, 
                "speechRecEvent; {create: always, node: {type: topic}}");
        
        return conf;
    }
    
    public static <T> T getValue(Class<T> clazz, String key){
        return defaultConfiguration().getPropertySource(clazz, key).getValue();
    }
    
    public static <T> void setValue(Class<T> clazz, String key, T val){
        defaultConfiguration().getPropertySetter(clazz, key).handleEvent(val);
    }
}
