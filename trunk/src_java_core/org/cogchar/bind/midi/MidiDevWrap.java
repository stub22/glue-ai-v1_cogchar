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
package org.cogchar.bind.midi;

import java.util.ArrayList;
import java.util.List;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import org.appdapter.core.log.BasicDebugger;
import org.slf4j.Logger;

/**
 * @author Stu B. <www.texpedient.com>
 */
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

	public boolean ensureDevOpen() {
		try {
			if (myDevice != null) {
				if (!(myDevice.isOpen())) {
					myDevice.open();
				}
				return true;
			} else {
				return false;
			}
		} catch (Throwable t) {
			getLogger().error("Problem opening device for: {}", this, t);
			return false;
		}
	}

	public void ensureDevClosed() {
		try {
			if (myDevice != null) {
				myDevice.close();
			}
		} catch (Throwable t) {
			getLogger().error("Problem opening device for: {}", this, t);
		}
	}
	
	public static List<MidiDevWrap> findMatchingDevs(MidiDevMatchPattern pattern, Logger logger) throws Throwable {
		// Match happens in 2 stages - 1 = uses only dev-info, 2 = uses dev-info + sub-comps
		List<MidiDevWrap> results = new ArrayList<MidiDevWrap>();
		if (pattern == null) {
			pattern = new MidiDevMatchPattern();
		}
		MidiDevice.Info devInfoArr[] = MidiSystem.getMidiDeviceInfo();
		if (devInfoArr == null) {
			return null;
		}
		logger.info("Candidate DeviceInfo array has length: " + devInfoArr.length);
		for (int i = 0; i < devInfoArr.length; i++) {
			logger.info("-----------------------------");
			MidiDevice.Info devInfo = devInfoArr[i];
			boolean stageOneMatch = pattern.matchInfo(devInfo);
			if (stageOneMatch) {
				logger.info("Finding actual MIDI device for: {}", devInfo);
				try {
					MidiDevice device = MidiSystem.getMidiDevice(devInfo);
					boolean stageTwoMatch = pattern.matchDevice(device, devInfo);
					if (stageTwoMatch) {
						MidiDevWrap mdw = new MidiDevWrap(devInfo, device);
						results.add(mdw);
					} else {
						logger.debug("Device does not match pattern, discarding");
					}
				} catch (Throwable t) {
					logger.warn("Problem looking up device for: {}", devInfo, t);
				}
			} else {
				logger.debug("Dev-Info does not match pattern, discarding");
			}
		}
		return results;
	}	
}
