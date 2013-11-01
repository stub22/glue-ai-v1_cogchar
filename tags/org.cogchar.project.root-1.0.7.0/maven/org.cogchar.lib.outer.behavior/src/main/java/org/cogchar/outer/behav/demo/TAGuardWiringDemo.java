/*
 * Copyright 2013 by The Cogchar Project (www.cogchar.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cogchar.outer.behav.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.bind.rdf.jena.model.JenaFileManagerUtils;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.impl.scene.ThingActionGuardSpec;
import org.cogchar.impl.thing.filters.ThingActionFilterBuilder;
import org.osgi.framework.BundleContext;
import org.jflux.impl.services.rk.lifecycle.ManagedService;
import org.jflux.impl.services.rk.lifecycle.ServiceLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.utils.SimpleLifecycle;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponent;

/**
 *
 * @author matt
 */
public class TAGuardWiringDemo {

	public static final String GROUP_KEY_FOR_TA_Guard_SPEC = "taGuardSpecGroupId";
	public static final String TA_Guard_GROUP_QN = "demoTAGuardGroup";

	public static Map<ThingActionGuardSpec, ManagedService> loadAndRegisterSpecs(
			BundleContext context, RepoClient defaultDemoRepoClient, String... taGuardGraphQNs) {
		
		JenaFileManagerUtils.ensureClassLoaderRegisteredWithDefaultJenaFM(TAGuardWiringDemo.class.getClassLoader());
		JenaFileManagerUtils.ensureClassLoaderRegisteredWithDefaultJenaFM(ThingActionGuardSpec.class.getClassLoader());
		JenaFileManagerUtils.ensureClassLoaderRegisteredWithDefaultJenaFM(ThingActionFilterBuilder.class.getClassLoader());
//		AssemblerUtils.buildAllObjectsInRdfFile("file:./bmd_A.ttl");

		Map<ThingActionGuardSpec, ManagedService> specServices = new HashMap<ThingActionGuardSpec, ManagedService>();

		List<ThingActionGuardSpec> specs = new ArrayList<ThingActionGuardSpec>();
		for (String taGuardGraphQN : taGuardGraphQNs) {
			specs.addAll(loadTAGuardSpecs(defaultDemoRepoClient, taGuardGraphQN));
		}
		for (ThingActionGuardSpec spec : specs) {
			if (specServices.containsKey(spec)) {
				continue;
			}
			ManagedService service = registerTAGuardSpec(context, spec);
			specServices.put(spec, service);
		}
		return specServices;
	}

	private static List<ThingActionGuardSpec> loadTAGuardSpecs(RepoClient defaultDemoRepoClient, String taGuardGraphQN) {
		List<ThingActionGuardSpec> specs = new ArrayList();
		// Determine the URI for the 'qualified name' which identifies the data in the repo
		Ident taGuardGraphID = defaultDemoRepoClient.makeIdentForQName(taGuardGraphQN);
		// Collect the objects from the repo, building them from RDF raw data
		Set<Object> assembledRoots = defaultDemoRepoClient.assembleRootsFromNamedModel(taGuardGraphID);
		for (Object root : assembledRoots) {
			// Ignore anything that is not a ConnectionSpec
			if (root == null || !ThingActionGuardSpec.class.isAssignableFrom(root.getClass())) {
				continue;
			}
			specs.add((ThingActionGuardSpec) root);
		}
		return specs;
	}

	/**
	 * Allows JFlux to register TAGuard specs
	 * 
	 *  I know it might be considered weird to do so .. but sounds like fun!
	 * 
	 * @param context the BundleContext used to register the spec
	 * @param taGuardSpec the spec to be registered
	 */
	private static ManagedService registerTAGuardSpec(
			BundleContext context, ThingActionGuardSpec taGuardSpec) {
		Properties props = new Properties();
		props.put(GROUP_KEY_FOR_TA_Guard_SPEC, TA_Guard_GROUP_QN);

		ServiceLifecycleProvider lifecycle =
				new SimpleLifecycle(taGuardSpec, ThingActionGuardSpec.class);
		ManagedService ms = new OSGiComponent(context, lifecycle, props);
		ms.start();
		return ms;
	}
}
