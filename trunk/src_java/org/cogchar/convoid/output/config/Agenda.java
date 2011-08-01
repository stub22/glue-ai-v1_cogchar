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

package org.cogchar.convoid.output.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.cogchar.platform.stub.CueStub;

/**
 *
 * @author Matt Stevenson
 */
public class Agenda extends CueStub {
    private String  myName;
    private Long    myResumeAgendaTime;
    private Long    myCursorTimeoutLength;
    private Long    myAdvanceAgendaTime;
    private Long    myAdvancePhaseTime;
    
    /* We keep the meanings in reverse order
     * We do this so we can grab and remove the next meaning we want from the
     * back, and avoid shifting everything forward. */
    private List<String>    myMeanings;
    private List<String>    myRemainingMeanings;
    private List<String>    myVisitedMeanings;
    private Integer         myCurrentIndex;
    private String          myNextPhase;

    public String getName() {
        return myName;
    }

    public void setName(String myName) {
        this.myName = myName;
    }

    public void setNextPhase(String name, Long time){
        myNextPhase = name;
        myAdvancePhaseTime = time;
    }

    public String getNextPhase(){
        return myNextPhase;
    }

    public Long getAdvancePhaseTime(){
        return myAdvancePhaseTime;
    }

    public Long getAdvanceAgendaTime() {
        return myAdvanceAgendaTime;
    }

    public void setAdvanceAgendaTime(Long myAdvanceAgendaTime) {
        this.myAdvanceAgendaTime = myAdvanceAgendaTime;
    }

    public Integer getCurrentIndex() {
        return myCurrentIndex;
    }

    public void setCurrentIndex(Integer myCurrentIndex) {
        this.myCurrentIndex = myCurrentIndex;
    }

    public Long getCursorTimeoutLength() {
        return myCursorTimeoutLength;
    }

    public void setCursorTimeoutLength(Long myCursorTimeoutLength) {
        this.myCursorTimeoutLength = myCursorTimeoutLength;
    }

    public List<String> getMeanings() {
        return myMeanings;
    }

    public void setMeanings(List<String> myMeanings) {
        if(myMeanings == null){
            myMeanings = new ArrayList();
        }
        Collections.reverse(myMeanings);
        this.myMeanings = myMeanings;
        myRemainingMeanings = new ArrayList<String>(myMeanings);
        myVisitedMeanings = new ArrayList<String>();
    }

    public List<String> getRemainingMeanings() {
        return myRemainingMeanings;
    }

    public Long getResumeAgendaTime() {
        return myResumeAgendaTime;
    }

    public void setResumeAgendaTime(Long myResumeAgendaTime) {
        this.myResumeAgendaTime = myResumeAgendaTime;
    }

    public List<String> getVisitedMeanings() {
        return myVisitedMeanings;
    }

    public void addMeaning(String m){
        myMeanings.add(0,m);
    }

    public boolean isEmpty(){
        return myRemainingMeanings.size() == 0;
    }

    public String getNextMeaning(){
        int last = myRemainingMeanings.size()-1;
        if(last < 0){
            return null;
        }
        String m = myRemainingMeanings.remove(last);
        myVisitedMeanings.add(m);
        return m;
    }

    public void markVisited(String meaning){
        myRemainingMeanings.remove(meaning);
        myVisitedMeanings.add(meaning);
    }

    @Override
    public String toString(){
        return "Agenda[" + getContentSummaryString() + "]";
    }

    @Override
    public String getContentSummaryString() {
        String desc = "name=" + myName + ", meanings=[";
        for(String m : myMeanings){
            desc += m + ", ";
        }
        desc = desc.substring(0,desc.length()-2);
        desc += "], timers=[" + myResumeAgendaTime + ", " + myCursorTimeoutLength +
                ", " + myAdvanceAgendaTime + "]";
        return desc;
    }

}
