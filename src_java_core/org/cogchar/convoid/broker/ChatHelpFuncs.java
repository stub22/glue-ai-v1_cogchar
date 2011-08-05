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

package org.cogchar.convoid.broker;

import org.cogchar.convoid.cursors.MeaningCursorMap;
import org.cogchar.convoid.output.config.Step;
import org.cogchar.convoid.output.exec.SpeechJob;
import org.cogchar.convoid.output.speech.CursorManager;
import org.cogchar.convoid.output.exec.context.BehaviorContext;
import org.cogchar.convoid.output.exec.context.BehaviorContext.PromptSource;
import org.cogchar.convoid.output.exec.context.IBehaviorPlayable;
import org.cogchar.convoid.output.exec.context.PlayerAction;
import org.cogchar.convoid.output.exec.context.SpeechPlayer;
import org.cogchar.convoid.output.speech.CursorRequest;
import org.cogchar.convoid.output.speech.CursorRequest.BackupOption;
import org.cogchar.convoid.output.speech.CursorRequest.ResetMode;
import org.cogchar.convoid.output.speech.CursorRequest.ScoreMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.cogchar.platform.util.TimeUtils;

/**
 *
 * @author Matt Stevenson
 */
public class ChatHelpFuncs {
	private static Logger theLogger = Logger.getLogger(ChatHelpFuncs.class.getName());

    public static void continueSpeechJob(ConvoidFacadeSource igf, SpeechJob job) {
        theLogger.fine("Continuing: " + job.getCategoryName() + "(" + job.getClass().getSimpleName() + ")");
		// Assume ej is already running/resumed?
		ConvoidFacade cf = igf.getConvoidFacade();
		job.startNextStepAtTime(cf, currentTime());
		getCursorManager(igf).setLastPlayed(job);
	}
	public static void pauseSpeechJob(ConvoidFacadeSource igf, SpeechJob job, boolean cancelCurrentStep) {
        theLogger.fine("Pausing: " + job.getCategoryName() + "(" + job.getClass().getSimpleName() + ")");
		job.markPaused();
		if (cancelCurrentStep) {
			ConvoidHelpFuncs.purgeStepJobs(igf);
		}
	}
	public static boolean resumeSpeechJob(ConvoidFacadeSource igf, SpeechJob job) {
        theLogger.fine("Resuming: " + job.getCategoryName() + "(" + job.getClass().getSimpleName() + ")");
		ConvoidFacade cf = igf.getConvoidFacade();
		job.markResumed();
		if(job.startNextStepAtTime(cf, currentTime())){
            getCursorManager(igf).setLastPlayed(job);
            return true;
        }
        return false;
	}
	public static void killSpeechJob(ConvoidFacadeSource igf, SpeechJob job) {
        theLogger.fine("Killing: " + job.getCategoryName() + "(" + job.getClass().getSimpleName() + ")");
        job.markCanceled();
		ConvoidHelpFuncs.purgeStepJobs(igf);    //Stop anything being said
        getCursorManager(igf).killSpeechJob(job);   //Remove the job from the group so it cannot come back
        //I don't think we need to resort to purging the job entirely
        //ConvoidHelpFuncs.purgeJob(igf, job);
	}

    public static SpeechJob getLastPlayed(ConvoidFacadeSource igf){
        return getCursorManager(igf).getLastPlayed();
    }
    public static BehaviorContext getMoreFromLastJob(ConvoidFacadeSource igf){
        theLogger.fine("Getting More");
		return getCursorManager(igf).getMoreToSay().and(PromptSource.USER).andPrompt("MORE");
    }

	public static void addBehaviorMeaning(ConvoidFacadeSource igf, String meaning, String type){
        theLogger.fine("Adding meaning for type: " + type);
        getCursorManager(igf).getCursorGroup(type).getScoreKeeper().addMeaningAtTime(meaning, currentTime());
	}

    public static BehaviorContext getJobContinuation(ConvoidFacadeSource igf, SpeechJob job){
		String type = job.getCategoryCursor().getGroupType();
        BehaviorContext context = new BehaviorContext().withIntendedType(type).and(PromptSource.SELF);
        getCursorManager(igf).addMeaningsForJob(job, 0.5, currentTime());
		if(job.isFinished()){
			theLogger.info("Category is completed.");
			IBehaviorPlayable player = new SpeechPlayer(job, PlayerAction.COMPLETE);
            return context.with(player).andActualType(type).andPrompt("COMPLETED");
		}else if(job.isCurrentSequenceFinished()){
			theLogger.info("Act sequence completed.");
			IBehaviorPlayable player = new SpeechPlayer(job, PlayerAction.COMPLETE);
            return context.with(player).andActualType(type).andPrompt("SEQUENCE COMPLETED");
		}else if(job.isCurrentActFinished()){
			theLogger.info("Act completed.");
			IBehaviorPlayable player = new SpeechPlayer(job, PlayerAction.PAUSE);
            return context.with(player).andActualType(type).andPrompt("ACT COMPLETED");
		}else{
			theLogger.info("Automatically continue to the next step or act.");
            Step step = job.getCategoryCursor().getBestStep();
			if(step != null){
                IBehaviorPlayable player = new SpeechPlayer(step, job);
                return context.with(player).andActualType(type).andPrompt("CONTINUE");
            }
		}
        return context.with(BehaviorContext.makeEmpty()).andPrompt("CONTINUE");
    }

    public static BehaviorContext getJobPlayerContext(ConvoidFacadeSource igf, SpeechJob job, PlayerAction action){
		String type = job.getCurrentCursor().getGroupType();
        if(action == PlayerAction.PLAY){
            return getJobPlayerForNextStep(job, type);
        }else if(action == PlayerAction.PAUSE ||
                 action == PlayerAction.INTERRUPT ||
                 action == PlayerAction.CANCEL ||
                 action == PlayerAction.COMPLETE )
        {
            IBehaviorPlayable player = new SpeechPlayer(job, action);
            return new BehaviorContext().with(player).andIntendedType(type).
                    andActualType(type);
        }
        return BehaviorContext.makeEmpty().withIntendedType(type);
    }

    public static BehaviorContext getJobPlayerForNextStep(SpeechJob job, String type){
        theLogger.fine("Fetching next step for job " + job.getCategoryName() + "(" + job.getClass().getSimpleName() +
                ") with type: " + type);
        BehaviorContext context = new BehaviorContext().withIntendedType(type);
        if(job == null){
            return context.with(BehaviorContext.makeEmpty());
        }
        Step step = job.getCategoryCursor().getBestStep();
        if(step == null){
            return context.with(BehaviorContext.makeEmpty());
        }
        /*if(ChatFactory.theNoTransitionRoot != null){
            if(ChatFactory.theNoTransitionRoot.findSubCategory(job.getCategoryName()) != null){
                context.with(Detail.NO_TRANSITION);
            }
        }*/
        IBehaviorPlayable player = new SpeechPlayer(step, job);
        return context.with(player).andActualType(type);
    }

    public static BehaviorContext getResponseBehavior(ConvoidFacadeSource igf, String meaning){
        theLogger.fine("Getting response behavior: " + meaning);
        MeaningCursorMap mcm = igf.getConvoidFacade().getResponseMap();
        return mcm.getResponseBehavior(meaning);
    }

    public static void addMeaningsToGroups(ConvoidFacadeSource igf, Map<String, Double> meanings){
        theLogger.fine("Adding meanings to groups: " + meanings.keySet());
        getCursorManager(igf).addMeaningsAtTime(meanings, 1.0, currentTime());
    }

    private static Long currentTime(){
        return TimeUtils.currentTimeMillis();
    }
	public static CursorManager getCursorManager(ConvoidFacadeSource igf){
		return igf.getConvoidFacade().getCursorManager();
	}

    public static BehaviorContext requestCursor(ConvoidFacadeSource igf, CursorRequest request){
        BehaviorContext bc = null;
        try{
            bc = getCursorManager(igf).getBehaviorContext(request);
            if(bc == null || bc.getPlayerAction() == null){
                bc = BehaviorContext.makeEmpty();
            }
        }catch(Throwable t){
            bc = BehaviorContext.makeEmpty();
        }
        if(bc.isEmptyBehavior()){
            theLogger.severe("Failed to request a cursor with: Required" +
                    Arrays.toString(request.getRequiredMeanings().toArray()) + " and Prompt" +
                    Arrays.toString(request.getMeanings().keySet().toArray()));
        }
        return bc;
    }

    public static CursorRequest forcedRequest(){
        CursorRequest req = new CursorRequest(currentTime(), 0L, 0.9, 5);
        req.setResetMode(ResetMode.RESET);
        return req;
    }

    public static CursorRequest responseRequest(){
        CursorRequest req = new CursorRequest(currentTime(), 30000L, 0.5, 3);
        req.setResetMode(ResetMode.TIMED);
        req.getBackupOptions().add(BackupOption.REMOTE);
        req.getBackupOptions().add(BackupOption.RANDOM);
        req.getBackupOptions().add(BackupOption.RESET);
        return req;
    }

    public static CursorRequest selfPromptRequest(){
        CursorRequest req = responseRequest();
        req.setResetTime(1800000L);
        req.getRequiredMeanings().add("SELF-PROMPT");
        return req;
    }

    public static CursorRequest randomRequest(){
        CursorRequest req = responseRequest();
        req.getMeanings().put("RANDOM", 1.0);
        req.getMeanings().put("SELF-PROMPT", 1.0);
        req.setScoreMode(ScoreMode.IGNORE);
        return req;
    }

    public static List<String> getBehaviorMeanings(BehaviorContext bc){
        IBehaviorPlayable player = bc.getBehavior();
        if(player != null && SpeechPlayer.class.isAssignableFrom(player.getClass())){
            SpeechJob job = ((SpeechPlayer)player).getJob();
            if(job != null){
                List<String> meanings = job.getCategory().getMeanings();
                if(meanings != null){
                    return meanings;
                }
            }
        }
        return new ArrayList<String>();
    }
}