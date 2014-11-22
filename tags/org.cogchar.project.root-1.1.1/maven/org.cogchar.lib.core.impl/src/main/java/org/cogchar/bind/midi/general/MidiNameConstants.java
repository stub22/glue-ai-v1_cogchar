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

package org.cogchar.bind.midi.general;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class MidiNameConstants {
	public static final String[]		sm_astrKeyNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

	public static final String[]		sm_astrKeySignatures = {"Cb", "Gb", "Db", "Ab", "Eb", "Bb", "F", "C", "G", "D", "A", "E", "B", "F#", "C#"};
	public static final String[]		SYSTEM_MESSAGE_TEXT =
	{
		"System Exclusive (should not be in ShortMessage!)",
		"MTC Quarter Frame: ",
		"Song Position: ",
		"Song Select: ",
		"Undefined",
		"Undefined",
		"Tune Request",
		"End of SysEx (should not be in ShortMessage!)",
		"Timing clock",
		"Undefined",
		"Start",
		"Continue",
		"Stop",
		"Undefined",
		"Active Sensing",
		"System Reset"
	};

	public static final String[]		QUARTER_FRAME_MESSAGE_TEXT =
	{
		"frame count LS: ",
		"frame count MS: ",
		"seconds count LS: ",
		"seconds count MS: ",
		"minutes count LS: ",
		"minutes count MS: ",
		"hours count LS: ",
		"hours count MS: "
	};

	public static final String[]		FRAME_TYPE_TEXT =
	{
		"24 frames/second",
		"25 frames/second",
		"30 frames/second (drop)",
		"30 frames/second (non-drop)",
	};
}
