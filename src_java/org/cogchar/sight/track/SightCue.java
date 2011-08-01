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

package org.cogchar.sight.track;


import org.cogchar.platform.stub.CueStub;
import org.cogchar.platform.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class SightCue extends CueStub {
	private static Logger	theLogger = LoggerFactory.getLogger(SightCue.class.getName());

	private static Integer	theNextSessionCueID = 1;
	// Immutable and always present - set by constructor
	private		Integer					mySessionCueID;
	private		SightAttentionStatus	myAttentionStatus;
	private		Long					myASChangeStampMsec;

	private		Long					myAttentionStartStampMsec;
	private		Long					myAttentionStopStampMsec;
	private		Long					myAttentionConfirmStampMsec;

	protected SightCue() {
		super();
		mySessionCueID = takeNextSightCueSessionID();
		setAttentionStatus(SightAttentionStatus.IGNORED);
	}
	private static synchronized Integer takeNextSightCueSessionID() {
		return theNextSessionCueID++;
	}
	/* Not an MX-Bean property, has no setter */
	public Integer fetchSessionCueID() {
		return mySessionCueID;
	}
	public SightAttentionStatus getAttentionStatus() {
		return myAttentionStatus;
	}

	@Override public String getContentSummaryString() {
		return "sessCueID=" + fetchSessionCueID()
				+ ", attentionStatus[age]=" + getAttentionStatus()
				+ "[" + getAttentionStatusAgeSec()
				+ "], startAge/stopAge/confirmAge="
						+ getAttentionStartAgeSec() + ", "
						+ getAttentionStopAgeSec()  + ", "
						+ getAttentionConfirmAgeSec();
			
	}
	public void notifyAttentionStarted() {
		theLogger.info("Attention start for sight cue with id=" + fetchSessionCueID() + " and class " + getClass().getSimpleName());
		myAttentionStartStampMsec = TimeUtils.currentTimeMillis();
		this.markUpdatedNow();
	}
	public void notifyAttentionStopped() {
		theLogger.info("Attention stop for sight cue with id=" + fetchSessionCueID() + " and class " + getClass().getSimpleName());
		myAttentionStopStampMsec = TimeUtils.currentTimeMillis();
		this.markUpdatedNow();
	}
	public void notifyAttentionConfirmed() {
		theLogger.info("Attention confirmed for sight cue with id=" + fetchSessionCueID() + " and class " + getClass().getSimpleName());
		myAttentionConfirmStampMsec = TimeUtils.currentTimeMillis();
		this.markUpdatedNow();
	}
	public void setAttentionStatus(SightAttentionStatus stat) {
		myAttentionStatus = stat;
		myASChangeStampMsec = TimeUtils.currentTimeMillis();
		this.markUpdatedNow();
	}
	
	public Double getAttentionStartSec() {
		return TimeUtils.msecStampObjToSecAgeObj(myAttentionStartStampMsec);
	}
	public Double getAttentionStopSec() {
		return TimeUtils.msecStampObjToSecAgeObj(myAttentionStopStampMsec);
	}
	public Double getAttentionConfirmSec() {
		return TimeUtils.msecStampObjToSecAgeObj(myAttentionConfirmStampMsec);
	}
	 
	public Double getAttentionStartAgeSec() {
		return TimeUtils.msecStampObjToSecAgeObj(myAttentionStartStampMsec);
	}
	public Double getAttentionStopAgeSec() {
		return TimeUtils.msecStampObjToSecAgeObj(myAttentionStopStampMsec);
	}
	public Double getAttentionConfirmAgeSec() {
		return TimeUtils.msecStampObjToSecAgeObj(myAttentionConfirmStampMsec);
	}

	public Double getAttentionStatusAgeSec() {
		return TimeUtils.msecStampObjToSecAgeObj(myASChangeStampMsec);
	}
}
