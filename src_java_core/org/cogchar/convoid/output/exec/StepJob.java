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

package org.cogchar.convoid.output.exec;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.cogchar.convoid.output.config.Step;
import java.util.Map;
import org.cogchar.platform.stub.JobStub;
import org.cogchar.platform.stub.JobStub.Status;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class StepJob extends JobStub implements StepProgressListener {
	private static Logger	theLogger = Logger.getLogger(StepJob.class.getName());
	static {
		theLogger.setLevel(Level.ALL);
	}	
	private		transient StepExecution	myStepExecution;
	
	public StepJob(StepExecution stex) {
		super();
		myStepExecution = stex;
		myStepExecution.registerProgressListener(this);
	}

    public Step getStep(){
        if(myStepExecution == null){
            return null;
        }
        return myStepExecution.getStep();
    }
    @Override
	protected void start() {
		myStepExecution.start();
		// expect STARTED event to trigger our status change
	}
    @Override
	protected void abort() {
		setStatus(Status.ABORTING);	
		myStepExecution.stop();
		// expect a STOPPED event to change our status to ABORTED
	}
	public void handleStepProgress(StepExecution sexec) {
		StepExecution.Status sexStatus = sexec.checkStatus();
		switch(sexStatus) {
		case STARTED:
			setStatus(Status.RUNNING);
		break;
		case FINISHED:
            theLogger.info("StepJob finished: " + sexec.hashCode());
			setStatus(Status.COMPLETED);
		break;
		case STOPPED:
            theLogger.info("StepJob stopped: " + sexec.hashCode());
			setStatus(Status.ABORTED);
		break;
		}
	}
	public static StepJob build(Step s, Map<String, String> configMap, String stepExecFactoryClassName) {
		StepJob	stepJob = null;
		StepExecution sexec = StepExecution.makeStepExecution(s, configMap, stepExecFactoryClassName);
		if (sexec != null) {
			stepJob = new StepJob (sexec); 
			stepJob.myConfigMap = configMap;
		} else {
			theLogger.warning("Can't construct stepJob for step: " + s);
		}
		return stepJob; 
	}
    
    @Override
	public String getTypeString() {
		String stepType = "UNKNOWN_STEP_TYPE";
		if (myStepExecution != null) {
			Step s = myStepExecution.getStep();
			if (s != null) {
				stepType = s.getType();
			}
		}
		return "StepJob[type=" + stepType + "]";
	}
	@Override public String getContentSummaryString() {
		// TODO: get step's position in category hierarchy
		String result = "NULL";
		String stepText = "NO_STEP_TEXT";
		if (myStepExecution != null) {
			Step s = myStepExecution.getStep();
			if (s != null) {
				stepText = s.getText();
			}		
		}
		if (stepText != null) {
			int length = stepText.length();
			if (length > 50) {
				length = 50;
			}
			result = stepText.substring(0, length);
		}
		return result;
	}
}
