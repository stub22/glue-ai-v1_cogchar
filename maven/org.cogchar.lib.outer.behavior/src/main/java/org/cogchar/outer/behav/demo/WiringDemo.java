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


package org.cogchar.outer.behav.demo;

import java.util.Properties;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.help.repo.RepoClient;
import org.osgi.framework.BundleContext;
import org.jflux.impl.services.rk.lifecycle.utils.SimpleLifecycle;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponent;
import org.cogchar.impl.scene.read.SceneSpecReader;
import org.cogchar.impl.scene.read.BehavMasterConfigTest;

/**
 * @author Stu B. <www.texpedient.com>
 */

public abstract class WiringDemo extends BasicDebugger {
	private		BundleContext			myDefaultBundleContext;
	private		RepoClient		myDefaultRepoClient;
	
	abstract public void registerJFluxExtenders(BundleContext bundleCtx);

	public WiringDemo(BundleContext bc, RepoClient rc) {
		myDefaultBundleContext = bc;
		myDefaultRepoClient = rc;
	}
	protected BundleContext getDefaultBundleContext() { 
		return myDefaultBundleContext;
	}
	protected RepoClient getDefaultRepoClient() {
		return myDefaultRepoClient;
	}
	
	protected SceneSpecReader getSceneSpecReader() { 
		return BehavMasterConfigTest.getSceneSpecReader();
	}
    public static Runnable getRegistrationRunnable(
            final BundleContext context, final Class clazz, final Object obj, final String key, final String val){
        return new Runnable() {
            @Override public void run() {
                Properties props = null;
                if(key != null){
                    props = new Properties();
                    props.put(key, val);
                }
                new OSGiComponent(context, new SimpleLifecycle(obj, clazz, props)).start();
            }
        };
    }	
}
