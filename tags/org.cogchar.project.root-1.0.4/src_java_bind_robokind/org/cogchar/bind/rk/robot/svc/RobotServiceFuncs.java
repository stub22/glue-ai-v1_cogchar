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
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.robokind.api.common.lifecycle.ServiceLifecycleProvider;
import org.robokind.api.common.lifecycle.utils.SimpleLifecycle;
import org.robokind.api.common.osgi.lifecycle.OSGiComponent;
import org.robokind.api.common.services.Constants;
import org.robokind.api.common.services.ServiceConnectionDirectory;
import org.robokind.api.motion.Robot;

import org.robokind.api.motion.jointgroup.JointGroup;
import org.robokind.api.motion.jointgroup.RobotJointGroup;

import org.robokind.api.motion.lifecycle.RobotJointGroupLifecycle;
import org.robokind.impl.motion.jointgroup.RobotJointGroupConfigXMLReader;
/*
import org.robokind.impl.motion.messaging.JMSMotionFrameAsyncReceiver;
import org.robokind.impl.motion.messaging.TargetFrameListener;
 * 
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class RobotServiceFuncs {
	static Logger theLogger = LoggerFactory.getLogger(RobotServiceFuncs.class);
	
	public static JointGroup registerJointGroup(BundleContext bundleCtx, File jointGroupConfigXML_file) throws Throwable {
		JointGroup group = ServiceConnectionDirectory.buildService(
				bundleCtx,
				RobotJointGroup.VERSION,
				RobotJointGroupConfigXMLReader.VERSION,
				jointGroupConfigXML_file,
				File.class,
				JointGroup.class);
		if (group != null) {
			bundleCtx.registerService(JointGroup.class.getName(), group, new Properties());
			theLogger.warn("JointGroup Registered.");
		}
		return group;
	}
    public static void startJointGroup(BundleContext bundleCtx, Robot robot, File jointGroupConfigXML_file){
		Robot.Id robotId = robot.getRobotId();
		startJointGroup(bundleCtx, robotId, jointGroupConfigXML_file);
	}
    public static void startJointGroup(BundleContext bundleCtx, 
            Robot.Id robotId, File jointGroupConfigXML_file){
        String paramId = "robot/" + robotId + "/jointgroup/config/param/xml";
        launchJointGroupLifecycle(bundleCtx, robotId, paramId);
        launchJointGroupConfig(bundleCtx, jointGroupConfigXML_file, paramId);
    }
    
    protected static OSGiComponent launchJointGroupLifecycle(
            BundleContext bundleCtx, Robot.Id robotId, String configFileId){
        RobotJointGroupLifecycle<File> lifecycle =
                new RobotJointGroupLifecycle<File>(robotId, File.class, 
                        configFileId, RobotJointGroupConfigXMLReader.VERSION);
        OSGiComponent jointGroupComp = new OSGiComponent(bundleCtx, lifecycle);
        jointGroupComp.start();
        return null;
    }
    
    protected static OSGiComponent launchJointGroupConfig(BundleContext context, 
            File jointGroupConfigXML, String configFileId){
        Properties props = new Properties();
        props.put(Constants.CONFIG_PARAM_ID, configFileId);
        props.put(Constants.CONFIG_FORMAT_VERSION, 
                RobotJointGroupConfigXMLReader.VERSION.toString());
        ServiceLifecycleProvider lifecycle = new SimpleLifecycle(
                        jointGroupConfigXML, File.class, props);
        OSGiComponent paramComp = new OSGiComponent(context, lifecycle);
        paramComp.start();
        return paramComp;
    }
}
