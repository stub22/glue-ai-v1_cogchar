package org.cogchar.convoid.player;

import org.cogchar.api.convoid.act.Step;
import org.cogchar.convoid.player.BehaviorContext.Detail;
import java.util.ArrayList;
import java.util.List;
import org.cogchar.convoid.broker.ConvoidFacade;
import org.cogchar.convoid.broker.ConvoidFacadeSource;
import org.cogchar.api.convoid.cue.ConvoidCueSpace;
import org.cogchar.convoid.job.ConversationJob;
import org.cogchar.platform.stub.ThalamentStub;

/**
 *
 * @author matt
 */
public class StepPlayer implements IBehaviorPlayable{
    private static final List<Detail> theEmptyList = new ArrayList<Detail>();
    private Step				myStep;
    private ThalamentStub       myCause;

    public StepPlayer(Step step){
        if(step == null){
            throw new IllegalArgumentException("Step must not be null");
        }
        myStep = step;
    }
    public StepPlayer(String text){
        if(text == null){
            throw new IllegalArgumentException("Text must not be null");
        }
        Step step = new Step();
        step.setType(Step.ST_SAPI5_LITERAL);
        step.setText(text);
        myStep = step;
    }

    public void run(ConvoidFacadeSource igf) {
        ConvoidFacade cf = igf.getConvoidFacade();
        ConversationJob cj = cf.getMainConversationJob();
        ConvoidCueSpace ccs = cf.getCueSpace();
        cj.playSingleStep(ccs, myStep, myCause);
    }

    public PlayerAction getAction(){
        return PlayerAction.PLAY;
    }
    public List<Detail> getDetails() {
        return theEmptyList;
    }

    public Step getStep() {
        return myStep;
    }

    public void addDetail(Detail detail) {}

    public void setCause(ThalamentStub t){
        myCause = t;
    }
}
