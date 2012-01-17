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

import org.cogchar.avrogen.bind.robokind.RotationAxis;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class ModelBoneRotation {
	private String myBoneName;
	private RotationAxis myRotationAxis;
	private double myAngleRadians;

	public ModelBoneRotation(String boneName, RotationAxis axis, double angleRads) {
		myBoneName = boneName;
		myRotationAxis = axis;
		myAngleRadians = angleRads;
	}

	public String getBoneName() {
		return myBoneName;
	}

	public RotationAxis getRotationAxis() {
		return myRotationAxis;
	}

	public double getAngleRadians() {
		return myAngleRadians;
	}
	@Override public String toString () {
		return "MBR[" + myBoneName + "," + myRotationAxis + "," + myAngleRadians + "]";
	}
	
}
