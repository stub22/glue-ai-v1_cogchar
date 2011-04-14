/**
 * Copyright 2008-9 Hanson Robotics Inc.
 * All Rights Reserved.
 * 
 * Cues start with no explicit constructors.
 */

package org.cogchar.platform.stub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author Stu Baurmann
 */
public abstract class CueStub extends ThalamentStub {
	private static Logger	theLogger = LoggerFactory.getLogger(CueStub.class.getName());
	
	/**
	 * 
	 */
	public static final String PROP_STRENGTH				= "strength";
	/**
	 * 
	 */
	public static final String PROP_EXPIRY					= "scheduledExpireTimeMsec";
	
	private		Double		myStrength;
	private		Long		myScheduledExpireTimeMsec;
	
	/**
	 * 
	 * @return
	 */
	public Double getStrength() {
		return myStrength;
	}
	/**
	 * 
	 * @param s
	 */
	public void setStrength(double s) {
		Double oldS = myStrength;
		myStrength = s;
		markUpdatedNow();
		// safelyFirePropertyChange(PROP_STRENGTH, oldS, s);
	}
	/**
	 * 
	 * @return
	 */
	public Long getScheduledExpireTimeMsec() {
		return myScheduledExpireTimeMsec;
	}
	/**
	 * 
	 * @param etm
	 */
	public void setScheduledExpireTimeMsec(Long etm) {
		Long oldETM = myScheduledExpireTimeMsec;
		myScheduledExpireTimeMsec = etm;
		markUpdated();
		// safelyFirePropertyChange(PROP_EXPIRY, oldETM, myScheduledExpireTimeMsec);
	}
		
	/**
	 * 
	 * @return
	 */
	public Double getScheduledExpireTimeSec() {
		if (myScheduledExpireTimeMsec != null) {
			return myScheduledExpireTimeMsec / 1000.0;
		} else {
			return null;
		}
	}
	/**
	 * 
	 * @return
	 */
	public String getStatString() {
		return "strength=" + getStrength() 
				+ ", createStampMillis=" + getCreateStampMsec();
	}
	public String toString() {
		return getTypeString() + "[" + getContentSummaryString() + "," + getStatString() + "]";
	}
	/**
	 * 
	 * @return
	 */
	public boolean autoUpdateOnPropertyChange() {
		// Note on 2009-08-12: We were using this to control Drools update, but have switched
		// to using the markUpdated method in ThalamentStub.
		return false;
	}

	protected void broadcastUpdateNotice() {
		// ((CueBroker) fetchBroker()).broadcastCueUpdate(this);
	}
	
}
