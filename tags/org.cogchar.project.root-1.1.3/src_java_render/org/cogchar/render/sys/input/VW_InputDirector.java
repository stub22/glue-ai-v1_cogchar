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
// import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.sys.registry.RenderRegistryClient;


import org.cogchar.platform.gui.keybind.KeyBindingTracker;

import org.cogchar.impl.web.in.SceneActions;

import com.jme3.system.AppSettings;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.FlyByCamera;
import com.jme3.input.controls.*;
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
    public RenderRegistryClient myRenderRegCli;
    public KeyBindingConfig myKeyBindCfg;
    public WorkaroundAppStub myAppStub;
//	public	HumanoidRenderContext	myHRC_elim;
    public AppSettings myAppSettings;
    public CommandSpace myCommandSpace;
    private VW_HelpScreenMgr myHelpScreenMgr = new VW_HelpScreenMgr();
    private KeyBindingTracker myKeyBindingTracker = KeyBindingTracker.getTheTracker();

    public void clearKeyBindingsAndHelpScreen() {
        InputManager inputManager = myRenderRegCli.getJme3InputManager(null);
        // If the help screen is displayed, we need to remove it since we'll be making a new one later
        myHelpScreenMgr.clearHelpText(myAppStub, myRenderRegCli);

        inputManager.clearMappings(); // May be a reload, so let's clear the mappings
        myKeyBindingTracker.clearMap();
    }

    public static class InputManagerDoodad implements SceneActions.SceneTriggerManager {

        InputManager myJME3InputManager;

        @Override
        public void addKeyMapping(String sceneName, int keyCode) {
            KeyTrigger keyTrig = new KeyTrigger(keyCode);
            myJME3InputManager.addMapping(sceneName, keyTrig);
        }
    }

    public void setupKeyBindingsAndHelpScreen() {
        InputManagerDoodad doodad = new InputManagerDoodad();
        doodad.myJME3InputManager = myRenderRegCli.getJme3InputManager(null);
        // If we do that, we'd better clear the KeyBindingTracker too
        // Since we just cleared mappings and are (for now at least) using the default FlyByCamera mappings, we must re-register them
        FlyByCamera fbCam = myAppStub.getFlyByCamera();
        fbCam.registerWithInput(doodad.myJME3InputManager);
        // Now we'll register the mappings in Cog Char based on theConfig
        // HumanoidPuppetActions.setupActionListeners(inputManager, myHRC_elim, myKeyBindCfg, myKeyBindingTracker);
        setupActionListeners(doodad, myKeyBindCfg, myKeyBindingTracker);
        addScrollWheelBindings(doodad);
        setupCommandKeybindings();

        // ... and finally set up the help screen now that the mappings are done
        myHelpScreenMgr.updateHelpTextContents(myRenderRegCli, myAppSettings, myKeyBindCfg, myKeyBindingTracker);
    }

    /**
     * The scroll wheel has a problem where you can zoom out to far, and not be
     * able to zoom back in. These bindings using a scrollWheelListener fix this
     * problem.
     *
     * @param doodad
     */
    private void addScrollWheelBindings(InputManagerDoodad doodad) {
        InputManager myJME3InputManager = doodad.myJME3InputManager;
        if (myJME3InputManager == null) {
            return;
        }

        // Add scroll wheel mappings
        myJME3InputManager.addMapping("Scroll_Wheel_Up", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        myJME3InputManager.addMapping("Scroll_Wheel_Down", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        
        // Add Listeners on each scroll wheel mapping
        myJME3InputManager.addListener(scrollWheelListener, "Scroll_Wheel_Up");
        myJME3InputManager.addListener(scrollWheelListener, "Scroll_Wheel_Down");
    }
    /**
     * @author Ben The scroll wheel allows the camera to be zoomed out so far
     * that you cannot zoom back in to prevent this error I am applying a limit
     * to the zoom out distance.
     */
    private AnalogListener scrollWheelListener = new AnalogListener() {
        private boolean canZoom = true;
        private int scrollLoc = 0;
        private FlyByCamera fbCam = null;
        private int maxZoom = -17;
        private int minZoom = 15;

        @Override
        public void onAnalog(String name, float value, float tpf) {
            // If null, try to assign the right fly by camera
            if (fbCam == null) {
                if (myAppStub != null) {
                    fbCam = myAppStub.getFlyByCamera();
                }
            }
            // If it couldn't be assigned, exit
            if (fbCam == null) {
                return;
            }
            // If we are Zooming in
            if (name.equals("Scroll_Wheel_Up")) {
                zoomIn();
            }
            if (name.equals("Scroll_Wheel_Down")) {
                zoomOut();
            }
        }

        private void zoomIn() {
            if (!canZoom) {
                enableZoom();
            }
            if (scrollLoc >= maxZoom) {
                scrollLoc--;
            }
        }

        private void zoomOut() {
            if (scrollLoc <= minZoom) {
                scrollLoc++;
            } else {
                disableZoom();
            }
        }

        private void disableZoom() {
            canZoom = false;
            fbCam.setZoomSpeed(0);
        }

        private void enableZoom() {
            fbCam.setZoomSpeed(-20);
            canZoom = true;
        }
    };

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
                Trigger jme3InTrig = null;
                // BUTTON_LEFT = 0
                if (keyNumber > 0) {
                    jme3InTrig = new KeyTrigger(keyNumber);
                } else {
                    jme3InTrig = new MouseButtonTrigger(-1 * keyNumber);
                }

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
                    inputManager.addMapping(actionName, jme3InTrig);
                    keyBindingMap.put(actionName, keyNumber);
                    actionNameList.add(actionName);
                    getLogger().warn("Registered " + actionName + " for " + jme3InTrig + " at " + keyNumber);
                } else {
                    getLogger().warn("Cannot find command binding for {}", cmdID);
                }
            }
        }
        String actionNames[] = actionNameList.toArray(new String[0]);
        VW_InputBindingFuncs.registerActionListeners(actionBindingMap, actionNames, keyBindingMap, inputManager, kbt);
    }

    public VW_HelpScreenMgr getHelpScreenMgr() {
        return myHelpScreenMgr;
    }

    public static void setupActionListeners(SceneActions.SceneTriggerManager stm, //InputManager inputManager, 
            KeyBindingConfig config, KeyBindingTracker kbt) {
        SceneActions.numberOfBindings = config.mySceneBindings.size();
        String actionNames[] = new String[SceneActions.numberOfBindings];
        //theBoundActions = new DummyBinding[numberOfBindings];
        Iterator<KeyBindingConfigItem> sceneMappings = config.mySceneBindings.values().iterator();
        // We'll put the bindings in this temporary map so we can deliver a sorted sequence to KeyBindingTracker
        Map<String, Integer> bindingMap = new TreeMap<String, Integer>();
        int idx = 0;
        while (sceneMappings.hasNext()) {
            KeyBindingConfigItem nextMapping = sceneMappings.next();
            String keyName = nextMapping.myBoundKeyName;
            int sceneTrigKeyNum = VW_InputBindingFuncs.getKeyConstantForName(keyName);
            if (sceneTrigKeyNum != VW_InputBindingFuncs.NULL_KEY) {
                String sceneTrigName = nextMapping.myTargetActionName;
                // Factored out JME3 dependent code into implementation of SceneTriggerManager.
                // KeyTrigger keyTrig = new KeyTrigger(sceneTrigKeyNum);
                // inputManager.addMapping(sceneTrigName, keyTrig);
                stm.addKeyMapping(sceneTrigName, sceneTrigKeyNum);
                bindingMap.put(sceneTrigName, sceneTrigKeyNum);
                actionNames[idx] = sceneTrigName;
                idx++;
            }
        }
        VW_InputBindingFuncs.registerActionListeners(SceneActions.theBoundActionsByTrigName, actionNames, bindingMap,
                ((InputManagerDoodad) stm).myJME3InputManager, kbt);

    }
}
