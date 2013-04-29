/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.app.puma.net.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.robokind.api.common.lifecycle.utils.ManagedServiceFactory;
import org.robokind.api.common.osgi.lifecycle.OSGiComponentFactory;
import org.robokind.impl.messaging.config.RKMessagingConfigUtils;
import org.cogchar.app.puma.net.demo.AnimationConnector;
import org.cogchar.app.puma.net.demo.SpeechConnector;

/**
 *
 * @author matt
 */
public class RobotConnector {
    public final static String ROBOT_CONNECTION_CONFIG_ID_SUFFIX = "RobotConnection";
    public final static String ANIM_PLAYER_ID_SUFFIX = "AnimPlayer";
    public final static String SPEECH_SERVICE_ID_SUFFIX = "SpeechService";
    public final static String ROBOT_CONNECTIONS_DELIMETER = ",";
    public final static String ROBOT_CONNECTION_PARTS_DELIMETER = ";";
    
    /**
     * Reads the system environment variable with the given key, parses the 
     * variable into connection information, and connects to animation players 
     * and speech servers for the given robots.
     * The variable value is expected to be in the format of:
     * robotName;ipaddress,robotName;ipaddress,...
     * ex: robot01;127.0.0.1,robot02;192.168.0.100,robot03;192.168.0.103
     * For each robot it creates and registers a qpid connection config with an 
     * id of robotName/RobotConnection, 
     * a speech service with an id of robotName/SpeechService, 
     * and animation player with id robotName/AnimPlayer
     * @param context
     * @param envVarKey 
     */
    public static void connectRobotsFromSysEnv(BundleContext context, String envVarKey){
        List<RobotConnection> connections = getRobotConnectionsFromSysEnv(envVarKey);
        if(connections == null || connections.isEmpty()){
            return;
        }
        connectRobots(context, connections);
    }
    
    public static void connectRobots(BundleContext context, List<RobotConnection> robots){
        for(RobotConnection rc : robots){
            if(rc == null){
                continue;
            }
            connectRobot(context, rc);
        }
    }
    
    public static void connectRobot(BundleContext context, RobotConnection con){
        if(context == null || con == null){
            throw new NullPointerException();
        }
        String animDestPrefix = "";
        String speechDestPrefix = "";
        String connectConfigId = con.robotId + "/" + ROBOT_CONNECTION_CONFIG_ID_SUFFIX;
        ManagedServiceFactory fact = new OSGiComponentFactory(context);
        RKMessagingConfigUtils.registerConnectionConfig(
                connectConfigId, con.ipAddress, null, fact);
        AnimationConnector.connect(
                context, con.robotId + "/" + ANIM_PLAYER_ID_SUFFIX, 
                animDestPrefix, connectConfigId);
        SpeechConnector.connect(
                context, con.robotId + "/" + SPEECH_SERVICE_ID_SUFFIX, 
                speechDestPrefix, connectConfigId);
    }
    
    private static List<RobotConnection> getRobotConnectionsFromSysEnv(String envVarKey){
        String envVar = System.getProperty(envVarKey, java.lang.System.getenv(envVarKey));
        if(envVar == null || envVar.isEmpty()){
            return Collections.EMPTY_LIST;
        }
        return parseConnections(envVar);
    }
    
    private static List<RobotConnection> parseConnections(String conStr){
        String[] parts = conStr.split(ROBOT_CONNECTIONS_DELIMETER);
        if(parts == null || parts.length == 0){
            return Collections.EMPTY_LIST;
        }
        List<RobotConnection> cons = new ArrayList<RobotConnection>(parts.length);
        for(String s : parts){
            RobotConnection con = parseConnection(s.trim());
            if(con != null){
                cons.add(con);
            }
        }
        return cons;
    }
    
    private static RobotConnection parseConnection(String conStr){
        String[] parts = conStr.split(ROBOT_CONNECTION_PARTS_DELIMETER);
        if(parts.length != 2){
            return null;
        }
        return new RobotConnection(parts[0].trim(), parts[1].trim());
    }
    
    public static class RobotConnection {
        public String robotId;
        public String ipAddress;

        public RobotConnection(String robotId, String ipAddress) {
            this.robotId = robotId;
            this.ipAddress = ipAddress;
        }
    }
}