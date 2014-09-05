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

import java.util.List;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.bind.midi.general.MidiDevMatchPattern;
import org.cogchar.bind.midi.general.MidiDevWrap;
import org.cogchar.bind.midi.general.MidiReceiverDevWrap;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * Here we test our ability to connect to a Novation Launchpad (tm) device, and control its LED light output.
 */

public class NovLpadTest extends BasicDebugger {
	MidiReceiverDevWrap		myLpadDevWrap;
	
	public static void main(String[] args) {

		NovLpadTest nlt  = new NovLpadTest();
		try {
			nlt.startLightDemo();
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			nlt.logInfo("Doing cleanup");
			nlt.cleanup();
		}		
	}
	public boolean startLightDemo()  {
		try {
			myLpadDevWrap = findLaunchpadOutRcvr();
			if (myLpadDevWrap != null) {
				myLpadDevWrap.ensureDevOpen();
				playLightStates();
				return true;
			} else {
				getLogger().warn("Cannot find/open launchpad for light-output");
			}
		} catch (Throwable t) {
			getLogger().error("Caught: ", t);
		}
		return false;
		
	}
	public void cleanup() { 
		if (myLpadDevWrap != null) {
			myLpadDevWrap.ensureDevClosed();
			myLpadDevWrap = null;
		}
	}
	public MidiReceiverDevWrap findLaunchpadOutRcvr() throws Throwable {
		MidiDevMatchPattern devPattern = new MidiDevMatchPattern();
		List<MidiDevWrap> devWrapsAll = MidiDevWrap.findMatchingDevs(devPattern, getLogger());
		MidiReceiverDevWrap lpadDevWrap = null;
		for (MidiDevWrap devWrapCand : devWrapsAll) {
			MidiDevice devCand = devWrapCand.myDevice;
			if (devWrapCand.myDevInfo.getName().toLowerCase().contains("launchpad")) {
				getLogger().info("Found launchpad dev: {}, opening it", devWrapCand);
				// devWrapCand.ensureDevOpen();
				// MaxReceivers == -1 -> unknown, unlimited, or what? 
				if (devCand.getMaxReceivers() != 0) {
					Receiver lpadRecvrCand = devCand.getReceiver();
					if (lpadRecvrCand != null) {
						getLogger().info("%%%%%%%%%%%%%%%%%% Found launchpad dev receiver: {}", lpadRecvrCand);
						lpadDevWrap = new MidiReceiverDevWrap(lpadRecvrCand, devWrapCand);
						break;
					}
				} else {
					getLogger().info("Device contains no receivers - should I close it? {}", devCand);	
				}
			}
		}
		return lpadDevWrap;
	}
	static int theDramaticColors[] = {12, 13, 15, 29, 63, 62, 28, 60};

	
	public void playLightStates() throws Throwable {
		int brightYellow = 58;
		wipeAllCellsToColor(brightYellow);
	}
	public void wipeAllCellsToColor(int colorState) throws Throwable {
		for (int rowIdx = 0; rowIdx < 8; rowIdx++) {
			for (int colIdx = 0; colIdx < 9; colIdx++) {
				int cellNum = rowIdx * 16 + colIdx;
				writeLaunchpadLightState(cellNum, colorState);
			}
		}
	}	
	public void writeLaunchpadLightState(int lightNum, int lightState) throws Throwable {
		
		ShortMessage noteOnMsg = new ShortMessage();
		// Novation LPad listens on channel "1" = zero-offset 0.
		noteOnMsg.setMessage(ShortMessage.NOTE_ON, 0, lightNum, lightState);
		myLpadDevWrap.myReceiver.send(noteOnMsg, -1);
	}
	//  Invalid column numbers (9 to 15) are also interpreted as column 8. 
}
/*
 * From Launchpad Programmer's Reference:
 * 
 * The following tables of pre-calculated velocity values for normal use may also be helpful: 
        Hex   Decimal   Colour     Brightness 
        0Ch   12        Off        Off 
        0Dh   13        Red        Low 
        0Fh   15        Red        Full 
        1Dh   29        Amber      Low 
        3Fh   63        Amber      Full 
        3Eh   62        Yellow     Full 
        1Ch   28        Green      Low 
        3Ch   60        Green      Full 
 
Values for flashing LEDs are: 
        Hex   Decimal   Colour     Brightness 
        0Bh   11        Red        Full 
        3Bh   59        Amber      Full 
        3Ah   58        Yellow     Full 
        38h   56        Green      Full 
 */