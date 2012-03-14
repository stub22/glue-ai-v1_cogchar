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

import org.cogchar.platform.trigger.DummyBinding;
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
import org.cogchar.platform.trigger.DummyBox;
import org.cogchar.platform.trigger.DummyTrigger;

import org.cogchar.blob.emit.BonyConfigEmitter;

/**
 
 */
public class HumanoidPuppetActions {
    public enum PlayerAction {
        RESET_CAMERA {
            void act(HumanoidRenderContext ctx) {
				ctx.setDefaultCameraLocation();
            }
            Trigger[] makeJME3InputTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_R)};
            }
			@Override boolean includedInMinSim() { 	return true; }				
        },
        UPDATE_BONY_CONFIG {
			// uses default act() and boxy wiring
            Trigger[] makeJME3InputTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_U)};
            }
			@Override boolean includedInMinSim() { 	return true; }				
        },
        POKE {
			// uses default act() and boxy wiring
            Trigger[] makeJME3InputTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_P)};
            }
			@Override boolean includedInMinSim() { 	return true; }		
        },
        TALK {
			// uses default act() and boxy wiring
            Trigger[] makeJME3InputTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_T)};
            }
			@Override boolean includedInMinSim() { 	return true; }		
        },      		
        TOGGLE_KIN_MODE {
            void act(HumanoidRenderContext ctx) {
				HumanoidFigure hw = getSinbad(ctx);
				hw.toggleKinMode();
            }
            Trigger[] makeJME3InputTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_H),
                      new KeyTrigger(KeyInput.KEY_N)  };
            }
        },
        STAND_UP {
            void act(HumanoidRenderContext ctx) {
				HumanoidFigure hw = getSinbad(ctx);
				hw.standUp();
            }
            Trigger[] makeJME3InputTriggers() { 
                return new Trigger[] {new KeyTrigger(KeyInput.KEY_SPACE)};
            }
			@Override boolean includedInMinSim() { 	return true; }			
        },
        BOOGIE {
			// Triggers a JME3 animation
            void act(HumanoidRenderContext ctx) {
				HumanoidFigure hw = getSinbad(ctx);
				if (hw != null) {
					hw.boogie();
				}
            }
            Trigger[] makeJME3InputTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_B)};
            }
			@Override boolean includedInMinSim() { 	return true; }		
        },   		
  		
        SHOOT {
            void act(HumanoidRenderContext ctx) {
                ctx.cmdShoot();
            }
            Trigger[] makeJME3InputTriggers() { 
                return new Trigger[] {new MouseButtonTrigger(MouseInput.BUTTON_LEFT)};
            }            
        }, 
        BOOM {
            void act(HumanoidRenderContext ctx) {
                ctx.cmdBoom();
            }
            Trigger[] makeJME3InputTriggers() { 
                return new Trigger[] { new MouseButtonTrigger(MouseInput.BUTTON_RIGHT)};
            }            
        }, 
        BIGGER_PROJECTILE {
            void act(HumanoidRenderContext ctx) {
                ctx.getProjectileMgr().cmdBiggerProjectile();
            }
            Trigger[] makeJME3InputTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_PERIOD) };
            }            
        },
        SMALLER_PROJECTILE {
            void act(HumanoidRenderContext ctx) {
                ctx.getProjectileMgr().cmdSmallerProjectile();
            }
            Trigger[] makeJME3InputTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_COMMA) };
            }            
        };  // Last enum constant code block gets a semicolon.
		
     
		BoundAction	myBoundAction = new BoundAction();
        abstract Trigger[] makeJME3InputTriggers();
		
        void act(HumanoidRenderContext ctx) {
			myBoundAction.perform();
		}
		boolean includedInMinSim() { 
			return myBoundAction.includedInMinSim();
		}
		public DummyBinding getBinding() { 
			return myBoundAction;
		}
		HumanoidFigure getSinbad(HumanoidRenderContext hrc) { 
			BonyConfigEmitter bce = hrc.getBonyConfigEmitter();
			return hrc.getHumanoidFigure(bce.SINBAD_CHAR_URI());
		}
	};
    static void setupActionListeners(InputManager inputManager, final HumanoidRenderContext ctx) {
        PlayerAction pavals[] = PlayerAction.values();
		List<String> actionNamesList = new ArrayList<String>();
		boolean minSimMode = ctx.getBonyConfigEmitter().isMinimalSim();
        for (int pai =0; pai < pavals.length; pai++) { 
            PlayerAction pa = pavals[pai];
			String actionName = pa.name();
			if (minSimMode) {
				if (!pa.includedInMinSim()) {
					continue;
				}
			}
			actionNamesList.add(actionName);
            inputManager.addMapping(actionName, pa.makeJME3InputTriggers());
        }
		String actionNames[] = new String[actionNamesList.size()];
		actionNamesList.toArray(actionNames);
		
		registerListenerForActionSubset(inputManager, ctx, actionNames);
    }
	static void registerListenerForActionSubset(InputManager inputManager, final HumanoidRenderContext ctx,
				String actionNames[]) {
		
		// The trick below is that we use PlayerAction.valueOf instead of a hash table.	
        inputManager.addListener(new ActionListener() {

            public void onAction(String name, boolean isPressed, float tpf) {
                PlayerAction action = PlayerAction.valueOf(name);
                if ((action != null) && isPressed) {
                    action.act(ctx);
                }
            }
        }, actionNames);
	}
}
