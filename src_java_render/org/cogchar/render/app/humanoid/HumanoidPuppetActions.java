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

import org.cogchar.platform.trigger.DummyBinding;
import org.cogchar.render.model.humanoid.HumanoidFigure;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import java.util.ArrayList;
import java.util.List;

import org.cogchar.blob.emit.BonyConfigEmitter;
import org.cogchar.render.app.core.BoundAction;

/**
 
 */
public class HumanoidPuppetActions {
    public enum PlayerAction {
        RESET_CAMERA {
            void act(HumanoidRenderContext ctx) {
				ctx.setDefaultCameraLocation();
            }
            Trigger[] makeJME3InputTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_F1)};
            }
			@Override boolean includedInMinSim() { 	return true; }				
        },
        TOGGLE_SKEL_HILITE {
            void act(HumanoidRenderContext ctx) {
				ctx.toggleDebugSkeletons();
            }
            Trigger[] makeJME3InputTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_F2)};
            }
			@Override boolean includedInMinSim() { 	return true; }		
        },  		
        SAY_THE_TIME {
			// uses default act() and boxy wiring
            Trigger[] makeJME3InputTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_F3)};
            }
			@Override boolean includedInMinSim() { 	return true; }		
        },      

		STOP_AND_RESET_CHAR {
			// uses default act() and boxy wiring
            Trigger[] makeJME3InputTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_F4)};
            }
			@Override boolean includedInMinSim() { 	return true; }		
        },  		
		
        RELOAD_BEHAVIOR {
			// uses default act() and boxy wiring
            Trigger[] makeJME3InputTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_F8)};
            }
			@Override boolean includedInMinSim() { 	return true; }				
        },
		
        DANGER_YOGA {
			// This yoga-dance shold not be routed to physical robot.
			// 
			// uses default act() and boxy wiring
            Trigger[] makeJME3InputTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_F9)};
            }
			@Override boolean includedInMinSim() { 	return true; }		
        },
		
		/*** F10 is intercepted by Netbeans platform - do not use!!! **/
        
		UPDATE_BONY_CONFIG {
			// uses default act() and boxy wiring
            Trigger[] makeJME3InputTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_F12)};
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
            Trigger[] makeJME3InputTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_K)};
			}
        },
        STAND_UP {
            void act(HumanoidRenderContext ctx) {
				HumanoidFigure hw = getSinbad(ctx);
				hw.makeSinbadStandUp();
            }
            Trigger[] makeJME3InputTriggers() { 
                return new Trigger[] {new KeyTrigger(KeyInput.KEY_SPACE)};
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
            Trigger[] makeJME3InputTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_B)};
            }	
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
			return hrc.getHumanoidFigure(bce.SINBAD_CHAR_IDENT());
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
