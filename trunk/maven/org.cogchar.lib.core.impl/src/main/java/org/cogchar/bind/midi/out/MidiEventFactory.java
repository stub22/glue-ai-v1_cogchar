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

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class MidiEventFactory {

	private static final int VELOCITY = 64;

	public static MidiEvent createNoteOnEvent(int nKey, long lTick) {
		return createNoteEvent(ShortMessage.NOTE_ON,
			nKey,
			VELOCITY,
			lTick);
	}

	public static MidiEvent createNoteOffEvent(int nKey, long lTick) {
		return createNoteEvent(ShortMessage.NOTE_OFF,
			nKey,
			0,
			lTick);
	}

	private static MidiEvent createNoteEvent(int nCommand,
		int nKey,
		int nVelocity,
		long lTick) {
		ShortMessage message = new ShortMessage();
		try {
			message.setMessage(nCommand,
				0, // always on channel 1
				nKey,
				nVelocity);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
			return null;
		}
		MidiEvent event = new MidiEvent(message,
			lTick);
		return event;
	}

}
