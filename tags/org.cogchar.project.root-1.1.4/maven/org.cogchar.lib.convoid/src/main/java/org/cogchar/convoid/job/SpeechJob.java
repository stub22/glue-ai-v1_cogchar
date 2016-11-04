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

package org.cogchar.convoid.job;

import org.cogchar.xploder.cursors.IConvoidCursor;
import org.cogchar.xploder.cursors.CategoryCursor;
import org.cogchar.api.convoid.act.Category;
import org.cogchar.api.convoid.act.Step;
import java.util.List;
import java.util.Random;
import org.cogchar.convoid.broker.ConvoidFacade;
import org.cogchar.zzz.platform.stub.JobStub;
import org.cogchar.platform.util.TimeUtils;

/**
 * @author Stu B. <www.texpedient.com>
 */
public abstract class SpeechJob extends ConvoidJob {
    private static Random       theRandomizer = new Random();

	private		transient	CategoryCursor	myCategoryCursor;
    private     transient	IConvoidCursor  myCurrentCursor;
	private		transient	StepJob			myLastStepJob;
	private		Long			myLastPauseStamp, myLastResumeStamp;
    private     Integer         myInterruptScore;
    private     Integer         myInterruptThreshold;
    private     boolean         canSelfResume;

	public SpeechJob(CategoryCursor cursor) {
		myCategoryCursor = cursor;
        myInterruptScore = 0;
        myInterruptThreshold = theRandomizer.nextInt(3) + 3;
        canSelfResume = true;
        myCurrentCursor = cursor;
	}

	public StepJob getLastStepJob() {
		return myLastStepJob;
	}

	public synchronized void markPaused() {
		myLastPauseStamp = TimeUtils.currentTimeMillis();
		setStatus(JobStub.Status.PAUSED);
	}
	public synchronized void markCanceled() {
		myLastPauseStamp = TimeUtils.currentTimeMillis();
		setStatus(JobStub.Status.CANCELED);
	}
	public synchronized void markResumed() {
		myLastResumeStamp = TimeUtils.currentTimeMillis();
		setStatus(JobStub.Status.RUNNING);
	}
	public synchronized void markCompleted() {
		setStatus(JobStub.Status.COMPLETED);
	}

	public Long getLastPauseStampMsec() {
		return myLastPauseStamp;
	}

	public Double getLastPauseStampSec() {
		if (myLastPauseStamp != null) {
			return myLastPauseStamp/1000.0;
		} else {
			return null;
		}
	}

	public Long getLastResumeStampMsec() {
		return myLastResumeStamp;
	}

	public Double getLastResumeStampSec() {
		if (myLastResumeStamp != null) {
			return myLastResumeStamp/1000.0;
		} else {
			return null;
		}
	}

	public String getCategoryName(){
		return myCategoryCursor.getName();
	}

	public Category getCategory(){
		return myCategoryCursor.getCategory();
	}

    public CategoryCursor getCategoryCursor(){
        return myCategoryCursor;
    }

	@Override
    public String getContentSummaryString() {
		String lastStepDesc = "NULL";
		if (myLastStepJob != null) {
			lastStepDesc = myLastStepJob.getContentSummaryString();
		}
		return "(" + myInterruptScore + ", " + myInterruptThreshold + ") topic=" + getCategoryName() + ", canResume= " +
                canSelfResume + ", lastPause=" + myLastPauseStamp
				+ ", lastResume=" + myLastResumeStamp + ", lastStep=" + lastStepDesc;
	}

    public boolean startNextStepAtTime(ConvoidFacade cf, long time){
		return startNextStepAtTimeForCursor(cf, myCategoryCursor, time);
    }

    public boolean startNextStepAtTimeForCursor(ConvoidFacade cf, IConvoidCursor cursor, long time){
        if(!cf.getJobSpace().getJobList().contains(this)){
            cf.getJobSpace().postManualJob(this);
        }
		markResumed();
		ConvoidJobSpace cjs = cf.getJobSpace();
		ConversationJob cj = cf.getMainConversationJob();
        Step step = cursor.getBestStepAtTime(time);
        if(step == null){
            return false;
        }
		StepJob sj = cj.playSingleStep(cf.getCueSpace(), step, this);
		if (myLastStepJob != null) {
			cjs.clearJob(myLastStepJob);
		}
		myLastStepJob = sj;
		if(!myCategoryCursor.isFinishedAtTime(time)){
			this.markUpdatedNow();
		}
        return true;
    }

    public void playStepAtTime(ConvoidFacade cf, Step step, long time){
        if(step == null){
            throw new IllegalArgumentException("Step must not be null");
        }
        if(!cf.getJobSpace().getJobList().contains(this)){
            cf.getJobSpace().postManualJob(this);
        }
        if(getStatus() != JobStub.Status.RUNNING){
            markResumed();
        }
		ConvoidJobSpace cjs = cf.getJobSpace();
		ConversationJob cj = cf.getMainConversationJob();
		StepJob sj = cj.playSingleStep(cf.getCueSpace(), step, this);
		if (myLastStepJob != null) {
			cjs.clearJob(myLastStepJob);
		}
		myLastStepJob = sj;
		if(!myCategoryCursor.isFinishedAtTime(time)){
			this.markUpdatedNow();
		}
    }

    public boolean isCurrentActFinished(){
        if(myCategoryCursor == null){
            return true;
        }
        return myCategoryCursor.isCurrentActFinishedAtTime(TimeUtils.currentTimeMillis());
    }

    public boolean isCurrentSequenceFinished(){
        if(myCategoryCursor == null){
            return true;
        }
        return myCategoryCursor.isCurrentActEndOfSequence() && isCurrentActFinished();
    }

    public boolean isFinished(){
        return myCategoryCursor.isFinishedAtTime(TimeUtils.currentTimeMillis());
    }

    public boolean getCanSelfResume(){
        return canSelfResume;
    }
    public void setCanSelfResume(boolean resume){
        canSelfResume = resume;
    }
    public Integer getInterruptScore(){
        return myInterruptScore;
    }
    public void setInterruptScore(Integer score){
        myInterruptScore = score;
        if(myInterruptScore <= 0){
            myInterruptScore = 0;
        }
    }
    public void addInterrupt(Integer score){
        setInterruptScore(getInterruptScore() + score);
    }
    public Integer getInterruptKillThreshold(){
        return myInterruptThreshold;
    }
    public void setInterruptKillThreshold(Integer thresh){
        myInterruptThreshold = thresh;
    }
    public boolean isDead(){
        return myInterruptScore >= myInterruptThreshold || getStatus() == JobStub.Status.CANCELED;
    }
    public boolean getIsPlayable(){
        return myCategoryCursor.isPlayableAtTime(TimeUtils.currentTimeMillis());
    }

	public boolean containsActiveMeaning(List<String> meanings){
		List active = myCategoryCursor.getActiveMeanings();
		for(String m : meanings){
			if(active.contains(m)){
				return true;
			}
		}
		return false;
	}

    public IConvoidCursor getCurrentCursor() {
        return myCurrentCursor;
    }

    public void setCurrentCursor(IConvoidCursor myCurrentCursor) {
        this.myCurrentCursor = myCurrentCursor;
    }

}
