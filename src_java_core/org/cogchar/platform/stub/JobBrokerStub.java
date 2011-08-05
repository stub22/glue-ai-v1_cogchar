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

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.cogchar.platform.util.TimeUtils;
// Drools 4
// import org.drools.FactHandle;
// import org.drools.WorkingMemory;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class JobBrokerStub extends ThalamusBrokerStub implements JobSpaceStub {
	private static Logger	theLogger = Logger.getLogger(JobBrokerStub.class.getName());

	/**
	 * 
	 */
	protected	JobConfig.Source		myJobConfigSource;
	// private List<JobListener> myJobListeners = new ArrayList<JobListener>();
	
	/**
	 * 
	 * @param t
	 * @param sks
	 */
	public JobBrokerStub() { // { Thalamus t, StatefulKnowledgeSession sks) {
		super(); // t, sks);
	}
	/**
	 * 
	 * @param j
	 */
	protected void assertJob(JobStub j) {
		long now = TimeUtils.currentTimeMillis();
		j.setCreateStampMsec(now);
	//	myThalamus.setLastJobAdded(j);		
	//	mySKS.insert(j);
		j.setBroker(this);		
	}
	/**
	 * 
	 * @param j
	 */
	public void postManualJob(JobStub j) {
		assertJob(j);
	}	
	/**
	 * 
	 * @param j
	 */
	public synchronized void clearJob(JobStub j) {
		theLogger.info("Clearing job: " + j);
	/*
		 * if (retractFactForObject(j)) {
			myThalamus.setLastJobRemoved(j);
		}
		 * 
		 */
	}
	public void terminateAndClearJob(JobStub j) {
		theLogger.info("Terminating and Clearing Job with createStamp=" + j.getCreateStampMsec()
					+ ": " + j);
		// Blender unregisters the anim based on its status.
		j.requestCancelOrAbort();
		clearJob(j);
	}
	public <JT extends JobStub> void terminateAndClearJobsInClass(Class<JT> jtClass) {
		List<JT> jobs = getAllJobsMatchingClass(jtClass);
		for (JT job : jobs) {
			terminateAndClearJob(job);
		}
	}
	/*
	public void addJobListener(JobListener cl) {
		myJobListeners.add(cl);
	}
	public void removeJobListener(JobListener cl) {
		myJobListeners.remove(cl);
	}
	 * 
	 */
	public synchronized void notifyJobPosted(JobStub j) {
		theLogger.finer("notifyJobPosted:" + j.getTypeString());
	//	for (JobListener jl: myJobListeners) {
	//		jl.notifyJobPosted(j);
	//	}
	}
	public synchronized void notifyJobCleared(JobStub j) {
		theLogger.info("notifyJobCleared:" + j.getTypeString());
//		for (JobListener jl: myJobListeners) {
//			jl.notifyJobCleared(j);
//		}
	}	

	public void setJobConfigSource(JobConfig.Source src) {
		myJobConfigSource = src;
	}	
	public JobConfig getJobConfig() {
		return myJobConfigSource.getJobConfig();
	}
	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();
		Object propertyValue = evt.getNewValue();
		theLogger.fine("propertyChange:  " + propertyName + " := " + propertyValue);
		if (java.beans.Beans.isDesignTime()) {
			theLogger.fine("It's design time!  No further processing of event");
			return;
		}
		/*
		if (propertyName.equals(Thalamus.PROP_LAST_JOB_ADDED)) {
			notifyJobPosted((Job) propertyValue);
		} else if (propertyName.equals(Thalamus.PROP_LAST_JOB_REMOVED)) {
			notifyJobCleared((Job) propertyValue);
		}
		 * 
		 */
	}		
	/**
	 * 
	 * @return
	 */
	public List<JobStub> getJobList() {
		return null;
		// return myThalamus.getJobList();
	}
	/**
	 * 
	 * @return
	 */
	public List<JobStub> getJobListCopy() {
		return new ArrayList<JobStub>(getJobList());
	}
	public synchronized <JT extends JobStub> List<JT> getAllJobsMatchingClass(Class<JT> clazz) {
		return getAllFactsMatchingClass(clazz);
	}
}
