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

package org.cogchar.integroid.jmxwrap;


import org.cogchar.convoid.broker.ConvoidHelpFuncs;
import org.cogchar.convoid.job.StepJob;
import org.cogchar.integroid.broker.IntegroidFacade;
import org.cogchar.integroid.broker.IntegroidHelpFuncs;
import org.cogchar.zzz.platform.stub.CueListener;
import org.cogchar.zzz.platform.stub.CueStub;
import org.cogchar.zzz.platform.stub.JobListener;
import org.cogchar.zzz.platform.stub.JobStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import javax.management.ObjectName;

/**
 * @author Stu Baurmann
 */
public class IntegroidWrapper extends NotifyingBeanImpl
		implements IntegroidWrapperMXBean, JobListener, CueListener {
	private static final Logger theLogger = LoggerFactory.getLogger(IntegroidWrapper.class);

	IntegroidFacade myIGF;

	public IntegroidWrapper(IntegroidFacade igf, ObjectName on) {
		super(on);
		myIGF = igf;
/*
 * 		CueSpace cs = ss.getCueSpace();
		cs.addCueListener(this);
		
		JobSpace js = ss.getJobSpace();
		js.addJobListener(this);
 * 
 */
	}

	public static IntegroidWrapper createAndRegister(IntegroidFacade igf) throws Throwable {
		ObjectName on = new ObjectName(IntegroidWrapperMXBean.INTEGROID_JMX_OBJNAME);
		IntegroidWrapper w = new IntegroidWrapper(igf, on);
		w.register();
		return w;
	}

	public void postVerbalCue(Map<String, Double> meanings, double strength) {
		theLogger.info("postVerbalCue: " + meanings);
		ConvoidHelpFuncs.addVerbalCue(myIGF, meanings, strength);
		myIGF.processWhenSafe();
	}

	public void postThoughtCue(String thoughtName, double strength) {
		theLogger.info("postThoughtCue: " + thoughtName);
		IntegroidHelpFuncs.addThoughtCueForName(myIGF, thoughtName, strength);
		myIGF.processWhenSafe();
	}

	public void postTextCue(String channel, String textData, double strength) {
		theLogger.info("postTextCue: channel=" + channel + ", textData=" + textData);
		IntegroidHelpFuncs.addTextCue(myIGF, channel, textData, strength);
		myIGF.processWhenSafe();
	}

	public void postVariableCue(String name, String value, double strength) {
		theLogger.info("postVariableCue: varName=" + name + ", value=" + value);
		IntegroidHelpFuncs.setVariable(myIGF, name, value, strength);
		myIGF.processWhenSafe();
	}

	public void postHeardCue(String text) {
		theLogger.info("postHeardCue: " + text);
		ConvoidHelpFuncs.addHeardCue(myIGF, text);
	}

	public void notifyCuePosted(CueStub c) {
		theLogger.debug("notifyCuePosted: " + c);

		// Including the cue object in the message will prevent JConsole from
		// displaying this notification, unless JConsole has the cue class graph
		// on its classpath.
		sendAttributeChangeNotification(
				"CuePosted=" + c.toString(), ATTRIB_CUE_POSTED, null, c);
	}

	public void notifyCueUpdated(CueStub c) {
		theLogger.debug("notifyCueUpdated: " + c);
		sendAttributeChangeNotification(
				"CueUpdated=" + c.toString(), ATTRIB_CUE_UPDATED, null, c);
	}

	public void notifyCueCleared(CueStub c) {
		theLogger.debug("notifyCueCleared: " + c);
		sendAttributeChangeNotification(
				"CueCleared=" + c.toString(), ATTRIB_CUE_CLEARED, null, c);
	}

	public void notifyJobPosted(JobStub j) {
		theLogger.info("DEBUG - notifyJobPosted, job=" + j.toString());
		theLogger.debug("notifyJobPosted: " + j);
		// Including the job object in the message will prevent JConsole from
		// displaying this notification, unless JConsole has the job class graph
		// on its classpath.
		// Also, Convoid StepJobs seem to be non-serializable for some other reason
		// (related to the SAPI block?)
		Object noticeObj = mapJobNoticeObject(j);
		sendAttributeChangeNotification(
				"JobPosted=" + j.toString(), ATTRIB_JOB_POSTED, null, noticeObj); //  j);
	}

	public void notifyJobCleared(JobStub j) {
		theLogger.info("DEBUG - notifyJobCleared, job=" + j.toString());
		theLogger.debug("notifyJobCleared: " + j);
		Object noticeObj = mapJobNoticeObject(j);
		sendAttributeChangeNotification(
				"JobCleared=" + j.toString(), ATTRIB_JOB_CLEARED, null, noticeObj);
	}

	public Object mapJobNoticeObject(JobStub j) {
		if (j instanceof StepJob) {
			return "StepJob is not serializable?!";
		} else {
			return j;
		}
	}
}
