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

import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.app.puma.behavior.OSGiTheater;
import org.cogchar.impl.scene.Theater;
import org.osgi.framework.BundleContext;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class TheaterWiringDemo extends WiringDemo {
	public TheaterWiringDemo(BundleContext bundleCtx,  RepoClient demoRepoClient) {
		super(bundleCtx, demoRepoClient);
	}	
	public OSGiTheater testTheaterStartup(BundleContext bundleCtx, RepoClient demoRepoClient) {
		String sceneOSGiFilterForTheater = null;

		String debugCharQN = "hrk:debug_QN_for_theater_70";
		Theater t = makeTheater(demoRepoClient, debugCharQN);
		OSGiTheater osgiT = setupTheatreOSGiComp(bundleCtx, t, sceneOSGiFilterForTheater);
		getLogger().info("************************ Starting Theater thread()");
		t.startThread();
		return osgiT;
	}
	
	public OSGiTheater makeOSGiTheaterAndStart(BundleContext bundleCtx, Theater t, String sceneFilterText) {
		String key = "TheaterGroupId";
		String val = "demo_master_theater_group_44";
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
}
