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

package org.cogchar.bind.midi.in;

import java.util.ArrayList;
import java.util.List;
import javax.sound.midi.Receiver;
import org.appdapter.core.log.BasicDebugger;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class MidiEventReporter extends BasicDebugger {
	public static interface Listener {
		public void reportEvent(InterestingMidiEvent ime);
	}

	private List<Listener> myListeners = new ArrayList<Listener>();

	public void registerListener(Listener l) {
		synchronized (myListeners) {
			if (!myListeners.contains(l))
				myListeners.add(l);
		}
	}

	public void unregisterListener(Listener l) {
		synchronized (myListeners) {
			myListeners.remove(l);
		}
	}

	protected void deliverEvent(InterestingMidiEvent ime) {
		Listener[] myListenersArray;
		synchronized (myListeners) {
			myListenersArray = myListeners.toArray(new Listener[myListeners.size()]);
		}
		for (Listener l : myListenersArray) {
			l.reportEvent(ime);
		}
	}

	protected void noticeControlChange(Receiver rcvr, int channel, int controller, int value) {
		InterestingMidiEvent ime = new InterestingMidiEvent.ControlChange(rcvr, channel, controller, value);
		deliverEvent(ime);
	}

	protected void noticeNoteOn(Receiver rcvr, int channel, int note, int vel) {
		InterestingMidiEvent ime;
		if (vel == 0) {
			// Zero-velocity note-on implies note-off.  Sweet!
			// http://www.kvraudio.com/forum/viewtopic.php?t=291892
			ime = new InterestingMidiEvent.NoteOff(rcvr, channel, note, vel);
		} else {
			ime = new InterestingMidiEvent.NoteOn(rcvr, channel, note, vel);
		}
		deliverEvent(ime);
	}

	protected void noticeNoteOff(Receiver rcvr, int channel, int note, int vel) {
		InterestingMidiEvent ime = new InterestingMidiEvent.NoteOff(rcvr, channel, note, vel);
		deliverEvent(ime);
	}
}
