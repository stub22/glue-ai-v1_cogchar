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
package org.cogchar.bind.mio.osgi;

import java.io.File;
import org.jflux.api.core.config.Configuration;
import org.jflux.api.core.config.DefaultConfiguration;
import org.jflux.api.common.rk.config.VersionProperty;
import org.mechio.api.motion.jointgroup.JointGroup;
import org.mechio.api.motion.jointgroup.RobotJointGroup;
import org.mechio.api.motion.jointgroup.RobotJointGroupConfig;
import org.mechio.impl.motion.jointgroup.RobotJointGroupConfigXMLReader;

/**
 *
 * @author Matthew Stevenson
 */
public class MechIOBindingConfigUtils {
    public static String CONF_JOINTGROUP_XML_PATH = "ccrkJointGroupXMLPath";
    
    public static String CONF_JOINTGROUP_PARAM_ID_FORMAT = "ccrkJointGroupParmIdFormatString";
    
    public static String MSGCONF_ROBOT_HOST = "ccrkRobotHostMsgConf";
    
    public static String SVCCONF_ROBOT_JOINTGROUP = "ccrkJointGroupServiceConfig";
    
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
        
        conf.addProperty(String.class, 
                CONF_JOINTGROUP_XML_PATH, "resources/jointGroup.xml");
        
        conf.addProperty(Configuration.class, MSGCONF_ROBOT_HOST, 
                ConnectionConfigUtils.buildDefaultConfig("127.0.0.1"));
        
        //Change these to load the RobotJoingGroupConfig a different way.
        addJointGroupConfig(conf, 
                File.class, RobotJointGroupConfigXMLReader.VERSION);  
        
        return conf;
    }
    
    private static void addJointGroupConfig(DefaultConfiguration<String> conf, 
            Class paramClass, VersionProperty configFormat){
        conf.addProperty(Configuration.class, 
                SVCCONF_ROBOT_JOINTGROUP, 
                ServiceConfigUtils.buildDefaultConfig(
                        JointGroup.class, RobotJointGroup.VERSION, null,
                        RobotJointGroupConfig.class, configFormat, null,
                        paramClass, null));
        
        conf.addProperty(String.class, CONF_JOINTGROUP_PARAM_ID_FORMAT, 
                "robot/%s/jointgroup/config/param/xml");
    }
    
    
    public static <T> T getValue(Class<T> clazz, String key){
        return defaultConfiguration().getPropertySource(clazz, key).getValue();
    }
    
    public static <T> void setValue(Class<T> clazz, String key, T val){
        defaultConfiguration().getPropertySetter(clazz, key).handleEvent(val);
    }
    
    public static <T> T get(Class<T> clazz, Configuration<String> conf, String key){
        return conf.getPropertySource(clazz, key).getValue();
    }
}
