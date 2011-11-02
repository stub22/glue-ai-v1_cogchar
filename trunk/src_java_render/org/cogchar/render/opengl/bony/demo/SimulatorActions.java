/*
 * Copyright 2011 Hanson Robokind LLC.
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
package org.cogchar.render.opengl.bony.demo;

import org.cogchar.render.opengl.bony.model.HumanoidRagdollWrapper;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;

/**
 *
 * @author Matthew Stevenson <www.robokind.org>
 */
public class SimulatorActions {
    enum SimulatorAction {
        RESET_CAMERA {
            void act(BowlAtHumanoidApp app) {
				app.setDefaultCameraLocation();
            }
            Trigger[] makeTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_R)};
            }
        };  // Last enum constant code block gets a semicolon.
        abstract void act(BowlAtHumanoidApp app);
        abstract Trigger[] makeTriggers();
    };
    
    static void setupActionListeners(InputManager inputManager, final BowlAtHumanoidApp app) {
        SimulatorAction pavals[] = SimulatorAction.values();
        String actionNames[] = new String[pavals.length];
        for (int pai =0; pai < pavals.length; pai++) { 
            SimulatorAction pa = pavals[pai];
            actionNames[pai] = pa.name();
            inputManager.addMapping(pa.name(), pa.makeTriggers());
        }
        inputManager.addListener(new ActionListener() {

            public void onAction(String name, boolean isPressed, float tpf) {
                SimulatorAction action = SimulatorAction.valueOf(name);
                if ((action != null) && isPressed) {
                    action.act(app);
                }
            }
        }, actionNames);       
    } 
}
