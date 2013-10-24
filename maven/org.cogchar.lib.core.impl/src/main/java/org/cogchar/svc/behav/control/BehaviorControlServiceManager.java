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
package org.cogchar.svc.behav.control;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.cogchar.api.scene.ActionCallbackMap;
import org.jflux.api.registry.basic.BasicDescriptor;
import org.jflux.api.service.ServiceManager;
import org.jflux.api.service.binding.ServiceBinding;
import org.jflux.api.service.binding.ServiceBinding.BindingStrategy;
import org.jflux.api.registry.Registry;

/**
 *
 * @author Jason Randolph Eads <jeads362@gmail.com>
 */
public class BehaviorControlServiceManager {

    public static void init(Registry registry) {
        BehaviorControlLifecycle lifecycle = new BehaviorControlLifecycle();
        Map<String, ServiceBinding> bindings =
                new HashMap<String, ServiceBinding>();
        getBinding(bindings, lifecycle, "sceneActionCallbackMap", "behaviorID", 0);
        getBinding(bindings, lifecycle, "adminActionCallbackMap", "adminID", 1);
        ServiceManager serviceManager =
                new ServiceManager(lifecycle, bindings, Collections.EMPTY_MAP, null);
        serviceManager.start(registry);
    }

    private static Map getBinding(
            Map<String, ServiceBinding> bindings,
            BehaviorControlLifecycle l,
            String dependancyName, String mapId,
            int i) {
        Map<String, String> props = new HashMap<String, String>();
        props.put("triggerPanelID", mapId);
        BasicDescriptor d =
                new BasicDescriptor(ActionCallbackMap.class.getName(), props);
        ServiceBinding binding = new ServiceBinding(
                l.getDependencySpecs().get(i), d, BindingStrategy.LAZY);
        bindings.put(dependancyName, binding);
        return bindings;
    }
}
