/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
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
 
 */
public class HumanoidPuppetActions {
    enum PlayerAction {

        TOGGLE_KIN_MODE {
            void act(HumanoidPuppetApp app) {
				HumanoidRagdollWrapper hw = app.getHumdWrap();
				hw.toggleKinMode();
            }
            Trigger[] makeTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_H),
                      new KeyTrigger(KeyInput.KEY_N)  };
            }
        },
        STAND_UP {
            void act(HumanoidPuppetApp app) {
				HumanoidRagdollWrapper hw = app.getHumdWrap();
				hw.standUp();
            }
            Trigger[] makeTriggers() { 
                return new Trigger[] {new KeyTrigger(KeyInput.KEY_SPACE)};
            }            
        },
        BOOGIE {
            void act(HumanoidPuppetApp app) {
				HumanoidRagdollWrapper hw = app.getHumdWrap();
				hw.boogie();
            }
            Trigger[] makeTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_B)};
            }
        },        
        SHOOT {
            void act(HumanoidPuppetApp app) {
                app.cmdShoot();
            }
            Trigger[] makeTriggers() { 
                return new Trigger[] {new MouseButtonTrigger(MouseInput.BUTTON_LEFT)};
            }            
        }, 
        BOOM {
            void act(HumanoidPuppetApp app) {
                app.cmdBoom();
            }
            Trigger[] makeTriggers() { 
                return new Trigger[] { new MouseButtonTrigger(MouseInput.BUTTON_RIGHT)};
            }            
        }, 
        BIGGER_PROJECTILE {
            void act(HumanoidPuppetApp app) {
                app.getProjectileMgr().cmdBiggerProjectile();
            }
            Trigger[] makeTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_PERIOD) };
            }            
        },
        SMALLER_PROJECTILE {
            void act(HumanoidPuppetApp app) {
                app.getProjectileMgr().cmdSmallerProjectile();
            }
            Trigger[] makeTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_COMMA) };
            }            
        };  // Last enum constant code block gets a semicolon.
        
        abstract void act(HumanoidPuppetApp app);
        abstract Trigger[] makeTriggers();
    };
    static void setupActionListeners(InputManager inputManager, final HumanoidPuppetApp app) {
        PlayerAction pavals[] = PlayerAction.values();
        String actionNames[] = new String[pavals.length];
        for (int pai =0; pai < pavals.length; pai++) { 
            PlayerAction pa = pavals[pai];
            actionNames[pai] = pa.name();
            inputManager.addMapping(pa.name(), pa.makeTriggers());
        }
        inputManager.addListener(new ActionListener() {

            public void onAction(String name, boolean isPressed, float tpf) {
                PlayerAction action = PlayerAction.valueOf(name);
                if ((action != null) && isPressed) {
                    action.act(app);
                }
            }
        }, actionNames);       
    }      
}
