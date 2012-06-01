/*
 * Copyright 2012 The Cogchar Project (www.cogchar.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cogchar.bind.rk.osgi;

import javax.jms.Connection;
import org.jflux.api.core.Source;
import org.jflux.api.core.util.Configuration;
import org.jflux.api.core.util.DefaultConfiguration;
import org.jflux.api.core.util.DefaultTimestampSource;
import org.robokind.impl.messaging.utils.ConnectionManager;

/**
 *
 * @author Matthew Stevenson
 */
public class ConnectionConfigUtils {
    public static String CONF_BROKER_IP = "msgBrokerIp";
    public static String CONF_BROKER_PORT = "msgBrokerPort";
    public static String CONF_BROKER_USERNAME = "msgBrokerUser";
    public static String CONF_BROKER_PASSWORD = "msgBrokerPassword";
    public static String CONF_BROKER_CLIENT_NAME = "msgBrokerClientName";
    public static String CONF_BROKER_VIRTUAL_HOST = "msgBrokerVirtualHost";
    
    public static Configuration<String> buildDefaultConfig(){
        Source<Long> ts = new DefaultTimestampSource();
        DefaultConfiguration<Long,String> conf = 
                new DefaultConfiguration<Long, String>(ts);
        
        conf.addProperty(String.class, CONF_BROKER_IP, "127.0.0.1");
        conf.addProperty(String.class, CONF_BROKER_PORT, "5672");
        conf.addProperty(String.class, CONF_BROKER_USERNAME, "admin");
        conf.addProperty(String.class, CONF_BROKER_PASSWORD, "admin");
        conf.addProperty(String.class, CONF_BROKER_CLIENT_NAME, "client1");
        conf.addProperty(String.class, CONF_BROKER_VIRTUAL_HOST, "test");
        
        return conf;
    }
    
    public static Configuration<String> buildDefaultConfig(String ip){
        if(ip == null){
            throw new NullPointerException();
        }
        Source<Long> ts = new DefaultTimestampSource();
        Configuration<String> conf = buildDefaultConfig();
        if(ip != null){
            set(conf, CONF_BROKER_IP, ip);
        }        
        return conf;
    }
    
    public static Configuration<String> buildConfig(
            String ip, String port, String username, String password, 
            String clientName, String virtualHost){
        Configuration<String> conf = buildDefaultConfig(ip);
        if(port != null){
           set(conf, CONF_BROKER_PORT, port);
        }if(username != null){
           set(conf, CONF_BROKER_USERNAME, username);
        }if(password != null){
           set(conf, CONF_BROKER_PASSWORD, password);
        }if(clientName != null){
           set(conf, CONF_BROKER_CLIENT_NAME, clientName);
        }if(virtualHost != null){
           set(conf, CONF_BROKER_VIRTUAL_HOST, virtualHost);
        }
        return conf;
    }
    
    public static Connection createConnection(Configuration<String> config){
        String ip = get(config, CONF_BROKER_IP);
        String port = get(config, CONF_BROKER_PORT);
        String addr = "tcp://" + ip + ":" + port;
        return ConnectionManager.createConnection(
                get(config, CONF_BROKER_USERNAME),
                get(config, CONF_BROKER_PASSWORD),
                get(config, CONF_BROKER_CLIENT_NAME),
                get(config, CONF_BROKER_VIRTUAL_HOST),
                addr);
    }
    
    private static String get(Configuration<String> config, String key){
        return config.getPropertySource(String.class, key).getValue();
    }
    
    private static void set(Configuration<String> conf, String key, String val){
        conf.getPropertySetter(String.class, key).handleEvent(val);
    }
}
