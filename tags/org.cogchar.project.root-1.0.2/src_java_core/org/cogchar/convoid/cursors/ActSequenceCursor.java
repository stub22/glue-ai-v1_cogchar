package org.cogchar.convoid.cursors;

import java.util.ArrayList;
import java.util.List;
import org.cogchar.convoid.output.config.Step;

/**
 *
 * @author matt
 */
public class ActSequenceCursor implements IConvoidCursor {
    private CategoryCursor      myCursor;
    private List<ActCursor>     myActCursors;
    private Integer             myCurrentIndex;
    private Long                myLastAdvanceTime;
    private Long                myTimeoutLength;
    private Integer             myStartIndex;
    private Integer             myEndIndex;

    public ActSequenceCursor(CategoryCursor cursor, Integer startIndex, Integer endIndex, Long timeoutLength){
        myCursor = cursor;
        List<ActCursor> acts = myCursor.getActCursors();
        if(startIndex < 0 || startIndex >= acts.size() 
                || endIndex < 0 || endIndex >= acts.size()
                || endIndex < startIndex){
            throw new IllegalArgumentException("An index is out of bounds.");
        }

        myActCursors = acts.subList(startIndex, endIndex+1);
        myStartIndex = startIndex;
        myEndIndex = endIndex;
        myTimeoutLength = timeoutLength;
    }

    public Step getBestStepAtTime(long time){
        Integer i = getBestActIndexAtTime(time);
        if(i == null){
            return null;
        }
        ActCursor ac = myActCursors.get(i);
        if(ac == null)
            return null;
        Step step = ac.getNextStepAtTime(time);
        if(step != null){
            myCurrentIndex = i;
            myLastAdvanceTime = time;
            Integer index = myStartIndex + i;
            myCursor.setActVisited(index, time);
        }
        return step;
    }

    public Integer getBestActIndexAtTime(long time){
        if(myActCursors == null || myActCursors.isEmpty())
            return null;
        if(isTimedOutAtTime(time) || isFinishedAtTime(time)){
            return null;
        }
        Integer current = myCurrentIndex;
        if(current == null)
            current = 0;

        ActCursor cur = myActCursors.get(current);
        if(cur.isPlayableAtTime(time)){
            return current;
        }
        while(!cur.isPlayableAtTime(time) && current <= myActCursors.size()-1){
            if(isTimedOutForActAtTime(cur, time)){
                cur.resetAtTime(time);
                return current;
            }
			if(++current < myActCursors.size()){
				cur = myActCursors.get(current);
			}
        }
        if(!cur.isPlayableAtTime(time)){
            return null;
        }
        return current;
    }

    public void resetAtTime(long time){
        myLastAdvanceTime = null;
        Integer current = myCurrentIndex;
		Long timeout = Math.max(myTimeoutLength, myCursor.getTimeoutLength());
        if(current == null)
            current = 0;
        while(current > 0){
            myActCursors.get(current).forceTimeoutForLengthAtTime(timeout, time);
            current--;
        }
		myActCursors.get(0).resetAtTime(time);
        myCurrentIndex = null;
		if(!myCursor.getUnvisitedStartActIndices().contains(myStartIndex)){
			myCursor.getUnvisitedStartActIndices().add(myStartIndex);
		}
		myCursor.resetAtTime(time);
    }

    public boolean isTimedOutAtTime(long time){
        if(myLastAdvanceTime == null)
            return false;
        return (time - myLastAdvanceTime) > myTimeoutLength;
    }

    public Boolean isTimedOutForActAtTime(ActCursor ac,long time){
        if(isTimedOutAtTime(time)){
            return true;
        }
        if(ac.getLastAdvanceTime() == null)
            return false;
        return (time - ac.getLastAdvanceTime()) > myTimeoutLength;
    }

    public boolean isFinishedAtTime(long time){
		if(myActCursors == null || myActCursors.isEmpty()){
			return true;
		}
        for(ActCursor ac : myActCursors){
            if(ac.isPlayableAtTime(time)){
                return false;
            }
        }
		if(myCurrentIndex == null || myCurrentIndex == myActCursors.size() - 1){
			return true;
		}
		ActCursor cur = myActCursors.get(myCurrentIndex);
		ActCursor next = myActCursors.get(myCurrentIndex+1);
		if(cur.isFinishedAtTime(time) && isTimedOutForActAtTime(next, time)){
			return false;
		}
		return true;
    }

    public List<ActCursor> getActCursors(){
        return myActCursors;
    }

    public CategoryCursor getCategoryCursor(){
        return myCursor;
    }

    public Integer getStartIndex(){
        return myStartIndex;
    }

    public Integer getEndIndex(){
        return myEndIndex;
    }

    public Integer getCurrentIndex(){
        return myCurrentIndex;
    }

    public long getTimeoutLength(){
        return myTimeoutLength;
    }

    public Long getLastAdvanceTime() {
        return myLastAdvanceTime;
    }

    public boolean isPlayableAtTime(long time){
        return !(isTimedOutAtTime(time) || isFinishedAtTime(time));
    }

    public List<String> getMeanings(){
        if(myActCursors == null || myActCursors.isEmpty())
            return new ArrayList<String>();
        return myActCursors.get(0).getMeanings();
    }

    public void setMeanings(List<String> meanings){
        getActCursors().get(myStartIndex).setMeanings(meanings);
    }

	public List<String> getActiveMeanings(){
		List<String> meanings = new ArrayList(getMeanings());
		ActCursor cc = myActCursors.get(myCurrentIndex);
		meanings.addAll(cc.getMeanings());
		return meanings;
	}

    public boolean isRandom(){
        List<String> meanings = getMeanings();
        if(meanings == null || meanings.isEmpty())
            return false;
        return getMeanings().contains("RANDOM");
    }

    public String getName(){
        return myActCursors.get(0).getName();
    }

    public boolean isActive(){
        return myCurrentIndex != null;
    }

    public boolean isCurrentActFinishedAtTime(long time) {
        if(isFinishedAtTime(time)){
            return true;
        }
        if(myCurrentIndex == null){
            return false;
        }
        ActCursor act = myActCursors.get(myCurrentIndex);
        return act.isFinishedAtTime(time);
    }

    public boolean isCurrentActEndOfSequence(){
        if(myCurrentIndex == null){
            return false;
        }
        ActCursor act = myActCursors.get(myCurrentIndex);
        return act.isNextRandom();
    }

    public boolean containsIndex(Integer i) {
        return myStartIndex <= i && myEndIndex >= i;
    }

    public String getGroupType() {
        return myCursor.getGroupType();
    }

    @Override
    public String toString(){
        return "ActSequence: " + getName() + ", " + myStartIndex;
    }
}
