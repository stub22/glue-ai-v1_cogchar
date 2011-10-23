/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.demo.bony;

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
 * @author pow
 */
public class BowlAtHumanoidActions {
    enum PlayerAction {

        TOGGLE_KIN_MODE {
            void act(BowlAtHumanoidApp app) {
				HumanoidRagdollWrapper hw = app.getHumdWrap();
				hw.toggleKinMode();
            }
            Trigger[] makeTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_H),
                      new KeyTrigger(KeyInput.KEY_N)  };
            }
        },
        STAND_UP {
            void act(BowlAtHumanoidApp app) {
				HumanoidRagdollWrapper hw = app.getHumdWrap();
				hw.standUp();
            }
            Trigger[] makeTriggers() { 
                return new Trigger[] {new KeyTrigger(KeyInput.KEY_SPACE)};
            }            
        },
        BOOGIE {
            void act(BowlAtHumanoidApp app) {
				HumanoidRagdollWrapper hw = app.getHumdWrap();
				hw.boogie();
            }
            Trigger[] makeTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_B)};
            }
        },        
        SHOOT {
            void act(BowlAtHumanoidApp app) {
                app.cmdShoot();
            }
            Trigger[] makeTriggers() { 
                return new Trigger[] {new MouseButtonTrigger(MouseInput.BUTTON_LEFT)};
            }            
        }, 
        BOOM {
            void act(BowlAtHumanoidApp app) {
                app.cmdBoom();
            }
            Trigger[] makeTriggers() { 
                return new Trigger[] { new MouseButtonTrigger(MouseInput.BUTTON_RIGHT)};
            }            
        }, 
        BIGGER_PROJECTILE {
            void act(BowlAtHumanoidApp app) {
                app.getProjectileMgr().cmdBiggerProjectile();
            }
            Trigger[] makeTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_PERIOD) };
            }            
        },
        SMALLER_PROJECTILE {
            void act(BowlAtHumanoidApp app) {
                app.getProjectileMgr().cmdSmallerProjectile();
            }
            Trigger[] makeTriggers() { 
                return new Trigger[] { new KeyTrigger(KeyInput.KEY_COMMA) };
            }            
        };  // Last enum constant code block gets a semicolon.
        
        abstract void act(BowlAtHumanoidApp app);
        abstract Trigger[] makeTriggers();
    };
    static void setupActionListeners(InputManager inputManager, final BowlAtHumanoidApp app) {
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
