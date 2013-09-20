/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.lifter.behavior;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cogchar.api.scene.ActionCallbackMap;
import org.jflux.api.registry.Descriptor;
import org.jflux.api.registry.basic.BasicDescriptor;
import org.jflux.api.service.DefaultRegistrationStrategy;
import org.jflux.api.service.RegistrationStrategy;
import org.jflux.api.service.ServiceDependency;
import org.jflux.api.service.ServiceManager;
import org.jflux.api.service.binding.ServiceBinding;
import org.jflux.impl.registry.OSGiRegistry;
//import org.robokind.api.messaging.Constants;
//import org.robokind.api.messaging.services.ServiceCommand;
//import org.robokind.api.messaging.services.ServiceError;

/**
 *
 * @author Jason Randolph Eads <jeads362@gmail.com>
 */
public class BehaviorControlServiceManager {

    public static void init(OSGiRegistry anOSGiRegistry) {

//        ServiceLifecycle lifecycle = new BehaviorControlLifecycle();
//        new OSGiComponent(context, lifecycle).start();
//        new OSGiComponent

//                ServiceLifecycle lifecycle;
//        Map<String, ServiceBinding> bindings =
//                new HashMap<String, ServiceBinding>();
//        
//        ServiceDependency sceneActionCallbackMap = 
//                new ServiceDependency(
//                "sceneActionCallbackMap",
//                ActionCallbackMap.class.getName(),
//                ServiceDependency.Cardinality.MANDATORY_UNARY,
//                ServiceDependency.UpdateStrategy.STATIC, null)

//        bindings.put( BehaviorControlLifecycle , null)

//        if(serviceManagerSpec == null ||
//                myManagedServicesMap.containsKey(serviceManagerSpec)) {
//            return;
//        }

//        try {
//            Class lifecycleClass =
//                    Class.forName(serviceManagerSpec.getLifecycleClassName());
//            lifecycle = (ServiceLifecycle)lifecycleClass.newInstance();
//        } catch(Exception e) {
//            theLogger.log(
//                    Level.SEVERE, "Unable to instantiate class: {0}",
//                    serviceManagerSpec.getLifecycleClassName());
//            return;
//        }
//        
//        for(Map.Entry<String, ServiceBindingSpec> specItem:
//                serviceManagerSpec.getServiceBindings().entrySet()) {
//            ServiceBindingSpec spec = specItem.getValue();
//            ServiceDependencySpec depSpec = spec.getServiceDependency();
//            ServiceDependency dep =
//                    new ServiceDependency(
//                    depSpec.getName(), depSpec.getClassName(),
//                    depSpec.getCardinality(), depSpec.getUpdateStrategy(),
//                    depSpec.getProperties());
//            ServiceBinding binding =
//                    new ServiceBinding(
//                    dep, spec.getDescriptor(), spec.getBindingStrategy());
//            bindings.put(specItem.getKey(), binding);
//        }
//        
//        DefaultRegistrationStrategySpec stratSpec =
//                serviceManagerSpec.getServiceRegistration();
//        RegistrationStrategy strat =
//                new DefaultRegistrationStrategy(
//                stratSpec.getClassNames(),
//                stratSpec.getRegistrationProperties());
//        
//        ServiceManager serviceManager =
//                new ServiceManager(lifecycle, bindings, strat, null);
//        // Start the service manager
//        serviceManager.start(myRegistry);
//        // Store the service manager so it may be removed later.
//        myManagedServicesMap.put(serviceManagerSpec, serviceManager);



        /**
         * ***********************************************
         */

//        ServiceDependency theSceneServiceDependancy =
//                (ServiceDependency) lifecycle.getDependencySpecs().get(0);
//
//        Map propertyMap = new HashMap<String, String>(1);
//        propertyMap.put("sceneActionCallbackMap", "BehaviorControlSceneActionCallbackMap");
//
//        BasicDescriptor descriptor = new BasicDescriptor(
//                ActionCallbackMap.class.getName(),
//                propertyMap);
//
//        ServiceBinding sceneActionCallbackMapBinding =
//                new ServiceBinding(
//                theSceneServiceDependancy,
//                descriptor,
//                ServiceBinding.BindingStrategy.EAGER,
//                ServiceDependency.UpdateStrategy.STATIC);
//
//        ServiceDependency theAdminServiceDependancy =
//                (ServiceDependency) lifecycle.getDependencySpecs().get(1);
//
//        ServiceBinding adminActionCallbackMapBinding =
//                new ServiceBinding(
//                theAdminServiceDependancy,
//                null,
//                ServiceBinding.BindingStrategy.EAGER,
//                ServiceDependency.UpdateStrategy.STATIC);

        /**
         * ***********************************************
         */
        
        
        
        
        
        
        
        BehaviorControlLifecycle lifecycle = new BehaviorControlLifecycle();
        
        
        
//        ServiceBinding sceneActionCallbackMapBinding = 
//                getServiceBinding(
//                lifecycle,
//                "sceneActionCallbackMap",
//                "BehaviorControlSceneActionCallbackMap",
//                false);
//                
//        ServiceBinding adminActionCallbackMapBinding =
//                getServiceBinding(
//                lifecycle,
//                "adminActionCallbackMap",
//                "BehaviorControlAdminActionCallbackMap",
//                false);
        
        

        Map<String, ServiceBinding> bindings =
                new HashMap<String, ServiceBinding>();
        
        getBinding(bindings, lifecycle, "sceneActionCallbackMap", "behaviorID", 0);
        getBinding(bindings, lifecycle, "adminActionCallbackMap", "adminID",1);

        ServiceManager serviceManager =
                new ServiceManager(
                lifecycle,
                bindings,
                Collections.EMPTY_MAP,
                null);
        
        serviceManager.start(anOSGiRegistry);
    }
    
    private static Map getBinding(
            Map<String, ServiceBinding> bindings,
            BehaviorControlLifecycle l,
            String dependancyName, String mapId,
            int i
            ) {
        
        Map<String,String> props = new HashMap<String, String>();
        props.put("triggerPanelID", mapId);
        BasicDescriptor d = 
                new BasicDescriptor(
                ActionCallbackMap.class.getName(),
                props);
        
        ServiceBinding binding = new ServiceBinding (
                (ServiceDependency)l.getDependencySpecs().get(i),
                d,
                ServiceBinding.BindingStrategy.LAZY);
                
        bindings.put(dependancyName, binding);
        
        return bindings;
    }                
                
//                        // Configue the bindings for the VisemeBindingManagerLifecycle
//        VisemeBindingManagerLifecycle l3 = new VisemeBindingManagerLifecycle();
//        bindings = new HashMap<String, ServiceBinding>();
//        
//        i = 0;
//        props = new HashMap<String, String>();
////        props.put(Constants.PROP_MESSAGE_SENDER_ID, "speechService_01");
////        props.put(Constants.PROP_MESSAGE_TYPE, SpeechService.class.getName());
//        d = new BasicDescriptor(l3.getServiceDependencys().get(i).getDependencyClassName(), props);
//        bindings.put(l3.getServiceDependencys().get(i).getDependencyName(), new ServiceBinding(l3.getServiceDependencys().get(i), d, ServiceBinding.BindingStrategy.LAZY));
//        
//                
//        i++;
//        props = new HashMap<String, String>();
//        props.put(Constants.PROP_MESSAGE_RECEIVER_ID, "speechService_01/RKSpeechGroup/speechError/RKMessagingGroup/remoteListener");
//        props.put(Constants.PROP_MESSAGE_TYPE, ServiceError.class.getName());
//        d = new BasicDescriptor(l.getServiceDependencys().get(i).getDependencyClassName(), props);
//        bindings.put(l.getServiceDependencys().get(i).getDependencyName(), new ServiceBinding(l.getServiceDependencys().get(i), d, ServiceBinding.BindingStrategy.LAZY));
//        
//    }

//    private ServiceBinding getServiceBinding(
//            BehaviorControlLifecycle lifecycle,
//            String propertyKey, //TODO: more descriptive
//            String propertyValue, //TODO: more descriptive
//            boolean isAdminMap) {
//
//        ServiceBinding actionCallbackMapBinding;
//
//        // Extract the dependency spec.
//        int pos = 0;
//        if (isAdminMap) {
//            pos = 1;
//        }
//        ServiceDependency spec =
//                (ServiceDependency) lifecycle.getDependencySpecs().get(pos);
//
//        // Build descriptor
//                Map propertyMap = new HashMap<String, String>(1);
//        propertyMap.put(propertyKey, propertyValue);
//        BasicDescriptor desc = new BasicDescriptor(
//                ActionCallbackMap.class.getName(),
//                propertyMap);
//
//        
//        return new ServiceBinding(
//                spec,
//                desc,
//                ServiceBinding.BindingStrategy.EAGER,
//                ServiceDependency.UpdateStrategy.STATIC);
//    }
}
