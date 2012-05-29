/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.xploder.cursors;

import java.util.ArrayList;
import java.util.List;
import org.cogchar.api.convoid.act.Act;
import org.cogchar.api.convoid.act.Step;
import org.cogchar.platform.util.TimeUtils;

/**
 *
 * @author matt
 */
public class ActCursor implements IConvoidCursor {
    private long        myTimeoutLength;
    private Integer     myCurrentIndex;
    private Long        myLastAdvanceTime;
    private Act         myAct;

    public ActCursor(Act act, long timeoutLength){
        myAct = act;
        myTimeoutLength = timeoutLength;
    }

    public List<Step> getSteps(){
        return myAct.getSteps();
    }

    public long getTimeoutLength(){
        return myTimeoutLength;
    }

    public Integer getCurrentIndex() {
        return myCurrentIndex;
    }

    public Long getLastAdvanceTime() {
        return myLastAdvanceTime;
    }

    public Step getBestStepAtTime(long time){
        return getNextStepAtTime(time);
    }

    public Step getNextStep(){
        Long currentTime = TimeUtils.currentTimeMillis();
        return getNextStepAtTime(currentTime);
    }

    public Step getNextStepAtTime(long currentTime){
        if(myCurrentIndex == null){
            myCurrentIndex = -1;
        }
        if(myCurrentIndex >= getSteps().size() - 1 || isTimedOutAtTime(currentTime)){
            return null;
        }

        myLastAdvanceTime = currentTime;
        myCurrentIndex++;
        return getSteps().get(myCurrentIndex);
    }

    public void resetAtTime(long time){
        myLastAdvanceTime = null;
        myCurrentIndex = null;
    }

    public boolean isTimedOutAtTime(long time){
        if(myLastAdvanceTime == null)
            return false;
        return (time - myLastAdvanceTime) > myTimeoutLength;
    }

    public Boolean isTimedOut(){
        Long currentTime = TimeUtils.currentTimeMillis();
        return isTimedOutAtTime(currentTime);
    }

    public boolean isFinishedAtTime(long time){
        if(getSteps() == null || getSteps().isEmpty())
            return true;
        if(myCurrentIndex == null)
            return false;
        return myCurrentIndex == getSteps().size()-1;
    }

    public String getName(){
        return myAct.getName();
    }

    public boolean isStartAct(){
        if(myAct.getStart().equalsIgnoreCase("false")){
            return false;
        }
        if(!getMeanings().isEmpty()){
            return true;
        }
        return myAct.getStart().equalsIgnoreCase("true");
    }

    public boolean isNextRandom(){
        return myAct.isNextRandom();
    }

    public Act getAct(){
        return myAct;
    }

    public boolean isActive(){
        return myCurrentIndex != null;
    }

    public boolean isPlayableAtTime(long time){
        return !(isTimedOutAtTime(time) || isFinishedAtTime(time));
    }

    public List<String> getMeanings(){
        if(myAct == null)
            return new ArrayList<String>();
        return myAct.getMeanings();
    }

    public List<String> getActiveMeanings(){
		return getMeanings();
    }

    public boolean isRandom(){
        List<String> meanings = getMeanings();
        if(meanings == null || meanings.isEmpty())
            return false;
        return getMeanings().contains("RANDOM");
    }

    public boolean isCurrentActFinishedAtTime(long time) {
        return isFinishedAtTime(time);
    }

    public boolean isCurrentActEndOfSequence(long time) {
        return isNextRandom();
    }

	protected void forceTimeoutForLengthAtTime(long timeoutLength, long time){
		if(myLastAdvanceTime == null){
			myLastAdvanceTime = time - timeoutLength - 1;
			return;
		}
		if(time - myLastAdvanceTime > timeoutLength){
			return;
		}
		myLastAdvanceTime = time - timeoutLength - 1;
	}

    public void setMeanings(List<String> meanings) {
        getAct().setMeanings(meanings);
    }

    public String getGroupType() {
        return "";
    }

    @Override
    public String toString(){
        return "ActCursor: " + getName() + ", " + myCurrentIndex;
    }
}
