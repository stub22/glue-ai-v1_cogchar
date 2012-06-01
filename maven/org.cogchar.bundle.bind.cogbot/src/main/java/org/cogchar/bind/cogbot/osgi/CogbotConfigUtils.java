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
package org.cogchar.bind.cogbot.osgi;

import org.jflux.api.core.Listener;
import org.jflux.api.core.Source;
import org.jflux.api.core.util.Configuration;
import org.jflux.api.core.util.DefaultConfiguration;
import org.jflux.api.core.util.DefaultTimestampSource;

/**
 *
 * @author Matthew Stevenson
 */
public class CogbotConfigUtils {
    public final static String CONF_COGBOT_IP = "cogbotServerIp";
    public final static String CONF_COGBOT_PORT = "cogbotServerPort";
    
    public final static String OLD_CONF_COBOT_BIN_DIR = "cogbotBinaryFolder";
    public final static String OLD_CONF_CONFIG_FOLDER = "cogbotConfigFolder";
    public final static String OLD_CONF_FULL_NAME = "robot_fullname";
    public final static String OLD_CONF_COGBOT_NAME = "cogbot_name";
    
    public final static String CONF_COGSIM_ENABLED = "cogsim_enable";
    public final static String CONF_COGSIM_USERNAME = "cogsim_username";
    public final static String CONF_COGSIM_POLL_ENABLED = "cogsim_poll_enabled";
    public final static String CONF_COGSIM_JMX_ENABLED = "cogsim_jmx_enabled";
    public final static String CONF_COGSIM_JMX_READY = "cogsim_jmx_ready";
    public final static String CONF_COGSIM_JMX_URL = "character_engine_jmx_url";
    public final static String CONF_COGSIM_POLL_INTERVAL = "cogsim_poll_ms";
    public final static String CONF_COGSIM_URL_SAID_TAIL = "cogsim_said_url_tail";
    public final static String CONF_COGSIM_URL_HEARD_TAIL = "cogsim_heard_url_tail";
    public final static String CONF_COGSIM_URL_ACTION_TAIL = "cogsim_act_url_tail";
    
    public final static String CONF_COGSIM_DEBUG_FLAG = "cogsim_debug_flag";
    
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
        Source<Long> ts = new DefaultTimestampSource();
        DefaultConfiguration<Long,String> conf = 
                new DefaultConfiguration<Long, String>(ts);
        
        conf.addProperty(String.class, CONF_COGBOT_IP, "127.0.0.1");
        conf.addProperty(String.class, CONF_COGBOT_PORT, "5580");
        
        conf.addProperty(String.class, OLD_CONF_COBOT_BIN_DIR, 
                "C:\\_hanson\\_deploy\\distro_20a\\cogbot");
        conf.addProperty(String.class, OLD_CONF_CONFIG_FOLDER,
                "./resources/config.properties");
        conf.addProperty(String.class, OLD_CONF_FULL_NAME, "Bina Daxeline");
        conf.addProperty(String.class, OLD_CONF_COGBOT_NAME, "Bina Daxeline");
        
        conf.addProperty(Boolean.class, CONF_COGSIM_ENABLED, false);
        conf.addProperty(String.class, CONF_COGSIM_USERNAME, "Test User");
        conf.addProperty(Boolean.class, CONF_COGSIM_POLL_ENABLED, false);
        conf.addProperty(Boolean.class, CONF_COGSIM_JMX_ENABLED, true);
        conf.addProperty(Boolean.class, CONF_COGSIM_JMX_READY, false);
        conf.addProperty(String.class, CONF_COGSIM_JMX_URL, "service:jmx:rmi:///jndi/rmi://localhost:7227/jmxrmi");
        conf.addProperty(Long.class, CONF_COGSIM_POLL_INTERVAL, 1000L);
        conf.addProperty(String.class, CONF_COGSIM_URL_SAID_TAIL, "posterboard/onchat-said");
        conf.addProperty(String.class, CONF_COGSIM_URL_HEARD_TAIL, "posterboard/onchat-heard");
        conf.addProperty(String.class, CONF_COGSIM_URL_ACTION_TAIL, "postaction");
        
        conf.addProperty(Boolean.class, CONF_COGSIM_DEBUG_FLAG, false);
        
        return conf;
    }
    
    public static <T> T getValue(Class<T> clazz, String key){
        return defaultConfiguration().getPropertySource(clazz, key).getValue();
    }
    
    public static <T> void setValue(Class<T> clazz, String key, T val){
        defaultConfiguration().getPropertySetter(clazz, key).handleEvent(val);
    }
    
    public static <T> void setOrCreateValue(Class<T> clazz, String key, T val){
        Configuration<String> conf = defaultConfiguration();
        Listener<T> l = conf.getPropertySetter(clazz, key);
        if(l == null){
            if(conf instanceof DefaultConfiguration){
                ((DefaultConfiguration)conf).addProperty(clazz, key, val);
                l = conf.getPropertySetter(clazz, key);
            }
        }
        if(l != null){
            l.handleEvent(val);
        }
    }    
}
