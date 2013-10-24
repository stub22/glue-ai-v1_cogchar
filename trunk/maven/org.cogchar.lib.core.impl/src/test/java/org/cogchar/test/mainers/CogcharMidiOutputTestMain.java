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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Patch;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.VoiceStatus;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;

import org.appdapter.core.log.BasicDebugger;
import org.cogchar.bind.midi.FunMidiEventRouter;
import org.cogchar.bind.midi.MidiDevMatchPattern;
import org.cogchar.bind.midi.MidiDevWrap;


/**
 * @author Stu B. <www.texpedient.com>
 */
public class CogcharMidiOutputTestMain extends BasicDebugger {

	public static void main(String[] args) {
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);

		FunMidiEventRouter fmer = new FunMidiEventRouter();
		try {
			CogcharMidiOutputTestMain cmotm = new CogcharMidiOutputTestMain();
			
			NovLpadTest nlt = new NovLpadTest();
			nlt.lpadLightDemo();

			cmotm.playSomeNotes();			
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			fmer.logInfo("Doing cleanup");
			fmer.cleanup();
		}
		fmer.logInfo("main() is done!");
	}



	public void findNocturnOutput() throws Throwable {
		MidiDevMatchPattern devPattern = new MidiDevMatchPattern();
	}

	public void playSomeNotes() throws Throwable {
		MidiDevMatchPattern devPattern = new MidiDevMatchPattern();


		List<MidiDevWrap> devs = MidiDevWrap.findMatchingDevs(devPattern, getLogger());

		for (MidiDevWrap devWrap : devs) {
			MidiDevice dev = devWrap.myDevice;
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
			Soundbank dsdbk = dsynth.getDefaultSoundbank();
			getLogger().info("Default synth has default soundbank {}, of class {}", dsdbk, (dsdbk != null) ? dsdbk.getClass() : "NULL");

			// Until we do this, all the instruments are still *AVAIL*, but not LOADED.
			dsynth.open();


			Instrument[] loadedInstrms = dsynth.getLoadedInstruments();
			Instrument[] availInstrms = dsynth.getAvailableInstruments();
			getLogger().info("Instrument counts: loaded={}, avail={}", loadedInstrms.length, availInstrms.length);
			getLogger().info("Loaded Instruments: {}", loadedInstrms);
			getLogger().info("Avail Instruments: {}", availInstrms);
			StringBuffer all = new StringBuffer();
			for (Instrument inst : availInstrms) {
				Patch instPatch = inst.getPatch();
	
				all.append("[" + inst.toString() + "], ");
			}
			getLogger().info("All avail instruments: {}", all.toString());

			VoiceStatus[] synthVoiceStatusArr = dsynth.getVoiceStatus();
			getLogger().info("VoiceStatus array size={}, firstEntry={}", synthVoiceStatusArr.length, synthVoiceStatusArr[0]);

			// TODO:  Set the programs of the channels

			MidiChannel[] midChans = dsynth.getChannels();
			for (int cidx = 0; cidx < midChans.length; cidx++) {
				MidiChannel chan = midChans[cidx];
				getLogger().info("****************************\nChannel at position {} is {} ", cidx, midChans[cidx]);
				if (chan != null) {
					printChanFacts(chan);
				}
			}

			Receiver synthRcvr = dsynth.getReceiver();


			// THEN this will work:
			// Play the note Middle C (60) moderately loud
			// (velocity = 93)on channel 4 (zero-based).
			ShortMessage noteOnMsg = new ShortMessage();
			ShortMessage progChangeMsg = new ShortMessage();
			ShortMessage notesOffMsg = new ShortMessage();
			for (int j=0; j < 25; j++) {
				for (int i = 0; i < 16; i++) {
					int instIdx = (i + 16 * j) % (loadedInstrms.length);
					Instrument tgtInst = loadedInstrms[instIdx];
					Patch tgtPatch = tgtInst.getPatch();
					
					notesOffMsg.setMessage(ShortMessage.NOTE_OFF, i, 59 + i, 0);
					synthRcvr.send(notesOffMsg, -1);
					getLogger().debug("Program change chanFromZ={} to {}", i, tgtInst);
					progChangeMsg.setMessage(ShortMessage.PROGRAM_CHANGE, i, tgtPatch.getProgram(), tgtPatch.getBank());
					synthRcvr.send(progChangeMsg, -1);
					noteOnMsg.setMessage(ShortMessage.NOTE_ON, i, 59 + i, 93);
					// Nice riff!  We hear a drum hit on channel "10" = 9
					synthRcvr.send(noteOnMsg, -1); // -1 means no time stamp
					Thread.sleep(125);
				}
			}

			//	channel.noteOn(nNoteNumber, nVelocity);
			//	channel.noteOff(nNoteNumber);


			Thread.sleep(5000);
			
			dsynth.close();
		}
		if (dseq != null) {
			Sequence seq = makeSequence();
			dseq.open();
			dseq.setSequence(seq);
			dseq.start();				
			Thread.sleep(5000);	
			dseq.close();
		}

	}

	private void printChanFacts(MidiChannel chan) {
		getLogger().info("program={}", chan.getProgram());

		getLogger().info("mono={}", chan.getMono());  // as in mono/poly-phonic
		getLogger().info("solo={}", chan.getSolo());
		getLogger().info("mute={}", chan.getMute());
		getLogger().info("omni={}", chan.getOmni()); // In omni mode, the channel responds to messages sent on all channels. 
		getLogger().info("pitchBend={}", chan.getPitchBend());
		getLogger().info("channelPressure={}", chan.getChannelPressure());

		int noteNum = 25;
		getLogger().info("polyPressure(note={})={}", noteNum, chan.getPolyPressure(noteNum));

		int controllerNum = 20;
		getLogger().info("controllerVal(controlNum={})={}", controllerNum, chan.getController(controllerNum));
	}

	private void callAllChannelMutators(MidiChannel chan) {
		int bendVal = 0, noteNum = 0, pressure = 0, program = 0, bank = 0, controllerNum = 0, ctrlValue = 0;

		chan.allNotesOff();
		chan.allSoundOff();
		chan.resetAllControllers();
		chan.controlChange(controllerNum, ctrlValue);
		chan.localControl(true);
		chan.setChannelPressure(pressure);
		chan.setPolyPressure(noteNum, pressure);
		chan.programChange(program);  // In "currently selected" bank - where does that state live? 
		chan.programChange(bank, program);
		chan.setPitchBend(bendVal);
		boolean soloState = false, muteState = false, omniState = false, monoState = false, localControlState = false;
		chan.setSolo(soloState);
		chan.setMute(muteState);
		chan.setOmni(omniState);
		chan.setMono(monoState);
		chan.localControl(localControlState);

	}
	private Sequence makeSequence() throws Throwable { 
		Sequence sequence = new Sequence(Sequence.PPQ, 1);
		sequence.createTrack();
		Track	track = sequence.createTrack();

		// first chord: C major
		track.add(createNoteOnEvent(60, 0));
		track.add(createNoteOnEvent(64, 0));
		track.add(createNoteOnEvent(67, 0));
		track.add(createNoteOnEvent(72, 0));
		track.add(createNoteOffEvent(60, 1));
		track.add(createNoteOffEvent(64, 1));
		track.add(createNoteOffEvent(67, 1));
		track.add(createNoteOffEvent(72, 1));

		// second chord: f minor N
		track.add(createNoteOnEvent(53, 1));
		track.add(createNoteOnEvent(65, 1));
		track.add(createNoteOnEvent(68, 1));
		track.add(createNoteOnEvent(73, 1));
		track.add(createNoteOffEvent(63, 2));
		track.add(createNoteOffEvent(65, 2));
		track.add(createNoteOffEvent(68, 2));
		track.add(createNoteOffEvent(73, 2));

		// third chord: C major 6-4
		track.add(createNoteOnEvent(55, 2));
		track.add(createNoteOnEvent(64, 2));
		track.add(createNoteOnEvent(67, 2));
		track.add(createNoteOnEvent(72, 2));
		track.add(createNoteOffEvent(64, 3));
		track.add(createNoteOffEvent(72, 3));

		// forth chord: G major 7
		track.add(createNoteOnEvent(65, 3));
		track.add(createNoteOnEvent(71, 3));
		track.add(createNoteOffEvent(55, 4));
		track.add(createNoteOffEvent(65, 4));
		track.add(createNoteOffEvent(67, 4));
		track.add(createNoteOffEvent(71, 4));

		// fifth chord: C major
		track.add(createNoteOnEvent(48, 4));
		track.add(createNoteOnEvent(64, 4));
		track.add(createNoteOnEvent(67, 4));
		track.add(createNoteOnEvent(72, 4));
		track.add(createNoteOffEvent(48, 8));
		track.add(createNoteOffEvent(64, 8));
		track.add(createNoteOffEvent(67, 8));
		track.add(createNoteOffEvent(72, 8));
		
		return sequence;
	}
	private static final int	VELOCITY = 64;
	private static MidiEvent createNoteOnEvent(int nKey, long lTick)
	{
		return createNoteEvent(ShortMessage.NOTE_ON,
							   nKey,
							   VELOCITY,
							   lTick);
	}

	private static MidiEvent createNoteOffEvent(int nKey, long lTick)
	{
		return createNoteEvent(ShortMessage.NOTE_OFF,
							   nKey,
							   0,
							   lTick);
	}
	private static MidiEvent createNoteEvent(int nCommand,
											 int nKey,
											 int nVelocity,
											 long lTick)
	{
		ShortMessage	message = new ShortMessage();
		try
		{
			message.setMessage(nCommand,
							   0,	// always on channel 1
							   nKey,
							   nVelocity);
		}
		catch (InvalidMidiDataException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		MidiEvent	event = new MidiEvent(message,
										  lTick);
		return event;
	}	
	private void callSequencerMethods(Sequencer seqr) throws Throwable {



		Sequence sequence = null;
		String mfName = "filename.mid";
		File midiFile = new File(mfName);
		sequence = MidiSystem.getSequence(midiFile);


		seqr.open();
		seqr.setSequence(sequence);
		seqr.start();
		/*
		 * 	 *	To accomplish this, we register a Listener to the Sequencer.
		 *	It is called when there are "meta" events. Meta event
		 *	47 is end of track.

		 * 
		 */
	}

	private void noticeSeqrTrackEnd(final Sequencer seqr) {
		getLogger().info("MidiPlayer.<...>.meta(): end of track message received, closing sequencer and attached MidiDevices...");
		seqr.close();

	}

	private void listenForSeqrEvents(final Sequencer seqr) throws Throwable {
		seqr.addMetaEventListener(new MetaEventListener() {
			public void meta(MetaMessage message) {

				getLogger().info("%%% MetaMessage: " + message);
				getLogger().info("%%% MetaMessage type: [{}]  length: {}" + message.getType(), message.getLength());
				if (message.getType() == 47) {
					noticeSeqrTrackEnd(seqr);
				}
			}
		});
		int[] allControllersMask = new int[128];
		for (int i = 0; i < allControllersMask.length; i++) {
			allControllersMask[i] = i;
		}
		seqr.addControllerEventListener(
			new ControllerEventListener() {
			public void controlChange(ShortMessage message) {
				getLogger().info("%%% ShortMessage: {}", message);
				getLogger().info("%%% ShortMessage controller={}, value={} ", message.getData1(), message.getData2());
			}
		}, allControllersMask);
	}

	private void closeAllDevsAndExit() {
		/*  Iterator iterator = sm_openedMidiDeviceList.iterator(); while (iterator.hasNext())	{
		 MidiDevice	device = (MidiDevice) iterator.next();
		 device.close();
		 } if (DEBUG) { out("MidiPlayer.<...>.meta(): ...closed, now exiting"); }	 //System.exit(0);	 */
	}

	private void synthMapperCode(Sequencer seqr) throws Throwable {
		ArrayList<MidiDevice> sm_openedMidiDeviceList = new ArrayList<MidiDevice>();

		Transmitter	midiTransmitter = seqr.getTransmitter();
		// Synth-receiver target
		Synthesizer synth = MidiSystem.getSynthesizer();
		synth.open();
		sm_openedMidiDeviceList.add(synth);
		Receiver synthReceiver = synth.getReceiver();
		Transmitter seqTransmitter = seqr.getTransmitter();
		seqTransmitter.setReceiver(synthReceiver);
		
		// OR: Default receiver target:
		Receiver	midiReceiver = MidiSystem.getReceiver();
		midiTransmitter.setReceiver(midiReceiver);
		
		// OR:  specific dev version		
		MidiDevice.Info		info = null; // new MidiDevice.Info(); //  MidiCommon.getMidiDeviceInfo(devName, true);
		MidiDevice	midiDevice = MidiSystem.getMidiDevice(info);
		midiDevice.open();
		sm_openedMidiDeviceList.add(midiDevice);
		Receiver	midiDevReceiver = midiDevice.getReceiver();
		midiTransmitter.setReceiver(midiDevReceiver);
		// Later
		midiDevReceiver.close();
		midiDevice.close();			
		
		// Dumping sequencer output to text, or our intermediate
			//	Receiver	dumpReceiver = new DumpReceiver(System.out);
			//	Transmitter	dumpTransmitter = sm_sequencer.getTransmitter();
			//	dumpTransmitter.setReceiver(dumpReceiver);	
	
	}
}

/*
 void	noteOff(int noteNumber)
 Turns the specified note off.* 
 * void	noteOff(int noteNumber, int velocity)

 void	noteOn(int noteNumber, int velocity)
 Starts the specified note sounding.
 * 
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
