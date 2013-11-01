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
import javax.sound.midi.Transmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class MidiTransmitDevWrap extends MidiDevWrap {
	static Logger theLogger = LoggerFactory.getLogger(MidiTransmitDevWrap.class);

	Transmitter myTransmitter;

	public MidiTransmitDevWrap(Transmitter tmit, MidiDevWrap devWrap) {
		super(devWrap);
		myTransmitter = tmit;
	}
	@Override 	public void ensureDevClosed() { 
		if (myTransmitter != null) {
			myTransmitter.close();
		}
		super.ensureDevClosed();
	}
	public static List<MidiTransmitDevWrap> findMatchingTransmitters(List<MidiDevWrap> devs, 
				MidiDevMatchPattern pattern, Logger logger) {
		List<MidiTransmitDevWrap> results = new ArrayList<MidiTransmitDevWrap>();
		for (MidiDevWrap mdw : devs) {
			int maxTmit = mdw.myDevice.getMaxTransmitters();
			int maxRecv = mdw.myDevice.getMaxReceivers();
			theLogger.info("mdw {} reported maxTransmitters={}, maxReceivers={}", mdw, maxTmit, maxRecv);
			// -1 == unknown/unlimited?
			// 0 == definitely none
			if (maxTmit != 0) {
				try {

					// Is it important to ensureDevOpen() before doing this fetch?
					Transmitter tmit = mdw.myDevice.getTransmitter();
					if (tmit != null) {
						MidiTransmitDevWrap tdw = new MidiTransmitDevWrap(tmit, mdw);
						results.add(tdw);
						logger.info("Made transmit-devWrap OK: {}", tdw);
					} else {
						logger.info("No tranmitter found for {}", mdw);
					}
				} catch (Throwable t) {
					logger.warn("Problem looking up transmitter for: {}", mdw, t);
				}
			}
		}
		return results;
	}
}
