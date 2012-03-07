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

import org.cogchar.render.opengl.bony.model.HumanoidFigure;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import java.util.ArrayList;
import java.util.List;

/**
 
 */
public class HumanoidPuppetActions {
    enum PlayerAction {

        TOGGLE_KIN_MODE {
            void act(HumanoidRenderContext app) {
				HumanoidFigure hw = app.getHumdWrap();
				hw.toggleKinMode();
            }
            Trigger[] makeTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_H),
                      new KeyTrigger(KeyInput.KEY_N)  };
            }
        },
        STAND_UP {
            void act(HumanoidRenderContext app) {
				HumanoidFigure hw = app.getHumdWrap();
				hw.standUp();
            }
            Trigger[] makeTriggers() { 
                return new Trigger[] {new KeyTrigger(KeyInput.KEY_SPACE)};
            }
			@Override boolean includedInMinSim() { 
				return true;
			}			
        },
        BOOGIE {
            void act(HumanoidRenderContext app) {
				HumanoidFigure hw = app.getHumdWrap();
				hw.boogie();
            }
            Trigger[] makeTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_B)};
            }
			@Override boolean includedInMinSim() { 
				return true;
			}
        },        
        SHOOT {
            void act(HumanoidRenderContext app) {
                app.cmdShoot();
            }
            Trigger[] makeTriggers() { 
                return new Trigger[] {new MouseButtonTrigger(MouseInput.BUTTON_LEFT)};
            }            
        }, 
        BOOM {
            void act(HumanoidRenderContext app) {
                app.cmdBoom();
            }
            Trigger[] makeTriggers() { 
                return new Trigger[] { new MouseButtonTrigger(MouseInput.BUTTON_RIGHT)};
            }            
        }, 
        BIGGER_PROJECTILE {
            void act(HumanoidRenderContext app) {
                app.getProjectileMgr().cmdBiggerProjectile();
            }
            Trigger[] makeTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_PERIOD) };
            }            
        },
        SMALLER_PROJECTILE {
            void act(HumanoidRenderContext app) {
                app.getProjectileMgr().cmdSmallerProjectile();
            }
            Trigger[] makeTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_COMMA) };
            }            
        };  // Last enum constant code block gets a semicolon.
        
        abstract void act(HumanoidRenderContext app);
        abstract Trigger[] makeTriggers();
		boolean includedInMinSim() { 
			return false;
		}
    };
    static void setupActionListeners(InputManager inputManager, final HumanoidRenderContext app) {
        PlayerAction pavals[] = PlayerAction.values();
		List<String> actionNamesList = new ArrayList<String>();
		boolean minSimMode = app.getBonyConfigEmitter().isMinimalSim();
        for (int pai =0; pai < pavals.length; pai++) { 
            PlayerAction pa = pavals[pai];
			String actionName = pa.name();
			if (minSimMode) {
				if (!pa.includedInMinSim()) {
					continue;
				}
			}
			actionNamesList.add(actionName);
            inputManager.addMapping(actionName, pa.makeTriggers());
        }
		String actionNames[] = new String[actionNamesList.size()];
		actionNamesList.toArray(actionNames);
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
