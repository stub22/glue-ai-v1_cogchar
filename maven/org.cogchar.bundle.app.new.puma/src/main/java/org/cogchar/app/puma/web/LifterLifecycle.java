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

import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.appdapter.fancy.rclient.RepoClient;
import org.appdapter.fancy.query.SolutionHelper;
import org.appdapter.fancy.query.SolutionList;
import org.cogchar.api.web.WebAppInterfaceTracker;
import org.cogchar.api.web.WebSceneInterface;
import org.cogchar.impl.web.config.ChatConfig;
import org.cogchar.impl.web.config.LiftAmbassador;
import org.cogchar.impl.web.config.AvailableCommands;
import org.cogchar.impl.web.config.WebappNetworkConfigHandle;
import org.cogchar.impl.web.config.AmbassadorServiceHandle;
import org.cogchar.impl.web.config.AmbassadorServiceImpl;
import org.cogchar.impl.web.config.LiftConfig;
import org.cogchar.impl.web.config.UserAccessConfig;
import org.cogchar.blob.emit.GlobalConfigEmitter;
import org.cogchar.name.lifter.LiftCN;
import org.jflux.impl.services.rk.lifecycle.AbstractLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.utils.DescriptorListBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *Lives in the  Puma project so it can access the Robokind Lifecycle API.
 * @author Ryan Biggs
 */
public class LifterLifecycle extends AbstractLifecycleProvider<AmbassadorServiceHandle, AmbassadorServiceImpl> {

	private final static Logger theLogger = LoggerFactory.getLogger(LifterLifecycle.class);
	static final String LIFTER_ENTITY_TYPE = "WebappEntity";
	private final static String rkrt = "urn:ftd:robokind.org:2012:runtime#";
	private final static Ident LIFT_CONFIG_ROLE = new FreeIdent(rkrt + "lifterConf", "lifterConf");
	private final static Ident GENERAL_CONFIG_ROLE = new FreeIdent(rkrt + "generalConf", "generalConf");
	private final static Ident USER_ACCESS_CONFIG_ROLE = new FreeIdent(rkrt + "userConf", "userConf");
	private final static String queryEmitterId = "queryInterface";
	private final static String globalConfigId = "globalConfig";
	private final static String theAvailableCommands_Key = "availableCommands";
	private final static String theLiftSceneInterface_Key = "liftSceneInterface";
	private final static String theLiftNetConfigInterface_Key = "liftNetConfigInterface";

	static class OurDescriptorBuilder {

		static DescriptorListBuilder get() {
			DescriptorListBuilder dlb = new DescriptorListBuilder()
					.dependency(queryEmitterId, RepoClient.class)
					.dependency(globalConfigId, GlobalConfigEmitter.GlobalConfigService.class)
					.dependency(theAvailableCommands_Key, AvailableCommands.class).optional()
					.dependency(theLiftSceneInterface_Key, WebSceneInterface.class).optional()
					.dependency(theLiftNetConfigInterface_Key, WebappNetworkConfigHandle.class).optional();
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
	protected synchronized AmbassadorServiceImpl create(Map<String, Object> dependencies) {
		theLogger.info("Creating AmbassadorServiceImpl from LifterLifecycle");
		LiftAmbassador myLiftAmbassador = LiftAmbassador.getLiftAmbassador();
		myLiftAmbassador.setAvailableCommands((AvailableCommands) dependencies.get(theAvailableCommands_Key));
		myLiftAmbassador.setSceneLauncher((WebSceneInterface) dependencies.get(theLiftSceneInterface_Key));
		myLiftAmbassador.setNetConfigInterface((WebappNetworkConfigHandle) dependencies.get(theLiftNetConfigInterface_Key));
		connectWebContent(myLiftAmbassador, (RepoClient) dependencies.get(queryEmitterId),
				(GlobalConfigEmitter.GlobalConfigService) dependencies.get(globalConfigId));
		WebAppInterfaceTracker.getTracker().setWebInterface(myLiftAmbassador);
		return new AmbassadorServiceImpl();
	}

	@Override
	protected void handleChange(String serviceId, Object dependency, Map<String, Object> availableDependencies) {
		//super.handleChange(name, dependency, availableDependencies); //Needed?
		theLogger.info("LifterLifecycle handling change to {}, new dependency is: {}", serviceId, ((dependency == null)? "null" : "not null"));
		LiftAmbassador myLiftAmbassador = LiftAmbassador.getLiftAmbassador();
		if ((queryEmitterId.equals(serviceId)) && (dependency != null) && (availableDependencies.get(globalConfigId) != null)) {
			connectWebContent(myLiftAmbassador, (RepoClient) dependency,
					(GlobalConfigEmitter.GlobalConfigService) availableDependencies.get(globalConfigId));
		} else if ((globalConfigId.equals(serviceId)) && (dependency != null) && (availableDependencies.get(queryEmitterId) != null)) {
			connectWebContent(myLiftAmbassador, (RepoClient) availableDependencies.get(queryEmitterId),
					(GlobalConfigEmitter.GlobalConfigService) dependency);
		} else if (theAvailableCommands_Key.equals(serviceId)) {
			myLiftAmbassador.setAvailableCommands((AvailableCommands) dependency);
		} else if (theLiftSceneInterface_Key.equals(serviceId)) {
			myLiftAmbassador.setSceneLauncher((WebSceneInterface) dependency);
		} else if (theLiftNetConfigInterface_Key.equals(serviceId)) {
			myLiftAmbassador.setNetConfigInterface((WebappNetworkConfigHandle) dependency);
		}
	}

	@Override
	public Class<AmbassadorServiceHandle> getServiceClass() {
		return AmbassadorServiceHandle.class;
	}

	public void connectWebContent(LiftAmbassador la, RepoClient qi, GlobalConfigEmitter.GlobalConfigService configService) {
		// First we need to figure out which graph to use, so we'll use our fabulous GlobalConfigService
		List<Ident> webAppEntities = configService.getEntityMap().get(LIFTER_ENTITY_TYPE);
		// Not sure what multiple web app entities would mean right now, so for now we'll assume there should be only one
		if (webAppEntities.size() > 1) {
			theLogger.warn("Multiple Web App Entities detected in global config! Ignoring all but the first");
		}
		if (webAppEntities.isEmpty()) {
			theLogger.warn("Could not find a specified web app entity, cannot create lift config");
		} else {
			Ident liftConfigQGraph;
			// Get the graph for the LiftConfig
			try {
				liftConfigQGraph = configService.getErgMap().get(webAppEntities.get(0)).get(LIFT_CONFIG_ROLE);
			} catch (Exception e) {
				theLogger.warn("Could not retrieve graph for lift config");
				return;
			}
			// Provide queryInterface to LiftAmbassador so it can reload lift configs
			la.setRepoClient(qi, liftConfigQGraph);
			// Load web app "home" startup screen config and store for later when we see if we have a UserAccessConfig
			// with a login page, which we will use instead if so
			Ident startupConfigIdent = getStartupLiftConfig(qi, liftConfigQGraph);
			// Get the graph for the general config
			Ident qGraph;
			try {
				qGraph = configService.getErgMap().get(webAppEntities.get(0)).get(GENERAL_CONFIG_ROLE);
			} catch (Exception e) {
				theLogger.warn("Could not retrieve graph for general config");
				return;
			}
			// Load "chat app" config
			ChatConfig cc = new ChatConfig(qi, qGraph);
			la.storeChatConfig(cc);
			// Get the graph for the user access config
			try {
				qGraph = configService.getErgMap().get(webAppEntities.get(0)).get(USER_ACCESS_CONFIG_ROLE);
			} catch (Exception e) {
				theLogger.warn("Could not retrieve graph for user access config");
				return;
			}
			// Load user access config
			UserAccessConfig uac = null;
			try {
				uac = new UserAccessConfig(qi, qGraph);
				la.storeUserAccessConfig(uac);
			} catch (Exception e) {
				theLogger.warn("Error attempting to get user access config; it may not be defined. "
						+ "Will use startLiftConfig defined in Lifter resource instead of login page for new sessions. " 
						+ "Error is: ", e);
			}
			// If the uac has a non-null loginPage, use it, otherwise use the "home" page loaded earlier if present
			boolean foundLoginPage = false;
			if (uac != null) {
				if (uac.loginPage != null) {
					LiftConfig lc = new LiftConfig(qi, liftConfigQGraph, uac.loginPage);
					la.activateControlsFromConfig(lc);
					foundLoginPage = true;
				}
			} 
			if ((!foundLoginPage) && (startupConfigIdent != null)) {
				LiftConfig lc = new LiftConfig(qi, liftConfigQGraph, startupConfigIdent);
				la.activateControlsFromConfig(lc);
			}
		}
	}
	
	// Queries for the desired liftConfig to be displayed at startup
	private Ident getStartupLiftConfig(RepoClient qi, Ident graphIdent) {
		SolutionHelper sh = new SolutionHelper();
		Ident startupConfig =  null;
		SolutionList liftStartConfSL = qi.queryIndirectForAllSolutions(LiftCN.START_CONFIG_QUERY_URI, graphIdent);
		List<Ident> startupConfigList = sh.pullIdentsAsJava(liftStartConfSL, LiftCN.CONFIG_VAR_NAME);
		if (startupConfigList.size() < 1) {
			theLogger.error("Did not find a startup liftConfig! Web app will not function.");
		} else {
			startupConfig = startupConfigList.get(0);
			if (startupConfigList.size() > 1) {
				theLogger.warn("Found more than one startup liftConfig; using {} and ignoring the rest", startupConfig);
			}
		}
		return startupConfig;
	}

}