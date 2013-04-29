/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.app.puma.web;

import com.hp.hpl.jena.query.Dataset;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.store.Repo;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.bind.lift.LiftAmbassador;
import org.cogchar.app.puma.boot.PumaContextCommandBox;
import org.cogchar.bind.lift.LifterLifecycle;
import org.cogchar.render.app.trigger.SceneActions;


import org.osgi.framework.BundleContext;
import org.robokind.api.common.lifecycle.ServiceLifecycleProvider;
import org.robokind.api.common.lifecycle.utils.SimpleLifecycle;
import org.robokind.api.common.osgi.lifecycle.OSGiComponent;
/**
 * Able to wire and start both our current HTTP services:  1) SPARQL-HTTP repo and 2) Lifter WebUI
 * @author Stu B. <www.texpedient.com>
 */
public class PumaWebMapper extends BasicDebugger {
	
	private CommandTargetForUseFromWeb		myCmdTargetForWeb; // The LiftInterface allows Lift app to hook in and trigger cinematics

	private OSGiComponent					myLiftAppComp;
	private OSGiComponent					myLiftSceneComp;
	private PumaContextCommandBox					myPCCB;
	
	// Make default constuctor private to prevent PumaWebMapper from being instantiated without a PumaAppContext
	private PumaWebMapper() {}
	
	public PumaWebMapper(PumaContextCommandBox pccb) {
		myPCCB = pccb;
	}
	public PumaContextCommandBox getCommandBox() { 
		return myPCCB;
	}
	protected CommandTargetForUseFromWeb geWebCommandTarget() {
		if (myCmdTargetForWeb == null) {
			myCmdTargetForWeb = new CommandTargetForUseFromWeb(myPCCB, this);
		}
		return myCmdTargetForWeb;
	}
	
	public void connectLiftSceneInterface(BundleContext bundleCtx) {
		if (myLiftSceneComp == null) {
			ServiceLifecycleProvider lifecycle = new SimpleLifecycle(SceneActions.getLauncher(), LiftAmbassador.LiftSceneInterface.class);
			myLiftSceneComp = new OSGiComponent(bundleCtx, lifecycle);
		}
		myLiftSceneComp.start();
	}

	public void disconnectLiftSceneInterface(BundleContext bundleCtx) {
		myLiftSceneComp.stop();
	}

	public void connectLiftInterface(BundleContext bundleCtx) {
		CommandTargetForUseFromWeb webCmdTarget = geWebCommandTarget();
		ServiceLifecycleProvider lifecycle = new SimpleLifecycle(webCmdTarget, LiftAmbassador.LiftAppInterface.class);
		myLiftAppComp = new OSGiComponent(bundleCtx, lifecycle);
		myLiftAppComp.start();
	}
	// Tell the lifter lifecycle to start, once its OSGi dependencies are satisfied
	public void startLifterLifecycle(BundleContext bunCtx) { 
		LifterLifecycle lifecycle = new LifterLifecycle();
    	OSGiComponent lifterComp = new OSGiComponent(bunCtx, lifecycle);
    	lifterComp.start();
	}	

	public Dataset getMainSparqlDataset() {
		PumaContextCommandBox pccb = getCommandBox();
		RepoClient mainConfRC = pccb.getMainConfigRepoClient();
		Repo mainConfRepo = mainConfRC.getRepo();
		Dataset mainConfDset = mainConfRepo.getMainQueryDataset();
		// Print out the available graphs, for debugging.
		java.util.List<Repo.GraphStat> gStats = mainConfRepo.getGraphStats();
		for (Repo.GraphStat gStat : gStats) {
			getLogger().debug("Found in main config:  " + gStat);
		}		
		return mainConfDset;
	}
	
}
