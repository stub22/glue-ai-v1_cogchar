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

import org.cogchar.bundle.app.puma.PumaDualCharacter;
import org.cogchar.platform.trigger.DummyBox;


/**
 * @author Stu B. <www.texpedient.com>
 */
public class TriggerItems {
	
	public static class StopAndReset extends TriggerItem {

		@Override public void fire(DummyBox targetBox) {
			logFiring(targetBox);
			PumaDualCharacter pdc = (PumaDualCharacter) targetBox;
			pdc.stopAndReset();
		}
	}

	public static class DangerYoga extends TriggerItem {

		@Override public void fire(DummyBox targetBox) {
			logFiring(targetBox);
			PumaDualCharacter pdc = (PumaDualCharacter) targetBox;
			pdc.playDangerYogaTestAnim();
		}
	}

	public static class SayTheTime extends TriggerItem {

		@Override public void fire(DummyBox targetBox) {
			logFiring(targetBox);
			PumaDualCharacter pdc = (PumaDualCharacter) targetBox;
			pdc.sayText("The time is now, " + System.currentTimeMillis());
		}
	}

	public static class UpdateBonyConfig extends TriggerItem {

		public ClassLoader myOptResourceClassLoader;

		@Override public void fire(DummyBox targetBox) {
			PumaDualCharacter pdc = (PumaDualCharacter) targetBox;
			String bonyRdfConfigPath = pdc.myUpdateBonyRdfPath;
			logInfo("Updating bony config using path[" + bonyRdfConfigPath + "] for char [" + pdc + "]");
			if ((pdc != null) && (bonyRdfConfigPath != null)) {
				pdc.updateBonyConfig(bonyRdfConfigPath, myOptResourceClassLoader);
			}
		}
	}

	public static class ReloadBehavior extends TriggerItem {

		public ClassLoader myOptResourceClassLoader;

		@Override public void fire(DummyBox targetBox) {
			PumaDualCharacter pdc = (PumaDualCharacter) targetBox;

			if (pdc != null) {
				try {
					logInfo("Stopping theater for char [" + pdc + "]");
					pdc.stopTheater();
					logInfo("Reloading behavior config for char [" + pdc + "]");
					pdc.loadBehaviorConfig(true);
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

		@Override public void fire(DummyBox targetBox) {
			logInfo("trigger[" + toString() + "] sending [" + sceneInfo + " to " + targetBox.toString());
			// PumaDualCharacter pdc = (PumaDualCharacter) targetBox;
			// pdc.sayText("The time is now, " + System.currentTimeMillis());
		}
	}
}
