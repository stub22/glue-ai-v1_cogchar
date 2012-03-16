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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Connection;
import org.apache.qpid.client.AMQQueue;

import org.robokind.impl.messaging.utils.ConnectionManager;
import org.cogchar.bind.rk.robot.config.BoneProjectionPosition;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.robokind.api.common.services.ServiceConnectionDirectory;
import org.robokind.api.motion.Robot;
import org.robokind.api.motion.Robot.RobotPositionHashMap;
import org.robokind.api.motion.Robot.RobotPositionMap;

import org.robokind.api.motion.utils.RobotFrameSource;
import org.robokind.api.motion.utils.RobotUtils;
import org.robokind.api.motion.jointgroup.JointGroup;
import org.robokind.api.motion.jointgroup.RobotJointGroup;

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
}
