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

import java.io.Serializable;
import org.cogchar.platform.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stu Baurmann
 */
public abstract class ThalamentStub extends PropertyChangeNotifier implements Serializable {
	private static Logger	theLogger = LoggerFactory.getLogger(ThalamentStub.class.getName());
    private static Integer  theNextThalamentID = 1;
   //  private static ThalamentArchive   theThalamentDirectory;

    private Integer                             myThalamentID;
    private Integer                             myCausingThalamentID;
    private ThalamentStub                           myCausingThalament;
	private	Long								myCreateStampMsec;
	private	Long								myUpdateStampMsec;
	// private	transient	ThalamusBroker			myBroker;

    public ThalamentStub(){
        myThalamentID = theNextThalamentID++;
    }

    public Integer getThalamentID(){
        return myThalamentID;
    }
/*
	public void setBroker(ThalamusBroker broker) {
		myBroker = broker;
        //myBroker.myThalamus.getThalamentArchive().addThalament(this);
	}
 *
 */
	// Called fetch rather than get to prevent JMX confusion.
	protected Object fetchBroker() {
		return null;
	}

	public void markUpdated() {
		// if (myBroker != null) {
			// myBroker.update(this);
		// }
	}
	public void setUpdateStampMsec(Long usm) {
		myUpdateStampMsec = usm;
		markUpdated();
	}
	protected void markUpdatedNow() {
		setUpdateStampMsec(TimeUtils.currentTimeMillis());
	}
	public void setCreateStampMsec(Long tstampMsec) {
		myCreateStampMsec = tstampMsec;
		setUpdateStampMsec(myCreateStampMsec);
	}


	public Long getCreateStampMsec() {
		return myCreateStampMsec;
	}
	public	Long getUpdateStampMsec() {
		return myUpdateStampMsec;
	}

	public Double getCreateAgeSec() {
		return TimeUtils.msecStampObjToSecAgeObj(myCreateStampMsec);
	}
	public Double getUpdateAgeSec() {
		return TimeUtils.msecStampObjToSecAgeObj(myUpdateStampMsec);
	}
    public ThalamentStub getCausingThalament(){
        return myCausingThalament;/*
        if(myBroker == null){
            theLogger.severe("Cannot use null broker");
            return null;
        }
        if(myCausingThalamentID == null){
            theLogger.severe("Causing ID is null");
            return null;
        }
        ThalamentStub t = myBroker.myThalamus.getThalamentArchive().getThalament(myCausingThalamentID);
        if(t == null){
            theLogger.severe("WTF why is our cause null");
        }
        return t;*/
    }
    public void setCausingThalament(ThalamentStub t){
        if(t==null){
            theLogger.warn("Setting Cause for Thalament(" + myThalamentID + ") to NULL");
            return;
        }
        theLogger.error("Setting Cause for (" + getTypeString() + ":" + myThalamentID +"): " +
                t.getTypeString() + " " + t.getThalamentID());

        myCausingThalament = t;/*
        if(t == null){
            myCausingThalamentID = null;
            theLogger.warning("Setting Cause for ThalamentStub(" + myThalamentID + ") to NULL");
            return;
        }
        myCausingThalamentID = t.getThalamentID();
        theLogger.severe("Setting Cause for (" + getTypeString() + ":" + myThalamentID +"): " +
                t.getTypeString() + " " + t.getThalamentID());*/
    }

	public String getSimpleClassName() {
		return getClass().getSimpleName();
	}
	public String getTypeString() {
		return getSimpleClassName();
	}
	public String getContentSummaryString() {
		return "[empty result from getContentSummaryString()]";
	}
}
