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

import org.cogchar.api.convoid.act.Step;
import org.cogchar.api.convoid.cue.ConvoidCueSpace;
import org.cogchar.zzz.platform.stub.ChainJob;
import org.cogchar.zzz.platform.stub.JobConfig;
import org.cogchar.zzz.platform.stub.JobStub;
import org.cogchar.zzz.platform.stub.ThalamentStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class ConversationJob extends JobStub {
	private static final Logger theLogger = LoggerFactory.getLogger(ConversationJob.class);
	private transient ChainJob myOpenChainJob;

	private transient ConvoidJobSpace myJobSpace;

	public ConversationJob(ConvoidJobSpace jobSpace) {
		myJobSpace = jobSpace;
	}

	public void openChainJob() {
		if (myOpenChainJob == null) {
			myOpenChainJob = new ChainJob();
		} else {
			theLogger.trace("A ChainJob is already open - ignoring request to open one");
		}
	}

	public void startOpenChainJob() {
		if (myOpenChainJob != null) {
			myOpenChainJob.scheduleToStartNow();
			myOpenChainJob.click();
			myOpenChainJob = null;
		} else {
			theLogger.trace("No ChainJob is open - ignoring request to start");
		}
	}

	public synchronized StepJob appendJobForStep(Step s, JobConfig jobConfig,
												 String stepExecFactoryClassName, ThalamentStub cause) {
		StepJob sj = StepJob.build(s, jobConfig, stepExecFactoryClassName);
		sj.setCausingThalament(cause);
		if (sj != null) {
			myJobSpace.postManualJob(sj);
			if (myOpenChainJob == null) {
				openChainJob();
			}
			myOpenChainJob.appendJob(sj);
		}
		return sj;
	}

	protected StepJob appendJobForStep(Step s, ThalamentStub cause) {
		JobConfig jobConfig = myJobSpace.getJobConfig();
		return appendJobForStep(s, jobConfig, null, cause);
	}

	public synchronized StepJob createDynamicSpeechStepAndRunJob(String stepXML, ThalamentStub cause) {
		// Currently we do not do the switch to "speaking vocab" stuff here.
		Step dynamicStep = new Step();
		dynamicStep.setType(Step.ST_SAPI5_LITERAL);
		dynamicStep.setText(stepXML);
		StepJob dsj = appendJobForStep(dynamicStep, cause);
		if (dsj != null) {
			dsj.scheduleToStartNow();
			startOpenChainJob();
		}
		return dsj;
	}

	public synchronized StepJob playSingleStep(ConvoidCueSpace ccs, Step step, ThalamentStub cause) {
		theLogger.info("Playing Step: " + step.getText());
		StepJob sj = appendJobForStep(step, cause);
		if (sj != null) {
			startOpenChainJob();
		}
		return sj;
	}
}
