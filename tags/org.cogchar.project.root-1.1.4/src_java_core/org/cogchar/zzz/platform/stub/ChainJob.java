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
package org.cogchar.zzz.platform.stub;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author Stu B. <www.texpedient.com>
 *
 *  ChainJob is a list of jobs to be run in order.
 */
public class ChainJob extends JobStub implements PropertyChangeListener {
	private static Logger	theLogger = Logger.getLogger(ChainJob.class.getName());
	static {
		theLogger.setLevel(Level.ALL);
	}	

	private		transient	List<JobStub>	myJobList;
	
	private					int			myCurrentJobIndex = -1;
	
	/**
	 * 
	 */
	public ChainJob() {
		myJobList = new ArrayList<JobStub>();
	}
	/**
	 * 
	 * @param j
	 */
	public void appendJob(JobStub j) {
		myJobList.add(j);
		j.addPropertyChangeListener(this);
	}
	/**
	 * 
	 */
	protected void start() {
		if (myJobList.size() > 0) {
			setStatus(Status.RUNNING);
			myCurrentJobIndex = 0;
			startCurrentChildJob();
		} else {
			setStatus(Status.COMPLETED);	
		}
	}
	private JobStub getCurrentChildJob() {
		JobStub result = null;
		if ((myCurrentJobIndex >= 0) && (myCurrentJobIndex < myJobList.size())) {
			result = myJobList.get(myCurrentJobIndex);
		}
		return result;
	}
	private synchronized void startCurrentChildJob() {
		JobStub currentChild = getCurrentChildJob();
		currentChild.scheduleToStartNow();
		currentChild.click();
	}
	
	private synchronized void advanceOrComplete() {
		myCurrentJobIndex++;
		if (myCurrentJobIndex >= myJobList.size()) {
			setStatus(Status.COMPLETED);			
		} else {
			startCurrentChildJob();
		}
	}
	/**
	 * 
	 */
	protected void abort() {
		// Abort current child job, if not aborting/ed already?
		setStatus(Status.ABORTING);	
	}
	/**
	 * 
	 * @return
	 */
	public String getTypeString() {
		return "ChainJob[childCount=" + myJobList.size() + "]";
	}
	/**
	 * 
	 * @return
	 */
	public String getContentSummaryString() {
		return myJobList.toString();
	}	
	public void propertyChange(PropertyChangeEvent evt) {
		Object source = evt.getSource();
		String propertyName = evt.getPropertyName();
		Object propertyValue = evt.getNewValue();
		theLogger.finest("ChainJob got property change [src,name]=[" + source + "," +  propertyName + "] := " + propertyValue);
		if ((source == getCurrentChildJob()) && propertyName.equals(JobStub.PROP_STATUS)) {
			JobStub.Status updatedStatus = (JobStub.Status) propertyValue;
			switch(updatedStatus) {
			case ABORTED:
			case COMPLETED:
				advanceOrComplete();
			break;
			}
		}
	}
}
