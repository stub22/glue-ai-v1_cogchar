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

package org.cogchar.convoid.cue;

import java.util.ArrayList;
import java.util.List;
import org.cogchar.platform.cues.NamedCue;

/**
 *
 * @author Matt Stevenson
 */
public class InhibitionCue extends NamedCue {
    private List<String> myInhibitedMeanings;

    public InhibitionCue(String n){
        super(n);
        myInhibitedMeanings = new ArrayList<String>();
    }

    public InhibitionCue(String n, List<String> meanings){
        super(n);
        myInhibitedMeanings = meanings;
    }

    public void setMeanings(List<String> meanings){
        myInhibitedMeanings = meanings;
    }

    public void addMeanings(List<String> meanings){
        myInhibitedMeanings.addAll(meanings);
    }

    public void addMeaning(String meaning){
        myInhibitedMeanings.add(meaning);
    }

    public List<String> getMeaningList(){
        return myInhibitedMeanings;
    }
}
