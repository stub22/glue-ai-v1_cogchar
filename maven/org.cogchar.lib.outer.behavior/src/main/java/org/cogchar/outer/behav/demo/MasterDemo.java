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

import org.cogchar.bind.mio.remote.AnimationConnector;
import org.cogchar.bind.mio.remote.RobotConnector;
import java.util.List;

import org.appdapter.core.log.BasicDebugger;
// import org.appdapter.core.matdat.*;
// import org.appdapter.core.repo.*;
import org.appdapter.fancy.rspec.RepoSpec;
import org.appdapter.fancy.rclient.RepoClient;
import org.appdapter.fancy.rclient.EnhancedRepoClient;
import org.appdapter.fancy.gpointer.PipelineQuerySpec;
import org.cogchar.api.scene.Scene;
import org.cogchar.impl.scene.BScene;
import org.cogchar.impl.scene.Theater;
import org.cogchar.outer.behav.impl.OSGiTheater;
import org.osgi.framework.BundleContext;
import org.jflux.impl.services.rk.lifecycle.utils.SimpleLifecycle;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponent;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class MasterDemo extends BasicDebugger {
	public ChannelWiringDemo myChannelWiringDemo;
	public SceneWiringDemo mySceneWiringDemo;
	public TheaterWiringDemo myTheaterWiringDemo;

	public void preLaunch_SetupMechioRemoteConns(BundleContext bundleCtx, String robotEnvVarKey) {
		try {
			AnimationConnector.launchPortableAnimEventFactory(bundleCtx);
			RobotConnector.connectRobotsFromSysEnv(bundleCtx, robotEnvVarKey);
		} catch (Throwable t) {
			getLogger().error("Connection Problem", t);
		}
	}

	public void launchDemoUsingDefaultOnlineRepoSheet(BundleContext bundleCtx) {
		RepoConnector repoConn = new RepoConnector();
		EnhancedRepoClient defDemoRepoCli = repoConn.makeRepoClientForDefaultOnlineSheet(bundleCtx);
		launchDemo(bundleCtx, defDemoRepoCli);
	}

	public void launchDemo(BundleContext bundleCtx, RepoSpec demoRepoSpec) {
		RepoConnector repoConn = new RepoConnector();
		EnhancedRepoClient demoRepoCli = repoConn.connectDemoRepoClient(demoRepoSpec);
		launchDemo(bundleCtx, demoRepoCli);
	}
/*  [java] 	at org.cogchar.outer.behav.demo.MasterDemo.launchDemo(MasterDemo.java:69)
     [java] 	at com.rkbots.demo.behavior.master.BehaviorMasterLifecycle.create(BehaviorMasterLifecycle.java:45)
     [java] 	at com.rkbots.demo.behavior.master.BehaviorMasterLifecycle.create(BehaviorMasterLifecycle.java:22)
     [java] 	at org.jflux.impl.services.rk.lifecycle.AbstractLifecycleProvider.start(AbstractLifecycleProvider.java:74)
*/
	public void launchDemo(BundleContext bundleCtx, RepoClient defDemoRepoCli) {
		try {
			getLogger().info("Launching demo using repoClient={}", defDemoRepoCli);
			initMajorParts(bundleCtx, defDemoRepoCli);
			launchDefaultDemoObjects(bundleCtx, defDemoRepoCli);
		} catch (Throwable t) {
			getLogger().error("Error Launching 'Master' Demo", t);
		}
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
		twd.registerJFluxExtenders(bundleCtx);

		String chanGroupQName = cwd.myDefaultChanGroupQName;
		cwd.initialChannelLoad(bundleCtx, demoRepoClient, chanGroupQName);

		String directGraphQN = swd.myDefaultDirectGraphQN;
		// String pipeQueryQN = swd.myDefaultPipelineQueryQN;
		// String pipeGraphQN = swd.myDefaultPipelineGraphQN;
		PipelineQuerySpec pqs = swd.myDefaultPipelineQuerySpec;
		String derivedGraphQN = swd.myDefaultDerivedGraphQN;
		String sceneGroupQN = swd.myDefaultSceneGroupQN;
		swd.initialSceneLoad(bundleCtx, demoRepoClient, directGraphQN, pqs, derivedGraphQN, sceneGroupQN);

		String theaterDebugQN = twd.myDefaultDebugCharQN;
		OSGiTheater osgiTheater = twd.testTheaterStartup(bundleCtx, demoRepoClient, theaterDebugQN);

		//DemoBrowser.showObject(null, this, false, true);
		//DemoBrowser.showObject(null, demoRepoClient, false, true);
		//		DemoBrowser.showObject(null, demoRepoClient.getRepo(), true, true);
		//		DemoBrowser.showObject(null, demoRepoClient.getRepo().getDirectoryModel(), true, true);
		new OSGiComponent(bundleCtx, new SimpleLifecycle(osgiTheater, OSGiTheater.class)).start();
	}

	public void reloadScenesAndRestartTheater(OSGiTheater osgiThtr, boolean cancelOutJobs) {
		BundleContext bundleCtx = mySceneWiringDemo.getDefaultBundleContext();
		RepoClient origRepoCli = mySceneWiringDemo.getDefaultRepoClient();
		reloadScenesAndRestartTheater(bundleCtx, osgiThtr, origRepoCli, cancelOutJobs);
	}

	public void reloadScenesAndRestartTheater(BundleContext bunCtx, OSGiTheater osgiThtr, RepoClient origRepoCli, boolean cancelOutJobs) {

		myTheaterWiringDemo.stopAndClearTheater(osgiThtr, cancelOutJobs);
		// Create a new fresh repo + client connection based on the origRepoCli's repoSpec.
		// This is a long operation, during which we are blocking the GUI.  
		// (Unless it is just a virtual version update, which trades
		//  off against the length of load time during reloadSceneSpecs below).  
		// That's why we did the stop above, to make sure user at least feels the keypress response quickly.
		// But in a real character app, we want to be silently loading all the time without interrupting current perfs.
		// TODO: Tell the repo to just reload certain graphs (need Appdapter >= 1.1.1 features to do this cleanly)

		/* We may not always have an EnhancedRepoClient, and just have to 
		 * deal with it.  Reload if we can, otherwise reload what we can.
		 * -Matt 2013-07-27 */
		RepoClient reloadedRepoClient = origRepoCli;
		if (origRepoCli instanceof EnhancedRepoClient) {
			reloadedRepoClient = ((EnhancedRepoClient) origRepoCli).reloadRepoAndClient();
		}

		// Now we do the comparatively fast (but still somewhat lengthy) step of unregistering, 
		// loading, and registering scene spec objects from the reloaded repo.  But note that if
		// this not an in-memory repoImpl,  (e.g. if it is SQL to disk or network) then
		// this may be a longer op as we read in the data.  

		mySceneWiringDemo.reloadSceneSpecs(bunCtx, reloadedRepoClient);
		myTheaterWiringDemo.startEmptyTheater(osgiThtr);
	}

	@Deprecated public void runTestSceneSequence(OSGiTheater osgiTheater) {
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
