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

package org.cogchar.api.integroid.cue;

// import org.cogchar.convoid.output.exec.ExpositionJob;

import org.cogchar.platform.stub.CueStub;
import org.cogchar.platform.util.TimeUtils;
import org.cogchar.api.sight.SightCue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class AwarenessCue extends CueStub {
	private static Logger	theLogger = LoggerFactory.getLogger(AwarenessCue.class.getName());

	public enum FocusKind {
		NONE,
		PRIMARY,
		FIXATION,
		GLANCE
	}
	/*
	public enum FixateMode {
		PRIMARY_PERSON,
		OTHER_PERSON,
		LOOK_FOR_FACES,  // may be implemented by animation or program
		TRACK_MOTION,	// e.g. attentively watching a bug fly around
		NONE			// inattentive, gesturally controlled, other
	}
	public enum GlanceMode {
		FIXATE,				// Not glancing - see the fixate mode
		GLANCE_NEW_FACE,	// currently glancing at face, soon to return to fixate
		GLANCE_MOTION,		// currently glancing at motion, soon to return to fixate
		COGNITIVE_SCAN		// currently scanning "in thought", soon to return to fixate
	}
	*/
	private		PersonCue		myPrimaryPerson;
	private		Long			myPrimaryStampMssec;

	// Fixation is what we are paying attention to "mainly, for the moment", and
	// is not disturbed by "glances".
	private		PersonCue		myFixationPerson;
	
	private		Long			myFixationStampMsec;

	private		SightCue		myGlanceSight;

	private		Long			myGlanceStampMsec;

	private		String			myGazeStrategyName;
	private		Long			myGazeStrategyStampMsec;

	/*
	// GlanceSightNumber is what we are trying to look at right this very millisec.
	private		Integer			myGlanceSightNumber;
	private		Long			myGlanceUpdateStampMillisec;
	*/
	private		Object			myActiveExposition;
	// ExpositionJob	myActiveExposition;
	
	public PersonCue getPrimaryPerson() { 
		return myPrimaryPerson;
	}
	public void setPrimaryPerson(PersonCue pc) { 
		myPrimaryPerson = pc;
		myPrimaryStampMssec = TimeUtils.currentTimeMillis();
		this.markUpdatedNow();
	}
	public PersonCue getFixationPerson() {
		return myFixationPerson;
	}
	public void setFixationPerson(PersonCue pc) {
		myFixationPerson = pc;
		myFixationStampMsec = TimeUtils.currentTimeMillis();
		this.markUpdatedNow();
	}
	public SightCue getGlanceSight() {
		return myGlanceSight;
	}
	public void setGlanceSight(SightCue pc) {
		myGlanceSight = pc;
		myGlanceStampMsec = TimeUtils.currentTimeMillis();
		this.markUpdatedNow();
	}
	public String getGazeStrategyName() {
		return myGazeStrategyName;
	}
	public void setGazeStrategyName(String gsn)  {
		myGazeStrategyName = gsn;
		myGazeStrategyStampMsec = TimeUtils.currentTimeMillis();
	}
	public String formatCueAndStamp(PersonCue pc, Long stamp) {
		String formatted = "NONE";
		if (pc != null) {
			formatted = "[stamp=" + stamp + ", cue=" + pc.toString() + "]";
		}
		return formatted;
	}
	@Override public String getContentSummaryString() {
		return "fixPerson=" + formatCueAndStamp(myFixationPerson, myFixationStampMsec)
				+ ", primaryPerson=" + formatCueAndStamp(myPrimaryPerson, myPrimaryStampMssec);
	}
	public Object getActiveExposition() {
		return myActiveExposition;
	}
	public void setActiveExposition(Object ej) {
		myActiveExposition = ej;
	}
	public boolean isFocusOnPerson(PersonCue pc) {
		return		(getPrimaryPerson() == pc)
				||  (getFixationPerson() == pc)
				||  (getGlanceSight() == pc);
	}
	public void transferFocus(PersonCue prevFocusCue, PersonCue nextFocusCue) {
		if (getPrimaryPerson() == prevFocusCue) {
			theLogger.info("Transferring primary focus from " + prevFocusCue + " to " + nextFocusCue);
			setPrimaryPerson(nextFocusCue);
		}
		if (getFixationPerson() == prevFocusCue) {
			theLogger.info("Transferring fixation focus from " + prevFocusCue + " to " + nextFocusCue);
			setFixationPerson(nextFocusCue);
		}
		if (getGlanceSight() == prevFocusCue) {
			theLogger.info("Transferring glance focus from " + prevFocusCue + " to " + nextFocusCue);
			setGlanceSight(nextFocusCue);
		}
	}
}
