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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.osgi.framework.BundleContext;
import org.jflux.impl.services.rk.lifecycle.DependencyDescriptor;
import org.jflux.impl.services.rk.lifecycle.ManagedService;
import org.jflux.impl.services.rk.lifecycle.ServiceLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.utils.DescriptorBuilder;
import org.jflux.impl.services.rk.lifecycle.utils.DescriptorListBuilder;
import org.jflux.impl.services.rk.lifecycle.utils.ManagedServiceFactory;
import org.jflux.impl.services.rk.osgi.ServiceClassListener;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponentFactory;

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
        myManagerMap = new HashMap();
        myBindingRegistrationProperties = bindingRegistrationProps;
    }

    @Override
    protected void addService(ChannelBindingConfig cbc) {
        //Called whenever a ChannelBindingConfig matching the OSGi filter is registered
        if(cbc == null || myManagerMap.containsKey(cbc)){
            return;
        }
		List<DependencyDescriptor> depDescList = makeDepDescList(cbc);
        ServiceLifecycleProvider provider = new ChannelBindingLifecycle(depDescList, cbc);
        ManagedService ms = myManagerFactory.createService(provider, myBindingRegistrationProperties);
        ms.start();
        myManagerMap.put(cbc, ms);
    }
	
	private List<DependencyDescriptor> makeDepDescList(ChannelBindingConfig cbc) { 
		/* was in lifecycleconstructor first line:
		 *   super(new DescriptorListBuilder()
                //The name "service" is used only within the lifecycle
                .dependency("service", conf.getChannelType().getServiceClass()).with(conf.getOSGiFilterString())
                .getDescriptors());
		 */
		
		DescriptorListBuilder firstListBuilder = new DescriptorListBuilder();
                //The name "service" is used only within the lifecycle
		ChannelBindingConfig.ChannelType chanType = cbc.getChannelType();
		Class depClazz = chanType.getServiceClass();
		String osgiFilter = cbc.getOSGiFilterString();
		 // This serviceName is used only within the lifecycle
		String serviceName = ChannelBindingLifecycle.SERVICE_DEP_KEY; // "service";
		DescriptorBuilder firstDBuilder = firstListBuilder.dependency(serviceName, depClazz);
		DescriptorBuilder secondDBuilder = firstDBuilder.with(osgiFilter);
		List<DependencyDescriptor> finishedList = secondDBuilder.getDescriptors();
		return finishedList;
	}


    @Override
    protected void removeService(ChannelBindingConfig cbc) {
        //Called whenever a ChannelBindingConfig is unregistered
        if(cbc == null){
            return;
        }
        ManagedService ms = myManagerMap.remove(cbc);
        if(ms == null){
            return;
        }
        ms.dispose();
    }
}
