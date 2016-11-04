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
package org.cogchar.bind.midi.out;

import org.appdapter.core.log.BasicDebugger;
import org.cogchar.bind.midi.general.FunMidiEventRouter;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class SeqSwitcherMain extends BasicDebugger {

	FunMidiEventRouter myFMER;

	public static void main(String[] args) {
		// Because there is a log4j.properties file in this project, we do not need to do this Log4J config from code.
		// It seems that when we do, we get two sets of loggers, which is kinda gross looking in the console output.
		// But in another main, somewhere else, we might want these two lines uncommented to make logging output go.
		// org.apache.log4j.BasicConfigurator.configure();
		// org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);
		SeqSwitcherMain ssm;
		try {
			ssm = new SeqSwitcherMain();
			ssm.go();

		} catch (Throwable t) {
			t.printStackTrace();
		}
		LoggerFactory.getLogger(SeqSwitcherMain.class).info("main() is done!");
	}

	public SeqSwitcherMain() {
		myFMER = new FunMidiEventRouter();
	}

	public void cleanup() {
		getLogger().info("Doing cleanup");
		if (myFMER != null) {
			myFMER.cleanup();
		}
	}

	public void go() {
		getLogger().info("We are goin, baby!  Starting light demo.");
		
			
		getLogger().info("Starting MIDI router event pump");
		myFMER.startPumpingMidiEvents();
		Switcheroo swoo = new Switcheroo();
		DemoMidiOutputPlayer dmop = new DemoMidiOutputPlayer();
		swoo.myDMOP = dmop;
		NovLpadTest nlt = new NovLpadTest();
		boolean lpadOK = nlt.startLightDemo();
		if (lpadOK) {
			swoo.myNLT = nlt;
		}
		myFMER.registerListener(swoo);
	}
}
