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

import org.cogchar.api.skeleton.config.BoneProjectionPosition;
import org.cogchar.api.skeleton.config.BoneProjectionRange;
import org.cogchar.api.skeleton.config.BoneJointConfig;
import java.util.ArrayList;
import java.util.List;

import org.robokind.api.common.position.NormalizableRange;
import org.robokind.api.common.position.NormalizedDouble;
import org.robokind.api.common.utils.Utils;
import org.robokind.api.motion.AbstractJoint;
import org.robokind.api.motion.Joint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class ModelJoint extends AbstractJoint {
	static Logger theLogger = LoggerFactory.getLogger(ModelJoint.class);
    
    private	boolean							myEnabledFlag = false;
	private	NormalizedDouble				myDefaultPosNorm;
	private	NormalizedDouble				myGoalPosNorm;
	private	String							myJointName;
    private List<BoneProjectionRange>		myBoneProjectionRanges;
	
    protected ModelJoint(Joint.Id jointId, BoneJointConfig bjc) {
        super(jointId);
        myJointName = bjc.myJointName;
		updateConfig(bjc);
		hardResetGoalPosToDefault();
    }
	public void updateConfig(BoneJointConfig bjc) { 
		double defPosVal = Utils.bound(bjc.myNormalDefaultPos, 0.0, 1.0);
		myDefaultPosNorm = new NormalizedDouble(defPosVal);
        myBoneProjectionRanges =  bjc.myProjectionRanges;	
		hardResetGoalPosToDefault();
	}
	public void hardResetGoalPosToDefault() { 
		setGoalPosition(getDefaultPosition());
	}
	protected String getDescription() { 
		return "JOINT[" + getId() + ", " + myJointName + "]";
	}
	@Override public String toString() { 
		return getDescription() + "-[enabled=" + myEnabledFlag + ", goalPos=" + myGoalPosNorm + "]";
	}
	@Override public void setEnabled(Boolean enabled) {
		theLogger.info(getDescription() + ".setEnabled(" + enabled + ")");
		myEnabledFlag = enabled;
	}

	@Override public Boolean getEnabled() {
		return myEnabledFlag;
	}
    @Override public String getName() {
		return myJointName;
    }
    @Override public NormalizedDouble getDefaultPosition() {
        return myDefaultPosNorm;
    }
    @Override public NormalizedDouble getGoalPosition() {
        return myGoalPosNorm;
    }
    
    public List<BoneProjectionRange> getBoneRotationRanges(){
        return myBoneProjectionRanges;
    }
    
    //This is used to allow the SkeletonRobot to set the GoalPosition and fire the event.
    void setGoalPosition(NormalizedDouble pos){
		theLogger.trace(getDescription() + ".setGoalPosition(" + pos + ")");
		NormalizedDouble old = getGoalPosition();
        //actually set the goal position here
        firePropertyChange(PROP_GOAL_POSITION, old, pos);
		myGoalPosNorm = pos;
    }
    
	public List<BoneProjectionPosition> getRotationListForNormPos(NormalizedDouble normPos) {
        List<BoneProjectionPosition> rotations = 
                new ArrayList<BoneProjectionPosition>(myBoneProjectionRanges.size());
		for(BoneProjectionRange range : myBoneProjectionRanges){
            rotations.add(range.makePositionForNormalizedFraction(normPos.getValue()));
        }
        return rotations;
	}
	public List<BoneProjectionPosition> getRotationListForCurrentGoal() { 
		NormalizedDouble normGoalPos = getGoalPosition();
		return getRotationListForNormPos(normGoalPos);
	}

    @Override
    public NormalizableRange getPositionRange() {
        return null;
    }
	
}
