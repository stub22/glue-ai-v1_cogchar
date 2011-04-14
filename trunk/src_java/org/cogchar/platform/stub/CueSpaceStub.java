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

/**
 *
 * @author Stu Baurmann
 */
public interface CueSpaceStub extends ThalamentSpaceStub {



	/**
	 * 
	 * @return
	 */
	//public JobConfig getJobConfig();
	/**
	 * 
	 * @return
	 */
	//public NowCue getSolitaryNowCue();
	
	/**
	 * 
	 * @param c
	 */
	public void clearCue(CueStub c);

	// Used to selectively broadcast cue updates over JMX
	public void broadcastCueUpdate(CueStub c);
	public void clearAllCues();
	

	public void clearAllNowCues();

	public void clearMatchingNamedCues(String name);

	// public void clearMatchingNamedCues(NamedCue nc);

	
//	public void addCueListener(CueListener cl);

//	public void removeCueListener(CueListener cl);

    public void addCue(CueStub c);

	public CueStub addThoughtCueForName(String thoughtName, double strength);


//	public <NCT extends NamedCue> NCT getNamedCue(Class<NCT> clazz, String cueName) ;
}
