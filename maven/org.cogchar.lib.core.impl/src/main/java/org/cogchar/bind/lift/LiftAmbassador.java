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

import org.cogchar.api.web.WebSessionActionParamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.api.web.WebAppInterface;
import org.cogchar.name.lifter.ChatAN;
import org.cogchar.name.lifter.LiftAN;
import org.cogchar.name.lifter.LiftCN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ryan Biggs
 */
public class LiftAmbassador implements WebAppInterface, WebAppInterface.WebSceneInterface {

	private static LiftAmbassador theLiftAmbassador;
	private static final Object theClassLock = LiftAmbassador.class;
	private static Logger theLogger = LoggerFactory.getLogger(LiftAmbassador.class); //OK?
	private LiftConfig myInitialConfig;
	private WebSceneInterface mySceneLauncher;
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
	private List<Ident> myTriggeredCinematics = new ArrayList<Ident>(); // We need this so we can reset previously played cinematics on replay
	private Map<Ident, LiftConfig> myLiftConfigCache = new HashMap<Ident, LiftConfig>(); // To avoid query config if page is reselected
	private Map<String, String> myChatConfigEntries = new HashMap<String, String>();
	private Map<Ident, UserAccessConfig.UserConfig> myUserMap = new HashMap<Ident, UserAccessConfig.UserConfig>();
	private Map<Ident, String> userSessionMap = new HashMap<Ident, String>(); // Tracks the session IDs corresponding to named users' last login
	private LiftQueryEnvoy myQueryEnvoy = new LiftQueryEnvoy();
	
	// The updateTargetURL and updateGraphQN are made up for testing for now! This needs to be revisited, and this stuff moved to onto at the very least...
	// Probably will want to be able to select the updateGraphQN somehow...
	// Our test repository URLs.
	private static final String repoBaseURL = "http://localhost:8080/cchr_josk/";
	private static final String repoBaseUpdURL = repoBaseURL + "sparql-update/";
	private static final String glueUpdURL = repoBaseUpdURL + "glue-ai";
	private LiftRepoMessenger myRepoMessenger = new LiftRepoMessenger(glueUpdURL,  "ccrt:thing_sheet_22"); 
	
	private final Object activationLock = new Object();
	private final Object cogcharLock = new Object();
	private final Object databallsLock = new Object();
	private final Object cogbotLock = new Object();
	private final Object networkConfigLock = new Object();
	

	// Empty private default constructor to prevent outside instantiation
	private LiftAmbassador() {}
	
	// Should this be moved to a registry soon?
	public static LiftAmbassador getLiftAmbassador() {
		synchronized (theClassLock) {
			if (theLiftAmbassador == null) {
				theLiftAmbassador = new LiftAmbassador();
			}
			return theLiftAmbassador;
		}
	}
	
	public interface LiftInterface {

		void notifyConfigReady();
		
		void setConfigForSession(String sessionId, LiftConfig config);
		
		void setControlForSessionAndSlot(String sessionId, int slotNum, ControlConfig newConfig);

		void loadPage(String sessionId, String path);
		
		String getVariable(String key);

		String getVariable(String sessionId, String key);

		void showError(String errorSourceKey, String errorText);
		
		void showError(String errorSourceKey, String errorText, String sessionId);
		
		List<String> getActiveSessions();
	}

	public interface LiftAppInterface {

		boolean triggerAnimation(Ident uri);

		boolean stopAnimation(Ident uri);

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
	// I *think* that due to improvements in LifterState, activation no longer needs to be synchronized
	// But it's a small performance hit, so for now I'll leave it until we're really sure.
	public void activateControlsFromConfig(LiftConfig newConfig) {
		synchronized (activationLock) {
			myInitialConfig = newConfig;
			theLogger.info("Lift config sent to LiftAmbassador");
			myConfigReady = true;
			if (myLift != null) {
				myLift.notifyConfigReady();
				theLogger.info("Lift notified of config ready");
			}
		}
	}
	
	// This flavor activates a new set of controls for a single session
	public void activateControlsFromConfig(String sessionId, LiftConfig newConfig) {
		//theLogger.info("Activating controls for session {}: {}", sessionId, newConfig); // TEST ONLY
		synchronized (activationLock) { // Likely doesn't actually need synchronization since LifterState is now threadsafe...
			if (myLift != null) {
				myLift.setConfigForSession(sessionId, newConfig);
			} else {
				theLogger.error("A new control set was requested for session {}, but no liftInterface was found!", sessionId);
			}
		}
	}
	
	// This method activates a single control for a single session
	public void activateControlFromConfig(String sessionId, int slotNum, ControlConfig newConfig) {
		synchronized (activationLock) { // Likely doesn't actually need synchronization since LifterState is now threadsafe...
			if (myLift != null) {
				myLift.setControlForSessionAndSlot(sessionId, slotNum, newConfig);
			} else {
				theLogger.error("A new control was requested for session {}, but no liftInterface was found!", sessionId);
			}
		}
	}
	public void activateControlFromConfig(String sessionId, int slotNum, WebAppInterface.Control newControl) {
		activateControlFromConfig(sessionId, slotNum, ControlConfig.getControlConfigFromControlInterface(newControl));
	}
		
	// Activate a control in all sessions -- probably just for temporary testing
	public void activateControlFromConfig(int slotNum, ControlConfig newConfig) {
		for (String sessionId : myLift.getActiveSessions()) {
			activateControlFromConfig(sessionId, slotNum, newConfig);
		}
	}
	public void activateControlFromConfig(int slotNum, WebAppInterface.Control newControl) {
		activateControlFromConfig(slotNum, ControlConfig.getControlConfigFromControlInterface(newControl));
	}
	
	// Activates a single control in a single session based on user
	public void activateControlFromConfigForUser(String userName, int slotNum, ControlConfig newConfig) {
		String sessionId = userSessionMap.get(getUserIdentFromName(userName));
		if (sessionId != null) {
			activateControlFromConfig(sessionId, slotNum, newConfig);
		} else {
			theLogger.warn("Could not set control based on user name; no login on record for user {}", userName);
		}
	}
	public void activateControlFromConfigForUser(String userName, int slotNum, WebAppInterface.Control newControl) {
		activateControlFromConfigForUser(userName, slotNum, ControlConfig.getControlConfigFromControlInterface(newControl));
	}
	
	// Activates a single control in all sessions of a given user class local name
	// Should the full URI be accepted as input instead? Perhaps, but for now local name is accepted to simplify
	// user experience in Robosteps and etc.
	public void activateControlFromConfigForUserClass(String desiredUserClassLN, int slotNum, ControlConfig newConfig) {
		Ident desiredUserClassURI  = getUserClassIdentFromLN(desiredUserClassLN);
		for (Ident activeUser : userSessionMap.keySet()) {
			if (myUserMap.get(activeUser).userClass.equals(desiredUserClassURI)) {
				activateControlFromConfig(userSessionMap.get(activeUser), slotNum, newConfig);
			}
		}
	}
	public void activateControlFromConfigForUserClass(String desiredUserClassLN, int slotNum, WebAppInterface.Control newControl) {
		activateControlFromConfigForUserClass(desiredUserClassLN, slotNum, ControlConfig.getControlConfigFromControlInterface(newControl));
	}
	
	// Activates controls identified by a LiftConfig URI
	public void activateControlsFromUri(String sessionId, Ident configIdent) {
		// May be OK not to have this synchronized if appdapter repo code is threadsafe, and if myLiftConfigCache
		// is made a ConcurrentHashMap
		synchronized (activationLock) { 
			boolean success = false;
			LiftConfig newConfig = null;
			if (myLiftConfigCache.containsKey(configIdent)) {
				newConfig = myLiftConfigCache.get(configIdent); // Use cached version if available
				theLogger.info("Got lift config {} from cache", configIdent.getLocalName());
			} else {
				if (myRepoClient != null) {
					newConfig = new LiftConfig(myRepoClient, myQGraph, configIdent);
					myLiftConfigCache.put(configIdent, newConfig);
					theLogger.info("Loaded lift config {} from sheet", configIdent.getLocalName());
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
	
	// Activates controls identified by a LiftConfig URI in all sessions -- probably just for temporary testing
	public void activateControlsFromUri(Ident configIdent) {
		for (String sessionId : myLift.getActiveSessions()) {
			activateControlsFromUri(sessionId, configIdent);
		}
	}
	
	// Activates controls in a single session based on user
	public void activateControlsFromUriForUser(String userName, Ident configIdent) {
		String sessionId = userSessionMap.get(getUserIdentFromName(userName));
		if (sessionId != null) {
			activateControlsFromUri(sessionId, configIdent);
		} else {
			theLogger.warn("Could not set Lifter config based on user name; no login on record for user {}", userName);
		}
	}
	
	// Activates controls in a single session based on user class
	public void activateControlsFromUriForUserClass(String desiredUserClassLN, Ident configIdent) {
		Ident desiredUserClassURI  = getUserClassIdentFromLN(desiredUserClassLN);
		for (Ident activeUser : userSessionMap.keySet()) {
			if (myUserMap.get(activeUser).userClass.equals(desiredUserClassURI)) {
				activateControlsFromUri(userSessionMap.get(activeUser), configIdent);
			}
		}
	}
	
	// Activates a single control in a single session
	public void activateControlFromUri(String sessionId, int slotNum, Ident configIdent) {
		// May be OK not to have this synchronized if appdapter repo code is threadsafe
		synchronized(activationLock) {
			boolean success = false;
			if (myRepoClient != null) {
				ControlConfig newControl = ControlConfig.getControlConfigFromUri(myRepoClient, myQGraph, configIdent);
				if (newControl != null) {
					theLogger.info("Loaded lift control {} from sheet", configIdent.getLocalName());
					activateControlFromConfig(sessionId, slotNum, newControl);
					success = true;
				} else {
					theLogger.warn("Control requested in session {}, but it was not found: {}", sessionId, configIdent);
				}
				
			} else {
				theLogger.error("New lift control requested in session {}, but no RepoClient set!", sessionId);
			}
		}
	}
	
	// Activates a single control in a single session by localname only, assuming the lci: prefix
	public void activateControlFromLocalName(String sessionId, int slotNum, String localName) {
		Ident controlIdent = new FreeIdent(LiftCN.LIFT_CONFIG_INSTANCE_PREFIX + localName, localName);
		activateControlFromUri(sessionId, slotNum, controlIdent);
	}
	
	public void activateControlAction(Ident actionIdent) {
		// May be OK not to have this synchronized if appdapter repo code is threadsafe
		synchronized(activationLock) {
			if (myRepoClient != null) {
				ControlActionConfig action = ControlActionConfig.getControlActionConfigFromUri(myRepoClient, myQGraph, actionIdent);
				if (action != null) {
					theLogger.info("Loaded lift control {} from sheet", actionIdent.getLocalName());
					// Still need to add code to check for user class -- this currently acts in all sessions
					boolean controlAction = false;
					boolean configAction = false;
					if (action.control != null) {
						controlAction = true;
					}
					if (action.config != null) {
						configAction = true;
					}
					for (String sessionId : myLift.getActiveSessions()) {
						if (configAction) { // Config actions take precidence over control actions if both are specified
							activateControlsFromUri(action.config);
						} else if (controlAction) {
							activateControlFromUri(sessionId, action.slotNum, action.control);
						} else {
							theLogger.warn("Control action did not specifiy a LiftConfig or control!");
						}
					}
				} else {
					theLogger.warn("Control action requested, but it was not found: {}", actionIdent);
				}
				
			} else {
				theLogger.error("Lift control action requested, but no RepoClient set!");
			}
		}
	}
	
	
	public LiftConfig getInitialConfig() {
		return myInitialConfig;
	}

	public String getControlPrefix() {
		return LiftAN.partial_P_control + "_";
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

	// Two "prototype" methods for sending repo updates follow. These will probably eventually evolve into something much more
	// general and elegant.
	// Synchronize these next two methods? Could be triggered concurrently by repo actions in multiple simultaneous Lift sessions.
	// Probably not a problem *if* repo update code is thread safe
	public void sendUserTextViaRepo(Ident senderIdent, String userText, String sessionId) {
		WebSessionActionParamWriter paramWriter = getInitializedParamWriter(sessionId);
		paramWriter.putOutputText(userText);
		// The below "Sender" is intended to identify the control action which initialized the repo output.
		// Currently as defined in RepoOutputHandler: 
		// senderIdent = new FreeIdent(ActionStrings.p_repoSender + control.action.getLocalName());
		paramWriter.putSender(senderIdent); 
		myRepoMessenger.sendMessage("userOutputActionRecord", "userOutput");
	}
	
	public void sendActionViaRepo(Ident actionIdent, String sessionId) {
        
        // hook up to the user channel
		WebSessionActionParamWriter paramWriter = getInitializedParamWriter(sessionId);
        
        // send the TA over that channel
		paramWriter.putActionUri(actionIdent);
        
        // notify the world a TA exists this may cause WantsThingAction Routers to notice 
		myRepoMessenger.sendMessage("lifterActionRecord", "lifterAction");
	}
	
	private WebSessionActionParamWriter getInitializedParamWriter(String sessionId) {
		WebSessionActionParamWriter paramWriter = myRepoMessenger.resetAndGetParamWriter();
		paramWriter.putSessionID(sessionId);
		Ident userId = getKeyByValue(userSessionMap, sessionId);
		if (userId != null) {
			paramWriter.putUserID(userId);
			paramWriter.putUserClass(myUserMap.get(userId).userClass);
		}
		return paramWriter;
	}
	
	// A way to get the first matched key from a value in a map, from
	// http://stackoverflow.com/questions/1383797/java-hashmap-how-to-get-key-from-value
	// Currently used by getInitializedParamWriter to look up a UserID from a session ID string; there may be a better way
	private <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (value.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	public boolean triggerCinematic(Ident cinematicUri) {
		synchronized (cogcharLock) {
			boolean success;
			if (myTriggeredCinematics.contains(cinematicUri)) {
				myLiftAppInterface.stopAnimation(cinematicUri); // In order to replay, we need to stop previously played cinematic first
			}
			success = myLiftAppInterface.triggerAnimation(cinematicUri);
			if (success) {
				myTriggeredCinematics.add(cinematicUri);
			}
			return success;
		}
	}
	
	public boolean triggerScene(String sceneName) {
		synchronized (cogcharLock) { // Not clear if this really needs to be synchronized, but won't hurt...
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
		synchronized (databallsLock) {
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
		synchronized (cogbotLock) { 
			String response = "";
			if (myLiftAppInterface != null) {
				if (myChatConfigEntries.containsKey(ChatAN.N_cogbotConvoUrl)) {
					String convoIp = myChatConfigEntries.get(ChatAN.N_cogbotConvoUrl).replaceFirst("http://", "");
					response = myLiftAppInterface.queryCogbot(query, convoIp);
					theLogger.info("Cogbot says {}", response);
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
		synchronized (theClassLock) { // Definitely needs to be threadsafe, and a good thing to lock down the entire LiftAmbassador
			boolean success = false;
			if (myLiftAppInterface != null) {
				success = myLiftAppInterface.performUpdate(desiredUpdate);
			} else {
				theLogger.error("Cannot perform update: {} because no LiftAppInterface is available", desiredUpdate);
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
			theLogger.error("Could not show the following error in Lift because no Lift messenger is set: {}: {}",errorSource, errorText);
		}
	}
	
	// Display error to session
	public void displayError(String errorSource, String errorText, String sessionId) {
		if (myLift != null) {
			myLift.showError(errorSource, errorText, sessionId);
		} else {
			theLogger.error("Could not show the following error in Lift session {} because no Lift messenger is set: {}: {}", new Object[]{sessionId, errorSource, errorText});
		}
	}
	
	public void requestNetworkConfig(String ssid, String security, String key) {
		synchronized (networkConfigLock) { // ... in case come crazy fools are both trying to configure the network simultaneously!
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
			Ident userIdent = getUserIdentFromName(userName);
			if (myUserMap.containsKey(userIdent)) {
				String hashedEnteredPassword = LiftCrypto.getStringFromBytes(LiftCrypto.getHash(password, myUserMap.get(userIdent).salt));
				if (myUserMap.get(userIdent).hashedPassword.equals(hashedEnteredPassword)) {
					userSessionMap.put(userIdent, sessionId); // Add last logged-in sessionID to userSessionMap to track userName-sessionID correspondence
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
	
	private Ident getUserIdentFromName(String userName) {
		return new FreeIdent(LiftAN.NS_user + userName, userName);
	}
	
	private Ident getUserClassIdentFromLN(String classLN) {
		return new FreeIdent(LiftAN.NS_uai + classLN, classLN);
	}
	
	public List<NameAndAction> getNamesAndActionsFromQuery(Ident queryUri) {
		// The qGraph here is the lifter one, so usually not what we want -- so far these queries use hard coded graph so this one is ignored
		// Want to figure out a better way to handle this soon though
		return myQueryEnvoy.getNamesAndActionsFromQuery(myRepoClient, myQGraph, queryUri, LiftCN.ACTION_VAR_NAME, LiftCN.NAME_VAR_NAME);
	}

	public void setSceneLauncher(WebSceneInterface launcher) {
		mySceneLauncher = launcher;
	}

	public void setLiftMessenger(LiftInterface li) {
		theLogger.info("Lift messenger set");
		myLift = li;
	}

	public void setAppInterface(LiftAppInterface lai) {
		myLiftAppInterface = lai;
	}

	public void setRepoClient(RepoClient qi, Ident graphIdent) {
		myRepoClient = qi;
		myQGraph = graphIdent;
	}
	
	public void setNetConfigInterface(LiftNetworkConfigInterface lnci) {
		myNetConfigInterface = lnci;
	}

	public boolean checkConfigReady() {
		return myConfigReady;
	}
	
	// Used by external classes which need to directly construct new ControlConfigs
	@Override public WebAppInterface.Control getNewControl() {
		return new ControlConfig();
	}
}
