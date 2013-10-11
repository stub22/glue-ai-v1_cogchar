/*
 *  Copyright 2013 by The Friendularity Project (www.friendularity.org).
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
package org.cogchar.bind.midi;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Transmitter;
import org.appdapter.core.log.BasicDebugger;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class FunMidiEventRouter extends BasicDebugger {

	List<MidiTransmitDevWrap> myTransmitters = new ArrayList<MidiTransmitDevWrap>();
	private MidiReceiverDumpsAndNotifies myReceiver = null;

	public FunMidiEventRouter() {
		// OutputStream noOut = new NullOutputStream();
		PrintStream noPrint = null; // new PrintStream(noOut);
		myReceiver = new MidiReceiverDumpsAndNotifies(noPrint); // System.out);
	}

	public void registerListener(MidiEventReporter.Listener listener) {
		myReceiver.registerListener(listener);
	}

	private void findTransmitters() {
		try {
			MidiDevMatchPattern devPattern = new MidiDevMatchPattern();
			List<MidiDevWrap> devs = MidiDevWrap.findMatchingDevs(devPattern, getLogger());
			List<MidiTransmitDevWrap> tmits = MidiTransmitDevWrap.findMatchingTransmitters(devs, devPattern, getLogger());
			getLogger().info("*********************************\nFound {} MIDI devs and {} MIDI transmitters", devs.size(), tmits.size());
			myTransmitters = tmits;
		} catch (Throwable t) {
			getLogger().error("Caught: ", t);
		}
	}

	public void startPumpingMidiEvents() {
		findTransmitters();
		for (MidiTransmitDevWrap tdw : myTransmitters) {
			tdw.myTransmitter.setReceiver(myReceiver);
			tdw.ensureDevOpen();			
		}
	}

	public void cleanup() {
		for (MidiTransmitDevWrap tdw : myTransmitters) {
			tdw.ensureDevClosed();
		}
	}

	public static class FunListener extends BasicDebugger implements MidiEventReporter.Listener {

		@Override public void reportEvent(InterestingMidiEvent ime) {
			getLogger().info("******************* Received interesting midi event: {} ", ime);
		}
	}

}
	/*	
	 String name = devInfo.getName();
	 if (name.equals("USB Audio Device") || name.equals("MPK mini")) {
	 //if (device instanceof Transmitter) {
	 //	System.out.println("Version: " + info.getVersion());
	 //	if (info.getDescription().equals("External MIDI Port")) {
	 //		return info;
	 //	}
	
	 */
		