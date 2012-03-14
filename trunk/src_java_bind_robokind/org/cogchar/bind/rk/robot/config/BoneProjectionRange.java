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
package org.cogchar.bind.rk.robot.config;

import org.robokind.api.common.position.NormalizedDouble;
import org.appdapter.gui.assembly.DynamicCachingComponentAssembler;
import org.appdapter.gui.box.KnownComponentImpl;
import org.appdapter.core.item.Ident;
import org.appdapter.core.item.Item;
import org.appdapter.core.item.ItemFuncs;
import org.appdapter.gui.box.BoxImpl;
import org.appdapter.gui.box.Trigger;
import org.appdapter.core.item.JenaResourceItem;
import org.appdapter.core.item.ModelIdent;
import org.appdapter.gui.box.KnownComponent;
import org.appdapter.gui.box.MutableKnownComponent;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 *
 * @author Matthew Stevenson <www.robokind.org> 
 * @author Stu B. <www.texpedient.com>
 */
public class BoneProjectionRange  {

	// We don't like this instance variable
	private String myBoneName;
	
	private	BoneJointConfig		myBJC;
	
	private BoneRotationAxis myRotationAxis;
	
	private double myMinPosAngRad;	// Means "joint fully retracted at 0.0 normal" - not necessarily "numerically least"
	private double myMaxPosAngRad;	// Means "joint fully extended at 1.0 normal" - not necessarily "numerically greatest"

	public BoneProjectionRange(BoneJointConfig bjc, BoneRotationAxis axis, double minAngRad, double maxAngRad) {
		if (bjc == null || axis == null) {
			throw new RuntimeException("Null joint or axis passed to constructor");
		}
		myBJC = bjc;
		myRotationAxis = axis;
		myMinPosAngRad = minAngRad;
		myMaxPosAngRad = maxAngRad;
	}
	public static BoneProjectionRange makeOne(BoneJointConfig bjc, Item configItem) {
		String rotAxisName = ItemFuncs.getString(configItem, BoneConfigNames.P_rotationAxis, null);
		Double minAngDeg = ItemFuncs.getDouble(configItem, BoneConfigNames.P_minAngleDeg, null);
		Double maxAngDeg = ItemFuncs.getDouble(configItem, BoneConfigNames.P_maxAngleDeg, null);
		double minAngRad = Math.toRadians(minAngDeg);
		double maxAngRad = Math.toRadians(maxAngDeg);
		BoneRotationAxis rax = BoneRotationAxis.valueOf(rotAxisName);
		BoneProjectionRange bpr = new BoneProjectionRange(bjc, rax, minAngRad, maxAngRad);
		return bpr;
	}
	public String getBoneName() {
		return myBJC.myBoneName;
	}

	public BoneRotationAxis getRotationAxis() {
		return myRotationAxis;
	}

	public BoneProjectionPosition makePositionForNormalizedFraction(NormalizedDouble normVal) {
		// This calc works regardless of signs of min/max
		double range = myMaxPosAngRad - myMinPosAngRad;
		double boneProjAngleRad = range * normVal.getValue() + myMinPosAngRad;
		return new BoneProjectionPosition(this, boneProjAngleRad);
	}

	protected String getFieldSummary() {
		return "boneName=[" + myBoneName + "], rotAxis=[" + myRotationAxis + "], minRot=[" + myMinPosAngRad
				+ "], maxRot=[" + myMaxPosAngRad + "]";
	}
	public String toString() { 
		return "BPR[" + getFieldSummary() + "]";
	}

}
