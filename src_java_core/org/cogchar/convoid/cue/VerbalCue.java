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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stu B. <www.texpedient.com>
 *
 */
public class VerbalCue extends ConvoidCue {
	private static Logger	theLogger = Logger.getLogger(VerbalCue.class.getName());	
	static {
		theLogger.setLevel(Level.ALL);
	}
	
    private Map<String, Double> myMeaningScoreMap;
	public VerbalCue() {
        myMeaningScoreMap = new HashMap<String, Double>();
	}
	public List<String> getMeaningList() {
		return new ArrayList<String>(myMeaningScoreMap.keySet());
	}
    public Map<String, Double> getMeanings(){
        return myMeaningScoreMap;
    }
    public void setMeanings(Map<String, Double> meanings){
        myMeaningScoreMap = meanings;
		// markContentSummaryUpdate();
    }
    @Override
	public String toString() {
		return "VerbalCue[" + getContentSummaryString() + "," + getStatString() + "]";
	}

    @Override
	public String getContentSummaryString() {
        String meaningSummary = "";
        for(Entry<String, Double> meaning : myMeaningScoreMap.entrySet()){
            meaningSummary += meaning.getKey() + " (" + meaning.getValue() + "), ";
        }
        meaningSummary.trim();
        return meaningSummary;
	}
	public boolean matchesMeaningBlock(String meaningBlock) {
        List<String> meaningList = getMeaningList();
		theLogger.finest("Testing meaningBlock: " + meaningBlock + " against " + meaningList);
		String meanings[] = meaningBlock.split("[\\s]+");
		boolean result = true;
		for (int i=0; i < meanings.length; i++) {
			if (!meaningList.contains(meanings[i])) {
				result = false;
				break;
			} else {
				theLogger.finest("***********meaning #" + i + "-{" + meanings[i] + "}-matches!");
					
			}
		}
		theLogger.finest("returning " + result);
		return result;
	}

    public void removeMeanings(List<String> meanings){
        boolean changed = false;
        for(String m : meanings){
            if(myMeaningScoreMap.containsKey(m)){
                myMeaningScoreMap.remove(m);
                changed = true;
            }
        }
        if(changed){
            // markContentSummaryUpdate();
            markUpdated();
        }
		
    }
}
