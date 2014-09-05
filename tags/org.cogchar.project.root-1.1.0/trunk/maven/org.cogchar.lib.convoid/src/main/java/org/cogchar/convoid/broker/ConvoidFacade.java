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

import org.cogchar.xploder.cursors.CursorFactory;
import org.cogchar.xploder.cursors.MeaningCursorMap;
import org.cogchar.api.convoid.act.Category;
import org.cogchar.api.convoid.act.Step;
import org.cogchar.convoid.job.ConversationJob;
import org.cogchar.convoid.job.SpeechJob;
import org.cogchar.convoid.job.ConvoidJobSpace;
import org.cogchar.xploder.mgr.CursorManager;
import org.cogchar.xploder.cursors.IConvoidCursor;
import org.cogchar.convoid.job.AgendaManager;
import org.cogchar.convoid.job.StepJob;
import java.util.logging.Logger;
import org.cogchar.api.convoid.cue.ConvoidCueSpace;
import org.cogchar.zzz.platform.stub.JobStub;
import org.cogchar.zzz.platform.stub.ThalamentStub;
import org.cogchar.platform.util.TimeUtils;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class ConvoidFacade implements ConvoidFacadeSource {
	private static Logger theLogger = Logger.getLogger("com.hansonrobotics.convoid.broker.ConvoidFacade");
    private static IRemoteResponseInterface	theRemoteResponseIterface;

	private			Category				myRootCategory;
	private			ConvoidCueSpace			myCueSpace;
	private			ConvoidJobSpace			myJobSpace;
	private			ConversationJob			myMainConvJob;
    private         MeaningCursorMap        myResponseMap;
	private			CursorManager           myCursorManager;
    private         AgendaManager           myAgendaMangaer;

	public ConvoidFacade(ConvoidCueSpace ccs, ConvoidJobSpace cjs, Category rootCat,
            IRemoteResponseInterface remote, AgendaManager am) {
		myCueSpace = ccs;
		myJobSpace = cjs;
		myRootCategory = rootCat;
		myMainConvJob = new ConversationJob(myJobSpace);
        myResponseMap = CursorFactory.buildTransitionMap(rootCat.findSubCategory("Transitions"));
        theRemoteResponseIterface = remote;
		myCursorManager = new CursorManager(rootCat);
        myAgendaMangaer = am;
        theLogger.info("agendaManager=" + am);
	}
	@Override
    public ConvoidFacade getConvoidFacade() {
		return this;
	}
	public Category getRootCategory() {
		return myRootCategory;
	}
	public ConversationJob getMainConversationJob() {
		return myMainConvJob;
	}
	public void purgeStepJobs() {
		// Old comment (circa June 2008)
		// This is dangerous when jobs are scheduled to set things back to
		// normal - e.g. to clear S_SPEAKING, to set vocab back to LISTENING...
		// New comment (Oct 2008) BUT - those vocab switch jobs aren't
		// step jobs, and the chain job parent should still complete.
		// So...do we have a problem or not?
		purgeJobs(StepJob.class);
	}
	private void purgeJobs(Class clazz) {
		myJobSpace.terminateAndClearJobsInClass(clazz);
		/*
		// I had refactored this out to make a purgeExpositionJobs, but
		// it was no longer needed after adding purgeJob below.
		// It may be useful in the future. - MattS (8/2009)
		List<Job>	jobListCopy = myJobSpace.getJobListCopy();
		for (Job j : jobListCopy) {
			if (clazz.isInstance(j)) {
				j.requestCancelOrAbort();
				myJobSpace.clearJob(j);
			}
		}
		 */
	}
	public void purgeJob(JobStub j){
		myJobSpace.terminateAndClearJob(j);
	}
	public ConvoidCueSpace getCueSpace() {
		return myCueSpace;
	}
	public ConvoidJobSpace getJobSpace() {
		return myJobSpace;
	}
	
	public void setLastPlayed(SpeechJob job){
		getCursorManager().setLastPlayed(job);
	}
    public String getRemoteResponse(){
		if(theRemoteResponseIterface == null){
			return "";
		}
        return theRemoteResponseIterface.getResponse();
    }

    public StepJob playResponse(ConvoidCueSpace ccs, String meaning, ThalamentStub cause){
        IConvoidCursor cursor = myResponseMap.getCursor(meaning);
        if(cursor == null){
            return null;
        }
        long time = TimeUtils.currentTimeMillis();
        if(!cursor.isPlayableAtTime(time)){
            cursor.resetAtTime(time);
        }
        Step step = cursor.getBestStepAtTime(time);
        theLogger.severe("Playing Response: " + step.getText());
        return myMainConvJob.playSingleStep(ccs, step, cause);
    }

    public MeaningCursorMap getResponseMap(){
        return myResponseMap;
    }

	public CursorManager getCursorManager(){
		return myCursorManager;
	}

    public AgendaManager getAgendaManager(){
        return myAgendaMangaer;
    }
}
