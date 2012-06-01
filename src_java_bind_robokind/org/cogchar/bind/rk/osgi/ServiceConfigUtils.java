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

import org.jflux.api.core.config.Configuration;
import org.jflux.api.core.config.DefaultConfiguration;
import org.robokind.api.common.config.VersionProperty;
import org.robokind.api.common.osgi.lifecycle.ConfiguredServiceParams;

/**
 *
 * @author Matthew Stevenson
 */
public class ServiceConfigUtils {
    public static String CONF_SERVICE_CLASS = "confSvcServiceClass";
    public static String CONF_SERVICE_VERSION = "confSvcServiceVersion";
    public static String CONF_SERVICE_ID = "confSvcServiceId";
    public static String CONF_CONFIG_CLASS = "confSvcConfigClass";
    public static String CONF_CONFIG_READER_VERSION = "confSvcConfigReaderVersion";
    public static String CONF_CONFIG_ID = "confSvcConfigId";
    public static String CONF_PARAM_CLASS = "confSvcParamClass";
    public static String CONF_PARAM_ID = "confSvcParamId";
    
    public static <S,C,P> Configuration<String> buildDefaultConfig(
            Class<S> serviceClass, VersionProperty serviceType, String serviceId, 
            Class<C> configClass, VersionProperty configReaderType, String configId, 
            Class<P> paramClass, String paramId){
        if(serviceClass == null|| serviceType == null || configReaderType == null){
            throw new NullPointerException();
        }
        DefaultConfiguration<String> conf = new DefaultConfiguration<String>();
        
        conf.addProperty(Class.class, CONF_SERVICE_CLASS, serviceClass);
        conf.addProperty(VersionProperty.class, CONF_SERVICE_VERSION, serviceType);
        conf.addProperty(String.class, CONF_SERVICE_ID, serviceId);
        
        conf.addProperty(Class.class, CONF_CONFIG_CLASS, configClass);
        conf.addProperty(VersionProperty.class, CONF_CONFIG_READER_VERSION, configReaderType);
        conf.addProperty(String.class, CONF_CONFIG_ID, configId);
                
        conf.addProperty(Class.class, CONF_PARAM_CLASS, paramClass);
        conf.addProperty(String.class, CONF_PARAM_ID, paramId);
        
        return conf;
    }
    
    public static <S,C,P> ConfiguredServiceParams<S,C,P> buildParams(
            Configuration<String> conf, C configInst, P paramInst){        
        return new ConfiguredServiceParams<S, C, P>(
                get(Class.class, conf, CONF_SERVICE_CLASS),
                get(Class.class, conf, CONF_CONFIG_CLASS),
                get(Class.class, conf, CONF_PARAM_CLASS), 
                configInst, paramInst, 
                get(String.class, conf, CONF_PARAM_ID), 
                get(VersionProperty.class, conf, CONF_SERVICE_VERSION),
                get(VersionProperty.class, conf, CONF_CONFIG_READER_VERSION));
    }
    
    private static <T> T get(Class<T> clazz, Configuration<String> conf, String key){
        return conf.getPropertySource(clazz, key).getValue();
    }
}
