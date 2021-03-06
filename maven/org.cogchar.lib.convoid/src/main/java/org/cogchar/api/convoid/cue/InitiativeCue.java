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

import org.cogchar.zzz.api.platform.cues.NamedCue;

/**
 *
 * @author Matt Stevenson
 */
public class InitiativeCue extends NamedCue {
    private String    myBehaviorType;
    public InitiativeCue(String name, String type){
        super(name);
        myBehaviorType = type;
    }

    public String getType(){
        return myBehaviorType;
    }

    @Override
	public String toString() {
		return "InitiativeCue[" + getContentSummaryString() + "," + getStatString() + "]";
	}

    @Override
	public String getContentSummaryString() {
        return "BehaviorType=" + getType();
	}
}
