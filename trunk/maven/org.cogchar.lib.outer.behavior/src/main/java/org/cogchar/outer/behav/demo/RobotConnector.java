/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.outer.behav.demo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.cogchar.platform.util.ClassLoaderUtils;
import org.osgi.framework.BundleContext;
import org.jflux.impl.services.rk.lifecycle.utils.ManagedServiceFactory;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponentFactory;
import org.robokind.api.motion.Robot;
import org.robokind.impl.messaging.config.RKMessagingConfigUtils;
import org.robokind.integration.motion_speech.VisemeMotionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author matt
 */
public class RobotConnector {
    private final static Logger theLogger = LoggerFactory.getLogger(RobotConnector.class);
    public final static String ROBOT_CONNECTION_CONFIG_ID_SUFFIX = "RobotConnection";
    public final static String ANIM_PLAYER_ID_SUFFIX = "AnimPlayer";
    public final static String SPEECH_SERVICE_ID_SUFFIX = "SpeechService";
    public final static String ROBOT_CONNECTIONS_DELIMETER = ",";
    public final static String ROBOT_CONNECTION_PARTS_DELIMETER = ";";
    public final static String ROBOT_EXTRA_SPEECH_CHANS_DELIMETER = ":";
    
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
     * You may specify additional speech services for a robot by adding colon 
     * separated speech service prefixes after the ip address:
     * robot01;127.0.0.1;klauss:saul, robot02...
     * This will create speech services with ids robot01/klaussSpeechService 
     * and robot01/saulSpeechService.  Klauss will use destinations such as 
     * klaussRequest and klaussEvent instead of the default destinations of 
     * speechRequest and speechEvent.
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
        String speechDestPrefix = "speech";
        String connectConfigId = con.robotId + "/" + ROBOT_CONNECTION_CONFIG_ID_SUFFIX;
        ManagedServiceFactory fact = new OSGiComponentFactory(context);
        RKMessagingConfigUtils.registerConnectionConfig(
                connectConfigId, con.ipAddress, null, fact);
        AnimationConnector.connect(
                context, con.robotId + "/" + ANIM_PLAYER_ID_SUFFIX, 
                animDestPrefix, connectConfigId);
        SpeechConnector.connect(
                fact, con.robotId + "/" + SPEECH_SERVICE_ID_SUFFIX, 
                speechDestPrefix, connectConfigId);
        connectExtraSpeechChans(context, fact, con, connectConfigId);
    }
    
    private static void connectExtraSpeechChans(BundleContext context,
            ManagedServiceFactory fact, RobotConnection con, String connectConfigId){
        String[] chans = con.extraSpeechChannels;
        if(chans == null){
            return;
        }
        for(String chan : chans){
            String groupId = con.robotId + "/" + chan + SPEECH_SERVICE_ID_SUFFIX;
            SpeechConnector.connect(fact, groupId, chan, connectConfigId);
            Robot.Id rId = new Robot.Id(con.robotId.contains("robot") ? "myRobot" : "Avatar_ZenoR50");
            List<ClassLoader> loaders = ClassLoaderUtils.getFileResourceClassLoaders(context, ClassLoaderUtils.ALL_RESOURCE_CLASSLOADER_TYPES);
            URL visConfResURL = ClassLoaderUtils.findResourceURL("rk_conf/VisemeConf_AZR50_A12.json", loaders);
            InputStream visemeConfigStream = null;
            try{
                visemeConfigStream = visConfResURL.openStream();
            }catch(IOException ex){
                theLogger.error("Unable to load Viseme config as resource.", ex);
            }
            VisemeMotionUtils.startVisemeFrameSourceStreamGroup(fact, rId, groupId, visemeConfigStream);
        }
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
        if(parts.length < 2){
            return null;
        }
        String[] extraSpeechChans = null;
        if(parts.length > 2){
            String[] chans = parts[2].split(ROBOT_EXTRA_SPEECH_CHANS_DELIMETER);
            extraSpeechChans = new String[chans.length];
            for(int i=0; i<chans.length; i++){
                extraSpeechChans[i] = chans[i].trim();
            }
        }
        return new RobotConnection(parts[0].trim(), parts[1].trim(), extraSpeechChans);
    }
    
    public static class RobotConnection {
        public String robotId;
        public String ipAddress;
        public String[] extraSpeechChannels;

        public RobotConnection(
                String robotId, String ipAddress, String[] extraSpeechChannels) {
            this.robotId = robotId;
            this.ipAddress = ipAddress;
            this.extraSpeechChannels = extraSpeechChannels;
            if(this.extraSpeechChannels == null){
                this.extraSpeechChannels = new String[0];
            }
        }
    }
}
