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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
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

import org.cogchar.platform.util.ClassLoaderUtils;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class RobotServiceFuncs {
	static Logger theLogger = LoggerFactory.getLogger(RobotServiceFuncs.class);
	
	// A static list of all the Joint Group Lifecycles registered via launchJointGroupLifecycle
	// We'll need this to dispose 'em all on "mode" change
	private static List<OSGiComponent> startedJointGroupLifecycles = new ArrayList<OSGiComponent>();
	// A static list of all the Joint Group Configs registered via launchJointGroupConfig
	// We'll need this to dispose 'em all on "mode" change
	private static List<OSGiComponent> startedJointGroupConfigs = new ArrayList<OSGiComponent>();
	
	
    public static <Param> void startJointGroup(
            BundleContext bundleCtx, Robot robot, 
            Param jointGroupConfigParam, Class<Param> paramClass){
		Robot.Id robotId = robot.getRobotId();
		startJointGroup(bundleCtx, robotId, jointGroupConfigParam, paramClass);
	}
    public static <Param> void startJointGroup(
            BundleContext bundleCtx, Robot.Id robotId, 
            Param jointGroupConfigParam, Class<Param> paramClass){
        String paramId = formatJointGroupParamId(robotId.getRobtIdString());
        Configuration<String> jgSvcConf = 
                getValue(Configuration.class, SVCCONF_ROBOT_JOINTGROUP);
        launchJointGroupLifecycle(bundleCtx, robotId, paramClass, paramId, jgSvcConf);
        launchJointGroupConfig(bundleCtx, jointGroupConfigParam, paramClass, paramId, jgSvcConf);
    }
    
    private static String formatJointGroupParamId(String robotId){
        String format = getValue(String.class, CONF_JOINTGROUP_PARAM_ID_FORMAT);
        return String.format(format, robotId);
    }
    
    private static OSGiComponent launchJointGroupLifecycle(
            BundleContext bundleCtx, Robot.Id robotId,
            Class paramClass, String configParamId, 
            Configuration<String> jgSvcConf){
        RobotJointGroupLifecycle lifecycle =
                new RobotJointGroupLifecycle(
                        robotId, 
                        paramClass, 
                        configParamId, 
                        get(VersionProperty.class, 
                                jgSvcConf, CONF_CONFIG_READER_VERSION));
        OSGiComponent jointGroupComp = new OSGiComponent(bundleCtx, lifecycle);
        jointGroupComp.start();
		startedJointGroupLifecycles.add(jointGroupComp);
        return jointGroupComp;
    }
    
    private static <Param> OSGiComponent launchJointGroupConfig(
            BundleContext context, Param jointGroupConfigParam, 
            Class<Param> paramClass, String configParamId, 
            Configuration<String> jgSvcConf){
        Properties props = new Properties();
        props.put(Constants.CONFIG_PARAM_ID, configParamId);
        props.put(Constants.CONFIG_FORMAT_VERSION, 
                get(VersionProperty.class, jgSvcConf, 
                        CONF_CONFIG_READER_VERSION).toString());
        
        ServiceLifecycleProvider lifecycle = new SimpleLifecycle(
                jointGroupConfigParam, paramClass, props);
        OSGiComponent paramComp = new OSGiComponent(context, lifecycle);
        paramComp.start();
		startedJointGroupConfigs.add(paramComp);
        return paramComp;
    }
	
	public static void clearJointGroups() {
		for (OSGiComponent aJointGroupConfig : startedJointGroupConfigs) {
			aJointGroupConfig.dispose();
		}
		startedJointGroupConfigs.clear();
		for (OSGiComponent aJointGroupLifecycle : startedJointGroupLifecycles) {
			aJointGroupLifecycle.dispose();
		}
		startedJointGroupLifecycles.clear();
	}
	public static File copyJointGroupFile(String tgtFilePath, String jgFullPath, List<ClassLoader> possibleCLs) {
		ClassLoader cl = ClassLoaderUtils.findResourceClassLoader(jgFullPath, possibleCLs);
		if (cl != null) {
			return copyJointGroupFile(tgtFilePath, jgFullPath, cl);
		} else {
			return null;
		}
	}
	public static File copyJointGroupFile(String tgtFilePath, String jgFullPath, ClassLoader jgClassLoader) {
		File outputFile = null;
		try {
			InputStream stream = jgClassLoader.getResourceAsStream(jgFullPath);			
			outputFile = new File(tgtFilePath);
			OutputStream out = new FileOutputStream(outputFile);
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = stream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			stream.close();
			out.flush();
			out.close();
		} catch (Exception e) {
			theLogger.warn("Exception trying to load jointGroup from resource into temp file: ", e);
		}
		return outputFile;
	}	
}
