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

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import org.appdapter.core.log.BasicDebugger;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class MidiReceiverOurs extends BasicDebugger implements Receiver {

	@Override public void send(MidiMessage message, long timeStamp) {
		getLogger().info("Received at " + timeStamp + ": " + message);
	}

	@Override public void close() {
		getLogger().info("Closing");
	}
}
