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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.bind.rdf.jena.assembly.CachingComponentAssembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ryan Biggs
 */
public class LiftAmbassador {

	static Logger theLogger = LoggerFactory.getLogger(LiftAmbassador.class);
	private static LiftConfig config;
	private static LiftSceneInterface sceneLauncher;
	private static LiftInterface lift;
	private static LiftAppInterface liftAppInterface;
	private static boolean configReady = false;
	private static List<String> triggeredCinematics = new ArrayList<String>(); // We need this so we can reset previously played cinematics on replay
	private static ClassLoader myRdfCL; // We'll get this classloader so we can update control configuration from separate RDF files at runtime
	private static final String RDF_PATH_PREFIX = "metadata/web/liftconfig/"; // Prefix for path to Lift configuration TTL files from resources root
	public static Map<String, String> chatConfigEntries = new HashMap<String, String>();

	public interface LiftSceneInterface {

		boolean triggerScene(String scene);
	}

	public interface LiftInterface {

		void notifyConfigReady();

		void loadPage(String path);
	}

	public interface LiftAppInterface {

		boolean triggerNamedCinematic(String name);

		boolean stopNamedCinematic(String name);

		String queryCogbot(String query, String cogbotConvoUrl);
	}

	public static void activateControlsFromConfig(LiftConfig newConfig) {
		config = newConfig;
		theLogger.info("RDF Lift config sent to LiftAmbassador");
		configReady = true;
		if (lift != null) {
			lift.notifyConfigReady();
			theLogger.info("Lift notified of config ready");
		}
	}

	// Method to dyanamically reconfigure controls from RDF
	public static boolean activateControlsFromRdf(String rdfFilename) {
		boolean success = false;
		if (myRdfCL == null) {
			theLogger.error("Trying to activate controls from RDF, but RDF class loader not set");
		} else {
			CachingComponentAssembler.clearCacheFor(LiftConfig.Builder.class); // Clear cached model from Jena
			String flexPath = RDF_PATH_PREFIX + rdfFilename;
			LiftConfig lc;
			try {
				lc = AssemblerUtils.readOneConfigObjFromPath(LiftConfig.class, flexPath, myRdfCL);
			} catch (Exception e) {
				theLogger.error("Exception trying to load LiftConfig from " + rdfFilename + ": Exception was " + e);
				return false;
			}
			activateControlsFromConfig(lc);
			success = true;
		}
		return success;
	}

	// Method to set initial config and RDF classloader for future configs
	public static void storeControlsFromConfig(LiftConfig config, ClassLoader cl) {
		activateControlsFromConfig(config);
		myRdfCL = cl;
	}

	public static LiftConfig getConfig() {
		return config;
	}

	public static String getControlPrefix() {
		return LiftConfigNames.partial_P_control + "_";
	}

	public static void storeChatConfig(ChatConfig cc) {
		// How do we want to handle the possible case of more than one ChatConfigResource? Not sure quite what the future will hold for ChatConfig.
		// For now, let's just combine them all into one. This could be dangerous, but makes sense for now(?)
		for (ChatConfigResource ccr : cc.myCCRs) {
			chatConfigEntries.putAll(ccr.entries);
		}
	}

	public static boolean triggerAction(String action) {
		boolean success = false;
		if ((action.startsWith(LiftConfigNames.partial_P_triggerScene)) && (sceneLauncher != null)) {
			success = sceneLauncher.triggerScene(action);
			if (success && (lift != null)) {
				lift.loadPage("cogchar/scene_running.html");
			}
		} else if ((action.startsWith(LiftConfigNames.partial_P_cinematic)) && (liftAppInterface != null)) {
			if (triggeredCinematics.contains(action)) {
				liftAppInterface.stopNamedCinematic(action); // In order to replay, we need to stop previously played cinematic first
			}
			success = liftAppInterface.triggerNamedCinematic(action);
			if (success) {
				triggeredCinematics.add(action);
			}
		} else if ((action.startsWith(LiftConfigNames.partial_P_liftConfig)) && (lift != null)) {
			String desiredFile = action.replaceAll(LiftConfigNames.partial_P_liftConfig + "_", "");
			success = activateControlsFromRdf(desiredFile);
		}
		return success;
	}

	public static String getCogbotResponse(String query) {
		String response = "";
		if (liftAppInterface != null) {
			if (chatConfigEntries.containsKey(ChatConfigNames.N_cogbotConvoUrl)) {
				response = liftAppInterface.queryCogbot(query, chatConfigEntries.get(ChatConfigNames.N_cogbotConvoUrl));
				theLogger.info("Cogbot says " + response);
			} else {
				theLogger.error("No URL found from ChatConfig for Cogbot conversation server");
			}
		} else {
			theLogger.error("Attempting to query Cogbot, but no liftAppInterface is available");
		}
		return response;
	}

	public static void setSceneLauncher(LiftSceneInterface launcher) {
		sceneLauncher = launcher;
	}

	public static void setLiftMessenger(LiftInterface li) {
		theLogger.info("Lift messenger set");
		lift = li;
	}

	public static void setAppInterface(LiftAppInterface lai) {
		liftAppInterface = lai;
	}

	public static boolean checkConfigReady() {
		return configReady;
	}
}
