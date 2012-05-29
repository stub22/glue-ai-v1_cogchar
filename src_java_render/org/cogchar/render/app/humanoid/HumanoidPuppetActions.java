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

package org.cogchar.render.app.humanoid;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import java.util.ArrayList;
import java.util.List;

import org.cogchar.platform.trigger.DummyBinding;
import org.cogchar.render.model.humanoid.HumanoidFigure;
import org.cogchar.blob.emit.BonyConfigEmitter;
import org.cogchar.render.app.core.BoundAction;

import org.appdapter.core.log.BasicDebugger;

/**
 *	Keyboard / Mouse bindings for HumanoidPuppet app.
 *	The actions which do not override act() expect to be equipped from outside,
 *  by setting of their box + trigger.
 */
public class HumanoidPuppetActions extends BasicDebugger {
	static BasicDebugger theDbg = new BasicDebugger();
	
    public enum PlayerAction {
        RESET_CAMERA {
            void act(HumanoidRenderContext ctx) {
				ctx.setDefaultCameraLocation();
            }
            int getTriggerKey() { 
                return KeyInput.KEY_F1;
            }
			@Override boolean includedInMinSim() { 	return true; }				
        },
        TOGGLE_SKEL_HILITE {
            void act(HumanoidRenderContext ctx) {
				ctx.toggleDebugSkeletons();
            }
            int getTriggerKey() { 
                return KeyInput.KEY_F2;
            }
			@Override boolean includedInMinSim() { 	return true; }		
        },  		
        SAY_THE_TIME {
			// uses default act() and boxy wiring
            int getTriggerKey() { 
                return KeyInput.KEY_F3;
            }
			@Override boolean includedInMinSim() { 	return true; }		
        },      

		STOP_AND_RESET_CHAR {
			// uses default act() and boxy wiring
            int getTriggerKey() { 
                return KeyInput.KEY_F4;
            }
			@Override boolean includedInMinSim() { 	return true; }		
        },  		
		STOP_RESET_AND_RECENTER_CHAR {
			// uses default act() and boxy wiring
            int getTriggerKey() { 
                return KeyInput.KEY_F5;
            }
			@Override boolean includedInMinSim() { 	return true; }		
        },  		
		
		USE_PERM_ANIMS {
			// uses default act() and boxy wiring
            int getTriggerKey() { 
                return KeyInput.KEY_F6;
            }
			@Override boolean includedInMinSim() { 	return true; }		
        },  			
		USE_TEMP_ANIMS {
			// uses default act() and boxy wiring
            int getTriggerKey() { 
                return KeyInput.KEY_F7;
            }
			@Override boolean includedInMinSim() { 	return true; }		
        },  				
		
        RELOAD_BEHAVIOR {
			// uses default act() and boxy wiring
            int getTriggerKey() { 
                return KeyInput.KEY_F8;
            }
			@Override boolean includedInMinSim() { 	return true; }				
        },
		
        DANGER_YOGA {
			// This yoga-dance shold not be routed to physical robot.
			// 
			// uses default act() and boxy wiring
            int getTriggerKey() { 
                return KeyInput.KEY_F9;
            }
			@Override boolean includedInMinSim() { 	return true; }		
        },
		
		/*** F10 is intercepted by Netbeans platform - do not use!!! **/
        
		UPDATE_BONY_CONFIG {
			// uses default act() and boxy wiring
            int getTriggerKey() { 
                return KeyInput.KEY_F12;
            }
			@Override boolean includedInMinSim() { 	return true; }				
        },

		/*** The actions below are for V-world-only goodies (like Sinbad, projectiles, etc.)
		 *		not available in RK "Simulator"  
		 *		(hence they do not override includedInMinSim()).
		 ***/
	
        TOGGLE_KIN_MODE {
            void act(HumanoidRenderContext ctx) {
				HumanoidFigure hw = getSinbad(ctx);
				hw.togglePhysicsKinematicModeEnabled();
            }
            int getTriggerKey() { 
                return KeyInput.KEY_K;
            }
        },
        STAND_UP {
            void act(HumanoidRenderContext ctx) {
				HumanoidFigure hw = getSinbad(ctx);
				hw.makeSinbadStandUp();
            }
            int getTriggerKey() { 
                return KeyInput.KEY_SPACE;
            }	
        },
        BOOGIE {
			// Triggers a JME3 animation
            void act(HumanoidRenderContext ctx) {
				HumanoidFigure hw = getSinbad(ctx);
				if (hw != null) {
					hw.runSinbadBoogieAnim();
				}
            }
            int getTriggerKey() { 
                return KeyInput.KEY_B;
            }	
        },   		
  		
        SHOOT {
            void act(HumanoidRenderContext ctx) {
                ctx.getGameFeatureAdapter().cmdShoot();
            }  
            int getTriggerKey() { 
                return -MouseInput.BUTTON_LEFT; // Negative tells makeJME3InputTriggers this is mouse input - not ideal but will work for now
            }
        }, 
        BOOM {
            void act(HumanoidRenderContext ctx) {
                ctx.getGameFeatureAdapter().cmdBoom();
            }
            Trigger[] makeJME3InputTriggers() { 
                return new Trigger[] { new MouseButtonTrigger(MouseInput.BUTTON_RIGHT)};
            }
            int getTriggerKey() { 
                return -MouseInput.BUTTON_RIGHT; // Negative tells makeJME3InputTriggers this is mouse input - not ideal but will work for now
            } 
        }, 
        BIGGER_PROJECTILE {
            void act(HumanoidRenderContext ctx) {
                ctx.getGameFeatureAdapter().getProjectileMgr().cmdBiggerProjectile();
            }
            int getTriggerKey() { 
                return KeyInput.KEY_PERIOD;
            }            
        },
        SMALLER_PROJECTILE {
            void act(HumanoidRenderContext ctx) {
                ctx.getGameFeatureAdapter().getProjectileMgr().cmdSmallerProjectile();
            }
            int getTriggerKey() { 
                return KeyInput.KEY_COMMA;
            }            
        };  // Last enum constant code block gets a semicolon.
		
     
		BoundAction	myBoundAction = new BoundAction();
        abstract int getTriggerKey();           
		
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
			return hrc.getHumanoidFigure(bce.SINBAD_CHAR_IDENT());
		}
	};
    
    /* 
    * Below static method converts from jME KeyInput/MouseInput codes to Triggers
    * and registers with KeyBindingTracker.
    * We switch on sign of keyCode to determine if this is a mouse binding or not
    * Negative codes are assumed to be mouse codes and inverted before trigger creation
    * This is possible because all JME KeyInput codes are > 0
    * Not very nice in long run, but gets it going with minimum of modification for now
    */
    private static Trigger[] makeJME3InputTriggers(PlayerAction pa) {
            int keyCode = pa.getTriggerKey();
            if (keyCode > 0) {
                KeyBindingTracker.addBinding(pa.name(), keyCode);
                return new Trigger[] { new KeyTrigger(keyCode)};
            }
             else {return new Trigger[] { new MouseButtonTrigger(-keyCode)};}
        }
    
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
            inputManager.addMapping(actionName, makeJME3InputTriggers(pa));
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
					try {
	                    action.act(ctx);
					} catch (Throwable t) {
						theDbg.logError("Action for " + name + " threw exception", t);
					}
                }
            }
        }, actionNames);
	}
}
