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

package org.cogchar.nbui.render;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import org.apache.avro.generic.IndexedRecord;
import org.apache.qpid.client.AMQQueue;
import org.cogchar.avrogen.bind.robokind.RotationAxis;
import org.cogchar.bind.robokind.joint.BonyJoint;
import org.cogchar.bind.robokind.joint.BonyRobot;
import org.osgi.framework.BundleContext;
import org.robokind.api.motion.Robot;
import org.cogchar.bind.robokind.joint.BonyRobotUtils;
import org.cogchar.bind.robokind.joint.BonyRobotFactory;
import org.cogchar.render.opengl.bony.app.BonyVirtualCharApp;
import org.cogchar.render.opengl.bony.sys.BonyContext;
import org.cogchar.render.opengl.bony.model.HumanoidBoneConfig;
import org.cogchar.render.opengl.bony.model.HumanoidBoneDesc;
import org.cogchar.render.opengl.bony.model.DemoBonyWireframeRagdoll;
import org.cogchar.render.opengl.bony.state.FigureState;
import org.cogchar.render.opengl.bony.state.BoneState;
import org.osgi.framework.ServiceReference;
import org.robokind.api.messaging.PollingService;
import org.robokind.api.motion.Joint;
import org.robokind.avrogen.motion.MotionFrameRecord;
import org.robokind.impl.messaging.ConnectionManager;
import org.robokind.impl.messaging.JMSPollingService;
import org.robokind.impl.motion.messaging.RobotMoveServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 * @author Matthew Stevenson <www.robokind.org>
 */
public class RobokindBindingUtils {
    private final static Logger theLogger = LoggerFactory.getLogger(RobokindBindingUtils.class);
	private	static BonyRobot		myBonyRobot;
	private	static BundleContext	myBundleCtx;
	
	public static String	HARDCODED_ROBOT_ID = "COGCHAR_NB_ROBOT";
	
	public static Robot createAndRegisterRobot(BundleContext bundleCtx, File jointBindingConfigFile) throws Exception {
		String bindingFilePath = jointBindingConfigFile.getAbsolutePath();
		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& carr start, using file: " + bindingFilePath);
		//Create your Robot and register it
		myBonyRobot  = BonyRobotFactory.buildFromFile(jointBindingConfigFile);
        if(myBonyRobot == null){
            theLogger.warn("Error building Robot from file: " + bindingFilePath);
            return null;
        }
		BonyRobotUtils.registerRobokindRobot(myBonyRobot, bundleCtx);

		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& carr end");
        return myBonyRobot;
	}
    
	public static void connectToVirtualChar(final BonyContext bc) throws Exception {
		BonyVirtualCharApp app = bc.getApp();
		setupFigureState(bc);
		final BonyRobot br = getBonyRobot();
		br.registerMoveListener(new BonyRobot.MoveListener() {
			@Override public void notifyBonyRobotMoved(BonyRobot br) {
				propagateState(br, bc);
			}
		});
		/*
		BonyRagdollApp bra = (BonyRagdollApp) app;
		DemoBonyWireframeRagdoll dbwr = bra.getRagdoll();
		DanceDoer dd = new DanceDoer();
		dbwr.setDanceDoer(dd);
		 */
	}

	public static BonyRobot getBonyRobot() { 
		return myBonyRobot;
	}
    
	public static void setupFigureState(BonyContext bctx) { 
		BonyRobot br = getBonyRobot();
		FigureState fs = new FigureState();
		List<Joint> allJoints = br.getJointList();
		for (Joint cursorJoint : allJoints) {
			BonyJoint bj = (BonyJoint) cursorJoint;
			String boneName = bj.getBoneName();
			BoneState bs = fs.obtainBoneState(boneName);
		}
		bctx.setFigureState(fs);
	}
    
	public static void propagateState(BonyRobot br, BonyContext bc) { 
		FigureState fs = bc.getFigureState();
		for (BoneState bs : fs.getBoneStates()) {
			String boneName = bs.getBoneName();
			List<BonyJoint> bjList = BonyRobotUtils.findJointsForBoneName(br, boneName);
			for (BonyJoint bj : bjList) {
				RotationAxis axis = bj.getRotationAxis();
				if (axis != null) {
					float goalAngleRad = (float) bj.getGoalAngleRad();
					switch (axis) {
						case PITCH:
							bs.rot_X_pitch = goalAngleRad;
						break;
						case ROLL:
							bs.rot_Y_roll = goalAngleRad;
						break;
						case YAW:
							bs.rot_Z_yaw = goalAngleRad;
						break;
					}
				}
			}
		}
	}
    
    public static <T extends IndexedRecord> PollingService<T> 
             startPollingService(Class<T> clazz, 
                    BundleContext context, String connectionId, String destStr) 
                            throws JMSException, URISyntaxException, Exception{
         
        ServiceReference ref = ConnectionManager.retrieveConnectionReference(
                context, connectionId, null);
        if(ref == null){
            throw new NullPointerException();
        }
        Object obj = context.getService(ref);
        if(!(obj instanceof Connection)){
            theLogger.warn("Service not expected type.");
            return null;
        }
        Connection connection = (Connection)obj;
        Session session = 
                connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        Destination dest = new AMQQueue(destStr);
        MessageConsumer moveConsumer = session.createConsumer(dest);
        connection.start();
        PollingService pollService = new JMSPollingService
                (MotionFrameRecord.class, 
                MotionFrameRecord.SCHEMA$, 
                moveConsumer);
        return pollService;
    }
    
    protected static void startRobotServer(
            BundleContext context, Robot.Id id, String conStr, String destStr)
                throws Exception{
        RobotMoveServer server = new RobotMoveServer(context, id);
        PollingService<MotionFrameRecord> poll = startPollingService(
                MotionFrameRecord.class, context, conStr, destStr);
        server.setPollingService(poll);
        poll.start();
    }
		
}
