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

import org.cogchar.convoid.cursors.IConvoidCursor;
import org.cogchar.convoid.output.config.Agenda;
import org.cogchar.convoid.output.config.Category;
import org.cogchar.convoid.output.exec.AgendaManager;
import org.cogchar.convoid.output.exec.ConversationJob;

import org.cogchar.convoid.output.exec.SpeechJob;
import org.cogchar.convoid.output.exec.StepJob;
import org.cogchar.convoid.output.exec.context.BehaviorContext;
import org.cogchar.convoid.output.exec.context.BehaviorContext.Detail;
import org.cogchar.convoid.output.exec.context.BehaviorContext.PromptSource;
import org.cogchar.convoid.output.exec.context.PlayerAction;
import org.cogchar.convoid.output.speech.CursorRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import org.cogchar.convoid.cue.ConvoidCueSpace;
import org.cogchar.convoid.cue.HeardCue;
import org.cogchar.convoid.cue.InhibitionCue;
import org.cogchar.convoid.cue.InitiativeCue;
import org.cogchar.convoid.cue.ModeCue;
import org.cogchar.convoid.cue.PhaseCue;
import org.cogchar.convoid.cue.ResponseCue;
import org.cogchar.convoid.cue.VerbalCue;
import org.cogchar.platform.cues.TimerCue;
import org.cogchar.platform.stub.JobStub;
import org.cogchar.platform.stub.ThalamentStub;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class ConvoidHelpFuncs {
	private static Logger theLogger = Logger.getLogger(ConvoidHelpFuncs.class.getName());
	static Random theRandomizer = new Random();

	public static void sayStepXML(ConvoidFacadeSource igf, String stepXML, ThalamentStub cause) {
		ConversationJob convJob = igf.getConvoidFacade().getMainConversationJob();
		convJob.createDynamicSpeechStepAndRunJob(stepXML, cause);
	}

	public static VerbalCue addVerbalCue(ConvoidFacadeSource igf, String meaning, double strength){
        Map<String, Double> meanings = new HashMap();
        meanings.put(meaning, strength);
		return addVerbalCue(igf, meanings, strength);
	}

	public static VerbalCue addVerbalCue(ConvoidFacadeSource igf, Map<String, Double> meanings, double strength) {
		return igf.getConvoidFacade().getCueSpace().addVerbalCueForMeanings(meanings, strength);
	}

	public static void modeSwitch(ConvoidFacadeSource igf, String modeName) {
		ConvoidCueSpace ccs = igf.getConvoidFacade().getCueSpace();
		ccs.clearAllCuesMatching(ModeCue.class);
		ccs.addModeCueForName(modeName, 1.0);
	}

	public static void phaseSwitch(ConvoidFacadeSource igf, String phaseName) {
		ConvoidCueSpace ccs = igf.getConvoidFacade().getCueSpace();
		ccs.clearAllCuesMatching(PhaseCue.class);
        PhaseCue pc = new PhaseCue(phaseName);
        pc.setStrength(1.0);
        ccs.addCue(pc);
	}

    public static void addInitiativeCue(ConvoidFacadeSource igf, String name, String type, double strength){
		ConvoidCueSpace ccs = igf.getConvoidFacade().getCueSpace();
        InitiativeCue ic = new InitiativeCue(name, type);
        ic.setStrength(strength);
        ccs.addCue(ic);
    }

    public static void addResponseCue(ConvoidFacadeSource igf, String name, String type, double strength){
		ConvoidCueSpace ccs = igf.getConvoidFacade().getCueSpace();
        ResponseCue rc = new ResponseCue(name, type);
        rc.setStrength(strength);
        ccs.addCue(rc);
    }

    public static void addInhibitionCue(ConvoidFacadeSource igf, String name, List<String> meanings, double strength){
		ConvoidCueSpace ccs = igf.getConvoidFacade().getCueSpace();
        InhibitionCue ic = new InhibitionCue(name, meanings);
        ic.setStrength(strength);
        ccs.addCue(ic);
    }

	public static void addHeardCue(ConvoidFacadeSource igf, String text){
		ConvoidCueSpace ccs = igf.getConvoidFacade().getCueSpace();
		HeardCue hc = new HeardCue(text);
		hc.setStrength(1.0);
        ccs.addCue(hc);
	}

	public static void purgeStepJobs(ConvoidFacadeSource igf) {
		igf.getConvoidFacade().purgeStepJobs();
	}

	public static void purgeJob(ConvoidFacadeSource igf, JobStub j) {
		igf.getConvoidFacade().purgeJob(j);
	}

    private static boolean debug = false;
    public static void setDebug(boolean flag){
        debug = flag;
    }
    public static void playResponse(ConvoidFacadeSource igf, String meaning, ThalamentStub cause){
        if(debug){
            String xml = "<sapi>" + meaning.replaceAll("_", " ") + "</sapi>";
            sayStepXML(igf, xml, cause);
        }
        ConvoidCueSpace ccs = igf.getConvoidFacade().getCueSpace();
        StepJob sj = igf.getConvoidFacade().playResponse(ccs, meaning, cause);
        if(sj == null){
            return;
        }
        ccs.clearAllVerbalCues();
    }

    public static boolean categoryExists(ConvoidFacadeSource igf, String catName){
        ConvoidFacade cf = igf.getConvoidFacade();
		Category rootCat = cf.getRootCategory();
		Category targetCat = rootCat.findSubCategory(catName);
        return targetCat != null;
    }

    public static void playRemoteResponse(ConvoidFacadeSource igf){
        RemoteResponseFacade.getResponseBehavior().getBehavior().run(igf);
    }

    public static void setAgenda(ConvoidFacadeSource igf, String name){
		ConvoidCueSpace ccs = igf.getConvoidFacade().getCueSpace();
		ccs.clearAllCuesMatching(Agenda.class);
        AgendaManager am = igf.getConvoidFacade().getAgendaManager();
        if(am == null){
            return;
        }
        Agenda a = am.getAgenda(name);
        if(a == null){
            return;
        }
        a.setStrength(1.0);
        ccs.addCue(a);
    }

	public static void advanceAgendaTimerExpo(ConvoidFacadeSource igf,
				Agenda a, List<InitiativeCue> initiatives, List<SpeechJob> speechJobs) {
		ConvoidCueSpace ccs = igf.getConvoidFacade().getCueSpace();
		String agendaName = a.getName();
		List<String> types = new ArrayList();
		for(InitiativeCue ic : initiatives){
			types.add(ic.getType());
		}
		CursorRequest req  = ChatHelpFuncs.forcedRequest();
		req.setTypes(types);
		String required = a.getNextMeaning();
		if(required == null){
			theLogger.severe("AGENDA " + agendaName + " next meaning null, it should be empty");
			return;
		}
		theLogger.severe(required);
		req.getRequiredMeanings().add(required);
		req.getRequiredMeanings().add("AGENDA-PROMPT");
		BehaviorContext bc = ChatHelpFuncs.requestCursor(igf, req);
        bc.setCausingThalament(a);
		if(bc.isEmptyBehavior()){
			theLogger.severe("AGENDA " + agendaName + " behavior is empty");
			return;
		}
		if(speechJobs.size() > 0){
			bc.with(Detail.FROM_EXPO);
			for(SpeechJob sj : speechJobs){
				ChatHelpFuncs.pauseSpeechJob(igf, sj, false);
			}
		}
		purgeStepJobs(igf);
		theLogger.info("Inserting new  step for AGENDA " + agendaName);
		ccs.addThoughtCueForName("T_CLEAR_EXPECTATIONS", 1.0);
		BehaviorContext bcWithAgendaTimerPrompt = bc.with(PromptSource.TIMER).andPrompt("AGENDA");
        bcWithAgendaTimerPrompt.setCausingThalament(a);
		ccs.postFact(bcWithAgendaTimerPrompt);
	}
	public static void resumeAgendaTimer(ConvoidFacadeSource igf,
				Agenda a,	List<InitiativeCue> initiatives,
							List<SpeechJob> pausedSpeechJobs,
							List<TimerCue> timerCues) {
		ConvoidCueSpace ccs = igf.getConvoidFacade().getCueSpace();
		String agendaName = a.getName();
		List<String> types = new ArrayList();
		for(InitiativeCue ic : initiatives){
			types.add(ic.getType());
		}
		if(a.isEmpty()){
			theLogger.severe("AGENDA " + agendaName + " is empty.");
			return;
		}

		List<String> agendaMeanings = a.getMeanings();
		for(SpeechJob sj : pausedSpeechJobs){
			IConvoidCursor cc = sj.getCurrentCursor();
			if(cc == null){
				continue;
			}
			List<String> ms = cc.getMeanings();
			if(ms == null){
				continue;
			}
			for(String m : ms){
				if(!agendaMeanings.contains(m)){
					continue;
				}
				BehaviorContext bc = ChatHelpFuncs.getJobPlayerContext(igf, sj, PlayerAction.PLAY);
                bc.setCausingThalament(a);
				if(bc == null || bc.isEmptyBehavior()){
					sj.markCompleted();
					continue;
				}
				purgeStepJobs(igf);
				for(TimerCue tc : timerCues){
					ccs.clearCue(tc);
				}
				ccs.addThoughtCueForName("T_CLEAR_EXPECTATIONS", 1.0);
				BehaviorContext bcWithUserResumePrompt = bc.withPrompt("RESUME").and(PromptSource.USER);
				ccs.postFact(bcWithUserResumePrompt);
				return;
			}
		}
		theLogger.severe("AGENDA " + agendaName + " has no paused jobs to resume.");

		CursorRequest req = ChatHelpFuncs.forcedRequest();
		req.setTypes(types);
		String required = a.getNextMeaning();
		if(required == null){
			theLogger.severe("AGENDA " + agendaName + " next meaning null, it should be empty");
			return;
		}
		theLogger.info(required);
		req.getRequiredMeanings().add(required);
		req.getRequiredMeanings().add("AGENDA-PROMPT");
		BehaviorContext bc = ChatHelpFuncs.requestCursor(igf, req);
        bc.setCausingThalament(a);
		if(bc.isEmptyBehavior()){
			theLogger.severe("AGENDA " + agendaName + " behavior for meaning " + required + " is empty");
			return;
		}
		if(!bc.isEmptyBehavior()){
			purgeStepJobs(igf);
			for(TimerCue tc : timerCues){
				ccs.clearCue(tc);
			}
			ccs.addThoughtCueForName("T_CLEAR_EXPECTATIONS", 1.0);
			theLogger.info("Inserting new  step for AGENDA " + agendaName);
			BehaviorContext bcWithAgendaTimerPrompt = bc.with(PromptSource.TIMER).andPrompt("AGENDA");
            bcWithAgendaTimerPrompt.setCausingThalament(a);
			ccs.postFact(bcWithAgendaTimerPrompt);
		}		
	}
	private static void respondToSurpriseWithoutExpo(ConvoidFacadeSource igf,
			PhaseCue phaseCue, String meaningPrefix, String promptName, ThalamentStub cause) {
		ConvoidCueSpace ccs = igf.getConvoidFacade().getCueSpace();
		String meaning = meaningPrefix + phaseCue.getName();
		CursorRequest req = ChatHelpFuncs.forcedRequest();
		req.getTypes().add("PHASE");
		req.getRequiredMeanings().add(meaning);
		BehaviorContext bc = ChatHelpFuncs.requestCursor(igf, req);
        bc.setCausingThalament(cause);
		if(!bc.isEmptyBehavior()){
			purgeStepJobs(igf);
			BehaviorContext bcUserUnrecExpo = bc.with(PromptSource.USER).andPrompt(promptName);
			ccs.postFact(bcUserUnrecExpo);
		}
	}

	public static void respondToUnrecognizedTokenWithoutExpo(ConvoidFacadeSource igf,
			PhaseCue phaseCue, ThalamentStub cause) {
		respondToSurpriseWithoutExpo(igf, phaseCue, "PHASE_UNREC-", "UNRECOGNIZED", cause);
	}

	public static void respondToUnexpectedMeaningWithoutExpo(ConvoidFacadeSource igf,
			PhaseCue phaseCue, ThalamentStub cause) {
		respondToSurpriseWithoutExpo(igf, phaseCue, "PHASE_UNEXP-", "UNEXPECTED", cause);
	}	

	private static void respondToSurpriseDuringExpo(ConvoidFacadeSource igf,
				PhaseCue phaseCue, SpeechJob speechJob, String meaningPrefix,
				String promptName, ThalamentStub cause) {

		ConvoidCueSpace ccs = igf.getConvoidFacade().getCueSpace();
		String meaning = meaningPrefix + phaseCue.getName();
		CursorRequest req = ChatHelpFuncs.forcedRequest();
		req.getTypes().add("PHASE");
		req.getRequiredMeanings().add(meaning);
		BehaviorContext bc = ChatHelpFuncs.requestCursor(igf, req);
        bc.setCausingThalament(cause);
		if(bc.isEmptyBehavior()){
			return;
		}
		ChatHelpFuncs.pauseSpeechJob(igf, speechJob, true);
		BehaviorContext bcUserUnrecExpo = bc.with(PromptSource.USER).andPrompt(promptName).and(Detail.FROM_EXPO);
		ccs.postFact(bcUserUnrecExpo);
	}

	public static void respondToUnexpectedMeaningDuringExpo(ConvoidFacadeSource igf,
			PhaseCue phaseCue, SpeechJob speechJob, ThalamentStub cause) {

		respondToSurpriseDuringExpo(igf, phaseCue, speechJob, "PHASE_UNEXP-", "UNEXPECTED", cause);

	}
	public static void respondToUnrecognizedTokenDuringExpo(ConvoidFacadeSource igf,
			PhaseCue phaseCue, SpeechJob speechJob, ThalamentStub cause) {
		respondToSurpriseDuringExpo(igf, phaseCue, speechJob, "PHASE_UNREC-", "UNRECOGNIZED", cause);
	}

	public static void askQuestion(ConvoidFacadeSource igf, VerbalCue vc,
				SpeechJob optionalExpoSpeechJob) {
		ConvoidCueSpace ccs = igf.getConvoidFacade().getCueSpace();
		Map<String, Double> meaningScoreMap = vc.getMeanings();
		CursorRequest req = ChatHelpFuncs.forcedRequest();
		req.getTypes().add("QUESTION");
		req.setMeanings(meaningScoreMap);
		BehaviorContext bc = ChatHelpFuncs.requestCursor(igf, req);
        bc.setCausingThalament(vc);
		if(!bc.isEmptyBehavior()){
			if (optionalExpoSpeechJob != null) {
				ChatHelpFuncs.pauseSpeechJob(igf, optionalExpoSpeechJob, true);
			} else {
				purgeStepJobs(igf);
			}
			if(bc.getIntendedBehaviorType() == null){
				bc.withIntendedType("QUESTION");
			}
			BehaviorContext bcWithQuestionPrompt = bc.with(PromptSource.USER).andPrompt("QUESTION");
			if (optionalExpoSpeechJob != null) {
				bcWithQuestionPrompt = bcWithQuestionPrompt.and(Detail.FROM_EXPO);
			}
			ccs.postFact(bcWithQuestionPrompt);
			ccs.clearCue(vc);
		}
	}

	public static void chooseRandomResponse(ConvoidFacadeSource igf, VerbalCue vc,
					List<ResponseCue> responseCues, SpeechJob optionalExpoSpeechJob) {
		ConvoidCueSpace ccs = igf.getConvoidFacade().getCueSpace();

		List<String> responseTypes = new ArrayList();
		/*
		for(Object obj : $responses){
			types.add(((ResponseCue)obj).getType());
		}
		 */
		for (ResponseCue rc : responseCues) {
			String rcType = rc.getType();
			responseTypes.add(rcType);
		}
		purgeStepJobs(igf);
		ccs.clearCue(vc);
		CursorRequest req = ChatHelpFuncs.randomRequest();
		req.setTypes(responseTypes);
		Map<String, Double> meaningScoreMap = vc.getMeanings();
		req.setMeanings(meaningScoreMap);
		BehaviorContext bc = ChatHelpFuncs.requestCursor(igf, req);
        bc.setCausingThalament(vc);
		if(!bc.isEmptyBehavior()){
			theLogger.info("Saying RANDOM, found " + bc);
			purgeStepJobs(igf);
			BehaviorContext bcWithPrompt = bc.with(PromptSource.USER);
			if (optionalExpoSpeechJob != null) {
				ChatHelpFuncs.pauseSpeechJob(igf, optionalExpoSpeechJob, true);
				bcWithPrompt = bcWithPrompt.with(Detail.FROM_EXPO);
			}
			ccs.postFact(bcWithPrompt);
		}
	}
}

