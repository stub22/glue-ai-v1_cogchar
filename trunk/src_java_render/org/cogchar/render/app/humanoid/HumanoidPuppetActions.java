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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.cogchar.platform.trigger.DummyBinding;
import org.cogchar.render.model.humanoid.HumanoidFigure;
import org.cogchar.blob.emit.RenderConfigEmitter;
import org.cogchar.render.app.core.BoundAction;

import org.appdapter.core.log.BasicDebugger;
import org.cogchar.render.model.databalls.BallBuilder;

/**
 *	Keyboard / Mouse bindings for HumanoidPuppet app.
 *	The actions which do not override act() expect to be equipped from outside,
 *  by setting of their box + trigger.
 */
public class HumanoidPuppetActions extends BasicDebugger {
	static BasicDebugger theDbg = new BasicDebugger();
	
	private static KeyBindingConfig keyBindings;
	
    public enum PlayerAction {
        RESET_CAMERA {
            void act(HumanoidRenderContext ctx) {
				ctx.setDefaultCameraLocation();
            }
            int getTriggerKey() { 
				return getKey(this);
            }
			@Override boolean includedInMinSim() { 	return true; }				
        },
        TOGGLE_SKEL_HILITE {
            void act(HumanoidRenderContext ctx) {
				ctx.toggleDebugSkeletons();
            }
            int getTriggerKey() { 
                return getKey(this);
            }
			@Override boolean includedInMinSim() { 	return true; }		
        },  		
        SAY_THE_TIME {
			// uses default act() and boxy wiring
            int getTriggerKey() { 
                return getKey(this);
            }
			@Override boolean includedInMinSim() { 	return true; }		
        },      

		STOP_AND_RESET_CHAR {
			// uses default act() and boxy wiring
            int getTriggerKey() { 
                return getKey(this);
            }
			@Override boolean includedInMinSim() { 	return true; }		
        },  		
		STOP_RESET_AND_RECENTER_CHAR {
			// uses default act() and boxy wiring
            int getTriggerKey() { 
                return getKey(this);
            }
			@Override boolean includedInMinSim() { 	return true; }		
        },  		
		
		USE_PERM_ANIMS {
			// uses default act() and boxy wiring
            int getTriggerKey() { 
                return getKey(this);
            }
			@Override boolean includedInMinSim() { 	return true; }		
        },  			
		USE_TEMP_ANIMS {
			// uses default act() and boxy wiring
            int getTriggerKey() { 
                return getKey(this);
            }
			@Override boolean includedInMinSim() { 	return true; }		
        },  				
		
        RELOAD_BEHAVIOR {
			// uses default act() and boxy wiring
            int getTriggerKey() { 
                return getKey(this);
            }
			@Override boolean includedInMinSim() { 	return true; }				
        },
		
        DANGER_YOGA {
			// This yoga-dance shold not be routed to physical robot.
			// 
			// uses default act() and boxy wiring
            int getTriggerKey() { 
                return getKey(this);
            }
			@Override boolean includedInMinSim() { 	return true; }		
        },
		
		/*** F10 is intercepted by Netbeans platform - do not use!!! **/
        
		UPDATE_WORLD_CONFIG {
			void act(HumanoidRenderContext ctx) {
				ctx.requestConfigReload("WorldConfig");
            }
            int getTriggerKey() { 
                return getKey(this);
            }
			@Override boolean includedInMinSim() { 	return true; }				
        },
		
		UPDATE_BONY_CONFIG {
			// previously used default act() and boxy wiring
			// Now we are using the new "updateConfigByRequest" system in PumaAppContext
			// That does a couple of good things: for one, it's more compatible with having different means of
			// triggering reloads, like with the web app
			// Also, this method allows us to reload the repo from sheet or etc. only once to update all 
			// the active characters. And, in the future updateConfigByRequest will be able to handle updating
			// single characters.
			void act(HumanoidRenderContext ctx) {
				ctx.requestConfigReload("BoneRobotConfig");
            }
            int getTriggerKey() { 
                return getKey(this);
            }
			@Override boolean includedInMinSim() { 	return true; }				
        },
		
		UPDATE_HUMANOIDS {
			void act(HumanoidRenderContext ctx) {
				ctx.requestConfigReload("AllHumanoidConfig");
            }
            int getTriggerKey() { 
                return getKey(this);
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
                return getKey(this);
            }
        },
        STAND_UP {
            void act(HumanoidRenderContext ctx) {
				HumanoidFigure hw = getSinbad(ctx);
				hw.makeSinbadStandUp();
            }
            int getTriggerKey() { 
                return getKey(this);
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
                return getKey(this);
            }	
        },   		
  		
        SHOOT {
            void act(HumanoidRenderContext ctx) {
                ctx.getGameFeatureAdapter().cmdShoot();
            }  
            int getTriggerKey() { 
				return getKey(this);
            }
        }, 
        BOOM {
            void act(HumanoidRenderContext ctx) {
                ctx.getGameFeatureAdapter().toggleAnnoyingStuff();
            }
            int getTriggerKey() { 
				return getKey(this);
            } 
        }, 
		SHOW_RESOURCE_BALLS {
            void act(HumanoidRenderContext ctx) {
                BallBuilder.getTheBallBuilder().runBalls();
            }
            int getTriggerKey() { 
                return getKey(this);
            }            
        },
		PICK_BALLS {
            void act(HumanoidRenderContext ctx) {
                BallBuilder.getTheBallBuilder().pick();
            }
            int getTriggerKey() { 
                return getKey(this);
            }            
        },
        BIGGER_PROJECTILE {
            void act(HumanoidRenderContext ctx) {
                ctx.getGameFeatureAdapter().getProjectileMgr().cmdBiggerProjectile();
            }
            int getTriggerKey() { 
                return getKey(this);
            }            
        },
        SMALLER_PROJECTILE {
            void act(HumanoidRenderContext ctx) {
                ctx.getGameFeatureAdapter().getProjectileMgr().cmdSmallerProjectile();
            }
            int getTriggerKey() { 
                return getKey(this);
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
			RenderConfigEmitter bce = hrc.getConfigEmitter();
			return hrc.getHumanoidFigure(bce.SINBAD_CHAR_IDENT());
		}
		final static int NULL_KEY = -100; // This input not mapped to any key; we'll use it in the event of not finding one from keyBindings
		static int getKey(PlayerAction actionType) {
			int keyInput = NULL_KEY; 
			String keyString = null;
			String action = actionType.name();
			if (keyBindings.myGeneralBindings.containsKey(action)) {
				keyString = keyBindings.myGeneralBindings.get(action).myBoundKeyName;
			} else {
				theDbg.logWarning("Attemping to retrieve key binding for " + action + ", but none is found");
			}
			try {
				if ((keyString.startsWith("AXIS")) || (keyString.startsWith("BUTTON"))) { // In this case, must be MouseInput
					// We'll have to use reflection to get from the config strings to the fields in jME's KeyInput and MouseInput
					Field keyField = MouseInput.class.getField(keyString);
					// Inverting this result to stuck with old trick (for now) of having mouse triggers be negative so
					// makeJME3InputTriggers can ignore mouse inputs for the purpose of the keyboard mapping help screen
					// setup. Value is re-inverted there for proper handling.
					keyInput = -keyField.getInt(keyField);
				} else { // ... regular KeyInput
					Field keyField = KeyInput.class.getField("KEY_" + keyString.toUpperCase());
					keyInput = keyField.getInt(keyField);
				}
			} catch (Exception e) {
				getLoggerForClass(HumanoidPuppetActions.PlayerAction.class).warn(
						"Error getting binding for " + actionType.toString() + ": " + e);
			}
			return keyInput;
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
		Trigger[] newTrigger = null;
		if (keyCode != PlayerAction.NULL_KEY) {
			if (keyCode > 0) {
				KeyBindingTracker.addBinding(pa.name(), keyCode);
				newTrigger = new Trigger[] { new KeyTrigger(keyCode)};
			}
			else {newTrigger = new Trigger[] { new MouseButtonTrigger(-keyCode)};}
		}
		return newTrigger;
	}
    
    static void setupActionListeners(InputManager inputManager, final HumanoidRenderContext ctx, KeyBindingConfig config) {
		keyBindings = config;
        PlayerAction pavals[] = PlayerAction.values();
		List<String> actionNamesList = new ArrayList<String>();
		boolean minSimMode = ctx.getConfigEmitter().isMinimalSim();
        for (int pai =0; pai < pavals.length; pai++) { 
            PlayerAction pa = pavals[pai];
			String actionName = pa.name();
			if (minSimMode) {
				if (!pa.includedInMinSim()) {
					continue;
				}
			}
			Trigger[] newTrigger = makeJME3InputTriggers(pa);
			if (newTrigger != null) {
				actionNamesList.add(actionName);
				inputManager.addMapping(actionName, makeJME3InputTriggers(pa));
			}
            
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
