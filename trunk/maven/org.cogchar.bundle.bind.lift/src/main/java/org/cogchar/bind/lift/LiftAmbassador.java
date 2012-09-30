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
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ryan Biggs
 */
public class LiftAmbassador {

	private static LiftAmbassador theLiftAmbassador;
	private static final Object theClassLock = LiftAmbassador.class;
	private static Logger theLogger = LoggerFactory.getLogger(LiftAmbassador.class); //OK?
	private LiftConfig myInitialConfig;
	private LiftSceneInterface mySceneLauncher;
	private LiftInterface myLift;
	private LiftAppInterface myLiftAppInterface;
	// The following RepoClient is now an instance variable, but currently is set with a setter (setRepoClient)
	// so that PageCommander, etc. can call into the active LiftAmbassador instance without knowing about RepoClient.
	// Perhaps we'll ultimately want to get rid of this setter and just specify the RepoClient/qGraph in a constructor.
	// However, setting interfaces this way allows LifterLifecycle to change any of the interfaces upon dependency change without
	// requiring a new LiftAmbassador instance.
	private RepoClient myRepoClient; 
	private LiftNetworkConfigInterface myNetConfigInterface;
	private Ident myQGraph;
	private boolean myConfigReady = false;
	private List<String> myTriggeredCinematics = new ArrayList<String>(); // We need this so we can reset previously played cinematics on replay
	private Map<Ident, LiftConfig> myLiftConfigCache = new HashMap<Ident, LiftConfig>(); // To avoid query config if page is reselected
	private Map<String, String> myChatConfigEntries = new HashMap<String, String>();
	private Map<Ident, UserAccessConfig.UserConfig> myUserMap = new HashMap<Ident, UserAccessConfig.UserConfig>();

	// Empty private default constructor to prevent outside instantiation
	private LiftAmbassador() {}
	
	public static LiftAmbassador getLiftAmbassador() {
		synchronized (theClassLock) {
			if (theLiftAmbassador == null) {
				theLiftAmbassador = new LiftAmbassador();
			}
			return theLiftAmbassador;
		}
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
		synchronized (theClassLock) {
			myInitialConfig = newConfig;
			theLogger.info("RDF Lift config sent to LiftAmbassador");
			myConfigReady = true;
			if (myLift != null) {
				myLift.notifyConfigReady();
				theLogger.info("Lift notified of config ready");
			}
		}
	}
	
	// This flavor activates a new set of controls for a single session
	public void activateControlsFromConfig(String sessionId, LiftConfig newConfig) {
		synchronized (theClassLock) {
			if (myLift != null) {
				myLift.setConfigForSession(sessionId, newConfig);
			} else {
				theLogger.error("A new control set was requested for session " + sessionId + ", but no liftInterface was found!");
			}
		}
	}
	
	// Activates controls identified by a LiftConfig URI
	public void activateControlsFromUri(String sessionId, Ident configIdent) {
		synchronized (theClassLock) {
			boolean success = false;
			LiftConfig newConfig = null;
			if (myLiftConfigCache.containsKey(configIdent)) {
				newConfig = myLiftConfigCache.get(configIdent); // Use cached version if available
				theLogger.info("Got lift config " + configIdent.getLocalName() + " from cache");
			} else {
				if (myRepoClient != null) {
					newConfig = new LiftConfig(myRepoClient, myQGraph, configIdent);
					myLiftConfigCache.put(configIdent, newConfig);
					theLogger.info("Loaded lift config " + configIdent.getLocalName() + " from sheet");
				} else {
					theLogger.error("New lift config requested, but no RepoClient set!");
				}
			}
			if (newConfig != null) {
				activateControlsFromConfig(sessionId, newConfig);
				success = true;
			}
		}
	}
	
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

	public boolean triggerCinematic(String cinematicName) {
		boolean success;
		if (myTriggeredCinematics.contains(cinematicName)) {
			myLiftAppInterface.stopNamedCinematic(cinematicName); // In order to replay, we need to stop previously played cinematic first
		}
		success = myLiftAppInterface.triggerNamedCinematic(cinematicName);
		if (success) {
			myTriggeredCinematics.add(cinematicName);
		}
		return success;
	}
	
	public boolean triggerScene(String sceneName) {
		synchronized (theClassLock) { // Not clear if this really needs to be synchronized, but won't hurt...
			boolean success = false;
			if (mySceneLauncher != null) {
				success = mySceneLauncher.triggerScene(sceneName);
			} else {
				theLogger.warn("Attempting to trigger scene, but no LiftSceneInterface found");
			}
			return success;
		}
	}
	
	public boolean performDataballAction(String databallAction, String databallText) {
		synchronized (theClassLock) {
			boolean success = false;
			if (myLiftAppInterface != null) {
				success = myLiftAppInterface.performDataballAction(databallAction, databallText);
			} else {
				theLogger.warn("Attempting to perform Databall action, but no LiftAppInterface found");
			}
			return success;
		}
	}

	public String getCogbotResponse(String query) {
		synchronized (theClassLock) { 
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
	}
	
	public boolean performCogCharUpdate(String desiredUpdate) {
		synchronized (theClassLock) { // Definitely needs to be threadsafe
			boolean success = false;
			if (myLiftAppInterface != null) {
				success = myLiftAppInterface.performUpdate(desiredUpdate);
			} else {
				theLogger.error("Cannot perform update: " + desiredUpdate + " because no LiftAppInterface is available");
			}
			return success;
		}
	}
	
	public void clearLiftConfigCache() {
		synchronized (theClassLock) { 
			myLiftConfigCache.clear();
		}
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
		synchronized (theClassLock) { // ... in case come crazy fools are both trying to configure the network simultaneously!
			if (myNetConfigInterface != null) {
				myNetConfigInterface.configure(ssid, security, key);
			} else {
				theLogger.warn("Could not configure network because no LiftNetworkConfigInterface set");
			}
		}
	}
	
	public void login(String sessionId, String userName, String password) {
		// I believe this doesn't need to be synchronized...
		if (myUserMap != null) {
			Ident userIdent = new FreeIdent(LiftConfigNames.P_user + userName, userName);
			if (myUserMap.containsKey(userIdent)) {
				String hashedEnteredPassword = LiftCrypto.getStringFromBytes(LiftCrypto.getHash(password, myUserMap.get(userIdent).salt));
				if (myUserMap.get(userIdent).hashedPassword.equals(hashedEnteredPassword)) {
					activateControlsFromUri(sessionId, myUserMap.get(userIdent).startConfig);
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

	void setRepoClient(RepoClient qi, Ident graphIdent) {
		myRepoClient = qi;
		myQGraph = graphIdent;
	}
	
	void setNetConfigInterface(LiftNetworkConfigInterface lnci) {
		myNetConfigInterface = lnci;
	}

	public boolean checkConfigReady() {
		return myConfigReady;
	}
}
