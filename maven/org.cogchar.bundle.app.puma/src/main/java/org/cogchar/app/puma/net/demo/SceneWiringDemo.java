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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.appdapter.bind.rdf.jena.assembly.CachingComponentAssembler;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.app.puma.behavior.OSGiTheater;
import org.cogchar.blob.emit.BehavMasterConfigTest;
import org.cogchar.blob.emit.EnhancedRepoClient;
import org.cogchar.impl.scene.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.robokind.api.common.lifecycle.ManagedService;
import org.robokind.api.common.osgi.OSGiUtils;
import org.robokind.api.common.osgi.lifecycle.OSGiComponent;

import org.cogchar.bind.rk.behavior.SceneSpecExtender;
/**
 * @author Stu B. <www.texpedient.com>
 */

public class SceneWiringDemo extends WiringDemo {
	
	public static String GROUP_KEY_SCENE_SPEC = "SceneSpecGroupId";
	public String		myDefaultSceneGroupQN = "demo_master_scene_group_33";
	
	public	String		myDefaultDirectGraphQN = "hrk:behav_file_82";
	public	String		myDefaultDerivedGraphQN = "hrk:merged_model_5001";
				
	public SceneWiringDemo(BundleContext bc, EnhancedRepoClient rc) {
		super(bc, rc);
	}

	public void initialSceneLoad(BundleContext bundleCtx, RepoClient demoRepoClient,  String directGraphQN, String derivedGraphQN,
					String sceneGroupQN) {
		getLogger().info("************************ initialSceneLoad()");
		
		loadAndRegisterSceneSpecs(bundleCtx, demoRepoClient, directGraphQN, derivedGraphQN, sceneGroupQN);
	}

	public void loadAndRegisterSceneSpecs(BundleContext bundleCtx, RepoClient demoRepoClient, String directGraphQN, String derivedGraphQN, String sceneGroupQN) {
		Collection<SceneSpec> sceneSpecs = loadDemoSceneSpecs(demoRepoClient, directGraphQN, derivedGraphQN);
		setupSceneSpecOSGiComps(bundleCtx, sceneSpecs,  sceneGroupQN);
	}

	public void unregisterAllSceneSpecs(BundleContext bundleCtx) {
		try {
			ServiceReference[] refs = bundleCtx.getServiceReferences(ManagedService.class.getName(),
					OSGiUtils.createFilter(ManagedService.PROP_SERVICE_TYPE,
					SceneSpec.class.getName()));
			for (ServiceReference ref : refs) {
				OSGiComponent comp = (OSGiComponent) bundleCtx.getService(ref);
				getLogger().info("Disposing of component {}", comp);
				comp.dispose();
			}
		} catch (Throwable t) {
			getLogger().error("Problem looking up OSGiComponents for existing SceneSpecs", t);
		}
	}

	
	public List<SceneSpec> loadDemoSceneSpecs(RepoClient bmcRepoCli, String directGraphQN, String derivedGraphQN) {
		getLogger().info("************************ loadDemoSceneSpecs()");

		// SceneBook sceneBook = SceneBook.readSceneBookFromRepo(bmcRepoCli, chanGraphID, behavGraphID);
		List<SceneSpec> ssList = readSceneSpecsFromRepoClient(bmcRepoCli, directGraphQN);

		
		Ident derivedBehavGraphID = bmcRepoCli.makeIdentForQName(derivedGraphQN);
		List<SceneSpec> bonusList = BehavMasterConfigTest.readSceneSpecsFromDerivedRepo(bmcRepoCli.getRepo(), bmcRepoCli, derivedBehavGraphID);

		List<SceneSpec> comboList = new ArrayList<SceneSpec>();
		comboList.addAll(ssList);
		comboList.addAll(bonusList);
		getLogger().info("Loaded {} SceneSpecs ", comboList.size());
		getLogger().debug("Loaded SceneSpecs {} ", comboList);
		return comboList;
	}
	public List<SceneSpec> readSceneSpecsFromRepoClient(RepoClient repoCli, String sceneSpecsGraphQN) {
		Ident behavGraphID = repoCli.makeIdentForQName(sceneSpecsGraphQN);
		Set<Object> allBehavSpecs = repoCli.assembleRootsFromNamedModel(behavGraphID);
		List<SceneSpec> ssList = SceneBook.filterSceneSpecs(allBehavSpecs);
		return ssList;
	}
	public void playSceneCleanly(OSGiTheater osgiThtr, BScene scene, boolean cancelPrevOutJobs) {
		Theater thtr = osgiThtr.getTheater();
		if (thtr != null) {
			thtr.stopAllScenesAndModules(cancelPrevOutJobs);
			thtr.exclusiveActivateScene(scene, cancelPrevOutJobs);
		}		
	}
	public void reloadScenes(OSGiTheater osgiThtr, boolean cancelOutJobs) {
		Theater thtr = osgiThtr.getTheater();
		if (thtr != null) {
			// TODO:  Get/save the bundleCtx from somewhere, as it is needed when we register new scene specs.
			BundleContext bundleCtx = getDefaultBundleContext();
			EnhancedRepoClient srcRepoCli = getDefaultRepoClient();
			EnhancedRepoClient reloadedClient = srcRepoCli.reloadRepoAndClient();
		// Reload the whole dang source repo:
		// TODO: Tell the repo to just reload certain graphs (need Appdapter 1.1.1 features to do this cleanly)			
			reloadScenes(bundleCtx, thtr, reloadedClient, cancelOutJobs);
		}
	}
	public void reloadScenes(BundleContext bunCtx, Theater thtr, EnhancedRepoClient srcRepoCli, boolean cancelOutJobs) {
		int killTimeWaitMsec = 250;
		//myWebMapper.disconnectLiftSceneInterface(myBundleCtx); // Now done in PumaAppContext.reloadAll
		thtr.fullyStop(killTimeWaitMsec, cancelOutJobs);
		// Dump old scenes
		unregisterAllSceneSpecs(bunCtx);
		
		// Clear the yucky global swizzle-caches (for scenes + behaviors, but not channels)
		CachingComponentAssembler.clearCacheForAssemblerSubclass(SceneSpecBuilder.class);
		CachingComponentAssembler.clearCacheForAssemblerSubclass(BehaviorSpecBuilder.class);

		
		// This method will automatically rebuild the DerivedRepo we are currently reading behavior from.
		// If bundleCtx != null, then it will 
		loadAndRegisterSceneSpecs(bunCtx, srcRepoCli, myDefaultDirectGraphQN, myDefaultDerivedGraphQN, myDefaultSceneGroupQN);
		// Start Theater again
		thtr.startThread();
	}
	
	public List<Runnable> makeSceneSpecRegRunnables(BundleContext bundleCtx, Collection<SceneSpec> sceneSpecs,
					String sceneGroupQN) {
		List<Runnable> runnables = new ArrayList<Runnable>();
		for (SceneSpec ss : sceneSpecs) {

			
			Runnable ssRegRunnable = makeSceneSpecRegRunnable(bundleCtx, ss, GROUP_KEY_SCENE_SPEC, sceneGroupQN);
			getLogger().info("Registered scene-spec and made runnable for {} ", ss.getIdent());
			getLogger().debug("Registered scene-spec details: {} ", ss);
			runnables.add(ssRegRunnable);
		}
		return runnables;
	}
	public void setupSceneSpecOSGiComps(BundleContext bundleCtx, Collection<SceneSpec> sceneSpecs, String sceneGroupQN) {
		getLogger().info("************************ setupSceneSpecOSGiComps()");
		Collection<Runnable> sceneSpecRegRunnables = makeSceneSpecRegRunnables(bundleCtx, sceneSpecs,  sceneGroupQN);
		for (Runnable r : sceneSpecRegRunnables) {
			r.run();
		}

	}
	@Override public void registerJFluxExtenders(BundleContext bundleCtx) {
		SceneSpecExtender sse = new SceneSpecExtender(bundleCtx, null, null);
		sse.start();
	}

    public static Runnable makeSceneSpecRegRunnable(BundleContext context, SceneSpec scnSpec, 
				final String key, final String val) {
		return getRegistrationRunnable(context, SceneSpec.class, scnSpec, key, val);
	}


	
}
