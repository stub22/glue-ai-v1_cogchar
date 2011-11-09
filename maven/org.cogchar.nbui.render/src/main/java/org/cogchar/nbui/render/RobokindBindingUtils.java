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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import org.apache.qpid.client.AMQQueue;
import org.cogchar.bind.robokind.joint.BoneRotationRange;
import org.cogchar.bind.robokind.joint.BoneRotationRange.BoneRotation;
import org.cogchar.bind.robokind.joint.BonyJoint;
import org.cogchar.bind.robokind.joint.BonyRobot;
import org.osgi.framework.BundleContext;
import org.robokind.api.motion.Robot;
import org.cogchar.bind.robokind.joint.BonyRobotUtils;
import org.cogchar.bind.robokind.joint.BonyRobotFactory;
import org.cogchar.render.opengl.bony.app.BonyVirtualCharApp;
import org.cogchar.render.opengl.bony.sys.BonyContext;
import org.cogchar.render.opengl.bony.state.FigureState;
import org.cogchar.render.opengl.bony.state.BoneState;
import org.osgi.framework.ServiceRegistration;
import org.robokind.api.motion.Joint;
import org.robokind.api.motion.protocol.RobotFrameSource;
import org.robokind.api.motion.utils.RobotUtils;
import org.robokind.impl.messaging.ConnectionManager;
import org.robokind.impl.motion.messaging.JMSRobotServer;
import org.robokind.impl.motion.messaging.MoveFrameListener;
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
    
    public static void createAndRegisterServer(
            BundleContext bundleCtx, Robot.Id robotId){
        Connection connection = ConnectionManager.createConnection(
                "admin", "admin", "client1", "test", "tcp://127.0.0.1:5672");
        try{
            connection.start();
        }catch(JMSException ex){
            theLogger.warn("Could not start connection.", ex);
        }
        if(connection != null){
            String connId = "connection1";
            String destId = "destination1";
            ServiceRegistration connReg =  ConnectionManager.registerConnection(
                    bundleCtx, connId, connection, null);
            String queue = "test.RobotMoveQueue; {create: always, node: {type: queue}}";
            Destination dest;
            try{
                dest = new AMQQueue(queue);
            }catch(URISyntaxException ex){
                theLogger.warn("Error creating destination.", ex);
                return;
            }
            ServiceRegistration destReg = ConnectionManager.registerDestination(
                    bundleCtx, destId, dest, null);
            try{
                startRobotServer(bundleCtx, robotId, 
                        connId, null, destId, null);
            }catch(Exception ex){
                theLogger.warn("Error starting Robot Server.", ex);
            }
        }
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
            for(BoneRotationRange range : bj.getBoneRotationRanges()){
                String name = range.getBoneName();
                fs.obtainBoneState(name);
            }
		}
		bctx.setFigureState(fs);
	}
    
	public static void propagateState(BonyRobot br, BonyContext bc) { 
		FigureState fs = bc.getFigureState();
        applyAllRotations(fs, getRotations(br));
	}
    
    private static void applyAllRotations(FigureState fs, Map<String,List<BoneRotation>> rotMap){
        for(Entry<String,List<BoneRotation>> e : rotMap.entrySet()){
            BoneState bs = fs.getBoneState(e.getKey());
            if(bs == null){
                continue;
            }
            applyRotations(bs, e.getValue());
        }
    }
    
    private static void applyRotations(BoneState bs, List<BoneRotation> rots){
        for(BoneRotation rot : rots){
            float rads = (float)rot.getAngleRadians();
            switch(rot.getRotationAxis()){
                case PITCH: bs.rot_X_pitch = rads; break;
                case ROLL:  bs.rot_Y_roll = rads;  break;
                case YAW:   bs.rot_Z_yaw = rads;   break;
            }
        }
    }
    
    private static Map<String,List<BoneRotation>> getRotations(BonyRobot robot){
        Map<String,List<BoneRotation>> rotMap = new HashMap();
        List<BonyJoint> joints = (List)robot.getJointList();
        for(BonyJoint j : joints){
            for(BoneRotation rot : j.getGoalAngleRad()){
                String bone = rot.getBoneName();
                List<BoneRotation> rots = rotMap.get(bone);
                if(rots == null){
                    rots = new ArrayList<BoneRotation>();
                    rotMap.put(bone, rots);
                }
                rots.add(rot);
            }
        }
        return rotMap;
    }
    
    public static void setInitialBoneRotations(FigureState fs, BonyRobot robot){
        applyAllRotations(fs, getInitialRotationMap(robot));
    }
    
    private static Map<String,List<BoneRotation>> getInitialRotationMap(BonyRobot robot){
        Map<String,List<BoneRotation>> rotMap = new HashMap();
        List<BoneRotation> initRots = robot.getInitialBoneRotations();
        if(initRots == null){
            return rotMap;
        }
        for(BoneRotation rot : initRots){
            String bone = rot.getBoneName();
            List<BoneRotation> rots = rotMap.get(bone);
            if(rots == null){
                rots = new ArrayList<BoneRotation>();
                rotMap.put(bone, rots);
            }
            rots.add(rot);
        }
        return rotMap;
    }
    
    
    private static JMSRobotServer startRobotServer(
            BundleContext context, Robot.Id id, String conId, String conFilter, 
            String destId, String destFilter)
                throws Exception{
        JMSRobotServer server = new JMSRobotServer(
                context, conId, conFilter, destId, destFilter);
        RobotFrameSource frameSource = new RobotFrameSource(context, id);
        MoveFrameListener moveHandler = new MoveFrameListener();
        ServiceRegistration reg = 
                RobotUtils.registerFrameSource(context, id, frameSource);
        moveHandler.setRobotFrameSource(frameSource);
        server.setMoveHandler(moveHandler);
        server.connect();
        return server;
    }		
}
