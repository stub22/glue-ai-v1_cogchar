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

/**
 * @author Stu B. <www.texpedient.com>
 */

public class BoneJoint extends AbstractJoint{
    
	protected BoneJoint(Joint.Id jointId){
        super(jointId);
	}

	@Override
	public void setEnabled(Boolean enabled) {
        throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Boolean getEnabled() {
        throw new UnsupportedOperationException("Not supported yet.");
	}

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public NormalizedDouble getDefaultPosition() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public NormalizedDouble getGoalPosition() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    //This is used to allow the SkeletonRobot to set the GoalPosition and fire the event.
    void setGoalPosition(NormalizedDouble pos){
        NormalizedDouble old = getGoalPosition();
        //actually set the goal position here
        firePropertyChange(PROP_GOAL_POSITION, old, pos);
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
