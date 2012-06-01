/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.bind.rk.robot.svc;

import org.robokind.api.common.config.VersionProperty;
import org.jflux.api.core.config.Configuration;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.robokind.api.common.lifecycle.ServiceLifecycleProvider;
import org.robokind.api.common.lifecycle.utils.SimpleLifecycle;
import org.robokind.api.common.osgi.lifecycle.OSGiComponent;
import org.robokind.api.common.services.Constants;
import org.robokind.api.motion.Robot;


import org.robokind.api.motion.lifecycle.RobotJointGroupLifecycle;
/*
import org.robokind.impl.motion.messaging.JMSMotionFrameAsyncReceiver;
import org.robokind.impl.motion.messaging.TargetFrameListener;
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.cogchar.bind.rk.osgi.RobokindBindingConfigUtils.*;
import static org.cogchar.bind.rk.osgi.ServiceConfigUtils.*;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class RobotServiceFuncs {
	static Logger theLogger = LoggerFactory.getLogger(RobotServiceFuncs.class);
	
	
    public static <Param> void startJointGroup(
            BundleContext bundleCtx, Robot robot, Param jointGroupConfigParam){
		Robot.Id robotId = robot.getRobotId();
		startJointGroup(bundleCtx, robotId, jointGroupConfigParam);
	}
    public static <Param> void startJointGroup(BundleContext bundleCtx, 
            Robot.Id robotId, Param jointGroupConfigParam){
        String paramId = formatJointGroupParamId(robotId.getRobtIdString());
        Configuration<String> jgSvcConf = 
                getValue(Configuration.class, SVCCONF_ROBOT_JOINTGROUP);
        launchJointGroupLifecycle(bundleCtx, robotId, paramId, jgSvcConf);
        launchJointGroupConfig(bundleCtx, jointGroupConfigParam, paramId, jgSvcConf);
    }
    
    private static String formatJointGroupParamId(String robotId){
        String format = getValue(String.class, CONF_JOINTGROUP_PARAM_ID_FORMAT);
        return String.format(format, robotId);
    }
    
    private static OSGiComponent launchJointGroupLifecycle(
            BundleContext bundleCtx, Robot.Id robotId, 
            String configFileId, Configuration<String> jgSvcConf){
        RobotJointGroupLifecycle lifecycle =
                new RobotJointGroupLifecycle(
                        robotId, 
                        get(Class.class, jgSvcConf, CONF_PARAM_CLASS), 
                        configFileId, 
                        get(VersionProperty.class, 
                                jgSvcConf, CONF_CONFIG_READER_VERSION));
        OSGiComponent jointGroupComp = new OSGiComponent(bundleCtx, lifecycle);
        jointGroupComp.start();
        return jointGroupComp;
    }
    
    private static <Param> OSGiComponent launchJointGroupConfig(
            BundleContext context, Param jointGroupConfigParam, 
            String configFileId, Configuration<String> jgSvcConf){
        Properties props = new Properties();
        props.put(Constants.CONFIG_PARAM_ID, configFileId);
        props.put(Constants.CONFIG_FORMAT_VERSION, 
                get(VersionProperty.class, jgSvcConf, 
                        CONF_CONFIG_READER_VERSION).toString());
        
        ServiceLifecycleProvider lifecycle = new SimpleLifecycle(
                jointGroupConfigParam, 
                get(Class.class, jgSvcConf, CONF_PARAM_CLASS), 
                props);
        OSGiComponent paramComp = new OSGiComponent(context, lifecycle);
        paramComp.start();
        return paramComp;
    }
}
