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

package org.cogchar.platform.stub;

import java.util.List;
import org.cogchar.platform.cues.NamedCue;
import org.cogchar.platform.cues.NowCue;
import org.cogchar.platform.cues.TextCue;
import org.cogchar.platform.cues.TimerCue;
import org.cogchar.platform.cues.VariableCue;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public interface CueSpaceStub extends ThalamentSpaceStub {




	public JobConfig getJobConfig();

	public NowCue getSolitaryNowCue();
	
	
	public void clearCue(CueStub c);

	// Used to selectively broadcast cue updates over JMX
	public void broadcastCueUpdate(CueStub c);
	public void clearAllCues();
	

	public void clearAllNowCues();

	public void clearMatchingNamedCues(String name);

	public void clearMatchingNamedCues(NamedCue nc);

	
//	public void addCueListener(CueListener cl);

//	public void removeCueListener(CueListener cl);

    public void addCue(CueStub c);

	public CueStub addThoughtCueForName(String thoughtName, double strength);


	public <NCT extends NamedCue> NCT getNamedCue(Class<NCT> clazz, String cueName) ;
	public NowCue addNowCue(Long prevDurationMsec, Long nextDurationMsec, Double strength) ;
	public TextCue addTextCue(String textChannelName, String textData, Double strength);
	public TimerCue addTimerCue(String timerName, Integer durationSec);

	public List<VariableCue> getAllVariableCues();

	public VariableCue setVariableCue(String varName, String varVal, Double strength);
}
