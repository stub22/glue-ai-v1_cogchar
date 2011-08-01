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
package org.cogchar.platform.stub;

import java.util.HashMap;
import java.util.Map;

import org.cogchar.platform.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 *
 */
public abstract class JobStub extends ThalamentStub {
	private static Logger	theLogger = LoggerFactory.getLogger(JobStub.class.getName());
	/**
	 * 
	 */
	public static String	PROP_STATUS			= "status";
	/**
	 * 
	 */
	public static String	PROP_STATUS_STRING	= "statusString";
	/**
	 * 
	 */
	public static String	PROP_SCHED_START	= "schedStart";
	/**
	 * 
	 */
	public static String	PROP_SCHED_END		= "schedEnd";
	/**
	 * 
	 */
	public static String	PROP_ACTUAL_START	= "actualStart";
	/**
	 * 
	 */
	public static String	PROP_ACTUAL_END		= "actualEnd";
	
	/**
	 * 
	 */
	public enum Status {

		/**
		 * 
		 */
		PENDING,
		/**
		 * 
		 */
		CANCELED,
		/**
		 * 
		 */
		RUNNING,
		/**
		 * 
		 */
		ABORTING,
		/**
		 * 
		 */
		ABORTED,
		/**
		 * 
		 */
		COMPLETED,
		/**
		 * 
		 */
		PAUSED
	}
	private		Status						myStatus = Status.PENDING;
	// All times are java-system-clock millisec
	private		Long						mySchedStart = null;
	private		Long						mySchedEnd = null;
	private		Long						myActualStart = null;
	private		Long						myActualEnd = null;
	
	/**
	 * 
	 */
	protected	Map<String, String>			myConfigMap = new HashMap<String, String>();

	
	/**
	 * 
	 */
	protected void start() {
		theLogger.info("Job default impl setting status = RUNNING");
		setStatus(JobStub.Status.RUNNING);
	}
	/**
	 * 
	 */
	protected void abort() {
		// This won't get called one all our status filters are working properly.
		theLogger.info("Setting status = ABORTED for job=" + this);
		setStatus(JobStub.Status.ABORTED);
	}	
	
	/**
	 * 
	 */
	public JobStub() {
		myStatus = Status.PENDING;
	}
	/**
	 * 
	 */
	public synchronized void click() {
		long	currentTimeMillis = TimeUtils.currentTimeMillis();
		if (getStatus() == Status.PENDING) {
			if (currentTimeMillis >= getSchedStart()) {
				start();
			}
		}
	}
	/**
	 * 
	 * @return
	 */
	public synchronized boolean requestCancelOrAbort() {
		if (myStatus == Status.PENDING) {
			setStatus(Status.CANCELED);
		} else if ((myStatus == Status.RUNNING) || (myStatus == Status.PAUSED)) {
			setStatus(Status.ABORTING);
			abort();
		} else {
			theLogger.warn("Can't cancel/abort job because status is: " + myStatus + " : [" + toString() + "]");
		}
		return true;
	}
	
	/**
	 * 
	 * @return
	 */
	public Status getStatus() {
		return myStatus;
	}
	/**
	 * 
	 * @param status
	 */
	protected synchronized void setStatus(Status status) {
		Status	oldStatus = myStatus;
		String	oldStatusString = getStatusString();
		myStatus = status;
		// safelyFirePropertyChange(PROP_STATUS, oldStatus, myStatus);
		// statusString is a computed property without it's own setter, so we notify its listeners here.
		// safelyFirePropertyChange(PROP_STATUS_STRING, oldStatusString, getStatusString());
		if ((myStatus == Status.RUNNING) && (oldStatus == Status.PENDING)) {
			markStartTime();
		}
		else if ((myStatus == Status.ABORTED) || (myStatus == Status.COMPLETED)) {
			markEndTime();
		}
		markUpdatedNow();
	}
	/**
	 * 
	 * @return
	 */
	public Long getSchedStart() {
		return mySchedStart;
	}
	/**
	 * 
	 * @param schedStart
	 */
	public synchronized void setSchedStart(Long schedStart) {
		Long	oldStart = mySchedStart;
		mySchedStart = schedStart;
		safelyFirePropertyChange(PROP_SCHED_START, oldStart, mySchedStart);
	}
	/**
	 * 
	 * @return
	 */
	public Long getSchedEnd() {
		return mySchedEnd;
	}
	/**
	 * 
	 * @param schedEnd
	 */
	public synchronized void setSchedEnd(Long schedEnd) {
		Long oldEnd = mySchedEnd;
		mySchedEnd = schedEnd;
	//	safelyFirePropertyChange(PROP_SCHED_END, oldEnd, mySchedEnd);
	}
	/**
	 * 
	 * @return
	 */
	public Long getActualStart() {
		return myActualStart;
	}
	private synchronized void setActualStart(Long actualStart) {
		Long oldAS = myActualStart;
		myActualStart = actualStart;
	//	safelyFirePropertyChange(PROP_ACTUAL_START, oldAS, myActualStart);
	}
	private void markStartTime() {
		Long now = TimeUtils.currentTimeMillis();
		setActualStart(now);
	}
	/**
	 * 
	 * @return
	 */
	public Long getActualEnd() {
		return myActualEnd;
	}
	/**
	 * 
	 * @return
	 */
	public Double getActualStartSec() {
		if (myActualStart != null) {
			return myActualStart / 1000.0;
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @return
	 */
	public Double getActualEndSec() {
		if (myActualEnd != null) {
			return myActualEnd / 1000.0;
		} else {
			return null;
		}
	}
	
	private synchronized void setActualEnd(Long actualEnd) {
		Long oldAE = myActualEnd;
		myActualEnd = actualEnd;
		safelyFirePropertyChange(PROP_ACTUAL_END, oldAE, myActualEnd);
	}
	private void markEndTime() {
		Long now = TimeUtils.currentTimeMillis();
		setActualEnd(now);
	}	
	
	/**
	 * 
	 * @param wait
	 */
	public void scheduleToStartAfterWait(Long wait) {
		Long now = TimeUtils.currentTimeMillis();
		Long startTime = now + wait;
		setSchedStart(startTime);
	}
	/**
	 * 
	 */
	public void scheduleToStartNow() {
		scheduleToStartAfterWait(0L);
	}
	/**
	 * 
	 * @return
	 */
	public String getStatusString() {
		return getStatus().toString();
	}
	/**
	 * 
	 * @return
	 */
	public Long getSchedDuration() {
		if ((mySchedStart == null) || (mySchedEnd == null)) {
			return null;
		} else {
			return mySchedEnd - mySchedStart;
		}
	}
	/**
	 * 
	 * @param duration
	 */
	public void scheduleForIntervalStartingNow(Long duration) {
		scheduleToStartNow();
		setSchedEnd(mySchedStart + duration);
	}
	public String toString() {
		// Should show the schedule/actual time stuff, too.
		return getTypeString() + "[status=" + getStatus()
					+ ", actualStart=" + getActualStartSec() 
					+ ", actualEnd=" + getActualEndSec() + "] :"
					+ getContentSummaryString();
	}
	/**
	 * 
	 * @param name
	 * @param val
	 */
	public void setConfigVariable(String name, String val) {
		myConfigMap.put(name, val);
	}
	/**
	 * 
	 * @param name
	 * @return
	 */
	public String getConfigVariable(String name) {
		return myConfigMap.get(name);
	}
	/**
	 * 
	 * @return
	 */
protected JobBrokerStub getJobBroker() {
		return (JobBrokerStub) fetchBroker();
	}

	public boolean mayBeRunnableNowOrLater() {
		switch(myStatus) {
			case	RUNNING:
			case	PENDING:
			case	PAUSED:
				return true;
			default:
				return false;
		}
	}
}
