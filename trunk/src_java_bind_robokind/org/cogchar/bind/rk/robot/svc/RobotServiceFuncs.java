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
import org.cogchar.bind.rk.robot.model.ModelBoneRotation;
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
	/*
	public static JMSMotionFrameAsyncReceiver createAndRegisterFrameReceiver(
			BundleContext bundleCtx, Robot.Id robotId) {
		
		Connection connection = ConnectionManager.createConnection(
				"admin", "admin", "client1", "test", "tcp://127.0.0.1:5672");
		JMSMotionFrameAsyncReceiver receiver = null;
		try {
			connection.start();
		} catch (JMSException ex) {
			theLogger.warn("Could not start connection.", ex);
			return null;
		}
		if (connection == null) {
			return null;
		}
		String queue = "test.RobotMoveQueue; {create: always, node: {type: queue}}";
		Session session;
		Destination destination;
		try {
			session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
			destination = new AMQQueue(queue);
		} catch (URISyntaxException ex) {
			theLogger.warn("Error creating destination.", ex);
			return null;
		} catch (JMSException ex) {
			theLogger.warn("Error creating session.", ex);
			return null;
		}

		try {
			receiver = startRobotFrameReceiver(bundleCtx, robotId, session, destination);
		} catch (Throwable t) {
			theLogger.warn("Error starting Robot Server.", t);
		}
		return receiver;
	}

	private static JMSMotionFrameAsyncReceiver startRobotFrameReceiver(BundleContext context, 
				Robot.Id id, Session session, Destination destination) throws Throwable {
		
		JMSMotionFrameAsyncReceiver receiver = new JMSMotionFrameAsyncReceiver(session, destination);
		// RobotFrameSource is now an interface
	//	RobotFrameSource frameSource = new RobotFrameSource(context, id); 
		// Was MoveFrameListener before - same semantics?
		TargetFrameListener moveHandler = new TargetFrameListener();
	//	ServiceRegistration reg =	RobotUtils.registerFrameSource(context, id, frameSource);
		// MoveFrameListener wants PositionTargetFrameSource, which is a sibling of RobotFrameSuource.
//		moveHandler.setRobotFrameSource(frameSource);
		receiver.addMessageListener(moveHandler);
		receiver.start();
		return receiver;
	}
	*/
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
