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

import org.cogchar.platform.gui.keybind.KeyBindingConfig;
import org.cogchar.platform.gui.keybind.KeyBindingTracker;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;

import  org.cogchar.render.sys.input.VW_InputBindingFuncs;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.cogchar.platform.trigger.CogcharActionBinding;
import org.cogchar.render.model.humanoid.HumanoidFigure;
import org.cogchar.render.model.humanoid.HumanoidFigureManager;
import org.cogchar.render.app.bony.BonyRenderContext;
import org.cogchar.blob.emit.RenderConfigEmitter;
import org.cogchar.platform.trigger.BasicActionBindingImpl;

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
			@Override boolean includedInMinSim() { 	return true; }				
        },
        TOGGLE_SKEL_HILITE {
            void act(HumanoidRenderContext ctx) {
				ctx.getHumanoidFigureManager().toggleDebugSkeletons();
            }
			@Override boolean includedInMinSim() { 	return true; }		
        },  		
        SAY_THE_TIME {
			// uses default act() and boxy wiring
			@Override boolean includedInMinSim() { 	return true; }		
        },      

		STOP_AND_RESET_CHAR {
			// uses default act() and boxy wiring
			@Override boolean includedInMinSim() { 	return true; }		
        },  		
		STOP_RESET_AND_RECENTER_CHAR {
			// uses default act() and boxy wiring
			@Override boolean includedInMinSim() { 	return true; }		
        },  		
		
		USE_PERM_ANIMS {
			// uses default act() and boxy wiring
			@Override boolean includedInMinSim() { 	return true; }		
        },  			
		USE_TEMP_ANIMS {
			// uses default act() and boxy wiring
			@Override boolean includedInMinSim() { 	return true; }		
        },  				
		
        RELOAD_BEHAVIOR {
			// uses default act() and boxy wiring
			@Override boolean includedInMinSim() { 	return true; }				
        },
		
        DANGER_YOGA {
			// This yoga-dance should not be routed to physical robot.
			// 
			// uses default act() and boxy wiring
			@Override boolean includedInMinSim() { 	return true; }		
        },
		
		/*** F10 is intercepted by Netbeans platform - do not use!!! **/
  /*      
		UPDATE_WORLD_CONFIG {
			void act(HumanoidRenderContext ctx) {
				ctx.requestConfigReload("WorldConfig");
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
			@Override boolean includedInMinSim() { 	return true; }				
        },
		
		UPDATE_HUMANOIDS {
			void act(HumanoidRenderContext ctx) {
				ctx.requestConfigReload("AllHumanoidConfig");
            }
			@Override boolean includedInMinSim() { 	return true; }				
        },
*/
		/*** The actions below are for V-world-only goodies (like Sinbad, projectiles, etc.)
		 *		not available in RK "Simulator"  
		 *		(hence they do not override includedInMinSim()).
		 ***/
	
        TOGGLE_KIN_MODE {
            void act(HumanoidRenderContext ctx) {
				HumanoidFigure hw = getSinbad(ctx, ctx.getHumanoidFigureManager());
				hw.togglePhysicsKinematicModeEnabled();
            }
        },
        STAND_UP {
            void act(HumanoidRenderContext ctx) {
				HumanoidFigure hw = getSinbad(ctx, ctx.getHumanoidFigureManager());
				hw.makeSinbadStandUp();
            }
        },
        BOOGIE {
			// Triggers a JME3 animation
            void act(HumanoidRenderContext ctx) {
				HumanoidFigure hw = getSinbad(ctx, ctx.getHumanoidFigureManager());
				if (hw != null) {
					hw.runSinbadBoogieAnim();
				}
            }
        },   		
  		
        SHOOT {
            void act(HumanoidRenderContext ctx) {
                ctx.getGameFeatureAdapter().cmdShoot();
            } 
        }, 
        BOOM {
            void act(HumanoidRenderContext ctx) {
                ctx.getGameFeatureAdapter().toggleAnnoyingStuff();
            }
        }, 
		SHOW_RESOURCE_BALLS {
            void act(HumanoidRenderContext ctx) {
                BallBuilder.getTheBallBuilder().runBalls();
            }     
        },
		PICK_BALLS {
            void act(HumanoidRenderContext ctx) {
                BallBuilder.getTheBallBuilder().pick();
            }         
        },
        BIGGER_PROJECTILE {
            void act(HumanoidRenderContext ctx) {
                ctx.getGameFeatureAdapter().getProjectileMgr().cmdBiggerProjectile();
            }
        },
        SMALLER_PROJECTILE {
            void act(HumanoidRenderContext ctx) {
                ctx.getGameFeatureAdapter().getProjectileMgr().cmdSmallerProjectile();
            }         
        };  // Last enum constant code block gets a semicolon.
		
     
		BasicActionBindingImpl	myBoundAction = new BasicActionBindingImpl();
		public CogcharActionBinding getBinding() { 
			return myBoundAction;
		}		
		boolean includedInMinSim() { 
			return myBoundAction.includedInMinSim();
		}
		int getTriggerKey() {
			return getKey(this);
		}
        void act(HumanoidRenderContext ctx) {
			myBoundAction.perform();
		}

		HumanoidFigure getSinbad(BonyRenderContext brc, HumanoidFigureManager  hfm) { 
			RenderConfigEmitter bce = brc.getConfigEmitter();
			return hfm.getHumanoidFigure(bce.SINBAD_CHAR_IDENT());
		}
		
		final static int NULL_KEY = -100; // This input not mapped to any key; we'll use it in the event of not finding one from keyBindings
		static int getKey(PlayerAction actionType) {
			int keyInputCode = NULL_KEY; 
			String keyName = null;
			String action = actionType.name();
			if (keyBindings.myGeneralBindings.containsKey(action)) {
				keyName = keyBindings.myGeneralBindings.get(action).myBoundKeyName;
			} else {
				theDbg.logWarning("Attemping to retrieve key binding for " + action + ", but none is found");
			}
			keyInputCode = VW_InputBindingFuncs.getKeyConstantForName(keyName);
			return keyInputCode;
		}
	};
  static public void setupActionListeners(InputManager inputManager, final HumanoidRenderContext ctx, 
				KeyBindingConfig kbConfig, KeyBindingTracker kbt) {
		keyBindings = kbConfig;
        HumanoidPuppetActions.PlayerAction pavals[] = HumanoidPuppetActions.PlayerAction.values();
		List<String> actionNamesList = new ArrayList<String>();
		boolean minSimMode = ctx.getConfigEmitter().isMinimalSim();
        for (int pai =0; pai < pavals.length; pai++) { 
            HumanoidPuppetActions.PlayerAction pa = pavals[pai];
			String actionName = pa.name();
			if (minSimMode) {
				if (!pa.includedInMinSim()) {
					continue;
				}
			}
			Trigger[] newTriggerBlock = VW_InputBindingFuncs.makeJME3InputTriggers(pa.getTriggerKey(), pa.name(), kbt);
			if (newTriggerBlock != null) {
				actionNamesList.add(actionName);
				inputManager.addMapping(actionName, newTriggerBlock);
			}
            
        }
		String actionNames[] = new String[actionNamesList.size()];
		actionNamesList.toArray(actionNames);
		
		registerListenerForActionSubset(inputManager, ctx, actionNames);
    }
	static void registerListenerForActionSubset(InputManager inputManager, final HumanoidRenderContext ctx,
				String actionNames[]) {
	  
		// Here we register a single actionListener to be called back for all the actionNames.
		// The trick below that justifies the enum contortions above is using PlayerAction.valueOf instead of a hash table.	
        inputManager.addListener(new ActionListener() {

            public void onAction(String name, boolean isPressed, float tpf) {
				// Do an enum lookup-by-name
                HumanoidPuppetActions.PlayerAction action = HumanoidPuppetActions.PlayerAction.valueOf(name);
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
