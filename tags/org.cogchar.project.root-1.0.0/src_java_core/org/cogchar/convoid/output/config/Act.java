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

import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.cogchar.platform.util.TimeUtils;

/**
 * @author Stu B. <www.texpedient.com>
 *
 */
public class Act {
	private static Logger theLogger = Logger.getLogger("com.hansonrobotics.convoid.output.config.Act");
	// See note  in the Category class  about why this is "name" and not "myName" 
	private	String				name;
	private	List<Step>			mySteps;
	private String				start;
	private String				next;
	private Long				myLastAdvanceTimeMsec;
	private Integer				myLastStep;
	private List<Integer>		myStepsSaid;
    private boolean             myMeaningsSet = false;

	@XStreamImplicit(itemFieldName="Meaning")
	private List<String>		myMeanings;
	
	public Act(String n) {
		name = n;
		completeInit();
	}
    protected Act(){
        completeInit();
    }
	public void completeInit() {
		if (mySteps == null) {
			mySteps = new ArrayList<Step>();
			myStepsSaid = new ArrayList<Integer>();
		}
		initMeanings();
	}
	public void initMeanings()
	{
        if(myMeaningsSet)
            return;
		if(myMeanings == null){
			myMeanings = new ArrayList<String>();
			return;
		}
        myMeaningsSet = true;
		List<String> meanings = new ArrayList<String>();
		for(String m : myMeanings){
			meanings.add(m.toUpperCase());
		}
		myMeanings = meanings;
	}
	public void addStep(Step s) {
		mySteps.add(s);
	}
    public void setName(String n){
        name = n;
    }
	public String getName() {
		return name;
	}
	@Override public String toString() {
		return "Act{name=" + name + ", steps=" + mySteps + "}";
	}
	public List<Step> getSteps() {
		completeInit();
		return mySteps;
	}
	public int getStepCount() {
		List<Step> steps = getSteps();
		return steps.size(); 
	}
	public Step getNumberedStep(int stepNum) {
		List<Step> steps = getSteps();
		int stepIndex = stepNum - 1;
		return steps.get(stepIndex);
	}

	public String getStart() {
		if(start == null)
			start = "true";

		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getNext() {
		if(next == null || next.isEmpty())
			next = "next";
		return next;
	}

	public void setNext(String next) {
		this.next = next;
	}

	public boolean isNextRandom(){
		return getNext().equalsIgnoreCase("random");
	}

	public Long getLastAdvanceTimeMsec() {
		return myLastAdvanceTimeMsec;
	}

	public void setLastAdvanceTimeMsec(long myLastAdvanceTimeMsec) {
		this.myLastAdvanceTimeMsec = myLastAdvanceTimeMsec;
	}

	public List<String> getMeanings(){
		if(myMeanings == null)
			initMeanings();
		
		return myMeanings;
	}

    public void setMeanings(List<String> meanings){
        if(meanings == null){
            myMeanings = new ArrayList<String>();
            return;
        }
        myMeanings = meanings;
    }

    public void addMeaning(String meaning){
        initMeanings();
        meaning = meaning.toUpperCase();
        if(!myMeanings.contains(meaning)){
            myMeanings.add(meaning);
        }
    }

	public int getLastStepNumber(){
		return myLastStep;
	}

	public void setLastStepNumber(int s){
		myLastStep = s - 1;
	}
	public void markStepSaid(int s){
		if(myStepsSaid == null)
			myStepsSaid = new ArrayList<Integer>();
		if(!myStepsSaid.contains(s - 1))
			myStepsSaid.add(s - 1);
	}

	public boolean isPlayable(long timeOutLenth, long replayLength){
		if(myLastStep == null)
			return true;

		if(myStepsSaid == null)
			myStepsSaid = new ArrayList<Integer>();

		if(mySteps == null || mySteps.size() == myStepsSaid.size())
			return false;

		long elapsed = TimeUtils.currentTimeMillis() - myLastAdvanceTimeMsec;
		return (elapsed > replayLength || elapsed < timeOutLenth);
	}

    public String toXML(){
        String act = "\n\t<Act name=\"" + name + "\" start=\"" + getStart() + "\" next=\"" +
                getNext() + "\">";
        for(String m : myMeanings){
            act += "\n\t\t<Meaning>" + m + "</Meaning>";
        }
        for(Step step : mySteps){
            act += "\n\t\t" + step.toXML();
        }
        act += "</Act>";
        return act;
    }

    public Act copy(){
        Act a = new Act(name);
        a.setStart(start);
        a.setNext(next);
        for(String m : myMeanings){
            a.addMeaning(m);
        }
        for(Step s : mySteps){
            a.addStep(s.copy());
        }
        return a;
    }
}
