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

import javax.sound.midi.Receiver;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class InterestingMidiEvent {

	public	Receiver	myReceiver;
	public	int			myChannel;
	
	protected InterestingMidiEvent(Receiver rcvr, int channel) {
		myReceiver = rcvr;
		myChannel = channel;		
	}
	
	public static class ControlChange extends InterestingMidiEvent {
		public int	myController;
		public int	myValue;
		
		public ControlChange(Receiver rcvr, int channel, int controller, int value) {
			super(rcvr, channel);
			myController = controller;
			myValue = value;
		}
		public String toString() {
			return "ControlChange[rcvr=" + myReceiver + ", chan=" + myChannel + ", ctrl=" + myController + ", val=" + myValue + "]";
		}
	}
	public static abstract class Note extends InterestingMidiEvent {
		public int myNote;
		public int myVelocity;
		public Note(Receiver rcvr, int channel, int note, int vel) {
			super(rcvr, channel);
			myNote = note;
			myVelocity = vel;
		}
	}
	public static class NoteOn  extends Note  {
		public NoteOn(Receiver rcvr, int channel, int note, int vel) {
			super(rcvr, channel, note, vel);
		}
		public String toString() {
			return "NoteOn[rcvr=" + myReceiver + ", chan=" + myChannel + ", note=" + myNote + ", vel=" + myVelocity + "]";
		}		
	}
	public static class NoteOff  extends Note {
		public NoteOff(Receiver rcvr, int channel, int note, int vel) {
			super(rcvr, channel, note, vel);
		}
		public String toString() {
			return "NoteOff[rcvr=" + myReceiver + ", chan=" + myChannel + ", note=" + myNote + ", vel=" + myVelocity + "]";
		}		
	}
}
