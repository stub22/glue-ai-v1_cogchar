/*
 * Copyright 2013 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.outer.behav.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.impl.channel.ThingActionChanSpec;
import org.osgi.framework.BundleContext;
import org.robokind.api.common.lifecycle.ManagedService;
import org.robokind.api.common.lifecycle.ServiceLifecycleProvider;
import org.robokind.api.common.lifecycle.utils.SimpleLifecycle;
import org.robokind.api.common.osgi.lifecycle.OSGiComponent;

/**
 *
 * @author matt
 */
public class TAGraphChanWiringDemo {
    
    public static final String GROUP_KEY_FOR_TA_CHAN_SPEC = "taChanSpecGroupId";
    public static final String TA_CHAN_GROUP_QN = "demoTAChanGroup";
    
    public static Map<ThingActionChanSpec,ManagedService> loadAndRegisterSpecs(
            BundleContext context, RepoClient defaultDemoRepoClient, String taChanGraphQN) {
        Map<ThingActionChanSpec,ManagedService> specServices = new HashMap<ThingActionChanSpec, ManagedService>();
        List<ThingActionChanSpec> specs = loadTAChanSpecs(defaultDemoRepoClient, taChanGraphQN);
        for(ThingActionChanSpec spec : specs){
            if(specServices.containsKey(spec)){
                continue;
            }
            ManagedService service = registerTAChanSpec(context, spec);
            specServices.put(spec, service);
        }
        return specServices;
    }
    
    private static List<ThingActionChanSpec> loadTAChanSpecs(RepoClient defaultDemoRepoClient, String taChanGraphQN) {
        List<ThingActionChanSpec> specs = new ArrayList();
        // Determine the URI for the 'qualified name' which identifies the data in the repo
        Ident taChanGraphID = defaultDemoRepoClient.makeIdentForQName(taChanGraphQN);
        // Collect the objects from the repo, building them from RDF raw data
        Set<Object> assembledRoots = defaultDemoRepoClient.assembleRootsFromNamedModel(taChanGraphID);
        for (Object root : assembledRoots) {
            // Ignore anything that is not a ConnectionSpec
            if (root == null || !ThingActionChanSpec.class.isAssignableFrom(root.getClass())) {
                continue;
            }
            specs.add((ThingActionChanSpec) root);
        }
        return specs;
    }

    /**
     * Allows JFlux to register TAChan specs
     * @param context the BundleContext used to register the spec
     * @param taChanSpec the spec to be registered
     */
    private static ManagedService registerTAChanSpec(
            BundleContext context, ThingActionChanSpec taChanSpec) {
        Properties props = new Properties();
        props.put(GROUP_KEY_FOR_TA_CHAN_SPEC, TA_CHAN_GROUP_QN);
        
        ServiceLifecycleProvider lifecycle = 
                new SimpleLifecycle(taChanSpec, ThingActionChanSpec.class);
        ManagedService ms = new OSGiComponent(context, lifecycle, props);
        ms.start();
        return ms;
    }
}
