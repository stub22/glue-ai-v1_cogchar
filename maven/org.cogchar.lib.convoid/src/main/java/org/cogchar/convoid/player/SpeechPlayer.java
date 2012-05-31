package org.cogchar.convoid.player;

import org.cogchar.api.convoid.act.Step;
import org.cogchar.convoid.player.BehaviorContext.Detail;
import java.util.ArrayList;
import java.util.List;
import org.cogchar.convoid.broker.ChatHelpFuncs;
import org.cogchar.convoid.broker.ConvoidFacadeSource;
import org.cogchar.convoid.broker.ConvoidHelpFuncs;
import org.cogchar.convoid.job.SpeechJob;
import org.cogchar.zzz.platform.stub.ThalamentStub;
import org.cogchar.platform.util.TimeUtils;

/**
 *
 * @author matt
 */
public class SpeechPlayer implements IBehaviorPlayable {
    private Step            myStep;
    private SpeechJob       mySpeechJob;
    private PlayerAction    myAction;
    private List<Detail>    myDetails;

    public SpeechPlayer(SpeechJob job, PlayerAction action){
        switch(action){
            case PAUSE:
            case INTERRUPT:
            case CANCEL:
            case COMPLETE:
                break;
            default:
                throw new IllegalArgumentException("PlayerAction can only be Pause, Cancel, or Complete.  " +
                    "Perhaps you want the other constructor for playing convoid jobs.");
        }
        myDetails = new ArrayList<Detail>();
        mySpeechJob = job;
        if(!job.getCanSelfResume()){
            myDetails.add(Detail.NO_SELF_RESUME);
        }
        if((action == PlayerAction.PAUSE || action == PlayerAction.INTERRUPT) && job.isDead()){
            myAction = PlayerAction.CANCEL;
        }else{
            myAction = action;
        }
    }

    public SpeechPlayer(Step step, SpeechJob job){
        if(step == null){
            throw new IllegalArgumentException("Step must not be null");
        }
        if(job == null){
            throw new IllegalArgumentException("SpeechJob must not be null");
        }
        myStep = step;
        mySpeechJob = job;
        myAction = PlayerAction.PLAY;
    }

    public void run(ConvoidFacadeSource igf) {
        run(igf, true);
    }
    
    public void run(ConvoidFacadeSource igf, boolean setLastPlayed) {
        switch(myAction){
            case PLAY : play(igf); break;
            case PAUSE : pause(igf); break;
            case INTERRUPT : interrupt(igf); break;
            case CANCEL : cancel(igf); break;
            case COMPLETE : complete(igf); break;
        }
        if(setLastPlayed){
            igf.getConvoidFacade().setLastPlayed(mySpeechJob);
        }
    }

    private void play(ConvoidFacadeSource igf){
        long time = TimeUtils.currentTimeMillis();
        if(mySpeechJob.isDead()){
            mySpeechJob.setInterruptScore(0);
        }
        mySpeechJob.playStepAtTime(igf.getConvoidFacade(), myStep, time);
    }

    private void pause(ConvoidFacadeSource igf){
		mySpeechJob.markPaused();
    }

    private void interrupt(ConvoidFacadeSource igf){
		ConvoidHelpFuncs.purgeStepJobs(igf);
        pause(igf);
    }

    private void cancel(ConvoidFacadeSource igf){
        ChatHelpFuncs.killSpeechJob(igf, mySpeechJob);
    }

    private void complete(ConvoidFacadeSource igf){
		mySpeechJob.markCompleted();
    }

    public PlayerAction getAction(){
        return myAction;
    }

    public void addDetail(Detail detail){
        if(myDetails == null){
            myDetails = new ArrayList<Detail>();
        }
        if(!myDetails.contains(detail)){
            myDetails.add(detail);
        }
    }

    public List<Detail> getDetails() {
        if(myDetails == null){
            myDetails = new ArrayList<Detail>();
        }
        return myDetails;
    }

    public SpeechJob getJob(){
        return mySpeechJob;
    }

    public Step getStep() {
        return myStep;
    }

    public void setCause(ThalamentStub t){
        if(mySpeechJob == null){
            return;
        }
        mySpeechJob.setCausingThalament(t);
    }
}
