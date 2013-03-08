/*
 * Copyright 2013 The Cogchar Project (www.cogchar.org).
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
package org.cogchar.bind.rk.behavior;

import java.util.Map;
import java.util.Properties;
import org.osgi.framework.BundleContext;
import org.robokind.api.common.lifecycle.ManagedService;
import org.robokind.api.common.lifecycle.ServiceLifecycleProvider;
import org.robokind.api.common.lifecycle.utils.ManagedServiceFactory;
import org.robokind.api.common.osgi.ServiceClassListener;
import org.robokind.api.common.osgi.lifecycle.OSGiComponentFactory;

/**
 *
 * @author Matthew Stevenson <www.robokind.org>
 */
public class ServiceChannelExtender extends ServiceClassListener<ChannelBindingConfig>{
    private ManagedServiceFactory myManagerFactory;
    private Map<ChannelBindingConfig,ManagedService> myManagerMap;
    private Properties myBindingRegistrationProperties;
    
    public ServiceChannelExtender(BundleContext context, 
            String bindingConfigOSGiFilter, Properties bindingRegistrationProps){
        super(ChannelBindingConfig.class, context, bindingConfigOSGiFilter);
        myManagerFactory = new OSGiComponentFactory(context);
    }

    @Override
    protected void addService(ChannelBindingConfig t) {
        if(t == null || myManagerMap.containsKey(t)){
            return;
        }
        ServiceLifecycleProvider provider = new ChannelBindingLifecycle(t);
        ManagedService ms = myManagerFactory.createService(provider, myBindingRegistrationProperties);
        ms.start();
        myManagerMap.put(t, ms);
    }

    @Override
    protected void removeService(ChannelBindingConfig t) {
        if(t == null){
            return;
        }
        ManagedService ms = myManagerMap.remove(t);
        if(ms == null){
            return;
        }
        ms.dispose();
    }
}
