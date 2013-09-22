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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.cogchar.api.scene.ActionCallbackMap;
import org.cogchar.api.thing.WantsThingAction;
import org.jflux.api.service.ServiceDependency;
import org.jflux.api.service.ServiceLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This lifecycle comprises the JFlux object registry interface for the
 * BehaviorControl object. JFlux will provide any dependencies that are
 * necessary and inform this class as they change. JFlux will also provide this
 * object to others once its dependencies are fulfilled.
 * 
 * Lifecycles are intended to be stateless, simply providing meta-code for the 
 * object to gracefully handle changes in its environment.
 * 
 * @author Jason Randolph Eads <jeads362@gmail.com>
 */
public class BehaviorControlLifecycle implements ServiceLifecycle<BehaviorControl> {
    

    private static Logger theLogger = 
            LoggerFactory.getLogger(BehaviorControlLifecycle.class);
    
    
    /**
     * This provides the relevant class names for use in JFlux.
     */
    private final static String[] theClassNameArray = 
            new String[]{
                BehaviorControl.class.getName(),
                WantsThingAction.class.getName()};

    /**
     * The simple string IDs to be used for JFlux interfacing.
     */
    private static final String theSceneActionCallbackMapID =
            "sceneActionCallbackMap";
    
    private static final String theAdminActionCallbackMapID =
            "adminActionCallbackMap";
            
    /**
     * Array of required dependencies
     */
    private final static ServiceDependency[] theDependencyArray = {
        new ServiceDependency(
            theSceneActionCallbackMapID,
            ActionCallbackMap.class.getName(),
            ServiceDependency.Cardinality.MANDATORY_UNARY,
            ServiceDependency.UpdateStrategy.STATIC,
            Collections.EMPTY_MAP),
        
        new ServiceDependency(
            theAdminActionCallbackMapID,
            ActionCallbackMap.class.getName(),
            ServiceDependency.Cardinality.MANDATORY_UNARY,
            ServiceDependency.UpdateStrategy.STATIC,
            Collections.EMPTY_MAP)
    };
    
    //Lifecycle Constructor - required for automated assembly
    public BehaviorControlLifecycle() {}
    
    @Override
    public List<ServiceDependency> getDependencySpecs() {
        return Arrays.asList(theDependencyArray);
    }

    /**
     * Builds up the object that provides the service.
     * 
     * @param dependencyMap A map of the dependencies provided.
     * @return The actual object that provides the service function.
     */
    @Override
    public BehaviorControl createService(Map<String, Object> dependencyMap) {
        
        
        ActionCallbackMap sceneActionCallbackMap = 
                (ActionCallbackMap)dependencyMap.get(theSceneActionCallbackMapID);
        
        ActionCallbackMap adminActionCallbackMap = 
                (ActionCallbackMap)dependencyMap.get(theAdminActionCallbackMapID);
        
        return new BehaviorControl(
                sceneActionCallbackMap,
                adminActionCallbackMap);
    }

    /**
     * Ensures the object can gracefully handle a change in its dependencies.
     * The BehaviorControl has two maps, both are required and can be replaced.
     * 
     * @param service The BehaviorControl object.
     * @param changeType What kind of change occurred. Defined in ServiceLifecycle.
     * @param dependencyName The name of the dependency.
     * @param dependency The dependency object. (type ActionCallbackMap)
     * @param availableDependencies Map of all available dependencies.
     * @return Returns the service.
     */
    @Override
    public BehaviorControl handleDependencyChange(
            BehaviorControl service, 
            String changeType, 
            String dependencyName, 
            Object dependency, 
            Map<String, Object> availableDependencies) {
        return service;
    }

    /**
     * Gracefully tears down the BehaviorControl.
     * 
     * @param service the BehaviorControl object to be disposed
     * @param availableDependencies dependencies that are available for the object.
     */
    @Override
    public void disposeService(
        BehaviorControl service, 
        Map<String, Object> availableDependencies) {
        service.setMyAdminActionCallbackMap(null);
        service.setMySceneActionCallbackMap(null);
    }

    /**
     * Returns the relevant class name used for JFlux.
     * 
     * @return Array of the relevant class names 
     */
    @Override
    public String[] getServiceClassNames() {
        return theClassNameArray;
    }
}
