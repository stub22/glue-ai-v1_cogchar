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
package org.cogchar.bind.lift;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.appdapter.core.item.FreeIdent;
import org.appdapter.core.item.Ident;
import org.cogchar.blob.emit.GlobalConfigEmitter;
import org.cogchar.blob.emit.QueryInterface;
import org.cogchar.blob.emit.SolutionList;
import org.robokind.api.common.lifecycle.AbstractLifecycleProvider;
import org.robokind.api.common.lifecycle.utils.DescriptorListBuilder;

/**
 *
 * @author Ryan Biggs
 */
public class LifterLifecycle extends AbstractLifecycleProvider<LiftAmbassador.LiftAmbassadorInterface, LiftAmbassador.inputInterface> {

	private final static Logger theLogger = Logger.getLogger(LifterLifecycle.class.getName());
	static final String LIFTER_ENTITY_TYPE = "WebappEntity";
	public static final String rkrt = "urn:ftd:robokind.org:2012:runtime#";
	public static Ident LIFT_CONFIG_ROLE = new FreeIdent(rkrt + "lifterConf", "lifterConf");
	public static Ident GENERAL_CONFIG_ROLE = new FreeIdent(rkrt + "generalConf", "generalConf");
	private final static String queryEmitterId = "queryInterface";
	private final static String globalConfigId = "globalConfig";
	private final static String theLiftAppInterfaceId = "liftAppInterface";
	private final static String theLiftSceneInterfaceId = "liftSceneInterface";
	private final static String theLiftNetConfigInterfaceId = "liftNetConfigInterface";

	static class OurDescriptorBuilder {

		static DescriptorListBuilder get() {
			DescriptorListBuilder dlb = new DescriptorListBuilder()
					.dependency(queryEmitterId, QueryInterface.class)
					.dependency(globalConfigId, GlobalConfigEmitter.GlobalConfigService.class)
					.dependency(theLiftAppInterfaceId, LiftAmbassador.LiftAppInterface.class).optional()
					.dependency(theLiftSceneInterfaceId, LiftAmbassador.LiftSceneInterface.class).optional()
					.dependency(theLiftNetConfigInterfaceId, LiftAmbassador.LiftNetworkConfigInterface.class).optional();
			return dlb;
		}
	}

	public LifterLifecycle() {
		super(OurDescriptorBuilder.get().getDescriptors());

		if (myRegistrationProperties == null) {
			myRegistrationProperties = new Properties();
		}
		//myRegistrationProperties.put(Robot.PROP_ID, robotId.getRobtIdString()); // Not sure which ones we need yet
	}

	@Override
	protected synchronized LiftAmbassador.inputInterface create(Map<String, Object> dependencies) {
		theLogger.info("Creating LiftAmbassador.inputInterface in LifterLifecycle");
		LiftAmbassador.setAppInterface((LiftAmbassador.LiftAppInterface) dependencies.get(theLiftAppInterfaceId));
		LiftAmbassador.setSceneLauncher((LiftAmbassador.LiftSceneInterface) dependencies.get(theLiftSceneInterfaceId));
		LiftAmbassador.setNetConfigInterface((LiftAmbassador.LiftNetworkConfigInterface) dependencies.get(theLiftNetConfigInterfaceId));
		connectWebContent((QueryInterface) dependencies.get(queryEmitterId),
				(GlobalConfigEmitter.GlobalConfigService) dependencies.get(globalConfigId));
		return new LiftAmbassador.inputInterface();
	}

	@Override
	protected void handleChange(String serviceId, Object dependency, Map<String, Object> availableDependencies) {
		//super.handleChange(name, dependency, availableDependencies); //Needed?
		theLogger.log(Level.INFO, "LifterLifecycle handling change to {0}", serviceId);
		if ((queryEmitterId.equals(serviceId)) && (dependency != null)) {
			connectWebContent((QueryInterface) dependency,
					(GlobalConfigEmitter.GlobalConfigService) availableDependencies.get(globalConfigId));
		} else if ((globalConfigId.equals(serviceId)) && (dependency != null)) {
			connectWebContent((QueryInterface) availableDependencies.get(queryEmitterId),
					(GlobalConfigEmitter.GlobalConfigService) dependency);
		} else if (theLiftAppInterfaceId.equals(serviceId)) {
			LiftAmbassador.setAppInterface((LiftAmbassador.LiftAppInterface) dependency);
		} else if (theLiftSceneInterfaceId.equals(serviceId)) {
			LiftAmbassador.setSceneLauncher((LiftAmbassador.LiftSceneInterface) dependency);
		} else if (theLiftNetConfigInterfaceId.equals(serviceId)) {
			LiftAmbassador.setNetConfigInterface((LiftAmbassador.LiftNetworkConfigInterface) dependency);
		}
	}

	@Override
	public Class<LiftAmbassador.LiftAmbassadorInterface> getServiceClass() {
		return LiftAmbassador.LiftAmbassadorInterface.class;
	}

	public void connectWebContent(QueryInterface qi, GlobalConfigEmitter.GlobalConfigService configService) {
		// First we need to figure out which graph to use, so we'll use our fabulous GlobalConfigService
		List<Ident> webAppEntities = configService.getEntityMap().get(LIFTER_ENTITY_TYPE);
		// Not sure what multiple web app entities would mean right now, so for now we'll assume there should be only one
		if (webAppEntities.size() > 1) {
			theLogger.warning("Multiple Web App Entities detected in global config! Ignoring all but the first");
		}
		if (webAppEntities.isEmpty()) {
			theLogger.warning("Could not find a specified web app entity, cannot create lift config");
		} else {
			Ident qGraph;
			// Get the graph for the LiftConfig
			try {
				qGraph = configService.getErgMap().get(webAppEntities.get(0)).get(LIFT_CONFIG_ROLE);
			} catch (Exception e) {
				theLogger.warning("Could not retrieve graph for lift config");
				return;
			}
			// Provide queryInterface to LiftAmbassador so it can reload lift configs
			LiftAmbassador.setQueryInterface(qi, qGraph);
			// Load web app "home" startup screen config
			Ident startupConfigIdent = getStartupLiftConfig(qi, qGraph);
			if (startupConfigIdent != null) {
				LiftConfig lc = new LiftConfig(qi, qGraph, startupConfigIdent);
				LiftAmbassador.activateControlsFromConfig(lc);
			}
			// Get the graph for the general config
			try {
				qGraph = configService.getErgMap().get(webAppEntities.get(0)).get(GENERAL_CONFIG_ROLE);
			} catch (Exception e) {
				theLogger.warning("Could not retrieve graph for general config");
				return;
			}
			// Load "chat app" config
			ChatConfig cc = new ChatConfig(qi, qGraph);
			LiftAmbassador.storeChatConfig(cc);
		}
	}
	
	// Queries for the desired liftConfig to be displayed at startup
	private Ident getStartupLiftConfig(QueryInterface qi, Ident graphIdent) {
		Ident startupConfig =  null;
		SolutionList solutionList = qi.getQueryResultList(LiftQueryNames.START_CONFIG_QUERY_URI, graphIdent);
		List<Ident> startupConfigList = qi.getIdentsFromSolutionAsJava(solutionList, LiftQueryNames.CONFIG_VAR_NAME);
		if (startupConfigList.size() < 1) {
			theLogger.severe("Did not find a startup liftConfig! Web app will not function.");
		} else {
			startupConfig = startupConfigList.get(0);
			if (startupConfigList.size() > 1) {
				theLogger.log(Level.WARNING, "Found more than one startup liftConfig; using {0} and ignoring the rest", startupConfig);
			}
		}
		return startupConfig;
	}

}