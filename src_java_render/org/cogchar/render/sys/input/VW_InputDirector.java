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

package org.cogchar.render.sys.input;

import org.cogchar.platform.gui.keybind.KeyBindingConfig;
import org.cogchar.render.app.core.WorkaroundAppStub;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.sys.registry.RenderRegistryClient;


import org.cogchar.platform.gui.keybind.KeyBindingTracker;
import org.cogchar.render.app.humanoid.HumanoidPuppetActions;
import org.cogchar.render.app.trigger.SceneActions;

import com.jme3.system.AppSettings;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.FlyByCamera;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.Ident;
import org.cogchar.impl.trigger.FancyBinding;
import org.cogchar.platform.gui.keybind.KeyBindingConfigItem;
import org.cogchar.platform.trigger.*;
/**
 * @author Stu B. <www.texpedient.com>
 */

public class VW_InputDirector extends BasicDebugger {
	// Stu 2012-10-17:  It's not necessarily safe to hold any of these handles, is the problem.
	// That was the sort-of justifiable part of pulling them all from HRC before.
	// Now we are getting one snapshot when HRC constructs us.
	
	// Next steps:  1) Reduce the number of these variables required
	//				2) Hide our variable accesses behind accessors with protected scope.
	//				3) Add proper constructor
	
	public	RenderRegistryClient	myRenderRegCli;
	public	KeyBindingConfig		myKeyBindCfg;
	public	WorkaroundAppStub		myAppStub;
	public	HumanoidRenderContext	myHRC_elim;

	public	AppSettings				myAppSettings;
	
	public	CommandSpace			myCommandSpace;
	
	private	VW_HelpScreenMgr		myHelpScreenMgr = new VW_HelpScreenMgr();
	
	private	KeyBindingTracker		myKeyBindingTracker = KeyBindingTracker.getTheTracker();
	
	public void clearKeyBindingsAndHelpScreen() { 
		InputManager inputManager = myRenderRegCli.getJme3InputManager(null);
		// If the help screen is displayed, we need to remove it since we'll be making a new one later
		myHelpScreenMgr.clearHelpText(myAppStub, myRenderRegCli);

		inputManager.clearMappings(); // May be a reload, so let's clear the mappings
		myKeyBindingTracker.clearMap();		
	}
	public void setupKeyBindingsAndHelpScreen() {
		InputManager inputManager = myRenderRegCli.getJme3InputManager(null);
		// If we do that, we'd better clear the KeyBindingTracker too
		// Since we just cleared mappings and are (for now at least) using the default FlyByCamera mappings, we must re-register them
		FlyByCamera fbCam = myAppStub.getFlyByCamera();
		fbCam.registerWithInput(inputManager);
		// Now we'll register the mappings in Cog Char based on theConfig
		HumanoidPuppetActions.setupActionListeners(inputManager, myHRC_elim, myKeyBindCfg, myKeyBindingTracker);
		SceneActions.setupActionListeners(inputManager, myKeyBindCfg, myKeyBindingTracker);
		
		setupCommandKeybindings();
		
		// ... and finally set up the help screen now that the mappings are done
		myHelpScreenMgr.initHelpScreen(myAppSettings, inputManager, myKeyBindCfg, myRenderRegCli, myKeyBindingTracker);
	}
	protected void setupCommandKeybindings() {
		CommandSpace cspace = myCommandSpace;
		KeyBindingTracker kbt = myKeyBindingTracker;
		KeyBindingConfig kbConfig = myKeyBindCfg;
		InputManager inputManager = myRenderRegCli.getJme3InputManager(null);
		List<String> actionNameList = new ArrayList<String>();
		final Map<String, CogcharActionBinding> actionBindingMap = new HashMap<String, CogcharActionBinding>();
		// We'll put the bindings in this temporary map so we can deliver a sorted sequence to KeyBindingTracker
		Map<String, Integer> keyBindingMap = new TreeMap<String, Integer>();
		
		for (KeyBindingConfigItem kbci : myKeyBindCfg.myCommandKeybindings) {
			String keyName = kbci.myBoundKeyName;
			getLogger().warn("Registering command keybinding for " + keyName);
			int keyNumber = VW_InputBindingFuncs.getKeyConstantForName(keyName);
			if (keyNumber != VW_InputBindingFuncs.NULL_KEY) {
				KeyTrigger keyTrig = new KeyTrigger(keyNumber);
				String actionName = kbci.myTargetActionName;
				Ident cmdID = kbci.myTargetCommandID;
				CommandBinding cbind = cspace.findBinding(cmdID);
				if (cbind != null) {
					
					// Currently a CommandBinding can contain multiple CogcharActionBindings.
					// We could write our actionListener so that it simply invokes the CommandBinding's
					// performAllActions.  However, that does not fit cleanly into the current 
					// registerActionListeners() code.
					CogcharActionBinding cwrap = new org.cogchar.impl.trigger.FancyBinding(null, cbind);
					actionBindingMap.put(actionName, cwrap);
					inputManager.addMapping(actionName, keyTrig);
					keyBindingMap.put(actionName, keyNumber);
					actionNameList.add(actionName);
				} else {
					getLogger().warn("Cannot find command binding for {}", cmdID);
				}				
			}
		}
		String actionNames[] = actionNameList.toArray(new String[0]);
		VW_InputBindingFuncs.registerActionListeners(actionBindingMap, actionNames, keyBindingMap, inputManager, kbt);
	}
}
