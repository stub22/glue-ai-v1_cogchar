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

package org.cogchar.bind.midi.in;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Transmitter;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.bind.midi.general.FunMidiEventRouter;
import org.cogchar.bind.midi.out.CogcharMidiOutputTestMain;
import org.cogchar.bind.midi.out.NovLpadTest;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class TempMidiBridge extends BasicDebugger {
	
	private		FunMidiEventRouter		myFMER;
	private		CCParamRouter			myCCPR;
	
	public static void main(String[] args) {
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.INFO);
		
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
		myCCPR= new CCParamRouter(myFMER);
	}
	public CCParamRouter getCCParamRouter() { 
		return myCCPR;
	}
	public void playSomeOutput() { 
	
		FunMidiEventRouter fmer = new FunMidiEventRouter();
		NovLpadTest nlt = new NovLpadTest();
		try {
			CogcharMidiOutputTestMain cmotm = new CogcharMidiOutputTestMain();
			nlt.startLightDemo();

			cmotm.playSomeNotes();
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			fmer.logInfo("Doing cleanup");
			fmer.cleanup();
			if (nlt != null) {
				nlt.cleanup();
			}
		}	
	}
	
}
