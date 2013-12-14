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

package org.cogchar.bind.midi.seq;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import org.appdapter.core.log.BasicDebugger;

/**
 * When we play a MIDI sequence that acts only on one melody (i.e. not-drum)
 * patch (e.g. piano), then it is relatively simple to expose metadata about 
 * musical events on that single melody patch, mainly notes.  
 * Such sequences may be polyphonic; but whether or not they use
 * multiple MIDI channels (e.g. to play 2 "A#" notes at the same time)
 * is not yet accounted for in this class definition - it is treated as
 * a parallel stream "across all channels" (compatible with MIDI "promiscuous"
 * listening mode - but again, all presumed to be on a single patch of
 * melody timber). 
 * @author Stu B. <www.texpedient.com>
 */

public class MonoPatchMelodyPerf extends BasicDebugger {

	private		Sequence		mySequence;
	private		Sequencer		mySequencer;
	
	public MonoPatchMelodyPerf(URL url) throws Throwable  {
		mySequence = MidiSystem.getSequence(url);
	}
	public MonoPatchMelodyPerf(File file) throws Throwable  {
		mySequence = MidiSystem.getSequence(file);
	}
	public MonoPatchMelodyPerf(InputStream istream) throws Throwable {
		mySequence = MidiSystem.getSequence(istream);
	}
	private void connectToSeqr() throws Throwable {
		if (mySequencer != null) {
			getLogger().info("Already connected to  sequencer {}", mySequencer);
		} else {
			boolean doConnectSeqToSynth = true;
			mySequencer = MidiSystem.getSequencer(doConnectSeqToSynth);
			getLogger().info("Connected to sys default sequencer {}, of class {}", mySequencer, 
						(mySequencer != null) ? mySequencer.getClass() : "NULL");
			
		}
	}
	public void startPlaying() {
		try {
			connectToSeqr();
			mySequencer.open();
			mySequencer.setSequence(mySequence);
			mySequencer.start();
		} catch (Throwable t) {
			getLogger().error("MIDI Out problem", t);
		}
	}
	public void close() {
		mySequencer.close();		
	}
}
