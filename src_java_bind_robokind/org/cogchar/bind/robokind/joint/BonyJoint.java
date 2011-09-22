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

package org.cogchar.bind.robokind.joint;

import org.robokind.api.common.position.NormalizedDouble;
import org.robokind.api.motion.AbstractJoint;
import org.robokind.api.motion.Joint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class BonyJoint extends AbstractJoint {
	static Logger theLogger = LoggerFactory.getLogger(BonyJoint.class);
	
    private	boolean				myEnabledFlag = false;
	private	NormalizedDouble	myDefaultPos;
	private	NormalizedDouble	myGoalPos;
	private	String				myName;
	
	protected BonyJoint(Joint.Id jointId, String name, NormalizedDouble defaultPos) {
        super(jointId);
		myName = name;
		myDefaultPos = defaultPos;
	}

	@Override public void setEnabled(Boolean enabled) {
		theLogger.info("BonyJoint[" + getId() + "] setEnabled(" + enabled + ")");
		myEnabledFlag = enabled;
	}

	@Override public Boolean getEnabled() {
		return myEnabledFlag;
	}

    @Override public String getName() {
		return myName;
    }

    @Override public NormalizedDouble getDefaultPosition() {
        return myDefaultPos;
    }

    @Override public NormalizedDouble getGoalPosition() {
        return myGoalPos;
    }
    
    //This is used to allow the SkeletonRobot to set the GoalPosition and fire the event.
    void setGoalPosition(NormalizedDouble pos){
		theLogger.info("BonyJoint[" + getId() + "] setGoalPosition(" + pos + ")");
		NormalizedDouble old = getGoalPosition();
        //actually set the goal position here
        firePropertyChange(PROP_GOAL_POSITION, old, pos);
		myGoalPos = pos;
    }
    
}
