/*
 * Copyright 2011 The Cogchar Project (www.cogchar.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cogchar.api.skeleton.config;

import org.appdapter.core.item.Item;
import org.appdapter.core.item.ItemFuncs;

/**
 *
 * @author Matthew Stevenson <www.robokind.org> 
 * @author Stu B. <www.texpedient.com>
 */
public class BoneProjectionRange  {
	
	private	BoneJointConfig		myBJC;
	
	public String			myBoneName;
	
	private BoneRotationAxis myRotationAxis;
	
	private double myMinPosAngRad;	// Means "joint fully retracted at 0.0 normal" - not necessarily "numerically least"
	private double myMaxPosAngRad;	// Means "joint fully extended at 1.0 normal" - not necessarily "numerically greatest"

	public BoneProjectionRange(BoneJointConfig bjc, String boneName, BoneRotationAxis axis, double minAngRad, double maxAngRad) {
		if (bjc == null || axis == null) {
			throw new RuntimeException("Null joint or axis passed to constructor");
		}
		myBJC = bjc;
		myBoneName = boneName;
		myRotationAxis = axis;
		myMinPosAngRad = minAngRad;
		myMaxPosAngRad = maxAngRad;
	}
	public static BoneProjectionRange makeOne(BoneJointConfig bjc, Item configItem) {
		String boneName = ItemFuncs.getString(configItem, BoneConfigNames.P_boneName, null);
		String rotAxisName = ItemFuncs.getString(configItem, BoneConfigNames.P_rotationAxis, null);
		Double minAngDeg = ItemFuncs.getDouble(configItem, BoneConfigNames.P_minAngleDeg, null);
		Double maxAngDeg = ItemFuncs.getDouble(configItem, BoneConfigNames.P_maxAngleDeg, null);
		double minAngRad = Math.toRadians(minAngDeg);
		double maxAngRad = Math.toRadians(maxAngDeg);
		BoneRotationAxis rax = BoneRotationAxis.valueOf(rotAxisName);
		BoneProjectionRange bpr = new BoneProjectionRange(bjc, boneName, rax, minAngRad, maxAngRad);
		return bpr;
	}
	public String getBoneName() {
		return myBoneName;
	}

	public BoneRotationAxis getRotationAxis() {
		return myRotationAxis;
	}
        
    public double getMinPosAngRad() {
        return myMinPosAngRad;
    }

    public double getMaxPosAngRad() {
        return myMaxPosAngRad;
    }
    
    public BoneJointConfig getJointConfig() {
        return myBJC;
    }
    
	/**
	 * 
	 * @param normVal  between 0.0 and 1.0
	 * @return 
	 */
	public BoneProjectionPosition makePositionForNormalizedFraction(double normVal) {
		// This calc works regardless of signs of min/max
		double rangeRad = myMaxPosAngRad - myMinPosAngRad;
		double boneProjAngleRad = rangeRad * normVal + myMinPosAngRad;
		return new BoneProjectionPosition(this, boneProjAngleRad);
	}

	protected String getFieldSummary() {
		return "rotAxis=[" + myRotationAxis + "], minRotRad=[" + myMinPosAngRad
				+ "], maxRotRad=[" + myMaxPosAngRad + "]";
	}
    @Override
	public String toString() { 
		return "BPR[" + getFieldSummary() + "]";
	}
}
