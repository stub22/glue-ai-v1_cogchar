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
package org.cogchar.app.buddy.busker;

import java.util.List;
import org.cogchar.app.puma.cgchr.PumaDualCharacter;
import org.cogchar.app.puma.boot.PumaContextCommandBox;
import org.cogchar.platform.trigger.BoxSpace;
import org.cogchar.platform.trigger.CogcharActionBinding;
import org.cogchar.platform.trigger.CogcharScreenBox;
import org.cogchar.platform.trigger.CommandSpace;
import org.cogchar.platform.trigger.CommandBinding;
import org.cogchar.platform.trigger.BasicActionBindingImpl;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.blob.emit.RepoClientTester;
import org.cogchar.blob.emit.RepoClientTester.CommandRec;

import org.cogchar.bind.rk.robot.client.RobotAnimClient.BuiltinAnimKind;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Stu B. <www.texpedient.com>
 */
public class TriggerItems {
	
	public static class StopAndReset extends TriggerItem {

		@Override public void fire(CogcharScreenBox targetBox) {
			logFiring(targetBox);
			PumaDualCharacter pdc = (PumaDualCharacter) targetBox;
			pdc.stopAndReset();
		}
	}
	public static class StopResetAndRecenter extends TriggerItem {

		@Override public void fire(CogcharScreenBox targetBox) {
			logFiring(targetBox);
			PumaDualCharacter pdc = (PumaDualCharacter) targetBox;
			pdc.stopResetAndRecenter();
		}
	}	

	public static class DangerYoga extends TriggerItem {

		@Override public void fire(CogcharScreenBox targetBox) {
			logFiring(targetBox);
			PumaDualCharacter pdc = (PumaDualCharacter) targetBox;
			pdc.playBuiltinAnimNow(BuiltinAnimKind.BAK_DANGER_YOGA);
		}
	}

	public static class SayTheTime extends TriggerItem {

		@Override public void fire(CogcharScreenBox targetBox) {
			logFiring(targetBox);
			PumaDualCharacter pdc = (PumaDualCharacter) targetBox;
			pdc.sayText("The time is now, " + System.currentTimeMillis());
		}
	}
	
	public static class ResetMainCamera extends TriggerItem {
		@Override public void fire(CogcharScreenBox targetBox) {
			logFiring(targetBox);
			PumaContextCommandBox pccb = (PumaContextCommandBox) targetBox;
			pccb.resetMainCameraLocation();
		}
		
	}
	
/*	Likely doing away with this class in favor of PumaAppContext.updateConfigByRequest --  see additional
 *	commentary in PumaAppContext and HumanoidPuppetActions
	public static class UpdateBonyConfig extends TriggerItem {

		public ClassLoader myOptResourceClassLoader;

		@Override public void fire(DummyBox targetBox) {
			PumaDualCharacter pdc = (PumaDualCharacter) targetBox;
			// Use of bonyConfigPaths seems to be going away in the brave new query-config era
			//String bonyRdfConfigPath = pdc.myUpdateBonyRdfPath;
			//logInfo("Updating bony config using path[" + bonyRdfConfigPath + "] for char [" + pdc + "]");
			logInfo("Updating bony config for char [" + pdc + "]");
			//if ((pdc != null) && (bonyRdfConfigPath != null)) {
				//pdc.updateBonyConfig(bonyRdfConfigPath, myOptResourceClassLoader);
			if (pdc != null)  {
				pdc.updateBonyConfig();
			}
		}
	}
*/

	public static class ReloadBehavior extends TriggerItem {

		public ClassLoader myOptResourceClassLoader;

		@Override public void fire(CogcharScreenBox targetBox) {
			PumaDualCharacter pdc = (PumaDualCharacter) targetBox;

			if (pdc != null) {
				try {
					logInfo("Stopping theater for char [" + pdc + "]");
					pdc.stopTheater();
					logWarning("Reloading behavior config FROM TEST FILE for char [" + pdc + "]");
					pdc.loadBehaviorConfigFromTestFile(true);
					logInfo("Restarting theater for char [" + pdc + "]");
					pdc.startTheater();
				} catch (Throwable t) {
					logError("Problem during ReloadBehavior_TI", t);
				}
			} else {
				logWarning("Not reloading behavior...character is null!");
			}
		}
	}

	public static class SceneMsg extends TriggerItem {

		public String sceneInfo = "none";

		@Override public void fire(CogcharScreenBox targetBox) {
			logInfo("trigger[" + toString() + "] sending [" + sceneInfo + " to " + targetBox.toString());
			// PumaDualCharacter pdc = (PumaDualCharacter) targetBox;
			// pdc.sayText("The time is now, " + System.currentTimeMillis());
		}
	}
	public static class UsePermAnims extends TriggerItem {

		@Override public void fire(CogcharScreenBox targetBox) {
			logFiring(targetBox);
			PumaDualCharacter pdc = (PumaDualCharacter) targetBox;
			pdc.usePermAnims();
		}
	}
	public static class UseTempAnims extends TriggerItem {

		@Override public void fire(CogcharScreenBox targetBox) {
			logFiring(targetBox);
			PumaDualCharacter pdc = (PumaDualCharacter) targetBox;
			pdc.useTempAnims();
		}
	}
	/**
	 * ./maven/org.cogchar.lib.render/src/main/j
estConfigReload("WorldConfig");
./maven/org.cogchar.lib.render/src/main/j
estConfigReload("BoneRobotConfig");
./maven/org.cogchar.lib.render/src/main/j
estConfigReload("AllHumanoidConfig");
	 */
	
	protected static boolean forceFreshDefaultMainConfig = false;
	
	
	public static class UpdateWorldConfig extends TriggerItem {
		@Override public void fire(CogcharScreenBox targetBox) {
			logFiring(targetBox);
			PumaContextCommandBox pccb = (PumaContextCommandBox) targetBox;
			pccb.updateConfigByRequest("WorldConfig", forceFreshDefaultMainConfig);
		}
	}
	public static class UpdateBoneRobotConfig extends TriggerItem {
		@Override public void fire(CogcharScreenBox targetBox) {
			logFiring(targetBox);
			PumaContextCommandBox pccb = (PumaContextCommandBox) targetBox;
			pccb.updateConfigByRequest("BoneRobotConfig", forceFreshDefaultMainConfig);
		}
	}
	public static class UpdateAllHumanoidConfig extends TriggerItem {
		@Override public void fire(CogcharScreenBox targetBox) {
			logFiring(targetBox);
			PumaContextCommandBox pccb = (PumaContextCommandBox) targetBox;
			pccb.updateConfigByRequest("AllHumanoidConfig", forceFreshDefaultMainConfig);
		}
	}
	
	
	
	private static Logger theLogger = LoggerFactory.getLogger(TriggerItems.class);
	private static Logger getLogger() { 
		return theLogger;
	}
	public static TriggerItem makeTriggerItem(String trigFQCN) {
		// forName(String name, boolean initialize, ClassLoader loader) 
        //  Returns the Class object associated with the class or interface with the given string name, using the given class loader.
		TriggerItem  ti = null;
		try {
			Class trigClass = Class.forName(trigFQCN);
			ti = (TriggerItem) trigClass.newInstance();
		} catch (Throwable t) {
			theLogger.error("Cannot make trigger item for class " + trigFQCN, t);
		}
		return ti;
	}
	public static void populateCommandSpace(RepoClient rc, CommandSpace cSpace, BoxSpace boxSpace) {
		List<CommandRec> cmdRecList = RepoClientTester.queryCommands(rc);
		for (CommandRec cRec : cmdRecList) {
			TriggerItem ti = makeTriggerItem(cRec.trigFQCN());
			CogcharScreenBox csBox = boxSpace.findBox(cRec.boxID());
			if ((ti != null) && (csBox != null)) {
				CommandBinding cb = cSpace.findOrMakeBinding(cRec.cmdID());
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
