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

import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.core.matdat.*;
import org.appdapter.help.repo.*;
import org.appdapter.core.repo.*;
import org.appdapter.core.name.Ident;
import org.cogchar.bind.mio.behavior.SceneSpecExtender;
import org.cogchar.impl.scene.BehaviorSpecBuilder;
import org.cogchar.impl.scene.SceneSpec;
import org.cogchar.impl.scene.SceneSpecBuilder;
import org.cogchar.impl.scene.read.SceneSpecReader;
import org.cogchar.impl.thing.filters.ThingActionFilterBuilder;
import org.cogchar.name.behavior.MasterDemoNames;
import org.jflux.impl.services.rk.lifecycle.ManagedService;
import org.jflux.impl.services.rk.osgi.OSGiUtils;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponent;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.appdapter.bind.rdf.jena.query.JenaArqQueryFuncs_TxAware.Oper;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class SceneWiringDemo extends WiringDemo {

	public static String GROUP_KEY_SCENE_SPEC = MasterDemoNames.GROUP_KEY_SCENE_SPEC;
	public String myDefaultSceneGroupQN = MasterDemoNames.SCENE_GROUP_QN;

	public String myDefaultDirectGraphQN = MasterDemoNames.DIRECT_BEHAV_GRAPH_QN;

	public PipelineQuerySpec myDefaultPipelineQuerySpec = new PipelineQuerySpec(MasterDemoNames.PIPE_QUERY_QN,
			MasterDemoNames.PIPE_SOURCE_QUERY_QN, MasterDemoNames.PIPELINE_GRAPH_QN);
	public String myDefaultDerivedGraphQN = MasterDemoNames.DERIVED_BEHAV_GRAPH_QN;

	//	public	String		myDefaultPipelineQueryQN = MasterDemoNames.PIPELINE_QUERY_QN;
	//	public	String		myDefaultPipelineGraphQN = MasterDemoNames.PIPELINE_GRAPH_QN;

	public SceneWiringDemo(BundleContext bc, RepoClient rc) {
		super(bc, rc);
	}

	public void initialSceneLoad(BundleContext bundleCtx, RepoClient directRepoClient, String directGraphQN,
			PipelineQuerySpec pipeQuerySpec, String derivedGraphQN, String sceneGroupQN) {
		getLogger().info("************************ initialSceneLoad()");

		loadAndRegisterSceneSpecs(bundleCtx, directRepoClient, directGraphQN, pipeQuerySpec, derivedGraphQN, sceneGroupQN);
	}

	public void loadAndRegisterSceneSpecs(BundleContext bundleCtx, RepoClient directRC, String directGraphQN,
			PipelineQuerySpec pipeQuerySpec, String derivedGraphQN, String sceneGroupQN) {
		Collection<SceneSpec> sceneSpecs = loadDemoSceneSpecs_TX(directRC, directGraphQN, pipeQuerySpec, derivedGraphQN);
		setupSceneSpecOSGiComps(bundleCtx, sceneSpecs, sceneGroupQN);
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
	public List<SceneSpec> loadDemoSceneSpecs_TX(final RepoClient bmcRepoCli, final String directGraphQN,
			final PipelineQuerySpec pipeQuerySpec, final String derivedGraphQN) {
		List<SceneSpec> res = null;
		// Does it need to be a write?  Is the derived model going into the dataset?
		res = RepoClientFuncs_TxAware.execReadTransCompatible(bmcRepoCli, null, new Oper<List<SceneSpec>>() {
			@Override public List<SceneSpec> perform() {
				return loadDemoSceneSpecs_Raw(bmcRepoCli, directGraphQN, pipeQuerySpec, derivedGraphQN);
			}
		});
		return res;
	}
	private List<SceneSpec> loadDemoSceneSpecs_Raw(RepoClient bmcRepoCli, String directGraphQN,
			PipelineQuerySpec pipeQuerySpec, String derivedGraphQN) {

		// SceneBook = "old" way, in which it was more obvious that channels are being resolved from Swizzle cache 
		// SceneBook sceneBook = SceneBook.readSceneBookFromRepo(bmcRepoCli, chanGraphID, behavGraphID);
		// Swizzle-caches are still used to find channels here, but are hidden in static variables.  Ewww!
		//		Ident directBehavGraphID = bmcRepoCli.makeIdentForQName(directGraphQN);
		//		BoundModelProvider directBMP = ModelProviderFactory.makeOneDirectModelProvider(bmcRepoCli, directBehavGraphID);
		//		List<SceneSpec> ssList = readSceneSpecsFromBMP(directBMP);
		// readSceneSpecsFromDirectGraph(bmcRepoCli, directGraphQN);
		Ident derivedBehavGraphID = bmcRepoCli.makeIdentForQName(derivedGraphQN);
		BoundModelProvider derivedBMP = ModelProviderFactory.makeOneDerivedModelProvider(bmcRepoCli, pipeQuerySpec, derivedBehavGraphID);
		// List<SceneSpec> bonusList = readSceneSpecsFromDerivedGraph(bmcRepoCli, pipeQuerySpec, derivedGraphQN);
		List<SceneSpec> bonusList = readSceneSpecsFromBMP(derivedBMP);

		List<SceneSpec> comboList = new ArrayList<SceneSpec>();
		//		comboList.addAll(ssList);
		comboList.addAll(bonusList);
		getLogger().info("Loaded {} SceneSpecs ", comboList.size());
		getLogger().debug("Loaded SceneSpecs {} ", comboList);
		return comboList;
	}

	public List<SceneSpec> readSceneSpecsFromBMP(BoundModelProvider bmp) {
		getLogger().info("loading SceneSpecs from BMP: {}", bmp);
		List<SceneSpec> ssList = new ArrayList<SceneSpec>();
		try {
			//	FancyRepo fr = (FancyRepo) bmcRepoCli.getRepo();
			//	Ident derivedBehavGraphID = bmcRepoCli.makeIdentForQName(derivedGraphQN);
			SceneSpecReader ssr = getSceneSpecReader();
			ssList = ssr.readSceneSpecsFromBMP(bmp);
		} catch (Throwable t) {
			getLogger().error("Problem loading sceneSpecs from bound graph {}", bmp, t);
		}
		return ssList;
	}

	/**
	 * 
	 * @param bunCtx
	 *            - needed when we register + unregister scene spces to OSGi
	 * @param freshRepoCli
	 *            - a source of data assumed to already be in a fresh + tasty state.
	 */
	public void reloadSceneSpecs(BundleContext bunCtx, RepoClient freshRepoCli) {
		// Dump old scenes from OSGi registry.  
		unregisterAllSceneSpecs(bunCtx);

		// Clear the swizzle-caches (for scenes + behaviors, but not channels)
		AssemblerUtils.clearCacheForAssemblerSubclassForSession(SceneSpecBuilder.class, AssemblerUtils.getDefaultSession());
		AssemblerUtils.clearCacheForAssemblerSubclassForSession(BehaviorSpecBuilder.class, AssemblerUtils.getDefaultSession());
		AssemblerUtils.clearCacheForAssemblerSubclassForSession(ThingActionFilterBuilder.class, AssemblerUtils.getDefaultSession());

		// This method will both reload sceneSpecs from the given repoCli, and also
		//  automatically rebuild+reload-from any DerivedRepo we are currently reading 
		// "bonus" behavior from.  (For example, guarded behavior demos using Cogchar 1.0.6).
		loadAndRegisterSceneSpecs(bunCtx, freshRepoCli, myDefaultDirectGraphQN, myDefaultPipelineQuerySpec,
				myDefaultDerivedGraphQN, myDefaultSceneGroupQN);
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
		Collection<Runnable> sceneSpecRegRunnables = makeSceneSpecRegRunnables(bundleCtx, sceneSpecs, sceneGroupQN);
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
