/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.test.mainers;

import org.cogchar.bind.midi.FunMidiEventRouter;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class CogcharMidiTestMain {
	public static void main(String[] args) {
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);
		FunMidiEventRouter fmer = new FunMidiEventRouter();
		try {
			fmer.startPumpingMidiEvents();
			FunMidiEventRouter.FunListener fl = new FunMidiEventRouter.FunListener();
			fmer.registerListener(fl);
			Thread.sleep(10 * 1000);
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			fmer.logInfo("Doing cleanup");
			fmer.cleanup();
		}
		fmer.logInfo("main() is done!");
	}	
}
