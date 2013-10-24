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
import javax.sound.midi.Patch;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.VoiceStatus;
import javax.sound.midi.MidiChannel;

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
			getLogger().info("Synth latency={}, polyphony={}", dsynth.getLatency(), dsynth.getMaxPolyphony());
			Soundbank	dsdbk = dsynth.getDefaultSoundbank();
			getLogger().info("Default synth has default soundbank {}, of class {}", dsdbk, (dsdbk != null) ? dsdbk.getClass() : "NULL");
			
			// Until we do this, all the instruments are still *AVAIL*, but not LOADED.
			dsynth.open();
			
			
			Instrument[] loadedInstrms = dsynth.getLoadedInstruments();
			Instrument[] availInstrms = dsynth.getAvailableInstruments();
			getLogger().info("Instrument counts: loaded={}, avail={}", loadedInstrms.length, availInstrms.length);
			getLogger().info("Loaded Instruments: {}",  loadedInstrms);
			getLogger().info("Avail Instruments: {}",  availInstrms);
			StringBuffer all = new StringBuffer();
			for (Instrument inst : availInstrms) {
				Patch instPath = inst.getPatch();
				all.append("[" + inst.toString() + "], ");
			}
			getLogger().info("All avail instruments: {}", all.toString());
			
			VoiceStatus[] synthVoiceStatusArr = dsynth.getVoiceStatus();
			getLogger().info("VoiceStatus array size={}, firstEntry={}", synthVoiceStatusArr.length, synthVoiceStatusArr[0]);
			
			// TODO:  Set the programs of the channels
			
			MidiChannel[] midChans = dsynth.getChannels();
			for(int cidx = 0; cidx < midChans.length; cidx++) {
				getLogger().info("Channel at position {} is {} ", cidx, midChans[cidx]);
			}
			
			
			// THEN this will work:
			// Play the note Middle C (60) moderately loud
			// (velocity = 93)on channel 4 (zero-based).
			ShortMessage noteOnMsg = new ShortMessage();
			noteOnMsg.setMessage(ShortMessage.NOTE_ON, 4, 60, 93); 
			
			Receiver synthRcvr = dsynth.getReceiver();
			synthRcvr.send(noteOnMsg, -1); // -1 means no time stamp
			
		//	channel.noteOn(nNoteNumber, nVelocity);
		//	channel.noteOff(nNoteNumber);
			
			
			Thread.sleep(5000);
			dsynth.close();
		}
		
	}
	

}

/*
 * 			nChannelNumber = Math.min(15, Math.max(0, nChannelNumber));
			nNoteNumberArgIndex = 1;
			// FALL THROUGH

		case 3:
			nNoteNumber = Integer.parseInt(args[nNoteNumberArgIndex]);
			nNoteNumber = Math.min(127, Math.max(0, nNoteNumber));
			nVelocity = Integer.parseInt(args[nNoteNumberArgIndex + 1]);
			nVelocity = Math.min(127, Math.max(0, nVelocity));
			nDuration = Integer.parseInt(args[nNoteNumberArgIndex + 2]);
			nDuration = Math.max(0, nDuration);
 * 
 * 
 * 
 * 	<formalpara><title>Bugs, limitations</title>
	<para>The precision of the duration depends on the precision
	of <function>Thread.sleep()</function>, which in turn depends on
	the precision of the system time and the latency of th
	thread scheduling of the Java VM. For many VMs, this
	means about 20 ms. When playing multiple notes, it is
	recommended to use a <classname>Sequence</classname> and the
	<classname>Sequencer</classname>, which is supposed to give better
	timing.</para>
	</formalpara>
	* 
	* 
 */
