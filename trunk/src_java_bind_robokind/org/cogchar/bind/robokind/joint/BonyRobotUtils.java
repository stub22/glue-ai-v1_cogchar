/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.bind.robokind.joint;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Connection;
import org.apache.qpid.client.AMQQueue;

import org.robokind.impl.messaging.ConnectionManager;
import org.cogchar.bind.robokind.joint.BoneRotationRange.BoneRotation;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.robokind.api.motion.Robot;
import org.robokind.api.motion.Robot.RobotPositionHashMap;
import org.robokind.api.motion.Robot.RobotPositionMap;
import org.robokind.api.motion.utils.RobotFrameSource;
import org.robokind.api.motion.utils.RobotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BonyRobotUtils {
	static Logger theLogger = LoggerFactory.getLogger(BonyRobotUtils.class);
	public static void registerRobotAndAttachBlender(Robot robot, BundleContext bundleCtx) throws Exception {
		robot.connect();
		ServiceRegistration reg = RobotUtils.registerRobot(bundleCtx, robot, null);
		if(reg == null){
			 throw new Exception("Error Registering Robot");
		}
        //Starts a blender
        ServiceRegistration[] blenderRegs = RobotUtils.startDefaultBlender(
                bundleCtx, robot.getRobotId(), RobotUtils.DEFAULT_BLENDER_INTERVAL);
		//create and register the MotionTargetFrameSource,
        RobotFrameSource robotFS = new RobotFrameSource(bundleCtx, robot.getRobotId());
        RobotUtils.registerFrameSource(bundleCtx, robot.getRobotId(), robotFS);
		RobotPositionMap positions = new RobotPositionHashMap();
		//... add positions
        
        //moves to the positions over 1.5 seconds
        robotFS.move(positions, 1500);
	}
    public static Map<String,List<BoneRotation>> getGoalAnglesAsRotations(BonyRobot robot){
        Map<String,List<BoneRotation>> rotMap = new HashMap();
        List<BonyJoint> joints = new ArrayList(robot.getJointList());
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
    public static void createAndRegisterServer(
            BundleContext bundleCtx, Robot.Id robotId){
        Connection connection = ConnectionManager.createConnection(
                "admin", "admin", "client1", "test", "tcp://127.0.0.1:5672");
        if(connection == null){
            return;
        }
        try{
            connection.start();
        }catch(JMSException ex){
            theLogger.warn("Could not start connection.", ex);
            return;
        }
        String queue = 
                "test.RobotMoveQueue; {create: always, node: {type: queue}}";
        Session session;
        Destination destination;
        try{
            session = connection.createSession(
                    false, Session.CLIENT_ACKNOWLEDGE);
            destination = new AMQQueue(queue);
        }catch(URISyntaxException ex){
            theLogger.warn("Error creating destination.", ex);
            return;
        }catch(JMSException ex){
            theLogger.warn("Error creating session.", ex);
            return;            
        }
        
        try{
            //startRobotServer(bundleCtx, robotId, session, destination);
        }catch(Exception ex){
            theLogger.warn("Error starting Robot Server.", ex);
        }
    }
    /*private static JMSRobotServer startRobotServer(
            BundleContext context, Robot.Id id,
            Session session, Destination destination) throws Exception{
        JMSRobotServer server = new JMSRobotServer(session, destination);
        RobotFrameSource frameSource = new RobotFrameSource(context, id);
        MoveFrameListener moveHandler = new MoveFrameListener();
        ServiceRegistration reg = 
                RobotUtils.registerFrameSource(context, id, frameSource);
        moveHandler.setRobotFrameSource(frameSource);
        server.setMoveHandler(moveHandler);
        server.connect();
        return server;
    }	*/
		
}
