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

package org.cogchar.api.convoid.cue;


import org.cogchar.zzz.platform.stub.CueStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author matt
 */
public class HeardCue extends CueStub {
	private static Logger theLogger = LoggerFactory.getLogger(HeardCue.class.getName());
	private String		myHeardText;

	public HeardCue(String text){
		myHeardText = text;
	}

	public String getHeardText(){
		return myHeardText;
	}

	@Override public String getContentSummaryString(){
		return "Heard: " + myHeardText;
	}
}
