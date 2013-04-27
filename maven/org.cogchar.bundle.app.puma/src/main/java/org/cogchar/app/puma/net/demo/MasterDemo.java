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

package org.cogchar.app.puma.net.demo;

import java.util.List;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.api.scene.Scene;
import org.cogchar.app.puma.behavior.OSGiTheater;
import org.cogchar.blob.emit.RepoSpec;
import org.cogchar.impl.scene.BScene;
import org.cogchar.impl.scene.Theater;
import org.osgi.framework.BundleContext;
import org.robokind.api.common.lifecycle.utils.SimpleLifecycle;
import org.robokind.api.common.osgi.lifecycle.OSGiComponent;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class MasterDemo extends BasicDebugger {
	public	ChannelWiringDemo	myChannelWiringDemo;	
	public	SceneWiringDemo		mySceneWiringDemo;
	public  TheaterWiringDemo	myTheaterWiringDemo;
	
	public void preLaunchSetup(BundleContext bundleCtx, String robotEnvVarKey) { 
		AnimationConnector.launchPortableAnimEventFactory(bundleCtx);
		RobotConnector.connectRobotsFromSysEnv(bundleCtx, robotEnvVarKey);
	}
	
	public void launchDefaultDemo(BundleContext bundleCtx) { 
		RepoClient defDemoRepoCli = makeDefaultRepoClient(bundleCtx);
		initMajorParts(bundleCtx, defDemoRepoCli);
		launchDefaultDemoObjects(bundleCtx, defDemoRepoCli);
	}
	public RepoClient makeDefaultRepoClient (BundleContext bundleCtx) { 
		RepoConnector repoConn = new RepoConnector();
		// TODO:  Add ability to load from a LocalSheetRepoSpec
		RepoSpec demoRepoSpec = repoConn.makeDefaultOnlineSheetRepoSpec(bundleCtx);
		RepoClient demoRepoClient = repoConn.connectDemoRepoClient(demoRepoSpec);
		return demoRepoClient;
	}
	
	public void initMajorParts(BundleContext bundleCtx, RepoClient demoRepoClient) {
		myChannelWiringDemo = new ChannelWiringDemo(bundleCtx, demoRepoClient);
		mySceneWiringDemo = new SceneWiringDemo(bundleCtx, demoRepoClient);
		myTheaterWiringDemo = new TheaterWiringDemo(bundleCtx, demoRepoClient);
	}
	
	public void launchDefaultDemoObjects(BundleContext bundleCtx, RepoClient demoRepoClient) { 
	
		ChannelWiringDemo cwd = myChannelWiringDemo;
		SceneWiringDemo swd = mySceneWiringDemo;
		TheaterWiringDemo twd = myTheaterWiringDemo;
		
		cwd.registerJFluxExtenders(bundleCtx);
		swd.registerJFluxExtenders(bundleCtx);
		
		String chanGroupQName = cwd.myDefaultChanGroupQName;
		cwd.initialChannelLoad(bundleCtx, demoRepoClient, chanGroupQName);
		
		String directGraphQN = swd.myDefaultDirectGraphQN;
		String derivedGraphQN = swd.myDefaultDerivedGraphQN;
		String sceneGroupQN = swd.myDefaultSceneGroupQN;
		swd.initialSceneLoad(bundleCtx, demoRepoClient, directGraphQN, derivedGraphQN, sceneGroupQN);
		
		String theaterDebugQN = twd.myDefaultDebugCharQN;
		OSGiTheater osgiTheater = twd.testTheaterStartup(bundleCtx, demoRepoClient, theaterDebugQN);
        new OSGiComponent(bundleCtx, new SimpleLifecycle(osgiTheater, OSGiTheater.class)).start();	
	}
	public void runTestSceneSequence(OSGiTheater osgiTheater) { 
		Theater thtr = osgiTheater.getTheater();
		// OSGiTheater gets notified of all matching available scenes.
		try {
			getLogger().info("Sleeping for 5 sec");
			Thread.sleep(5000);
			getLogger().info("Finished sleeping");
			List<Scene> scenes = osgiTheater.getScenes();
			getLogger().info("Fetched sceneList from OSGiTheater: {} ", scenes);
			for (Scene scn : scenes) {
				getLogger().info("Activating scene : {}", scn);
				thtr.activateScene((BScene) scn);
				getLogger().info("Sleeping for 10 sec");
				Thread.sleep(10000);
			}
			getLogger().info("Finished triggering all scenes");
		} catch (Throwable t) {
			getLogger().error("Caught exception", t);
		}		
	}
	
}
