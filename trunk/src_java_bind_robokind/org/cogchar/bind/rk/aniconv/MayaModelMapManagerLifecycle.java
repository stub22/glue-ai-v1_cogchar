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

package org.cogchar.bind.rk.aniconv;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.blob.emit.GlobalConfigEmitter;
import org.osgi.framework.BundleContext;
import org.robokind.api.common.lifecycle.AbstractLifecycleProvider;
import org.robokind.api.common.lifecycle.utils.DescriptorListBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */

public class MayaModelMapManagerLifecycle extends AbstractLifecycleProvider<MayaModelMapManagerInterface, MayaModelMapManager> {

	private final static Logger theLogger = LoggerFactory.getLogger(MayaModelMapManagerLifecycle.class);

	private static BundleContext theContext;

	private final static String repoClientId = "repoClient";
	private final static String globalConfigId = "globalConfig";
	private final static String MAYA_MAP_ENTITY_TYPE = "MayaMappingEntity";
	// Below could use a little refactoring and perhaps a common source for rkrt
	private final static String rkrt = "urn:ftd:robokind.org:2012:runtime#";
	private final static Ident MAYA_MAP_ROLE = new FreeIdent(rkrt + "mayaModelConf", "mayaModelConf");

	static class OurDescriptorBuilder {

		static DescriptorListBuilder get() {
			DescriptorListBuilder dlb = new DescriptorListBuilder().dependency(repoClientId, RepoClient.class).dependency(globalConfigId, GlobalConfigEmitter.GlobalConfigService.class);
			return dlb;
		}
	}

	// A very ugly and hopefully temporary way to set the context needed to register the MayaModelMap for MayaModelSelector
	// I think I have the right idea here, but almost certainly am not using the best lifecycle/registry tools for the job just yet
	public static void setContext(BundleContext context) {
		theContext = context;
	}

	private MayaModelMapManager myManager;

	public MayaModelMapManagerLifecycle() {
		super(OurDescriptorBuilder.get().getDescriptors());

		if (myRegistrationProperties == null) {
			myRegistrationProperties = new Properties();
		}
	}

	@Override
	protected synchronized MayaModelMapManager create(Map<String, Object> dependencies) {
		theLogger.info("Creating MayaModelMapManager in MayaModelMapManagerLifecycle");
		myManager = getManager((RepoClient) dependencies.get(repoClientId), (GlobalConfigEmitter.GlobalConfigService) dependencies.get(globalConfigId));
		registerMaps(myManager);
		return myManager;
	}

	@Override
	protected void handleChange(String serviceId, Object dependency, Map<String, Object> availableDependencies) {
		//super.handleChange(name, dependency, availableDependencies); //Needed?
		theLogger.info("MayaModelMapLifecycle handling change to {}, new dependency is: {}", serviceId, ((dependency == null) ? "null" : "not null"));
		// With only two dependencies, the handling here is simple:
		if ((availableDependencies.get(repoClientId) != null) && (availableDependencies.get(globalConfigId) != null)) {
			myManager = getManager((RepoClient) availableDependencies.get(repoClientId), (GlobalConfigEmitter.GlobalConfigService) availableDependencies.get(globalConfigId));
			registerMaps(myManager);
		} else {
			theLogger.warn("A dependency was null; cannot make new Maya Map");
		}

	}

	private MayaModelMapManager getManager(RepoClient rc, GlobalConfigEmitter.GlobalConfigService configService) {
		Ident qGraph = getGraph(configService);
		MayaModelMapManager manager = null;
		if (qGraph != null) {
			manager = new MayaModelMapManager(rc, qGraph);
		}
		return manager;
	}

	// Registers maps so they can be found by aniconv dialog:
	private void registerMaps(MayaModelMapManager mgr) {
		if (mgr != null) {
			for (MayaModelMap map : mgr.myMMMs) {
				theContext.registerService(MayaModelMap.class.getName(), map, null);
			}
		}
	}

	private Ident getGraph(GlobalConfigEmitter.GlobalConfigService configService) {
		Ident qGraph = null;
		// First we need to figure out which graph to use, so we'll use our fabulous GlobalConfigService
		List<Ident> mayaMapEntities = (List<Ident>) configService.getEntityMap().get(MAYA_MAP_ENTITY_TYPE);
		// For now we'll assume there should only be one entity, although this assumption may well soon change
		if (mayaMapEntities.size() > 1) {
			theLogger.warn("Multiple Maya Map Entities detected in global config! Ignoring all but the first");
		}
		if (mayaMapEntities.isEmpty()) {
			theLogger.warn("Could not find a specified Maya Map entity, cannot create config for animation converter");
		} else {
			// Get the graph for the MayaMap
			qGraph = ((Map<Ident, Ident>) configService.getErgMap().get(mayaMapEntities.get(0))).get(MAYA_MAP_ROLE);
		}
		return qGraph;
	}

	@Override
	// Needs to be revisited
	public Class<MayaModelMapManagerInterface> getServiceClass() {
		return MayaModelMapManagerInterface.class;
	}

}
