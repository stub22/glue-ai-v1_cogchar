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

package org.cogchar.api.animoid.protocol;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import static org.cogchar.api.animoid.protocol.JointStateCoordinateType.*;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class JointPosition extends JointStateItem {
	private static Logger	theLogger = LoggerFactory.getLogger(JointPosition.class.getName());
	public enum Rationale {
		OPEN_LOOP,		// Scripted
		CLOSED_LOOP,	// Feedback control, e.g. Gaze
		BLENDED			// Contains a "pleasing" blend of open and closed loop influences
	}
	private Rationale		myRationale;
	public JointPosition(Joint j) {
		super(j);
	}
	public JointPosition copy() {
		JointPosition cjp = new JointPosition(getJoint());
		cjp.copyStateFrom(this);
		return cjp;
	}

	@Override public String toString() {
		JointStateCoordinateType ctype = getCoordinateType();
		Double dval = getCoordinateFloat(ctype);
		return "JointPosition[" + getJoint().toString() + ", coordType=" + ctype
					+ ", coordFloat=" + dval + "]";
	}

	public void setRationale (Rationale r) {
		myRationale = r;
	}
	public Rationale getRationale() {
		return myRationale;
	}
	public static JointPosition sumJointPositions(JointPosition jp1, JointPosition jp2){
		return weightedSumJointPositions(jp1, 1.0, jp2, 1.0);
	}
	public static JointPosition weightedSumJointPositions(JointPosition jp1, Double w1, JointPosition jp2, Double w2) {
		if(w1 == null || w2 == null){
			theLogger.warn("Unable to take the weighted sum with null weights for Joints: " +
					jp1 + ", and " + jp2);
			return null;
		}
		JointPosition	result = null;
		if (jp1 != null) {
			if (jp2 != null) {
				Joint j1 = jp1.getJoint();
				Joint j2 = jp2.getJoint();
				if (!j1.equals(j2)) {
					throw new RuntimeException("Attempted to sum disparate joints: " + j1 + " and " + j2);
				}				
				JointStateCoordinateType ct1 = jp1.getCoordinateType();
				JointStateCoordinateType ct2 = jp2.getCoordinateType();
				if (ct1 == ct2) {
					switch(ct1) {
					case FLOAT_ABS_RANGE_OF_MOTION:
						if(w1 + w2 > 1.0){
							double nw1, nw2,sum;
							sum = w1 + w2;
							nw1 = w1/sum;
							nw2 = w2/sum;
							theLogger.warn("Weights: " + w1 + ", and " + w2 + " are greater than 1.\n" +
									"The weights have been normalized to " + nw1 + ", and " + nw2);
							w1 = nw1;
							w2 = nw2;
						}
					case FLOAT_VEL_RANGE_OF_MOTION_PER_SEC:
					case FLOAT_ACC_RANGE_OF_MOTION_PSPS:
					case FLOAT_REL_RANGE_OF_MOTION:
						Double v1 = jp1.getCoordinateFloat(ct1);
						Double v2 = jp2.getCoordinateFloat(ct2);
						Double sum = v1*w1 + v2*w2;
						result = new JointPosition(j1);
						result.setCoordinateFloat(ct1, sum);
					break;
					}
				} 
				if (result == null) {
					throw new RuntimeException("Can't sum " + ct1  + " and " + ct2 + " for joint " + j1);
				}
			} else {
				result = jp1;
			}
		} else if (jp2 != null) {
			result = jp2;
		}
		return result;
	}
	public JointPosition integrate(double time) {
		JointStateCoordinateType ct = getCoordinateType();
		double val = getCoordinateFloat(ct);
		JointPosition result = new JointPosition(getJoint());
		switch(ct) {
		case FLOAT_ACC_RANGE_OF_MOTION_PSPS:
			result.setCoordinateFloat(FLOAT_VEL_RANGE_OF_MOTION_PER_SEC, val * time);
		break;
		case FLOAT_VEL_RANGE_OF_MOTION_PER_SEC:
			result.setCoordinateFloat(FLOAT_REL_RANGE_OF_MOTION, val * time);
		break;
		default:
			result = null;
			theLogger.warn("Can't integrate: " + ct + " for joint " + getJoint());
			throw new RuntimeException("Attempted to integrate non-derivative coordinate " + ct);
		}
		return result;
	}	
	public static JointPosition differentiate(JointStateCoordinateType outType, JointPosition prevPos, JointPosition currPos, double timeSec) {
		JointStateCoordinateType inType;
		if (outType.equals(FLOAT_VEL_RANGE_OF_MOTION_PER_SEC)) {
			inType = FLOAT_ABS_RANGE_OF_MOTION;
		} else if (outType.equals(FLOAT_ACC_RANGE_OF_MOTION_PSPS)) {
			inType = FLOAT_VEL_RANGE_OF_MOTION_PER_SEC;
		} else {
			throw new RuntimeException("Can't differentiate to produce output type: " + outType);
		}
		double rate = computeRateOfChange(inType, prevPos, currPos, timeSec);
		JointPosition derivJP = new JointPosition(prevPos.getJoint());
		derivJP.setCoordinateFloat(outType, rate);
		return derivJP;
	}



	public void addDelta(JointPosition delta) {
		JointStateCoordinateType ct = getCoordinateType();
		JointStateCoordinateType dct = delta.getCoordinateType();
		double val = getCoordinateFloat(ct);
		double deltaVal = delta.getCoordinateFloat(dct);
		if ((ct == FLOAT_ABS_RANGE_OF_MOTION) && (dct == FLOAT_REL_RANGE_OF_MOTION)) {
			double updatedVal = val + deltaVal;
			setCoordinateFloat(ct, updatedVal);
		} else {
			throw new RuntimeException("Can't add delta " + dct + " to " + ct + " on joint " + getJoint());
		}		
	}
	public JointPosition convertToCooordinateType(JointStateCoordinateType ctype) {
		JointStateCoordinateType mytype = getCoordinateType();

		if (ctype.equals(mytype)) {
			return this.copy();
		}
		Joint j = getJoint();
		double origFloatVal = getCoordinateFloat(mytype);
		JointPosition cjp = new JointPosition(j);
		try {
			if ((ctype == FLOAT_ABS_RANGE_OF_MOTION) && (mytype == FLOAT_ABS_LOPSIDED_PIECEWISE_LINEAR)) {
				double convVal = j.convertLopsidedFloatToROM(origFloatVal);
				cjp.setCoordinateFloat(ctype, convVal);
			} else if ((ctype == FLOAT_ABS_LOPSIDED_PIECEWISE_LINEAR) && (mytype == FLOAT_ABS_RANGE_OF_MOTION)) {
				double convVal = j.convertROMtoLopsidedFloat(origFloatVal);
				cjp.setCoordinateFloat(ctype, convVal);
			} else {
				theLogger.warn("Can\'t convert from " + mytype + " to " + ctype);
				cjp = null;
			}
		} catch (Throwable t) {
			theLogger.error("problem converting jointPos coordinate for " + j, t);
			cjp = null;
		}
		return cjp;
	}

}
