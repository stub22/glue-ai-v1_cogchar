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
package org.cogchar.integroid.cue;


import org.cogchar.sight.track.SightCue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public abstract class PersonCue extends SightCue  {

	private static Logger	theLogger = LoggerFactory.getLogger(PersonCue.class.getName());
	public enum NameSource {
		NONE,
		HEARD,
		REMEMBERED
	}
	/*
	public enum Status {
		PRESENT,
		REMEMBERED
	}
	 */


	private		String					mySpokenName;
	private		NameSource				myNameSource = NameSource.NONE;
	// How many times have we greeted this person by name in this session?
	private		Integer					myPersonalGreetingCount = 0;
	// How many times have we asked this person's name in this session?
	private		Integer					myNameInquiryCount = 0;
	private		Boolean					myNameInquiryNeeded = false;
	// private		Status					myStatus;
	
	// Abstract placeholders
	private		Object					myFaceHistory;	
	private		Object					myGazeHistory;
	private		Object					myConvHistory;



	protected PersonCue() {
		super();
	}

	public String getSpokenName() { 
		return mySpokenName;
	}
	public NameSource getNameSource() { 
		return myNameSource;
	}	
	public void setSpokenNameAndSource(String name, NameSource ns) {
		mySpokenName = name;
		myNameSource = ns;
		this.markUpdatedNow();	
	}
	public Integer getPersonalGreetingCount() { 
		return myPersonalGreetingCount;
	}
	public void incrementPersonalGreetingCount() { 
		myPersonalGreetingCount++;
		this.markUpdatedNow();
	}
	public Integer getNameInquiryCount() { 
		return myNameInquiryCount;
	}
	public void incrementNameInquiryCount() { 
		myNameInquiryCount++;
		this.markUpdatedNow();
	}	
	public Boolean getNameInquiryNeeded() {
		return myNameInquiryNeeded;
	}
	public void setNameInquiryNeeded(Boolean needed) {
		myNameInquiryNeeded = needed;
		this.markUpdatedNow();
	}
	public void consume(PersonCue pc){
		// Can be used to pull a recently learned name from a BogeyCue into a FriendCue.
		String name = pc.getSpokenName();
		NameSource ns = pc.getNameSource();
		if(name != null && ns != null){
			if (getSpokenName() == null) {
				theLogger.info("Adopting name from consumed cue: " + name);
				setSpokenNameAndSource(name, ns);
			} else {
				theLogger.info("Ignoring name from consumed cue ('" + name + "') because I am already named: " + mySpokenName);
			}
		}
		// TODO:  Absorb greeting/inquiry counts?
	}
	public Double getAttentionEligibilityScore(double peakEligibilityStrength) {
		double strength = getStrength();
		double score = 0.0;
		// At max strength 1.0, eligibility is 1-peakEligStrength.
		// Eligibility rises linearly as strength falls, reaches a peak at 1.0,
		// and thereafter falls proportionally, reaching 0 as strength falls to 0.
		if (strength > peakEligibilityStrength) {
			score = 1.0 - (strength - peakEligibilityStrength);
		} else {
			score = strength / peakEligibilityStrength;
		}
		return score;
	}
	@Override public String getContentSummaryString() {
		return super.getContentSummaryString()
				+ ", name[src]=" + mySpokenName	+ "[" + myNameSource + "]"
				+ ", greetingCnt=" + myPersonalGreetingCount
				+ ", inquiryCnt=" + myNameInquiryCount
				+ ", inquiryNeeded=" + myNameInquiryNeeded;
	}

	public void setOrConfirmPermPersonID(String permID, Long confirmedObsStamp) {
		// Safeguard:   We want exception to be thrown unless the method is overridden.
		throw new UnsupportedOperationException("Cannot set permID=" + permID + " for non-Friend: " + this);
	}
	/**** Stub versions of methods that have moved to FriendCue ***/
	public String getPermPersonID() {
		return null;
	}
	public Long getPermPersonConfirmStamp() {
		return null;
	}
	public Double getPermPersonConfirmAgeSec() {
		return null;
	}
/*

	public Status getStatus() {
		return myStatus;
	}

	public void setStatus(Status myStatus) {
		this.myStatus = myStatus;
	}
 */

}
