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

package org.cogchar.bind.rk.robot.model;

import java.util.ArrayList;
import java.util.List;
import org.robokind.api.common.position.NormalizedDouble;
import org.robokind.api.motion.AbstractJoint;
import org.robokind.api.motion.Joint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class ModelJoint extends AbstractJoint {
	static Logger theLogger = LoggerFactory.getLogger(ModelJoint.class);
    
    private	boolean						myEnabledFlag = false;
	private	NormalizedDouble			myDefaultPosNorm;
	private	NormalizedDouble			myGoalPosNorm;
	private	String						myName;
    private List<ModelBoneRotRange>	myBoneRotRangeList;
	
    protected ModelJoint(Joint.Id jointId, String name, List<ModelBoneRotRange> rotations, NormalizedDouble defPos){
        super(jointId);
        myName = name;
        myBoneRotRangeList = rotations;
        myDefaultPosNorm = defPos;
        myGoalPosNorm = myDefaultPosNorm;
    }
	protected String getDescription() { 
		return "JOINT[" + getId() + "]";
	}
	@Override public void setEnabled(Boolean enabled) {
		theLogger.info(getDescription() + ".setEnabled(" + enabled + ")");
		myEnabledFlag = enabled;
	}

	@Override public Boolean getEnabled() {
		return myEnabledFlag;
	}
    @Override public String getName() {
		return myName;
    }
    @Override public NormalizedDouble getDefaultPosition() {
        return myDefaultPosNorm;
    }
    @Override public NormalizedDouble getGoalPosition() {
        return myGoalPosNorm;
    }
    
    public List<ModelBoneRotRange> getBoneRotationRanges(){
        return myBoneRotRangeList;
    }
    
    //This is used to allow the SkeletonRobot to set the GoalPosition and fire the event.
    void setGoalPosition(NormalizedDouble pos){
		theLogger.trace(getDescription() + ".setGoalPosition(" + pos + ")");
		NormalizedDouble old = getGoalPosition();
        //actually set the goal position here
        firePropertyChange(PROP_GOAL_POSITION, old, pos);
		myGoalPosNorm = pos;
    }
    
	public List<ModelBoneRotation> getRotationListForNormPos(NormalizedDouble normPos) {
        List<ModelBoneRotation> rotations = 
                new ArrayList<ModelBoneRotation>(myBoneRotRangeList.size());
		for(ModelBoneRotRange range : myBoneRotRangeList){
            rotations.add(range.makeRotationForNormalizedFraction(normPos));
        }
        return rotations;
	}
	public List<ModelBoneRotation> getRotationListForCurrentGoal() { 
		NormalizedDouble normGoalPos = getGoalPosition();
		return getRotationListForNormPos(normGoalPos);
	}
}
