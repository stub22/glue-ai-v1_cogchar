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

	List<TransmitDevWrap> myTransmitters;
	private MidiDevice myDevice = null;
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
			DevMatchPattern devPattern = new DevMatchPattern();
			List<MidiDevWrap> devs = findMatchingDevs(devPattern);
			List<TransmitDevWrap> tmits = findMatchingTransmitters(devs, devPattern);
			getLogger().info("*********************************\nGot {} devs and {} transmitters", devs.size(), tmits.size());
			myTransmitters = tmits;
		} catch (Throwable t) {
			getLogger().error("Caught: ", t);
		}
	}

	public void startPumpingMidiEvents() {
		findTransmitters();
		for (TransmitDevWrap tdw : myTransmitters) {
			tdw.myTransmitter.setReceiver(myReceiver);
			tdw.ensureDevOpen();			
		}
	}

	public void cleanup() {
		try {
			if (myDevice != null) {
				myDevice.close();
			}
		} catch (Throwable t) {
			getLogger().error("Caught: ", t);


		}
	}

	public static class FunListener extends BasicDebugger implements MidiEventReporter.Listener {

		@Override public void reportEvent(InterestingMidiEvent ime) {
			getLogger().info("******************* Received interesting midi event: {} ", ime);
		}
	}

	static class OurMidiReceiver extends BasicDebugger implements Receiver {

		@Override public void send(MidiMessage message, long timeStamp) {
			getLogger().info("Received at " + timeStamp + ": " + message);
		}

		@Override public void close() {
			getLogger().info("Closing");
		}
	}
// Match happens in 2 stages - 1 = uses only dev-info, 2 = uses dev-info + sub-comps

	public class DevMatchPattern {

		public boolean matchInfo(MidiDevice.Info infoCand) {
			String name = infoCand.getName();
			return true;
		}

		public boolean matchDevice(MidiDevice devCand, MidiDevice.Info infoCand) {
			return true;
		}

		public boolean matchTransmitter(Transmitter tmit, MidiDevice devCand, MidiDevice.Info infoCand) {
			return true;
		}
	}

	public class MidiDevWrap extends BasicDebugger {

		MidiDevice.Info myDevInfo;
		MidiDevice myDevice;

		public String toString() {
			return "DEVICE[" + myDevice + "], vendor[" + myDevInfo.getVendor() + "], name[" + myDevInfo.getName()
				+ ", description[" + myDevInfo.getDescription() + "]";
		}

		public MidiDevWrap(MidiDevice.Info info, MidiDevice dev) {
			myDevInfo = info;
			myDevice = dev;
		}

		public MidiDevWrap(MidiDevWrap other) {
			this(other.myDevInfo, other.myDevice);
		}

		public void ensureDevOpen() {
			try {
				if (!(myDevice.isOpen())) {
					myDevice.open();
				}
			} catch (Throwable t) {
				getLogger().error("Problem opening device for: {}", this, t);				
			}
		}
	}

	public class TransmitDevWrap extends MidiDevWrap {

		Transmitter myTransmitter;

		public TransmitDevWrap(Transmitter tmit, MidiDevWrap devWrap) {
			super(devWrap);
			myTransmitter = tmit;
		}
	}

	public List<MidiDevWrap> findMatchingDevs(DevMatchPattern pattern) throws Throwable {
		List<MidiDevWrap> results = new ArrayList<MidiDevWrap>();
		if (pattern == null) {
			pattern = new DevMatchPattern();
		}
		MidiDevice.Info devInfoArr[] = MidiSystem.getMidiDeviceInfo();
		if (devInfoArr == null) {
			return null;
		}
		getLogger().info("Candidate DeviceInfo array has length: " + devInfoArr.length);
		for (int i = 0; i < devInfoArr.length; i++) {
			getLogger().info("-----------------------------");
			MidiDevice.Info devInfo = devInfoArr[i];
			boolean stageOneMatch = pattern.matchInfo(devInfo);
			if (stageOneMatch) {
				getLogger().info("Finding actual MIDI device for: {}", devInfo);
				try {
					MidiDevice device = MidiSystem.getMidiDevice(devInfo);
					boolean stageTwoMatch = pattern.matchDevice(device, devInfo);
					if (stageTwoMatch) {
						MidiDevWrap mdw = new MidiDevWrap(devInfo, device);
						results.add(mdw);
					} else {
						getLogger().debug("Device does not match pattern, discarding");
					}
				} catch (Throwable t) {
					getLogger().warn("Problem looking up device for: {}", devInfo, t);
				}
			} else {
				getLogger().debug("Dev-Info does not match pattern, discarding");
			}
		}
		return results;
	}

	public List<TransmitDevWrap> findMatchingTransmitters(List<MidiDevWrap> devs, DevMatchPattern pattern) {
		List<TransmitDevWrap> results = new ArrayList<TransmitDevWrap>();
		for (MidiDevWrap mdw : devs) {
			try {
				// Is it important to ensureDevOpen() before doing this fetch?
				Transmitter tmit = mdw.myDevice.getTransmitter();
				if (tmit != null) {
					TransmitDevWrap tdw = new TransmitDevWrap(tmit, mdw);
					results.add(tdw);
				}
			} catch (Throwable t) {
				getLogger().warn("Problem looking up transmitter for: {}", mdw, t);
			}
		}
		return results;
	}

/*
	public Transmitter fetchTransmitter(MidiDevice.Info info) {
		Transmitter tmit = null;
		try {
			MidiDevice device = MidiSystem.getMidiDevice(info);
			getLogger().info("Found midi device: " + device);
			if (device != null) {
				tmit = device.getTransmitter();
			}
		} catch (Throwable t) {
			getLogger().warn("Cannot fetch transmitter for {}", info);
		}
		return tmit;

	}
*/

}
	/*	
	 Transmitter tmit = fetchTransmitter(devInfo);
	 if (tmit != null) {
	 String name = devInfo.getName();
	 getLogger().info("FOUND TRANSMITTER: " + tmit);
	 boolean
	 MidiDevice device = null;
	 try {
	 device = MidiSystem.getMidiDevice(devInfo);
	 } catch (Throwable t) {
	 getLogger().warn("Problem fetching MidiDevice for name=[" + name + "], tmit=[" + tmit + "]", t);
	 }

	 if (name.equals("USB Audio Device") || name.equals("MPK mini")) {
	 getLogger().info("MATCH\n===============");
	 return devInfo;
	 }
	 }
	 }
		
	 //if (device instanceof Transmitter) {
	 //	System.out.println("Version: " + info.getVersion());
	 //	if (info.getDescription().equals("External MIDI Port")) {
	 //		return info;
	 //	}
	 }
	 return null;
	 }
	 */
		/*
		 MidiDevice.Info inputInfo = fetchInputDeviceInfo();
		 if (inputInfo != null) {
		 getLogger().info("Selected MIDI input device-info: {}", inputInfo);
		 myDevice = MidiSystem.getMidiDevice(inputInfo);
		 if (myDevice != null) {
		 if (!(myDevice.isOpen())) {
		 myDevice.open();
		 }
		 Transmitter transmitter = myDevice.getTransmitter();
		 if (transmitter != null) {
		 transmitter.setReceiver(myReceiver);
		 } else {
		 getLogger().warn("Cannot find transmitter for device: {}", myDevice);
		 }
		 } else {
		 getLogger().warn("Cannot find device for info: {} ", inputInfo);
		 }
		 } else {
		 getLogger().warn("****************** No input midi device selected.");
		 }

		 }
		 catch (Throwable t) {
		 getLogger().error("Caught: ", t);
		 cleanup();
		 }
		 */