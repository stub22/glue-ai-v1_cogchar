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
//import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
//import org.appdapter.bind.rdf.jena.assembly.CachingComponentAssembler;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.QueryInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ryan Biggs
 */
public class LiftAmbassador {

	private static LiftAmbassador theLiftAmbassador;
	private static Logger theLogger = LoggerFactory.getLogger(LiftAmbassador.class); //OK?
	private LiftConfig myInitialConfig;
	private LiftSceneInterface mySceneLauncher;
	private LiftInterface myLift;
	private LiftAppInterface myLiftAppInterface;
	// The following QueryInterface is now an instance variable, but currently is set with a setter (setQueryInterface)
	// so that PageCommander, etc. can call into the active LiftAmbassador instance without knowing about QueryInterface.
	// Perhaps we'll ultimately want to get rid of this setter and just specify the QueryInterface/qGraph in a constructor.
	// However, setting interfaces this way allows LifterLifecycle to change any of the interfaces upon dependency change without
	// requiring a new LiftAmbassador instance.
	private QueryInterface myQueryInterface; 
	private LiftNetworkConfigInterface myNetConfigInterface;
	private Ident myQGraph;
	private boolean myConfigReady = false;
	private List<String> myTriggeredCinematics = new ArrayList<String>(); // We need this so we can reset previously played cinematics on replay
	//private ClassLoader myRdfCL; // We'll get this classloader so we can update control configuration from separate RDF files at runtime
	//private static final String RDF_PATH_PREFIX = "metadata/web/liftconfig/"; // Prefix for path to Lift configuration TTL files from resources root
	private Map<Ident, LiftConfig> myLiftConfigCache = new HashMap<Ident, LiftConfig>(); // To avoid query config if page is reselected
	private Map<String, String> myChatConfigEntries = new HashMap<String, String>();
	private Map<Ident, UserAccessConfig.UserConfig> myUserMap = new HashMap<Ident, UserAccessConfig.UserConfig>();

	public static LiftAmbassador getLiftAmbassador() {
		if (theLiftAmbassador == null) {
			theLiftAmbassador = new LiftAmbassador();
		}
		return theLiftAmbassador;
	}
	
	public interface LiftSceneInterface {

		boolean triggerScene(String scene);
	}

	public interface LiftInterface {

		void notifyConfigReady();
		
		void setConfigForSession(String sessionId, LiftConfig config);

		void loadPage(String sessionId, String path);
		
		String getVariable(String key);

		String getVariable(String sessionId, String key);

		void showError(String errorSourceKey, String errorText);
		
		void showError(String errorSourceKey, String errorText, String sessionId);
	}

	public interface LiftAppInterface {

		boolean triggerNamedCinematic(String name);

		boolean stopNamedCinematic(String name);

		String queryCogbot(String query, String cogbotConvoUrl);

		boolean performDataballAction(String action, String text);
		
		boolean performUpdate(String request);
	}

	// A (currently blank) interface used by service manager and available to add future PUMA/CogChar-to-Lifter channels
	public interface LiftAmbassadorInterface {
	}
	
	public interface LiftNetworkConfigInterface {

		void configure(String ssid, String security, String key);
	}

	public static class inputInterface implements LiftAmbassadorInterface {
	}

	// This (legacy) flavor of the method activates controls for the initial config for new sessions
	public void activateControlsFromConfig(LiftConfig newConfig) {
		myInitialConfig = newConfig;
		theLogger.info("RDF Lift config sent to LiftAmbassador");
		myConfigReady = true;
		if (myLift != null) {
			myLift.notifyConfigReady();
			theLogger.info("Lift notified of config ready");
		}
	}
	
	// This flavor activates a new set of controls for a single session
	public void activateControlsFromConfig(String sessionId, LiftConfig newConfig) {
		if (myLift != null) {
			myLift.setConfigForSession(sessionId, newConfig);
		} else {
			theLogger.error("A new control set was requested for session " + sessionId + ", but no liftInterface was found!");
		}
	}

	/* At this point, the Turtle builder for LiftConfig is no longer working
	 * That's because the assembler-based constructor for ControlConfig was broken when we switched from Lift action 
	 * strings to action URIs
	// Method to dynamically reconfigure controls from RDF
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
	// No longer needed for query-based config
	public static void storeControlsFromConfig(LiftConfig config, ClassLoader cl) {
		activateControlsFromConfig(config);
		myRdfCL = cl;
	}
	*/
	
	public LiftConfig getInitialConfig() {
		return myInitialConfig;
	}

	public String getControlPrefix() {
		return LiftConfigNames.partial_P_control + "_";
	}

	public void storeChatConfig(ChatConfig cc) {
		// How do we want to handle the possible case of more than one ChatConfigResource? Not sure quite what the future will hold for ChatConfig.
		// For now, let's just combine them all into one. This could be dangerous, but makes sense for now(?)
		myChatConfigEntries.clear(); // If we are doing a "mode change" or other reconfig, we need to clear out old entries first
		for (ChatConfigResource ccr : cc.myCCRs) {
			myChatConfigEntries.putAll(ccr.entries);
		}
	}
	
	public void storeUserAccessConfig(UserAccessConfig uac) {
		myUserMap = uac.users;
	}

	public boolean triggerAction(String sessionId, Ident actionUri) {
		boolean success = false;
		String actionUriPrefix = actionUri.getAbsUriString().replaceAll(actionUri.getLocalName(), "");
		String action = actionUri.getLocalName();
		if ((LiftConfigNames.p_scenetrig.equals(actionUriPrefix)) && (mySceneLauncher != null)) {
			success = mySceneLauncher.triggerScene(action);
		} else if ((LiftConfigNames.p_cinematic.equals(actionUriPrefix)) && (myLiftAppInterface != null)) {
			if (myTriggeredCinematics.contains(action)) {
				myLiftAppInterface.stopNamedCinematic(action); // In order to replay, we need to stop previously played cinematic first
			}
			success = myLiftAppInterface.triggerNamedCinematic(action);
			if (success) {
				myTriggeredCinematics.add(action);
			}
		} else if ((LiftConfigNames.p_liftconfig.equals(actionUriPrefix)) && (myLift != null)) {
			if (action.endsWith(".ttl")) {
				// This capability no longer exists since we broke the ControlConfig assembler based constructor when the 
				// switch to action URIs was made. We can fix it if we decide we'd like to...
				//success = activateControlsFromRdf(action);
				theLogger.warn("Turtle file based lift config is no longer supported, cannot load config from " + action);
			} else {
				Ident configIdent = actionUri;
				LiftConfig newConfig = null;
				if (myLiftConfigCache.containsKey(configIdent)) {
					newConfig = myLiftConfigCache.get(configIdent); // Use cached version if available
					theLogger.info("Got lift config " + configIdent.getLocalName() + " from cache");
				} else {
					if (myQueryInterface != null) {
						newConfig = new LiftConfig(myQueryInterface, myQGraph, configIdent);
						myLiftConfigCache.put(configIdent, newConfig);
						theLogger.info("Loaded lift config " + configIdent.getLocalName() + " from sheet");
					} else {
						theLogger.error("New lift config requested, but no QueryInterface set!");
					}
				}
				if (newConfig != null) {
					activateControlsFromConfig(sessionId, newConfig);
					success = true;
				}
			}
		} else if ((LiftConfigNames.p_liftcmd.equals(actionUriPrefix)) && (myLiftAppInterface != null)) {
			if (action.startsWith(LiftConfigNames.partial_P_databalls)) {
				String databallsAction = action.replaceAll(LiftConfigNames.partial_P_databalls + "_", ""); // replaceFirst?
				success = myLiftAppInterface.performDataballAction(databallsAction, null);
			} else if (action.startsWith(LiftConfigNames.partial_P_update)) {
				String desiredUpdate = action.replaceFirst(LiftConfigNames.partial_P_update + "_", "");
				success = myLiftAppInterface.performUpdate(desiredUpdate);
			} else if (LiftConfigNames.refreshLift.equals(action.toLowerCase())) {
				theLogger.info("Clearing LiftAmbassador page cache and refreshing global state...");
				myLiftConfigCache.clear();
				success = myLiftAppInterface.performUpdate("ManagedGlobalConfigService");
			}
		}
		return success;
	}

	public String getCogbotResponse(String query) {
		String response = "";
		if (myLiftAppInterface != null) {
			if (myChatConfigEntries.containsKey(ChatConfigNames.N_cogbotConvoUrl)) {
				String convoIp = myChatConfigEntries.get(ChatConfigNames.N_cogbotConvoUrl).replaceFirst("http://", "");
				response = myLiftAppInterface.queryCogbot(query, convoIp);
				theLogger.info("Cogbot says " + response);
			} else {
				theLogger.error("No URL found from ChatConfig for Cogbot conversation server");
			}
		} else {
			theLogger.error("Attempting to query Cogbot, but no liftAppInterface is available");
		}
		return response;
	}

	public boolean sendTextToCogChar(String actionToken, String text) {
		boolean success = false;
		if (actionToken.startsWith(LiftConfigNames.partial_P_databalls)) {
			String databallsAction = actionToken.replaceAll(LiftConfigNames.partial_P_databalls + "_", "");
			success = myLiftAppInterface.performDataballAction(databallsAction, text);
		}
		return success;
	}

	// Gets a global lifter variable
	public String getLiftVariable(String key) {
		if (myLift != null) {
			return myLift.getVariable(key);
		} else {
			theLogger.warn("Variable requested from Lift, but no Lift messenger set");
			return null;
		}
	}
	
	// Gets a session lifter variable
	public String getLiftVariable(String sessionId, String key) {
		if (myLift != null) {
			return myLift.getVariable(sessionId, key);
		} else {
			theLogger.warn("Variable requested from Lift, but no Lift messenger set");
			return null;
		}
	}

	// Display global error
	public void displayError(String errorSource, String errorText) {
		if (myLift != null) {
			myLift.showError(errorSource, errorText);
		} else {
			theLogger.error("Could not show the following error in Lift because no Lift messenger is set: " + errorSource + ": " + errorText);
		}
	}
	
	// Display error to session
	public void displayError(String errorSource, String errorText, String sessionId) {
		if (myLift != null) {
			myLift.showError(errorSource, errorText, sessionId);
		} else {
			theLogger.error("Could not show the following error in Lift session " + sessionId + " because no Lift messenger is set: " + errorSource + ": " + errorText);
		}
	}
	
	public void requestNetworkConfig(String ssid, String security, String key) {
		if (myNetConfigInterface != null) {
			myNetConfigInterface.configure(ssid, security, key);
		} else {
			theLogger.warn("Could not configure network because no LiftNetworkConfigInterface set");
		}
	}
	
	public void login(String sessionId, String userName, String password) {
		if (myUserMap != null) {
			Ident userIdent = new FreeIdent(LiftConfigNames.P_user + userName, userName);
			if (myUserMap.containsKey(userIdent)) {
				if (myUserMap.get(userIdent).password.equals(password)) {
					triggerAction(sessionId, myUserMap.get(userIdent).startConfig);
				} else {
					displayError("login", "Password not recognized", sessionId); // <- move strings to resource
				}
			} else {
				displayError("login", "Username not recognized", sessionId); // <- move strings to resource
			}
		} else {
			theLogger.error("Attempting to log in user, but myUserMap is not set!");
			displayError("login", "User database not set!", sessionId); // <- move strings to resource
		}
	}

	void setSceneLauncher(LiftSceneInterface launcher) {
		mySceneLauncher = launcher;
	}

	public void setLiftMessenger(LiftInterface li) {
		theLogger.info("Lift messenger set");
		myLift = li;
	}

	void setAppInterface(LiftAppInterface lai) {
		myLiftAppInterface = lai;
	}

	void setQueryInterface(QueryInterface qi, Ident graphIdent) {
		myQueryInterface = qi;
		myQGraph = graphIdent;
	}
	
	void setNetConfigInterface(LiftNetworkConfigInterface lnci) {
		myNetConfigInterface = lnci;
	}

	public boolean checkConfigReady() {
		return myConfigReady;
	}
}
