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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.appdapter.bind.rdf.jena.assembly.CachingComponentAssembler;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.appdapter.impl.store.FancyRepo;
import org.cogchar.blob.emit.BehavMasterConfigTest;
import org.cogchar.blob.emit.EnhancedRepoClient;
import org.cogchar.impl.scene.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.robokind.api.common.lifecycle.ManagedService;
import org.robokind.api.common.osgi.OSGiUtils;
import org.robokind.api.common.osgi.lifecycle.OSGiComponent;

import org.cogchar.bind.rk.behavior.SceneSpecExtender;

import org.cogchar.name.behavior.MasterDemoNames;
/**
 * @author Stu B. <www.texpedient.com>
 */

public class SceneWiringDemo extends WiringDemo {
	
	public static String GROUP_KEY_SCENE_SPEC = MasterDemoNames.GROUP_KEY_SCENE_SPEC;
	public String		myDefaultSceneGroupQN = MasterDemoNames.SCENE_GROUP_QN;
	
	public	String		myDefaultDirectGraphQN = MasterDemoNames.DIRECT_BEHAV_GRAPH_QN;
	
	public	String		myDefaultPipelineQueryQN = MasterDemoNames.PIPELINE_QUERY_QN;
	public	String		myDefaultPipelineGraphQN = MasterDemoNames.PIPELINE_GRAPH_QN;
	public	String		myDefaultDerivedGraphQN = MasterDemoNames.DERIVED_BEHAV_GRAPH_QN;
				
	public SceneWiringDemo(BundleContext bc, EnhancedRepoClient rc) {
		super(bc, rc);
	}

	public void initialSceneLoad(BundleContext bundleCtx, RepoClient demoRepoClient,  String directGraphQN, 
					String pipeQueryQN, String pipeGraphQN, String derivedGraphQN, String sceneGroupQN) {
		getLogger().info("************************ initialSceneLoad()");
		
		loadAndRegisterSceneSpecs(bundleCtx, demoRepoClient, directGraphQN, pipeQueryQN, pipeGraphQN, derivedGraphQN, sceneGroupQN);
	}

	public void loadAndRegisterSceneSpecs(BundleContext bundleCtx, RepoClient demoRepoClient, String directGraphQN, 
					String pipeQueryQN, String pipeGraphQN, String derivedGraphQN, String sceneGroupQN) {
		Collection<SceneSpec> sceneSpecs = loadDemoSceneSpecs(demoRepoClient, directGraphQN, pipeQueryQN, pipeGraphQN, derivedGraphQN);
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

	
	public List<SceneSpec> loadDemoSceneSpecs(RepoClient bmcRepoCli, String directGraphQN, String pipelineQueryQN, 
					String pipelineGraphQN, String derivedGraphQN) {

		
		// SceneBook = "old" way, in which it was more obvious that channels are being resolved from Swizzle cache 
		// SceneBook sceneBook = SceneBook.readSceneBookFromRepo(bmcRepoCli, chanGraphID, behavGraphID);
		// Swizzle-caches are still used to find channels here, but are hidden in static variables.  Ewww!
		List<SceneSpec> ssList = readSceneSpecsFromDirectGraph(bmcRepoCli, directGraphQN);
		List<SceneSpec> bonusList = readSceneSpecsFromDerivedGraph(bmcRepoCli, pipelineQueryQN, 
						pipelineGraphQN, derivedGraphQN);

		List<SceneSpec> comboList = new ArrayList<SceneSpec>();
		comboList.addAll(ssList);
		comboList.addAll(bonusList);
		getLogger().info("Loaded {} SceneSpecs ", comboList.size());
		getLogger().debug("Loaded SceneSpecs {} ", comboList);
		return comboList;
	}
	public List<SceneSpec> readSceneSpecsFromDirectGraph(RepoClient repoCli, String sceneSpecsGraphQN) {
		getLogger().info("loading SceneSpecs from direct graph {}", sceneSpecsGraphQN);
		List<SceneSpec> ssList = new ArrayList<SceneSpec>();
		try {
			Ident behavGraphID = repoCli.makeIdentForQName(sceneSpecsGraphQN);
			Set<Object> allBehavSpecs = repoCli.assembleRootsFromNamedModel(behavGraphID);
			ssList = SceneBook.filterSceneSpecs(allBehavSpecs);
		} catch (Throwable t) {
			getLogger().error("Problem loading sceneSpecs from direct graph {}", sceneSpecsGraphQN, t);
		}
		return ssList;
	}
	public List<SceneSpec> readSceneSpecsFromDerivedGraph(RepoClient bmcRepoCli, String pipelineQueryQN, 
					String pipelineGraphQN, String derivedGraphQN) {
		getLogger().info("loading SceneSpecs from derived graph {}", derivedGraphQN);
		List<SceneSpec> ssList = new ArrayList<SceneSpec>();
		try {
			FancyRepo fr = (FancyRepo) bmcRepoCli.getRepo();
			String resolvedQueryText = fr.resolveIndirectQueryText("ccrt:qry_sheet_77", pipelineQueryQN);
			getLogger().info("Found query text: {}", resolvedQueryText);		
			Ident derivedBehavGraphID = bmcRepoCli.makeIdentForQName(derivedGraphQN);
			ssList = BehavMasterConfigTest.readSceneSpecsFromDerivedRepo(bmcRepoCli, pipelineQueryQN, 
						pipelineGraphQN, derivedBehavGraphID);	
		} catch (Throwable t) {
			getLogger().error("Problem loading sceneSpecs from derived graph {}", derivedGraphQN, t);
		}
		return ssList;
	}
	/**
	 * 
	 * @param bunCtx - needed when we register + unregister scene spces to OSGi
	 * @param freshRepoCli - a source of data assumed to already be in a fresh + tasty state.
	 */
	public void reloadSceneSpecs(BundleContext bunCtx, EnhancedRepoClient freshRepoCli) {
		// Dump old scenes from OSGi registry.  
		unregisterAllSceneSpecs(bunCtx);
		
		// Clear the yucky global swizzle-caches (for scenes + behaviors, but not channels)
		CachingComponentAssembler.clearCacheForAssemblerSubclass(SceneSpecBuilder.class);
		CachingComponentAssembler.clearCacheForAssemblerSubclass(BehaviorSpecBuilder.class);

		// This method will both reload sceneSpecs from the given repoCli, and also
		//  automatically rebuild+reload-from any DerivedRepo we are currently reading 
		// "bonus" behavior from.  (For example, guarded behavior demos using Cogchar 1.0.6).
		loadAndRegisterSceneSpecs(bunCtx, freshRepoCli, myDefaultDirectGraphQN, myDefaultPipelineQueryQN, 
						myDefaultPipelineGraphQN, myDefaultDerivedGraphQN, myDefaultSceneGroupQN);
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
