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
package org.cogchar.app.puma.config;

import java.util.List;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.appdapter.impl.store.FancyRepo;

import org.cogchar.app.buddy.busker.TriggerItems;
import org.cogchar.blob.emit.GlobalConfigEmitter;



import org.osgi.framework.BundleContext;
import org.robokind.api.common.lifecycle.ServiceLifecycleProvider;
import org.robokind.api.common.lifecycle.utils.SimpleLifecycle;
import org.robokind.api.common.osgi.lifecycle.OSGiComponent;

import org.cogchar.platform.trigger.BoxSpace;
import org.cogchar.platform.trigger.CommandSpace;

/**
 * @author Stu B. <www.texpedient.com>
 */
public abstract class PumaConfigManager {
	// A query interface instance we can reuse - right now just to trigger repo reloads. May want to do that via
	// GlobalConfigEmitter or some other interface in the long run...?

	private RepoClient myCurrentMainConfigRepoClient;
	// A managed service instance of the GlobalConfigEmitter, currently used only by LifterLifecycle.
	// We need to keep track of it so we can stop and restart it for Lift "refresh"
	OSGiComponent myGcComp;
	// Same with the managed queryinterface used by Lift
	OSGiComponent myQueryComp;

	// Here's a GlobalConfigEmitter for our PUMA instance. Does it really belong here? Time will tell.
	private GlobalConfigEmitter myGlobalConfig;

	abstract public void applyDefaultRepoClientAsMainConfig(PumaContextMediator mediator, BundleContext optBundCtxForLifecycle);
	
	public GlobalConfigEmitter getGlobalConfig() {
		return myGlobalConfig;
	}
	protected void setMainConfigRepoClient(RepoClient rc) {
		myCurrentMainConfigRepoClient = rc;
	}
	public void clearMainConfigRepoClient() {
		myCurrentMainConfigRepoClient = null;
	}
	public RepoClient getMainConfigRepoClient() {
		return myCurrentMainConfigRepoClient;
	}
	
	public RepoClient getOrMakeMainConfigRepoClient(PumaContextMediator mediator, BundleContext optBundCtxForLifecycle) {
		if (myCurrentMainConfigRepoClient == null) {
			applyDefaultRepoClientAsMainConfig(mediator, optBundCtxForLifecycle);
			// applyFreshDefaultMainRepoClientToGlobalConfig(optBundCtxForLifecycle);
		}
		return myCurrentMainConfigRepoClient;
	}
 
	// This may be the same thing as updateGlobalConfig eventually. Right now we are holding open the possibility that Lifter is acting on
	// one global config and the rest of Cog Char on another. This allows us to update one but not the other, since Lifter uses the GlobalConfigService
	// and everything else uses myGlobalConfig in this class. (Lifter auto-updates when the GlobalConfigService restarts.)
	// But really this is a can of worms, so probably we should move to having both the
	// GlobalConfigService and myGlobalConfig always be updated at the same time. Not yet though, until the possible implications are worked through...

	public void applyGlobalConfig(BundleContext optBundCtxForLifecycle) {
		RepoClient repoCli = getMainConfigRepoClient();
		Ident gcIdent = PumaModeConstants.makeRoleIdent(PumaModeConstants.DEFAULT_GLOBAL_MODE_NAME);
		myGlobalConfig = new GlobalConfigEmitter(repoCli, gcIdent);
		if (optBundCtxForLifecycle != null) {
			startGlobalConfigService(optBundCtxForLifecycle);
		}
	}
	/*
	// Used to be called "updateGlobalConfig" - Currently this would cause a detach from any previous lifecycle-registered RepoCli.
	public void applyFreshDefaultMainRepoClientToGlobalConfig(BundleContext optBundCtxForLifecycle) {
		clearMainConfigRepoClient();
		applyGlobalConfig(optBundCtxForLifecycle);
	}
	* 
	*/ 



	
	// TODO : This can be pushed down into o.c.lib.core
	protected static OSGiComponent startRepoClientLifecyle(BundleContext bundCtx, RepoClient rc) {
		OSGiComponent rcComp = null;
		if (rc != null) {
			ServiceLifecycleProvider lifecycle = new SimpleLifecycle(rc, RepoClient.class);
			rcComp = new OSGiComponent(bundCtx, lifecycle);
			rcComp.start();
		}
		return rcComp;
	}
	
	public void clearOSGiComps() {
		if (myGcComp != null) {
			myGcComp.dispose();
		}
		if (myQueryComp != null) {
			myQueryComp.dispose();
		}
	}
	
	public Ident resolveGraphForCharAndRole(Ident charID, Ident roleID) { 
		return myGlobalConfig.ergMap().get(charID).get(roleID);
	}

	// Right now this really feels wrong to make this a service! I don't believe in these maps enough yet.
	// Putting it here since it's more experimental than the GlobalConfigEmitter itself, but if this ends up
	// being the "preferred" solution this interface should probably go into o.c.blob.emit
	class GlobalConfigServiceImpl implements GlobalConfigEmitter.GlobalConfigService {

		@Override public java.util.HashMap<Ident, java.util.HashMap<Ident, Ident>> getErgMap() {
			return myGlobalConfig.ergMap();
		}
		@Override public java.util.HashMap<String, java.util.List<Ident>> getEntityMap() {
			return myGlobalConfig.entityMap();
		}
	}
	
	// Now here's something I was hoping to avoid, but it necessary for our experiment in making Lift a managed
	// service. This is best seen as a trial of one possible way to handle the "GlobalMode" graph configuration.
	// What we'll do here is tell the PumaAppContext to make the GlobalConfigEmitter available as a no-lifecycle
	// managed service. (Why no-lifecycle? Because these lifecycles have to end somewhere! But it would make sense
	// to make this service depend on the query interface if we decide to keep it.)
	// Then Lifter can access it to load its config.
	// The problem with this approach is that it elevates the GlobalConfigEmitter to a data structure of particular 
	// importance outside of PUMA (we're putting it on the OSGi registry for crying out loud!), when at this early
	// point I've been trying to keep non-PUMA code "agnostic" to any details of the graph "mode" config other than
	// the Idents of the graph.
	// So this may be a bad-idea-dead-end. Unless we decide we've fallen in love with both the GlobalConfigEmitter
	// and the idea of doing config via managed services, in which it may turn out to be just what we need.
	// For now, we'll restrict usage of this to the LifterLifeCycle only...
	public boolean startGlobalConfigService(BundleContext bundCtx) {
		boolean success = false;
		if (myGlobalConfig != null) {
			ServiceLifecycleProvider lifecycle =
					new SimpleLifecycle(new GlobalConfigServiceImpl(), GlobalConfigEmitter.GlobalConfigService.class);
			myGcComp = new OSGiComponent(bundCtx, lifecycle);
			myGcComp.start();
			success = true;
		}
		return success;
	}
	
	
	public void populateCommandSpace(CommandSpace cmdSpc, BoxSpace boxSpc) {
		RepoClient repoCli = getMainConfigRepoClient();
		TriggerItems.populateCommandSpace(repoCli, cmdSpc, boxSpc);
	}
}
