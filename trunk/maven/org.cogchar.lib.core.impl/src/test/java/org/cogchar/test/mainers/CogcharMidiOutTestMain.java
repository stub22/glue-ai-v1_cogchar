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

import java.util.List;
import javax.sound.midi.Instrument;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.bind.midi.FunMidiEventRouter;
import org.cogchar.bind.midi.MidiDevMatchPattern;
import org.cogchar.bind.midi.MidiDevWrap;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class CogcharMidiOutTestMain extends BasicDebugger {
	public static void main(String[] args) {
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);
		
		FunMidiEventRouter fmer = new FunMidiEventRouter();
		try {
			CogcharMidiOutTestMain cmotm = new CogcharMidiOutTestMain();
			cmotm.playSomeNotes();
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			fmer.logInfo("Doing cleanup");
			fmer.cleanup();
		}
		fmer.logInfo("main() is done!");
	}	
	
	public void playSomeNotes() throws Throwable { 
		MidiDevMatchPattern devPattern = new MidiDevMatchPattern();
		
		
		List<MidiDevWrap> devs = MidiDevWrap.findMatchingDevs(devPattern, getLogger());
		
		for (MidiDevWrap dev : devs) {
			if (dev instanceof Synthesizer) {
				getLogger().info("Found synthesizer {} of class {}", dev, dev.getClass());
			}
			if (dev instanceof Sequencer) {
				getLogger().info("Found sequencer {} of class {}", dev, dev.getClass());
			}
			
			
		}
		// getSequencer()
		// Obtains the default Sequencer, connected to a default device.
/*
 * public static Sequencer getSequencer(boolean connected) throws MidiUnavailableException
* Obtains the default Sequencer, optionally connected to a default device.
* If connected is true, the returned Sequencer instance is connected to the default Synthesizer, as returned by 
* MidiSystem.getSynthesizer. If there is no Synthesizer available, or the default Synthesizer cannot be opened, the 
* sequencer is connected to the default Receiver, as returned by MidiSystem.getReceiver. The connection is made by 
* retrieving a Transmitter instance from the Sequencer and setting its Receiver. Closing and re-opening the sequencer 
* will restore the connection to the default device.
* If connected is false, the returned Sequencer instance is not connected, it has no open Transmitters. 
* In order to play the sequencer on a MIDI device, or a Synthesizer, it is necessary to get a Transmitter and set its 
* Receiver.  If the system property javax.sound.midi.Sequencer is defined or it is defined in the file 
* "sound.properties", it is used to identify the default sequencer. For details, refer to the class description.
*/		
		boolean doConnectSeqToSynth = true;
		Sequencer dseq = MidiSystem.getSequencer(doConnectSeqToSynth);
		getLogger().info("System default sequencer is {}, of class {}", dseq, (dseq != null) ? dseq.getClass() : "NULL");
		
		Synthesizer dsynth = MidiSystem.getSynthesizer();
		getLogger().info("System default synthesizer is {}, of class {}", dsynth, (dsynth != null) ? dsynth.getClass() : "NULL");
		if (dsynth != null) {
			Soundbank	dsdbk = dsynth.getDefaultSoundbank();
			getLogger().info("Default synth has default soundbank {}, of class {}", dsdbk, (dsdbk != null) ? dsdbk.getClass() : "NULL");
			Instrument[] loadedInstrms = dsynth.getLoadedInstruments();
			Instrument[] availInstrms = dsynth.getLoadedInstruments();
			getLogger().info("Instrument counts: loaded={}, avail={}", loadedInstrms.length, availInstrms.length);

		}
		
	}
	

}
