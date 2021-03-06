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
package org.cogchar.bundle.app.vworld.busker;

import java.util.List;
import java.util.concurrent.Future;
import org.cogchar.app.puma.behavior.PumaBehaviorAgent;
import org.cogchar.bundle.app.vworld.central.VWCtxCmdBox;
import org.cogchar.platform.trigger.BoxSpace;
import org.cogchar.platform.trigger.CogcharActionBinding;
import org.cogchar.platform.trigger.CogcharScreenBox;
import org.cogchar.platform.trigger.CommandSpace;
import org.cogchar.platform.trigger.CommandBinding;
import org.cogchar.platform.trigger.BasicActionBindingImpl;
import org.appdapter.fancy.rclient.RepoClient;
import org.cogchar.app.puma.behavior.DirectBehaviorAgent;
import org.cogchar.bundle.app.vworld.startup.PumaVirtualWorldMapper;
import org.appdapter.help.repo.ChannelsTriggersHelper;
import org.appdapter.help.repo.CommandRec;
import org.appdapter.core.name.Ident;

import org.cogchar.bind.mio.robot.client.RobotAnimClient.BuiltinAnimKind;
import org.cogchar.render.goody.basic.DataballGoodyBuilder;
import org.cogchar.render.model.humanoid.HumanoidFigure;
import org.cogchar.render.model.humanoid.HumanoidFigure_SinbadTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains all the trigger-"commands" currently available through V-World GUI
 * (except for scene-triggers). These triggers fire on either the systemContext
 * (VWCtxCmdBox) or a particular character (PumaBehaviorAgent).
 *
 * @author Stu B. <www.texpedient.com>
 */
public class TriggerItems {

    public static class SceneMsg extends TriggerItem {

        public String sceneInfo = "none";

        @Override
        public void fire(CogcharScreenBox targetBox) {
            logFiring(targetBox, sceneInfo, sceneInfo);
            // PumaDualCharacter pdc = (PumaDualCharacter) targetBox;
            // pdc.sayText("The time is now, " + System.currentTimeMillis());
        }
    }
    /*
     public static abstract class DualCharTI extends TriggerItem {
     abstract void fireOnPDC(PumaDualCharacter pdc);
     @Override public void fire(CogcharScreenBox targetBox) {
     logFiring(targetBox);
     PumaDualCharacter pdc = (PumaDualCharacter) targetBox;
     fireOnPDC(pdc);
     }
     }
     */

    public static abstract class BehaviorTI extends TriggerItem {

        abstract void fireOnPBA(PumaBehaviorAgent pba);

        @Override
        public void fire(CogcharScreenBox targetBox) {
            logFiring(targetBox);
            PumaBehaviorAgent pba = (PumaBehaviorAgent) targetBox;
            fireOnPBA(pba);
        }
    }

    public static class StopAndReset extends BehaviorTI {

        @Override
        public void fireOnPBA(PumaBehaviorAgent pba) {
            pba.stopAndReset();
        }
    }

    public static class StopResetAndRecenter extends BehaviorTI {

        @Override
        public void fireOnPBA(PumaBehaviorAgent pba) {
            pba.stopResetAndRecenter();
        }
    }

    public static abstract class DirectBehaviorTI extends TriggerItem {

        abstract void fireOnDBA(DirectBehaviorAgent dba);

        @Override
        public void fire(CogcharScreenBox targetBox) {
            logFiring(targetBox);
            DirectBehaviorAgent dba = (DirectBehaviorAgent) targetBox;
            fireOnDBA(dba);
        }
    }

    public static class DangerYoga extends DirectBehaviorTI {

        @Override
        public void fireOnDBA(DirectBehaviorAgent pba) {
            pba.playBuiltinAnimNow(BuiltinAnimKind.BAK_DANGER_YOGA);
        }
    }

    public static class SayTheTime extends DirectBehaviorTI {

        @Override
        public void fireOnDBA(DirectBehaviorAgent pba) {
            pba.sayTextNow("The time is now, " + System.currentTimeMillis());
        }
    }

    public static class ReloadBehavior extends BehaviorTI {

        public ClassLoader myOptResourceClassLoader;

        @Override
        public void fireOnPBA(PumaBehaviorAgent pba) {

            if (pba != null) {
                try {
                    boolean cancelOutJobs = true;
                    getLogger().info("Stopping theater for PBA [{}], cancelOutJobs={}", pba, cancelOutJobs);
                    pba.stopTheater(cancelOutJobs);
                    getLogger().warn("Reloading behavior config FROM TEST FILE for char [" + pba + "]");
                    pba.loadBehaviorConfigFromTestFile(true);
                    getLogger().info("Restarting theater for char [" + pba + "]");
                    pba.startTheater(null);
                } catch (Throwable t) {
                    getLogger().error("Problem during ReloadBehavior_TI", t);
                }
            } else {
                getLogger().warn("Not reloading behavior...character is null!");
            }
        }
    }

    public static class UsePermAnims extends BehaviorTI {

        @Override
        public void fireOnPBA(PumaBehaviorAgent pba) {
            pba.usePermAnims();
        }
    }

    public static class UseTempAnims extends BehaviorTI {

        @Override
        public void fireOnPBA(PumaBehaviorAgent pba) {
            pba.useTempAnims();
        }
    }

    public static abstract class CtxCmdBoxTI extends TriggerItem {

        protected boolean myForceMainConfigResetFlag = false;

        abstract void fireOnPCCB(VWCtxCmdBox pccb);

        @Override
        public void fire(CogcharScreenBox targetBox) {
            logFiring(targetBox);
            VWCtxCmdBox pccb = (VWCtxCmdBox) targetBox;
            fireOnPCCB(pccb);
        }
    }

    public static class ResetMainCamera extends CtxCmdBoxTI {

        @Override
        public void fireOnPCCB(VWCtxCmdBox pccb) {
            pccb.resetMainCameraLocation();
            // ctx.setDefaultCameraLocation();
        }
    }

    public static class UpdateWorldConfig extends CtxCmdBoxTI {

        @Override
        public void fireOnPCCB(VWCtxCmdBox pccb) {
            Future<Boolean> resultFuture = pccb.processUpdateRequestAsync(VWCtxCmdBox.WORLD_CONFIG, myForceMainConfigResetFlag);
        }
    }

    public static class UpdateBoneRobotConfig extends CtxCmdBoxTI {

        @Override
        public void fireOnPCCB(VWCtxCmdBox pccb) {
            Future<Boolean> resultFuture = pccb.processUpdateRequestAsync(VWCtxCmdBox.BONE_ROBOT_CONFIG, myForceMainConfigResetFlag);
        }
    }

    public static class UpdateAllHumanoidConfig extends CtxCmdBoxTI {

        @Override
        public void fireOnPCCB(VWCtxCmdBox pccb) {
            Future<Boolean> resultFuture = pccb.processUpdateRequestAsync(VWCtxCmdBox.ALL_HUMANOID_CONFIG, myForceMainConfigResetFlag);
        }
    }

    public static class ToggleSkeletonHilite extends CtxCmdBoxTI {

        @Override
        public void fireOnPCCB(VWCtxCmdBox pccb) {
            pccb.getFigureManager().toggleDebugSkeletons();
        }
    }

    public static class Shoot extends CtxCmdBoxTI {

        @Override
        public void fireOnPCCB(VWCtxCmdBox pccb) {
            pccb.getGameFeatureAdapter().cmdShoot();
        }
    }

    public static class Boom extends CtxCmdBoxTI {

        @Override
        public void fireOnPCCB(VWCtxCmdBox pccb) {
            pccb.getGameFeatureAdapter().toggleAnnoyingStuff();
        }
    }

    public static class ShowResourceBalls extends CtxCmdBoxTI {

        @Override
        public void fireOnPCCB(VWCtxCmdBox pccb) {
            DataballGoodyBuilder.getTheBallBuilder().runBalls();
        }
    }

    public static class PickBalls extends CtxCmdBoxTI {

        @Override
        public void fireOnPCCB(VWCtxCmdBox pccb) {
            DataballGoodyBuilder.getTheBallBuilder().pick();
        }
    }

    public static class BiggerProjectile extends CtxCmdBoxTI {

        @Override
        public void fireOnPCCB(VWCtxCmdBox pccb) {
            pccb.getGameFeatureAdapter().cmdBiggerProjectile();
        }
    }

    public static class SmallerProjectile extends CtxCmdBoxTI {

        @Override
        public void fireOnPCCB(VWCtxCmdBox pccb) {
            pccb.getGameFeatureAdapter().cmdSmallerProjectile();
        }
    }
    /*
     public static class ToggleKinMode extends CtxCmdBoxTI {
     @Override public void fireOnPCCB(VWCtxCmdBox pccb) {
     HumanoidFigure hw = pccb.getSinbad();
     if (hw != null) {
     hw.togglePhysicsKinematicModeEnabled();
     }
     }		
     }
     * 
     */

    public static class StandUp extends CtxCmdBoxTI {

        @Override
        public void fireOnPCCB(VWCtxCmdBox pccb) {
            HumanoidFigure_SinbadTest hw = pccb.getSinbad();
            if (hw != null) {
                hw.makeSinbadStandUp();
            }
        }
    }

    public static class Boogie extends CtxCmdBoxTI {

        @Override
        public void fireOnPCCB(VWCtxCmdBox pccb) {
            HumanoidFigure_SinbadTest hw = pccb.getSinbad();
            if (hw != null) {
                // This is an Ogre skeletal animation run by JME3, bypassing our figure-anim system.
                hw.runSinbadBoogieAnim();
            }
        }
    }

    public static class ToggleHelp extends CtxCmdBoxTI {

        @Override
        public void fireOnPCCB(VWCtxCmdBox pccb) {
            PumaVirtualWorldMapper pvwm = pccb.getVirtualWorld();
            pvwm.toggleHelpScreenDisplay();
        }
    }
    private static Logger theLogger = LoggerFactory.getLogger(TriggerItems.class);

    private static Logger getLogger() {
        return theLogger;
    }

    public static TriggerItem makeTriggerItem(String trigFQCN) {
        // forName(String name, boolean initialize, ClassLoader loader) 
        //  Returns the Class object associated with the class or interface with the given string name, using the given class loader.
        TriggerItem ti = null;
        try {
            //From Major 3-25-2014: 
            //This is a temporary fix until we fully switch over to the New PUMA system. 
            //The FQCNs are being created from a spreadsheet linked to the main spreadsheet we're using. 
            String testQN = trigFQCN.replace("org.cogchar.app.buddy.busker", "org.cogchar.bundle.app.vworld.busker");
//            Class trigClass = Class.forName(trigFQCN);


            Class trigClass = Class.forName(testQN);
            ti = (TriggerItem) trigClass.newInstance();
        } catch (Throwable t) {
            theLogger.error("Cannot make trigger item for class " + trigFQCN, t);
        }
        return ti;
    }

    public static void populateCommandSpace(RepoClient rc, CommandSpace cSpace, BoxSpace boxSpace) {
        List<CommandRec> cmdRecList = ChannelsTriggersHelper.queryCommands(rc);
        for (CommandRec cRec : cmdRecList) {
            TriggerItem ti = makeTriggerItem(cRec.trigFQCN());
            Ident cmdID = cRec.cmdID();
            Ident boxID = cRec.boxID();
			// We go ahead and resolve the box to prove box-space is still working.
            CogcharScreenBox csBox = boxSpace.findBox(boxID);
            
            if ((ti != null) && (csBox != null)) {
				getLogger().info("Resolved boxID {} for cmdID {} to csBox-class {}", boxID, cmdID, csBox.getClass());
                CommandBinding cb = cSpace.findOrMakeBinding(cmdID);
                CogcharActionBinding cab = new BasicActionBindingImpl();
                cab.addTargetBox(csBox);
                cab.setTargetTrigger(ti);
                cb.appendAction(cab);
                getLogger().info("Successfully populated command: {}", cRec);
            } else {
                getLogger().warn("Skipping failed binding for trig=[{}] and box=[{}], for cmd=[{}]", new Object[]{ti, csBox, cRec});
            }

        }
    }
}
