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
import java.util.List;
import org.cogchar.api.scene.Scene;
import org.cogchar.impl.scene.Theater;
import org.osgi.framework.BundleContext;
import org.robokind.api.common.osgi.ServiceClassListener;

/**
 * You must call start() before the OSGiTheater begins tracking Scenes.
 * 
 * 
 * @author Matthew Stevenson <www.robokind.org>
 */
public class OSGiTheater extends ServiceClassListener<Scene> {
    private List<Scene> myScenes;
    private Theater myTheater;
    
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
    protected void addService(Scene t) {
        // called by framework
        myScenes.add(t);
        System.out.println("OSGiTheater added scene:" + t);
    }

    @Override
    protected void removeService(Scene t) {
        // called by framework
        myScenes.remove(t);
        System.out.println("OSGiTheater removed scene:" + t);
    }

    @Override
    public void stop() {
        // called by user code - stops notifications
        super.stop();
        myScenes.clear();
    }
}
