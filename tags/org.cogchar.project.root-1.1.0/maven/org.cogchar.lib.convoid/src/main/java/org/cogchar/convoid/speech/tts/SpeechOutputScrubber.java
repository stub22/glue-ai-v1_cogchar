/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.convoid.speech.tts;

import java.util.logging.Logger;

/**
 *
 * @author Stu Baurmann
 */
public class SpeechOutputScrubber {
	private static Logger	theLogger = Logger.getLogger(SpeechOutputScrubber.class.getName());
	// TODO: This functionality should be in Convoid, not RoboNativeWrap.
	// Also, the scrubbing should be configurable.
	public static String scrubText(String inputText) {
		String scrubbedText = inputText;
		theLogger.info("Before scrubbing, output speech text is: " + inputText);
		// scrubbedText = inputText.replaceAll("")
		theLogger.info("After scrubbing, output speech text is: " + scrubbedText);
		return scrubbedText;
	}
	public static void scrubTest(String inputText) {
		String scrubbed = scrubText(inputText);
	}
	public static void main(String args[]) {
		scrubTest("Hey ! What's up with _ NASA these days ?  Are they OK?");
	}
}
