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

package org.cogchar.platform.cues;

import org.cogchar.platform.stub.CueStub;
import org.cogchar.platform.util.TimeUtils;

/**
 * 
 * @author Stu B. <www.texpedient.com>
 */
public class NowCue extends CueStub {
	private		Long	myPrevDurationMillisec;
	private		Long	myNextDurationMillisec;

	/**
	 * 
	 * @param prevDurationMsec
	 * @param nextDurationMsec
	 */
	public NowCue(Long prevDurationMsec, Long nextDurationMsec) {
		myPrevDurationMillisec = prevDurationMsec;
		myNextDurationMillisec = nextDurationMsec;
	}
	/**
	 * 
	 * @param pdms
	 */
	public void setPrevDurationMsec(Long pdms) {
		myPrevDurationMillisec = pdms;
	}
	/**
	 * 
	 * @param ndms
	 */
	public void setNextDurationMsec(Long ndms) {
		myNextDurationMillisec = ndms;
	}
	
	/**
	 * 
	 * @return
	 */
	public Long getPrevDurationMillisec() {
		return myPrevDurationMillisec;
	}
	/**
	 * 
	 * @return
	 */
	public Double getPrevDurationSec() {
		if (myPrevDurationMillisec != null) {
			return getPrevDurationMillisec() / 1000.0;
		} else {
			return null;
		}
	}	
	/**
	 * 
	 * @return
	 */
	public Long getNextDurationMillisec() {
		return myNextDurationMillisec;
	}
	/**
	 * 
	 * @return
	 */
	public Double getNextDurationSec() {
		if (myNextDurationMillisec != null) {
			return myNextDurationMillisec / 1000.0;
		} else {
			return null;
		}
	}	
	/**
	 * 
	 */
	public void updateNow() {
		long currentTimeMsec = TimeUtils.currentTimeMillis();
		long prevDurationMsec = currentTimeMsec - this.getUpdateStampMsec();
		this.setPrevDurationMsec(prevDurationMsec);
		// Use previous duration as estimate for next duration
		this.setNextDurationMsec(prevDurationMsec);
		this.setUpdateStampMsec(currentTimeMsec);
		this.markUpdated();
	}
	/**
	 * 
	 * @return
	 */
	@Override public String getContentSummaryString() {
		return "prevDurationSec=" + getPrevDurationSec() + ", nextDurationSec="+ getNextDurationSec();
	}
	
}
