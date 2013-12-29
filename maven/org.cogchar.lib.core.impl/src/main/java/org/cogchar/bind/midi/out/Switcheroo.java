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

import org.appdapter.core.log.BasicDebugger;
import org.cogchar.bind.midi.in.InterestingMidiEvent;
import org.cogchar.bind.midi.in.MidiEventReporter;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class Switcheroo extends BasicDebugger implements MidiEventReporter.Listener {
	public	DemoMidiOutputPlayer		myDMOP;
	public	NovLpadTest					myNLT;
	
	@Override public void reportEvent(InterestingMidiEvent ime) {
		try {

			if (ime instanceof InterestingMidiEvent.NoteOn) {
				InterestingMidiEvent.NoteOn noteOn = (InterestingMidiEvent.NoteOn) ime;
				getLogger().info("Got noteOn: {}", noteOn);
				ActionableNoteOn ano = new ActionableNoteOn(noteOn);
				if (ano.isLikelyLaunpadDirect()) {
					LaunchpadCell cell = ano.getLaunchpadCell();
					getLogger().info("Launching for Cell:  {}", cell);
					if (cell.myRowBase0 == 7) {
						int songIndex = cell.myColBase0;
						getLogger().info("Bottom row button, launching demo seq # {}", songIndex);
						if (myDMOP != null) {
							myDMOP.startPlayingDemoSeq(songIndex);
						}						
					}
					if (cell.myRowBase0 == 6) {
						int colorStyleIndex = cell.myColBase0;
						getLogger().info("Row[b0] 6 button, wiping colors to style # {}", colorStyleIndex);					
						if (myNLT != null) {
							myNLT.wipeAllCellsToColor(NovLpadTest.theDramaticColors[colorStyleIndex]);
						}
					}
				}
			}
			if (ime instanceof InterestingMidiEvent.NoteOff) {
				InterestingMidiEvent.NoteOff noteOff = (InterestingMidiEvent.NoteOff) ime;
				getLogger().info("Got noteOff: {}", noteOff);
			}			
			if (ime instanceof InterestingMidiEvent.ControlChange) {
				InterestingMidiEvent.ControlChange cchg = (InterestingMidiEvent.ControlChange) ime;
				double mult = cchg.myValue / 32.0f;
				getLogger().info("Setting multA to {} in response to: {}", mult, cchg);

			}

		} catch (Throwable t) {
			getLogger().error("Error during midi-mapping", t);
		}
	}
	public class ActionableEvent {
		private InterestingMidiEvent	myEvent;
		public ActionableEvent(InterestingMidiEvent event) {
			myEvent = event;
		}
		public int getChannel() { 
			return myEvent.myChannel;
		}
	}
	public class ActionableNoteOn extends ActionableEvent {
		private InterestingMidiEvent.NoteOn		myNoteOn;
		public ActionableNoteOn(InterestingMidiEvent.NoteOn  noteOn) {
			super(noteOn);
			myNoteOn = noteOn;
		}
		public boolean isLikelyLaunpadDirect() { 
			return (getChannel() == 1) && (myNoteOn.myVelocity == 127);
		}
		public LaunchpadCell getLaunchpadCell() { 
			return new LaunchpadCell(myNoteOn.myNote);
		}
	}
	public class LaunchpadCell {
		int	myRowBase0;
		int	myColBase0;
		public LaunchpadCell(int inputNoteNum) {
			myRowBase0 = inputNoteNum / 16;
			myColBase0 = inputNoteNum % 16;
		}
		public String toString(){ 
			return "LaunchpadCell[" + myRowBase0 + ", " + myColBase0 + "]";
		}
	}
	
}
