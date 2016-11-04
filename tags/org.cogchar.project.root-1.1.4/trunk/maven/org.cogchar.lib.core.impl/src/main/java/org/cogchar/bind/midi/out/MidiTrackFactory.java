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

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class MidiTrackFactory {
	public static MidiEvent createNoteOnEvent(int nKey, long lTick) {
		return MidiEventFactory.createNoteOnEvent(nKey, lTick);
	}
	public static MidiEvent createNoteOffEvent(int nKey, long lTick) {
		return MidiEventFactory.createNoteOffEvent(nKey, lTick);
	}
	public static Sequence makeSequenceOfChords() throws Throwable {
		Sequence sequence = new Sequence(Sequence.PPQ, 1);
		sequence.createTrack();
		Track track = sequence.createTrack();

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

		// fourth chord: G major 7
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
}
