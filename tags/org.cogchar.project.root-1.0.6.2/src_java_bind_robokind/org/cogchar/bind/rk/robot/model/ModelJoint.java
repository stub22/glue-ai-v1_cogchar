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

import org.robokind.api.common.position.DoubleRange;
import org.robokind.api.common.position.NormalizableRange;
import org.robokind.api.common.position.NormalizedDouble;
import org.robokind.api.common.utils.Utils;
import org.robokind.api.motion.AbstractJoint;
import org.robokind.api.motion.Joint;
import org.robokind.api.motion.JointProperty;
import org.robokind.api.motion.joint_properties.ReadCurrentPosition;
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
    private NormalizableRange       myNormalizableRange;
	
    protected ModelJoint(Joint.Id jointId, BoneJointConfig bjc) {
        super(jointId);
        myNormalizableRange = NormalizableRange.NORMALIZED_RANGE;;
        myJointName = bjc.myJointName;
		boolean flag_hardResetToDefaultOnJointInit = true;
		updateConfig(bjc, flag_hardResetToDefaultOnJointInit);
        ReadCurrentPosition currentPositionReader = new ReadCurrentPosition() {
            @Override public NormalizedDouble getValue() {
                return getGoalPosition();
            }
            @Override public NormalizableRange<NormalizedDouble> getNormalizableRange() {
                return NormalizableRange.NORMALIZED_RANGE;
            }
        };
		addProperty(currentPositionReader);
    }
	protected void updateConfig(BoneJointConfig bjc, boolean flag_hardResetGoalPosToDefault) { 
		double defPosVal = Utils.bound(bjc.myNormalDefaultPos, 0.0, 1.0);
		myDefaultPosNorm = new NormalizedDouble(defPosVal);
        myBoneProjectionRanges =  bjc.myProjectionRanges;
        List<BoneProjectionRange> projRanges = getBoneRotationRanges();
        if(projRanges == null || projRanges.isEmpty()){
            myNormalizableRange = NormalizableRange.NORMALIZED_RANGE;
		} else {
	        BoneProjectionRange range = projRanges.get(0);
			double min = range.getMinPosAngRad();
			double max = range.getMaxPosAngRad();
			myNormalizableRange = new DoubleRange(min, max);
		}
		if (flag_hardResetGoalPosToDefault) {
				hardResetGoalPosToDefault();
		}
	}
	private void hardResetGoalPosToDefault() { 
		setGoalPositionAndFirePropChangedEvent(getDefaultPosition());
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
    @Override public NormalizableRange getPositionRange() {
        /*
         * What impacts does this Range have?  Who uses it? - (Ryan or Stu?)
         * 
         * This is used by ChannelRobotParameters when saving an animation to 
         * include the ranges of the robot as animation metadata.  The metadata
         * is currently unused, but intended to be used for doing automated 
         * conversions of the animation later. - Matt 2013/02/19
         */
        return myNormalizableRange;
    }
    
    public List<BoneProjectionRange> getBoneRotationRanges(){
        return myBoneProjectionRanges;
    }
    /**
	 * This is how our owning robot tells us to go to a particular position, in
	 * response to the move() commands it receives.
	 * @param pos 
	 */
    protected void setGoalPositionAndFirePropChangedEvent(NormalizedDouble pos){
		theLogger.trace("Setting position for Joint {} to {}", myJointName, pos);
		NormalizedDouble old = getGoalPosition();
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

	
}
