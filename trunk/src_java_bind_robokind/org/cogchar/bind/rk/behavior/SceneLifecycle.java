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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.appdapter.core.name.Ident;
import org.cogchar.api.channel.Channel;
import org.cogchar.api.scene.Scene;
import org.cogchar.impl.scene.BScene;
import org.cogchar.impl.scene.FancyBScene;
import org.cogchar.impl.scene.SceneSpec;
import org.robokind.api.common.lifecycle.AbstractLifecycleProvider;
import org.robokind.api.common.lifecycle.DependencyDescriptor;
import org.robokind.api.common.lifecycle.DependencyDescriptor.DependencyType;
import org.robokind.api.common.lifecycle.utils.DescriptorListBuilder;
import org.robokind.api.common.osgi.OSGiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matthew Stevenson <www.robokind.org>
 */
public class SceneLifecycle extends AbstractLifecycleProvider<Scene, BScene> {
	private static Logger theLogger =  LoggerFactory.getLogger(SceneLifecycle.class);
    private SceneSpec mySceneSpec;
    
    private static List<DependencyDescriptor> buildDescriptorList(SceneSpec spec){
        List<String> chanURIs = getRequiredChannelURIs(spec);
        List<DependencyDescriptor> descriptors = new ArrayList<DependencyDescriptor>();
        for(String uri : chanURIs){
            descriptors.add(new DependencyDescriptor(uri, Channel.class, 
                    OSGiUtils.createFilter("URI", uri), DependencyType.REQUIRED));
        }
        return descriptors;
    }
    
    private static List<String> getRequiredChannelURIs(SceneSpec spec){
        return spec.getChannelUriStringsJList();
    }
    
    public SceneLifecycle(SceneSpec spec){
        super(buildDescriptorList(spec));
        mySceneSpec = spec;
    }

    @Override protected BScene create(Map<String, Object> dependencies) {
        BScene scene = new FancyBScene(mySceneSpec);
		List chansForWiring = new ArrayList<Channel>();
        for(Entry<String,Object> e : dependencies.entrySet()){
            String chanURI = e.getKey();
			Object entryVal = e.getValue();
            if(entryVal == null || !Channel.class.isAssignableFrom(entryVal.getClass())){
				theLogger.warn("Ignoring non-channel dependency {} for scene {}", entryVal, scene);
                continue;
            }
            Channel chan = (Channel)e.getValue();   //Dependency types already checked by AbstractLifecycle
			theLogger.warn("Found channel dependency {} for scene {}", chan, scene);
			chansForWiring.add(chan);
        }
		scene.wireSubChannels(chansForWiring);
        return scene;
    }

    @Override protected void handleChange(String dependencyKey, Object dependency, Map<String, Object> availableDependencies) {
        //simple way to recreate the Scene when dependencies change.
        myService = isSatisfied() ? create(availableDependencies) : null;
        
        //Example of actually handling the change:
//        if(!isSatisfied()){//Satisfied if all required dependencies are available.
//            //Causes the service to be unregistered, will be recreated when required dependencies are all available again.
//            myService = null;
//        }else if(dependency == null){
//            //optional dependency was unregistered
//        }else{
//            //dependency changed
//            //do something with myService
//        }
    }

    @Override
    protected Class<Scene> getServiceClass() {
        return Scene.class;
    }
}
