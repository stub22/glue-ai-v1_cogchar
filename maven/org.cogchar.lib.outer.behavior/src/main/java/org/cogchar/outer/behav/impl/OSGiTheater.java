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
package org.cogchar.outer.behav.impl;

import java.util.ArrayList;
import java.util.List;
import org.cogchar.api.scene.Scene;
import org.cogchar.bind.mio.behavior.SceneLifecycleDemo;
import org.cogchar.impl.scene.Theater;
import org.osgi.framework.BundleContext;
import org.jflux.impl.services.rk.osgi.ServiceClassListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * You must call start() before the OSGiTheater begins tracking Scenes.
 * 
 * 
 * @author Matthew Stevenson <www.cogchar.org>
 */
public class OSGiTheater extends ServiceClassListener<Scene> {
    private List<Scene> myScenes;
    private Theater myTheater;
	
	static Logger theLogger = LoggerFactory.getLogger(OSGiTheater.class);
    
    // sceneOSGiFilter might filter by associated SceneSpecBook URI
    public OSGiTheater(BundleContext context, Theater theater, String sceneOSGiFilter){
        super(Scene.class, context, sceneOSGiFilter);
//        if(theater == null){
//            throw new NullPointerException();
//        }
        myTheater = theater;
        myScenes = new ArrayList<Scene>();
    }
    
    public Theater getTheater(){
        return myTheater;
    }
    
    public List<Scene> getScenes(){
        // called by user code
        return myScenes;
    }

    @Override
    protected void addService(Scene matchingScene) {
        // called by framework
        myScenes.add(matchingScene);
        theLogger.info("OSGiTheater added scene with rootChan={}",  matchingScene.getRootChannel());
    }

    @Override
    protected void removeService(Scene matchingScene) {
        // called by framework
        myScenes.remove(matchingScene);
        theLogger.info("OSGiTheater removed scene with rootChan={}", matchingScene.getRootChannel());
    }

    @Override
    public void stop() {
        // called by user code - stops notifications
        super.stop();
        myScenes.clear();
    }
    public static Runnable makeTheaterRegRunnable(BundleContext context, OSGiTheater osgiTheater, final String key, final String val) {
		return SceneLifecycleDemo.getRegistrationRunnable(context, OSGiTheater.class, osgiTheater, key, val);
	}	
}
