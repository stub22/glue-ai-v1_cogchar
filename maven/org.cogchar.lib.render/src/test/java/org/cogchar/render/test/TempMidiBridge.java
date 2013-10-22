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

package org.cogchar.render.test;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Transmitter;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.bind.midi.FunMidiEventRouter;
import org.cogchar.bind.midi.InterestingMidiEvent;
import org.cogchar.bind.midi.MidiEventReporter;
/**
 * @author Stu B. <www.texpedient.com>
 */

public class TempMidiBridge extends BasicDebugger {
	
	private		FunMidiEventRouter		myFMER;
	
	public static void main(String[] args) {
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);
		
		TempMidiBridge tmb = new TempMidiBridge();
		try {
			tmb.initMidiRouter();
			Thread.sleep(15 * 1000);			
		}  catch (Throwable t) {
			tmb.getLogger().error("caught: ", t);
		}
		finally {
			if (tmb != null) {
				tmb.cleanup();
				tmb = null;
			}
		}	
	}
	public void cleanup() { 
		if (myFMER != null) {
			myFMER.cleanup();
			myFMER = null;
		}
	}
	public void initMidiRouter() { 
		if (myFMER != null) {
			getLogger().warn("a FunMidiEventRouter is already running - returning.");
			return;
		}
		myFMER = new FunMidiEventRouter();
		myFMER.startPumpingMidiEvents();
		OurListener ol = new OurListener();
		myFMER.registerListener(ol);		
	}
	// Something not unlike a Novation Nocturn, in x Channels
	public void connectAndMapContSurf (String devName, int chanCount) {
		
	}
	public static class OurListener extends BasicDebugger implements MidiEventReporter.Listener {

		@Override public void reportEvent(InterestingMidiEvent ime) {
			getLogger().info("*** Oulist received midi event: {} ", ime);
		}
	}	
}
