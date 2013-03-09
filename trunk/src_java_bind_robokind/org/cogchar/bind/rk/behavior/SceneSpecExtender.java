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
import java.util.Map;
import java.util.Properties;
import org.cogchar.impl.scene.SceneSpec;
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
public class SceneSpecExtender extends ServiceClassListener<SceneSpec>{
    private ManagedServiceFactory myManagerFactory;
    private Map<SceneSpec,ManagedService> myManagerMap;
    private Properties mySceneRegistrationProperties;
    
    public SceneSpecExtender(BundleContext context, 
            String bindingConfigOSGiFilter, Properties sceneRegistrationProps){
        super(SceneSpec.class, context, bindingConfigOSGiFilter);
        myManagerFactory = new OSGiComponentFactory(context);
        mySceneRegistrationProperties = sceneRegistrationProps;
        myManagerMap = new HashMap();
    }

    @Override
    protected void addService(SceneSpec t) {
        if(t == null || myManagerMap.containsKey(t)){
            return;
        }
        ServiceLifecycleProvider provider = new SceneLifecycle(t);
        ManagedService ms = myManagerFactory.createService(provider, mySceneRegistrationProperties);
        ms.start();
        myManagerMap.put(t, ms);
    }

    @Override
    protected void removeService(SceneSpec t) {
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
