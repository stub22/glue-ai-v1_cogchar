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
// import org.apache.mina.util.AvailablePortFinder;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.appdapter.core.store.Repo;
import org.appdapter.fancy.rclient.RepoClient;

import org.cogchar.api.thing.WantsThingAction;
// import org.cogchar.impl.perform.basic.AnimLaunchEntityAction;
import org.cogchar.impl.thing.route.BasicThingActionRouter;
import org.cogchar.api.web.WebSceneInterface;
import org.cogchar.api.web.WebEntityAction;
import org.cogchar.app.puma.boot.PumaContextCommandBox;
import org.cogchar.impl.web.config.AvailableCommands;

// import org.cogchar.bind.mio.robot.client.AnimMediaHandle;
// import org.cogchar.bind.mio.robot.client.AnimOutTrigChan;
import org.cogchar.blob.emit.GlobalConfigEmitter;
// import org.cogchar.impl.thing.basic.BasicThingActionConsumer;
import org.cogchar.name.entity.EntityRoleCN;
import org.cogchar.impl.web.in.SceneActions;
import org.osgi.framework.BundleContext;
import org.jflux.impl.services.rk.lifecycle.ServiceLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.utils.SimpleLifecycle;
import org.jflux.impl.services.rk.osgi.OSGiUtils;
import org.jflux.impl.services.rk.osgi.ServiceClassListener;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponent;
/**
 * Able to wire and start both our current HTTP services:  1) SPARQL-HTTP repo and 2) Lifter WebUI
 * @author Stu B. <www.texpedient.com>
 */
public class PumaWebMapper extends BasicDebugger {
	
	private CommandTargetForUseFromWeb		myCmdTargetForWeb; // The LiftInterface allows Lift app to hook in and trigger cinematics

	private OSGiComponent					myLiftAppComp;
	private OSGiComponent					myLiftSceneComp;
	private PumaContextCommandBox			myPCCB;
	private BundleContext context;
	// Make default constuctor private to prevent PumaWebMapper from being instantiated without a PumaAppContext
	private PumaWebMapper() {}
	
	public PumaWebMapper(PumaContextCommandBox pccb) {
		myPCCB = pccb;
	}
	public PumaContextCommandBox getCommandBox() { 
		return myPCCB;
	}
    
    public void attachContext(BundleContext context)
    {
        this.context=context;
    }
	/**
	 * 
	 * @return 
	 * Called only from	connectAvailableCommands()  
	 */
	protected CommandTargetForUseFromWeb geWebCommandTarget() {
		if (myCmdTargetForWeb == null) {
			myCmdTargetForWeb = new CommandTargetForUseFromWeb(context, this);
		}
		return myCmdTargetForWeb;
	}

	public void connectLiftSceneInterface(BundleContext bundleCtx) {
		if (myLiftSceneComp == null) {
			ServiceLifecycleProvider lifecycle = new SimpleLifecycle(SceneActions.getLauncher(), WebSceneInterface.class);
			myLiftSceneComp = new OSGiComponent(bundleCtx, lifecycle);
		}
		myLiftSceneComp.start();
	}

	public void disconnectLiftSceneInterface(BundleContext bundleCtx) {
		myLiftSceneComp.stop();
	}
/**
 * 
 * @param bundleCtx 
 * Called only from 
	 * PumaAppContext.connectWeb(), which is called from PUMA booter, typically from a Framework-started event
	 * handler in some "top" application bundle.
 */
	public void connectAvailableCommands(BundleContext bundleCtx) {
		CommandTargetForUseFromWeb webCmdTarget = geWebCommandTarget();
		ServiceLifecycleProvider lifecycle = new SimpleLifecycle(webCmdTarget, AvailableCommands.class);
		myLiftAppComp = new OSGiComponent(bundleCtx, lifecycle);
		myLiftAppComp.start();
	}
	// 
/**
 * Tell the lifter lifecycle to start, once its OSGi dependencies are satisfied.
 * So far, it is only called from Friendularity tests:  o.f.b.lifter - Activator and o.f.b.repo - Activator.
 * @param bunCtx 
 */
	public void startLifterLifecycle(BundleContext bunCtx) { 
		LifterLifecycle lifecycle = new LifterLifecycle();
    	OSGiComponent lifterComp = new OSGiComponent(bunCtx, lifecycle);
    	lifterComp.start();
	}	
/**
 * Called from o.f.b.repo - Activator ,   o.f.b.webapp.semrepo.Activator, o.f.b.demoserv.semrepo.Activator...
 * and from 
 * o.c.b.bind.joseki - o.c.joswrap.RepoJosDsetDesc
 *
 * @return 
 */
	public Dataset getMainLocalSparqlDataset() {
		Dataset mainConfDset = null;
		PumaContextCommandBox pccb = getCommandBox();
		RepoClient mainConfRC = pccb.getMainConfigRepoClient();
		Repo mainConfRepo = mainConfRC.getRepoIfLocal();
		if (mainConfRepo != null) {
			mainConfDset = mainConfRepo.getMainQueryDataset();
			// Print out the available graphs, for debugging.
			java.util.List<Repo.GraphStat> gStats = mainConfRepo.getGraphStats();
			for (Repo.GraphStat gStat : gStats) {
				getLogger().debug("Found in main config:  " + gStat);
			}
		} else {
			getLogger().warn("There is no local query dataset - mainConfigRepoClient must be remote!");
		}
		return mainConfDset;
	}
	/**
	 * Called only from  GruesomeTAProcessingFuncs.registerActionConsumers().
	 * This does 2 things:
	 * 1) Sets up some an assumed consumer for WebEntityAction
	 * 2) Sets up an OSGi listener that can append additonal consumers of type WantsThingAction when they appear
	 * in the OSGi registry.
	 * 
	 * @param router
	 * @param rc
	 * @param gce 
	 */
	public void registerActionConsumers(BasicThingActionRouter router, RepoClient rc, GlobalConfigEmitter gce) { 

	//	Ident backupWorldConfigID_orNull = new FreeIdent("if/exception/while/reading/this/ident/report#nullVal");
		Ident vwEntConfID = null;
		try {
			// Temporarily assuming exactly one vworldConfig supplied. This is a slightly different assumption
			// than what is used in PumaVirtualWorldMapper.
			String vwMarker = EntityRoleCN.VIRTUAL_WORLD_ENTITY_TYPE;
			Ident taBindRoleID = EntityRoleCN.THING_ACTIONS_BINDINGS_ROLE;
			vwEntConfID = gce.getFirstEntityIdent_orNull(vwMarker, null);
			if (vwEntConfID != null) {
				Ident entRoleGraphID = gce.getEntConfRoleGraphID_orNull(vwEntConfID, taBindRoleID);
				if (entRoleGraphID != null) {
					// First add hardwired consumer for web-stuff
					registerWebActionConsumer(router, entRoleGraphID);
					// Now set up a listener on the OSGi registry that can append any other WantsThingAction guys that come along.
					BundleContext context = OSGiUtils.getBundleContext(WantsThingAction.class);
					if(context != null){
						String filter = null;  //OSGiUtils.createFilter("thingActionChannelAgentId", "*aZR50");
						new TAConsumerTracker(context, filter, router, entRoleGraphID).start();
					} else {
						getLogger().error("No bundle context for WantsThingAction");
					}
				} else {
					getLogger().error("No ThingActionBinding found for vwConf={} at role={} in erg-subMap={}",
								vwEntConfID, taBindRoleID,  gce.ergMap().get(vwEntConfID));
				}
			}
			else {
				getLogger().error("No virtualWorld entity found at {} in gce.entityMap()={}",
						vwMarker, gce.entityMap());
			}

		} catch (Exception e) {
			getLogger().error("Caught exception while registering ThingActionConsumer for vw-confID={}", vwEntConfID, e);
		}
	}
	private void registerWebActionConsumer(BasicThingActionRouter router, Ident graphIdent) {
		// Set up a hadwired consumer to process
		// WebActionNames.WEBCONTROL.equals(tgtThingTypeID) || WebActionNames.WEBCONFIG.equals(tgtThingTypeID)) {
		// There is no reason this couldn't instead be posted into registry for the TaConsumerTracker to find.
		WebEntityAction.Consumer weaConsumer = new WebEntityAction.Consumer();
		router.appendConsumer(graphIdent, weaConsumer);

	}
    static class TAConsumerTracker extends ServiceClassListener<WantsThingAction> {
        private BasicThingActionRouter myRouter;
        private Ident myGraphIdent;

        public TAConsumerTracker(BundleContext context, String serviceFilter, BasicThingActionRouter router, Ident graphIdent) {
            super(WantsThingAction.class, context, serviceFilter);
            myRouter = router;
            myGraphIdent = graphIdent;
        }

        @Override
        protected void addService(WantsThingAction startedWanting) {
			myRouter.appendConsumer(myGraphIdent, startedWanting);
        }

        @Override
        protected void removeService(WantsThingAction stoppedWanting) {
        }
        
        
    }
    
}
