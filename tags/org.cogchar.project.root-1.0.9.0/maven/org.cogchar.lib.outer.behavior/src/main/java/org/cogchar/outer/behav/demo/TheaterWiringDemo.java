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

import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.outer.behav.impl.OSGiTheater;
import org.cogchar.impl.channel.GraphChannelHub;
import org.cogchar.impl.channel.ThingActionChanSpec;
import org.cogchar.impl.scene.BScene;
import org.cogchar.impl.scene.Theater;
import org.osgi.framework.BundleContext;
import org.jflux.impl.services.rk.osgi.ServiceClassListener;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class TheaterWiringDemo extends WiringDemo {
	
	public String myDefaultDebugCharQN = "csi:debug_QN_for_theater_70";
	
	public TheaterWiringDemo(BundleContext bundleCtx,  RepoClient demoRepoClient) {
		super(bundleCtx, demoRepoClient);
	}	
	public OSGiTheater testTheaterStartup(BundleContext bundleCtx, RepoClient demoRepoClient, String debugCharQN) {
		String sceneOSGiFilterForTheater = null;


		Theater t = makeTheater(demoRepoClient, debugCharQN);
		OSGiTheater osgiT = setupTheatreOSGiComp(bundleCtx, t, sceneOSGiFilterForTheater);
        GraphChannelHub hub = new GraphChannelHub(demoRepoClient);
        t.setGraphChanHub(hub);
		getLogger().info("************************ Starting Theater thread()");
		t.startThread();
        new TAGraphChanSpecListener(bundleCtx, null, hub).start();
		return osgiT;
	}
	
	public OSGiTheater makeOSGiTheaterAndStart(BundleContext bundleCtx, Theater t, String sceneFilterText) {
		// TODO:  Grab the constantsfrom MasterDemoNames and include here in registration.
		// String key = "TheaterGroupId";
		// String val = "demo_master_theater_group_44";
		OSGiTheater osgiTheatre = new OSGiTheater(bundleCtx, t, sceneFilterText);
		osgiTheatre.start();
		return osgiTheatre;
	}

	public OSGiTheater setupTheatreOSGiComp(BundleContext bundleCtx, Theater t, String sceneOSGiFilter) {
		getLogger().info("************************ setupTheatreOSGiComp()");

		OSGiTheater osgiT = makeOSGiTheaterAndStart(bundleCtx, t, sceneOSGiFilter);
		return osgiT;
	}
	public Theater makeTheater(org.appdapter.help.repo.RepoClient bmcRepoCli, String debugCharQN) {
		// This makeTheater method does not yet do any smart stuff, but it could.
		Ident debugCharID = bmcRepoCli.makeIdentForQName(debugCharQN);
		Theater t = new Theater(debugCharID);
		return t;
	}
	@Override public void registerJFluxExtenders(BundleContext bundleCtx) {	
		
	}
	public void stopAndClearTheater(OSGiTheater osgiThtr, boolean cancelOutJobs) {
		Theater thtr = osgiThtr.getTheater();
		if (thtr != null) {
			int killTimeWaitMsec = 250;
			thtr.fullyStop(killTimeWaitMsec, cancelOutJobs);
		}
	}
	public void startEmptyTheater(OSGiTheater osgiThtr) {
		Theater thtr = osgiThtr.getTheater();
		if (thtr != null) {
			// Start Theater again
			thtr.startThread();
		}
	}
	public void playSceneCleanly(OSGiTheater osgiThtr, BScene scene, boolean cancelPrevOutJobs) {
		Theater thtr = osgiThtr.getTheater();
		if (thtr != null) {
			thtr.stopAllScenesAndModules(cancelPrevOutJobs);
			thtr.exclusiveActivateScene(scene, cancelPrevOutJobs);
		}		
	}
	public void stopAndRestartTheater(OSGiTheater osgiThtr, boolean cancelOutJobs) {
		Theater thtr = osgiThtr.getTheater();
		if (thtr != null) {
			int killTimeWaitMsec = 250;
			thtr.fullyStop(killTimeWaitMsec, cancelOutJobs);
			thtr.startThread();
		}
	}
	
    static class TAGraphChanSpecListener extends ServiceClassListener<ThingActionChanSpec> {
        private GraphChannelHub myHub;
        
        public TAGraphChanSpecListener(BundleContext context, String serviceFilter, GraphChannelHub hub) {
            super(ThingActionChanSpec.class, context, serviceFilter);
            myHub = hub;
        }

        @Override
        protected void addService(ThingActionChanSpec t) {
            myHub.makeThingActionGraphChan(t.getIdent(), t.mySourceModel(), 0);
        }

        @Override
        protected void removeService(ThingActionChanSpec t) {
        }
        
        
        
    }
}
