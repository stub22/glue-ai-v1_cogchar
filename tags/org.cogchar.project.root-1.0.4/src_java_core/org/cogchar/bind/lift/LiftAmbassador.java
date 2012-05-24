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
package org.cogchar.bind.lift;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ryan Biggs
 */
public class LiftAmbassador {

	static Logger theLogger = LoggerFactory.getLogger(LiftAmbassador.class);
	private static List<ControlConfig> controls = new ArrayList<ControlConfig>();
	private static LiftSceneInterface sceneLauncher;
	private static LiftInterface lift;
	private static boolean configReady = false;

	public interface LiftSceneInterface {

		boolean triggerScene(String scene);
	}

	public interface LiftInterface {

		void notifyConfigReady();
	}

	public static void storeControlsFromConfig(LiftConfig config) {
		controls = config.myCCs;
		theLogger.info("RDF Lift config sent to LiftAmbassador");
		configReady = true;
		if (lift != null) {
			lift.notifyConfigReady();
			theLogger.info("Lift notified of config ready");
		}
	}

	public static ArrayList<ControlConfig> getControls() {
		return (ArrayList<ControlConfig>) controls;
	}

	public static String getPrefix() {
		return LiftConfigNames.partial_P_control + "_";
	}

	public static boolean triggerScene(String scene) {
		boolean success = false;
		if (sceneLauncher != null) {
			success = sceneLauncher.triggerScene(scene);
		}
		return success;
	}

	public static void setSceneLauncher(LiftSceneInterface launcher) {
		sceneLauncher = launcher;
	}

	public static void setLiftMessenger(LiftInterface li) {
		theLogger.info("Lift messenger set");
		lift = li;
	}

	public static boolean checkConfigReady() {
		return configReady;
	}
}
