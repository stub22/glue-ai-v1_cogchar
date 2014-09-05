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

package org.cogchar.bind.midi.out;

import org.cogchar.bind.midi.seq.DemoMidiSeq;
import org.cogchar.bind.midi.seq.MonoPatchMelodyPerf;
import org.cogchar.bind.midi.out.NovLpadTest;

import org.appdapter.core.log.BasicDebugger;
/**
 * @author Stu B. <www.texpedient.com>
 */

public class DemoMidiOutputPlayer extends BasicDebugger {
	private MonoPatchMelodyPerf	myCurrentPerf;
	
	public void startPlayingDemoSeq(int index) {
		stopPlaying();
		try {
			DemoMidiSeq.DemoMonoMelody melody = DemoMidiSeq.DemoMonoMelody.values()[index];
			myCurrentPerf = DemoMidiSeq.loadDemoMonoMelody(melody);
			myCurrentPerf.startPlaying();
		} catch (Throwable t) {
			getLogger().error("Problem starting demo seq at index: {}", index);
		}	
	}
	public void stopPlaying() { 
		try {
			if (myCurrentPerf != null) {
				myCurrentPerf.stopPlaying();
				myCurrentPerf.close();
				myCurrentPerf = null;
			}
		} catch (Throwable t) {
			getLogger().error("Problem stopping performance: {}", myCurrentPerf);
		}			
	}
	public void playAllDemoSeqsBriefly_Blocking(int briefMsec) {
		int numDemos = DemoMidiSeq.DemoMonoMelody.values().length;
		for (int idx = 0; idx < numDemos; idx++) {
			try {
				startPlayingDemoSeq(idx);
				Thread.sleep(briefMsec);
			} catch (Throwable t) {
				getLogger().error("Problem in blocking midi demo seq, at index: {}", idx, t);
			}					
		}
	}
	
}
