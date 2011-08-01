package org.cogchar.convoid.cursors;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import org.cogchar.convoid.output.config.Act;
import org.cogchar.convoid.output.config.Category;
import org.cogchar.convoid.output.config.Step;
import org.cogchar.platform.util.TimeUtils;

/**
 *
 * @author Matt Stevenson
 */
public class CategoryCursor implements IConvoidCursor{
    private static Logger theLogger = Logger.getLogger(CategoryCursor.class.getName());
    private static final long   theActTimeoutLength = 45000;
	private static Random       theRandomizer = new Random();
    private List<ActCursor>     myActCursors;
    private List<Integer>       myStartActIndices;
    private List<Integer>       myUnvisitedStartActIndices;
    private List<Integer>       myVisitedStartActIndices;

    private Category            myCategory;
    private Integer             myCurrentIndex;
    private Long                myLastAdvanceTime;
    private Long                myTimeoutLength;
    private String              myGroupType;

    public CategoryCursor(Category cat, Long timeoutLength, String groupType){
        myTimeoutLength = timeoutLength;
        myCategory = cat;
        myActCursors = new ArrayList<ActCursor>();
        myStartActIndices = new ArrayList<Integer>();
        myUnvisitedStartActIndices = new ArrayList<Integer>();
        int index = 0;
        for(Act a : cat.getActs()){
            ActCursor ac = new ActCursor(a, theActTimeoutLength);
            myActCursors.add(ac);
            if(ac.isStartAct()){
                myStartActIndices.add(index);
            }
            index++;
        }
        setupRandomActAccounting();
        myGroupType = groupType;
    }

    public List<ActCursor> getActCursors(){
        return myActCursors;
    }

    public void addAct(Act a){
        myCategory.addAct(a);
        myActCursors.add(new ActCursor(a, theActTimeoutLength));
    }

    private void setupRandomActAccounting(){
		myVisitedStartActIndices = new ArrayList<Integer>();
		myUnvisitedStartActIndices = new ArrayList<Integer>(myStartActIndices);
	}

    public void resetAtTime(long time){
        myCurrentIndex = null;
        myLastAdvanceTime = null;
        partialReset(2);
		int len = myUnvisitedStartActIndices.size();
        for(Integer i : myUnvisitedStartActIndices){
            while(i<len){
				ActCursor ac = myActCursors.get(i);
				ac.forceTimeoutForLengthAtTime(myTimeoutLength, time);
				if(ac.isNextRandom()){
					break;
				}
                i++;
			}
        }
        for(Integer i : myUnvisitedStartActIndices){
            myActCursors.get(i).resetAtTime(time);
        }
    }

	public void partialReset(Integer divide){
		Integer total = myStartActIndices.size();
		if(divide < 1){
			divide = 1;
		}else if(divide > total){
			divide = total;
		}
		Integer goal = (int)Math.ceil((double)total/divide);
		Integer count = myUnvisitedStartActIndices.size();
		if(count >= goal){
			return;
		}
		goal -= count;
		int len = myVisitedStartActIndices.size();
		myUnvisitedStartActIndices.addAll(myVisitedStartActIndices.subList(0,goal));
		myVisitedStartActIndices = new ArrayList<Integer>(myVisitedStartActIndices.subList(goal,len));
	}

    public Integer getCurrentIndex(){
        return myCurrentIndex;
    }

    public Long getLastAdvanceTime(){
        return myLastAdvanceTime;
    }

    public Step getBestStep(){
        Long currentTime = TimeUtils.currentTimeMillis();
        return getBestStepAtTime(currentTime);
    }

    public Step getBestStepAtTime(long time){
        ActCursor ac = getBestActForNextStepAtTime(time);
        Step step = null;
        if(ac != null)
            step = ac.getNextStepAtTime(time);
        return step;
    }

    protected ActCursor getBestActForNextStepAtTime(long time){
        Integer index = null;
        if(isTimedOutAtTime(time)){
            return null;
        }
        if(myCurrentIndex == null){
            index = getRandomActIndexAtTime(time);
        }else{
            index = getBestActIndexAtTime(time);
        }
        if(index == null)
            return null;

        setActVisited(index, time);
        return myActCursors.get(index);
    }

    private Integer getRandomActIndexAtTime(long time) {
        if(myUnvisitedStartActIndices.size() == 0){
            return null;
        }

        List<Integer> prune = new ArrayList<Integer>();
        for(Integer i : myUnvisitedStartActIndices){
            ActCursor ac = myActCursors.get(i);
            if(!ac.isPlayableAtTime(time)){
                prune.add(i);
            }
        }
        prune = new ArrayList<Integer>(prune);
        prune.removeAll(myVisitedStartActIndices);
        myVisitedStartActIndices.addAll(prune);
        //myUnvisitedStartActIndices.removeAll(prune);

		int actCount = myUnvisitedStartActIndices.size();
        if(actCount == 0)
            return null;
		int randomActIndex = myUnvisitedStartActIndices.get(theRandomizer.nextInt(actCount));
        return randomActIndex;
	}

    public Integer getBestActIndexAtTime(long time){
        Integer current = myCurrentIndex;
        if(current == null){
            return getRandomActIndexAtTime(time);
        }
        ActCursor cur = myActCursors.get(current);
        while(!cur.isPlayableAtTime(time) && current <= myActCursors.size()-1){
            if(!cur.isFinishedAtTime(time) && isTimedOutForActAtTime(cur, time)){
                cur.resetAtTime(time);
                return current;
            }
            if(cur.isNextRandom() || current == getActCount()-1){
                return getRandomActIndexAtTime(time);
            }
            cur = myActCursors.get(++current);
        }
        return current;
    }
    
	protected void setActVisited(Integer actIndex, Long time){
        myLastAdvanceTime = time;
        myCurrentIndex = actIndex;
		if(myUnvisitedStartActIndices.contains(actIndex)){
			myUnvisitedStartActIndices.remove(actIndex);
            if(!myVisitedStartActIndices.contains(actIndex)){
                myVisitedStartActIndices.add(actIndex);
            }
		}
	}

    public Integer getActCount(){
        return myActCursors.size();
    }

    protected List<Integer> getStartActIndices(){
        return myStartActIndices;
    }

    protected List<Integer> getUnvisitedStartActIndices(){
        return myUnvisitedStartActIndices;
    }

    protected List<Integer> getVisitedStartActIndices(){
        return myVisitedStartActIndices;
    }

    public long getTimeoutLength(){
        return myTimeoutLength;
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
        if(myActCursors == null || myActCursors.isEmpty()){
            return true;
        }
        if(myCurrentIndex == null){
            return false;
        }
        return (getBestActIndexAtTime(time) == null);
    }

    public boolean isPlayableAtTime(long time){
        return !(isTimedOutAtTime(time) || isFinishedAtTime(time));
    }

    public Category getCategory(){
        return myCategory;
    }

    public List<String> getMeanings(){
        if(myCategory == null || myCategory.getMeanings().isEmpty())
            return new ArrayList<String>();
        return myCategory.getMeanings();
    }

    public void setMeanings(List<String> meanings){
        getCategory().setMeanings(meanings);
    }

	public List<String> getActiveMeanings(){
		List<String> meanings = new ArrayList(getMeanings());
        Integer cur = myCurrentIndex;
        if(cur == null){
            cur = 0;
        }
        List<String> actMeanings = null;
        while((actMeanings == null || actMeanings.isEmpty()) && cur >= 0){
            ActCursor cc = myActCursors.get(cur);
            actMeanings = cc.getMeanings();
            cur--;
        }
        if(actMeanings != null){
            meanings.addAll(actMeanings);
        }
		return meanings;
	}

    public boolean isRandom(){
        List<String> meanings = getMeanings();
        if(meanings == null || meanings.isEmpty())
            return false;
        return meanings.contains("RANDOM");
    }

    public String getName(){
        return myCategory.getName();
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

    public Boolean isTimedOutForActAtTime(ActCursor ac,long time){
        if(isTimedOutAtTime(time)){
            return true;
        }
        if(ac.getLastAdvanceTime() == null)
            return false;
        return (time - ac.getLastAdvanceTime()) > myTimeoutLength;
    }

    public boolean containsAct(ActCursor act) {
        return getActCursors().contains(act);
    }

    public CategoryCursor copy(){
        Category c = new Category(getName());
        Category oldCat = getCategory();
        for(Category sc : oldCat.getSubCategories()){
            c.addSubCategory(sc);
        }
        for(Act a : oldCat.getActs()){
            c.addAct(a.copy());
        }
        for(String m : getMeanings()){
            c.addMeaning(m);
        }
        return new CategoryCursor(c, myTimeoutLength, myGroupType);
    }

    public String getGroupType() {
        return myGroupType;
    }

    @Override
    public String toString(){
        return "CategoryCursor: " + getName();
    }
}