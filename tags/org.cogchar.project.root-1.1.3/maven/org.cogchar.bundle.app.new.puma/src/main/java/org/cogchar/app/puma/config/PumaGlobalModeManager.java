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

package org.cogchar.app.puma.config;

import org.cogchar.name.entity.EntityRoleCN;
import org.appdapter.core.name.Ident;
import org.appdapter.fancy.rclient.RepoClient;

import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponent;
import org.cogchar.blob.emit.GlobalConfigEmitter;
import org.osgi.framework.BundleContext;
import org.jflux.impl.services.rk.lifecycle.ServiceLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.utils.SimpleLifecycle;


/**
 * @author Ryan B. and Stu B.
 * 
 * We are use this object class to capture the need for a top level assignment of role-graphs to characters.
 * The PUMA system allows us to set up any number of 
 */

public class PumaGlobalModeManager {
	// A managed service instance of the GlobalConfigEmitter, currently used only by LifterLifecycle.
	// We need to keep track of it so we can stop and restart it for Lift "refresh"
	OSGiComponent myGcComp;
	// Here's a GlobalConfigEmitter for our PUMA instance. Does it really belong here? Time will tell.
	private GlobalConfigEmitter myGlobalConfig;
	
	public GlobalConfigEmitter getGlobalConfig() {
		return myGlobalConfig;
	}
	
	// This may be the same thing as updateGlobalConfig eventually. Right now we are holding open the possibility that Lifter is acting on
	// one global config and the rest of Cog Char on another. This allows us to update one but not the other, since Lifter uses the GlobalConfigService
	// and everything else uses myGlobalConfig in this class. (Lifter auto-updates when the GlobalConfigService restarts.)
	// But really this is a can of worms, so probably we should move to having both the
	// GlobalConfigService and myGlobalConfig always be updated at the same time. Not yet though, until the possible implications are worked through...

	public void applyGlobalConfig(BundleContext optBundCtxForLifecycle, RepoClient repoCli) {
		Ident gcIdent = EntityRoleCN.makeRoleIdent(EntityRoleCN.DEFAULT_GLOBAL_MODE_NAME);
		myGlobalConfig = new GlobalConfigEmitter(repoCli, gcIdent);
		if (optBundCtxForLifecycle != null) {
			startGlobalConfigService(optBundCtxForLifecycle);
		}
	}
	// Used to resolve configuration graphs by:
	//		PumaDualBodyManager
	//		PumaAppContext
	public Ident resolveGraphForCharAndRole(Ident charID, Ident roleID) { 
		// This semi-hidden double-map is kinda ugly
		return myGlobalConfig.ergMap().get(charID).get(roleID);
	}

	class GlobalConfigServiceImpl implements GlobalConfigEmitter.GlobalConfigService {

		// Stu 2013-12-18:  This overall design using two maps, one of which is a map-of-maps, has never
		// been fully reviewed or approved.    We want to replace it with lifecycled goodies, in a
		// refined view of what a "character" is.
		
		// "ERG" stands for Entity-Role-Graph
		@Override public java.util.HashMap<Ident, java.util.HashMap<Ident, Ident>> getErgMap() {
			return myGlobalConfig.ergMap();
		}
		@Override public java.util.HashMap<String, java.util.List<Ident>> getEntityMap() {
			return myGlobalConfig.entityMap();
		}
	}
	// Stu 2013-12-18:   GlobalConfigEmitter has always been a placeholder slack-variable object, and one of the
	// primary manifestations of character-ness, without full clarity or formality.

	
	// Ryan - late 2012 sometime:
	// Now here's something I was hoping to avoid, but it necessary for our experiment in making Lift a managed
	// service. This is best seen as a trial of one possible way to handle the "GlobalMode" graph configuration.
	// What we'll do here is tell the PumaAppContext to make the GlobalConfigEmitter available as a no-lifecycle
	// managed service. (Why no-lifecycle? Because these lifecycles have to end somewhere! But it would make sense
	// to make this service depend on the query interface if we decide to keep it.)
	// Then Lifter can access it to load its config.
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
	
	public void clearOSGiComps() {
		if (myGcComp != null) {
			myGcComp.dispose();
		}
	}
}
