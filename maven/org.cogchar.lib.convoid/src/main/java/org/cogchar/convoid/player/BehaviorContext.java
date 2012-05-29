package org.cogchar.convoid.player;

import java.util.ArrayList;
import java.util.List;
import org.cogchar.platform.stub.ThalamentStub;

/**
 *
 * @author Matt Stevenson
 */
public class BehaviorContext extends ThalamentStub {
    //We are extending Thalament so the context will show up on the fact page.
    //Hopefully this will not lead to any confusion about its use in the future

    public enum PromptSource{
        USER,
        TIMER,
        SELF;
    }
    public enum Detail{
        RESET,
        RANDOM,
        BACKUP,
        UNPLAYABLE,
        NO_SELF_RESUME,
        FROM_EXPO,
		REMOTE,
        NO_TRANSITION;
    }
    private String        myIntendedBehaviorType;
    private String        myActualBehaviorType;
    private PromptSource        myPromptSource;
    private String              myPrompt;
    private List<Detail>        myDetails;
    private IBehaviorPlayable   myBehavior;

    public BehaviorContext(){
        myDetails = new ArrayList<Detail>();
    }

    public BehaviorContext withIntendedType(String type){
        myIntendedBehaviorType = type;
        return this;
    }
    public BehaviorContext andIntendedType(String type){
        return withIntendedType(type);
    }

    public BehaviorContext withActualType(String type){
        myActualBehaviorType = type;
        return this;
    }
    public BehaviorContext andActualType(String type){
        return withActualType(type);
    }

    public BehaviorContext with(PromptSource source){
        myPromptSource = source;
        return this;
    }
    public BehaviorContext and(PromptSource source){
        return with(source);
    }

    public BehaviorContext withPrompt(String prompt){
        myPrompt = prompt;
        return this;
    }
    public BehaviorContext andPrompt(String prompt){
        return withPrompt(prompt);
    }

    public BehaviorContext with(Detail detail){
        myDetails.add(detail);
        return this;
    }
    public BehaviorContext and(Detail detail){
        return with(detail);
    }

    public BehaviorContext with(IBehaviorPlayable behavior){
        myBehavior = behavior;
        List<Detail> playDetails = behavior.getDetails();
        myDetails.removeAll(playDetails);
        myDetails.addAll(playDetails);
        return this;
    }

	public boolean isEmptyBehavior(){
		return contains(makeEmpty());
	}

    public BehaviorContext with(BehaviorContext context){
		if(context.myActualBehaviorType != null){
			myActualBehaviorType = context.myActualBehaviorType;
		}
		if(context.myIntendedBehaviorType != null){
			myIntendedBehaviorType = context.myIntendedBehaviorType;
		}
		if(context.myPromptSource != null){
			myPromptSource = context.myPromptSource;
		}
		if(context.myPrompt != null){
			myPrompt = context.myPrompt;
		}
		if(context.myBehavior != null){
			myBehavior = context.myBehavior;
		}
		if(context.myDetails != null && !context.myDetails.isEmpty()){
			for(Detail d : context.myDetails){
				if(!myDetails.contains(d)){
					myDetails.add(d);
				}
			}
		}
        return this;
    }
    public BehaviorContext and(IBehaviorPlayable behavior){
        return with(behavior);
    }

    public String getIntendedBehaviorType(){
        return myIntendedBehaviorType;
    }

    public String getActualBehaviorType(){
        return myActualBehaviorType;
    }

    public PromptSource getPromptSource(){
        return myPromptSource;
    }

    public String getPrompt(){
        return myPrompt;
    }

    public List<Detail> getDetails(){
        return myDetails;
    }

    public IBehaviorPlayable getBehavior(){
        myBehavior.setCause(this);
        return myBehavior;
    }

    public PlayerAction getPlayerAction(){
        if(myBehavior == null || myBehavior.getAction() == null){
            return PlayerAction.EMPTY;
        }
        return myBehavior.getAction();
    }

    @Override
    public boolean equals(Object obj){
        if(obj.getClass() != BehaviorContext.class){
            return false;
        }
        BehaviorContext bc = (BehaviorContext)obj;

        if(myIntendedBehaviorType == null){
            myIntendedBehaviorType = "";
        }
        if(myActualBehaviorType == null){
            myActualBehaviorType = "";
        }
        return myIntendedBehaviorType.equals(bc.myIntendedBehaviorType) &&
           myActualBehaviorType.equals(bc.myActualBehaviorType) &&
           myPromptSource == bc.myPromptSource &&
           ((myPrompt != null && bc.myPrompt != null && myPrompt.equals(bc.myPrompt)) ||
           (myPrompt == null && bc.myPrompt == null)) &&
           myDetails.equals(bc.myDetails) &&
           myBehavior == bc.myBehavior;
    }

    public BehaviorContext copy(){
        BehaviorContext copy = new BehaviorContext();
        copy.myActualBehaviorType = myActualBehaviorType;
        copy.myBehavior = myBehavior;
        copy.myDetails = new ArrayList<Detail>(myDetails);
        copy.myIntendedBehaviorType = myIntendedBehaviorType;
        copy.myPrompt = myPrompt;
        copy.myPromptSource = myPromptSource;
        return copy;
    }

    public boolean contains(BehaviorContext bc) {
        if(equals(bc)){
            return true;
        }
        if(bc.myActualBehaviorType != null && !bc.myActualBehaviorType.equals(myActualBehaviorType)){
            return false;
        }else if(bc.myIntendedBehaviorType != null && !bc.myIntendedBehaviorType.equals(myIntendedBehaviorType)){
            return false;
        }else if(bc.myPromptSource != null && bc.myPromptSource != myPromptSource){
            return false;
        }else if(bc.myPrompt != null && !bc.myPrompt.equals(myPrompt)){
            return false;
        }else if(!myDetails.containsAll(bc.myDetails)){
            return false;
        }
        return true;
    }

    @Override
	public String getContentSummaryString() {
		return "intent=" + myIntendedBehaviorType + ", actual=" + myActualBehaviorType +
                ", promptSource=" + myPromptSource + ", prompt=" + myPrompt +
                ", details=" + myDetails + ", playerAction=" + getPlayerAction() +
                ", behavior=" + myBehavior;
	}

	@Override
	public String toString(){
		return "[" + getClass().getSimpleName() + ": " + getContentSummaryString() + "]";
	}

	public static BehaviorContext makeEmpty(){
		return new BehaviorContext().withActualType("EMPTY").and(Detail.UNPLAYABLE).and(new EmptyPlayer());
	}
}
