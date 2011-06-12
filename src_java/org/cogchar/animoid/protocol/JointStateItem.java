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

package org.cogchar.animoid.protocol;

import org.slf4j.LoggerFactory;
import java.io.Serializable;
import java.util.List;
import org.slf4j.Logger;

import static org.cogchar.animoid.protocol.JointStateCoordinateType.*;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public abstract class JointStateItem implements Serializable {
	private static Logger	theLogger = LoggerFactory.getLogger(JointStateItem.class.getName());
	private JointStateCoordinateType myCoordinateType;
	private Double myFloatCoordinate;
	private Integer myIntCoordinate;
	private Joint myJoint;

	public JointStateItem(Joint j) {
		myJoint = j;
	}
	public abstract JointStateItem copy();
	public void copyStateFrom(JointStateItem sourceJSI) {
		if (!sourceJSI.getJoint().equals(myJoint)) {
			throw new RuntimeException("Can't copy values across different joints");
		}
		myCoordinateType = sourceJSI.myCoordinateType;
		myFloatCoordinate = sourceJSI.myFloatCoordinate;
		myIntCoordinate = 	sourceJSI.myIntCoordinate;
	}


	public Double getCoordinateFloat(JointStateCoordinateType type) {
		// TODO:  Check type and do conversions as needed
		if (type != myCoordinateType) {
			throw new RuntimeException("Unsupported:  Tried to fetch coordinate of " + myCoordinateType + " as " + type);
		}
		return myFloatCoordinate;
	}

	public JointStateCoordinateType getCoordinateType() {
		return myCoordinateType;
	}

	public Joint getJoint() {
		return myJoint;
	}

	public void multiplyByScalar(double scalar) {
		JointStateCoordinateType ct = getCoordinateType();
		switch (ct) {
			case FLOAT_VEL_RANGE_OF_MOTION_PER_SEC:
			case FLOAT_ACC_RANGE_OF_MOTION_PSPS:
			case FLOAT_REL_RANGE_OF_MOTION:
				double val = getCoordinateFloat(ct);
				double updated = val * scalar;
				this.setCoordinateFloat(ct, updated);
				break;
			default:
				throw new RuntimeException("Can\'t multiply scalar to joint position of type " + ct + " on " + getJoint());
		}
	}

	public void setCoordinateFloat(JointStateCoordinateType type, Double value) {
		myCoordinateType = type;
		myFloatCoordinate = value;
	}

	public void verifyCoordinateTypeCompatibility(JointStateCoordinateType queryCT) {
		JointStateCoordinateType ct = getCoordinateType();
		if (!ct.equals(queryCT)) {
			throw new RuntimeException("Joint " + getJoint() + " ctype=" + ct + ", which is incompatible with " + queryCT);
		}
	}
	public boolean isZero() {
		if ((myFloatCoordinate < 0.001) && (myFloatCoordinate > -0.001)) {
			return true;
		} else {
			return false;
		}
	}
	public static <JSI extends JointStateItem> String dumpStateList(List<JSI> jsil) {
		StringBuffer result = new StringBuffer("[");
		boolean first = true;
		for (JSI jsi: jsil) {
			if (first) {
				first = false;
			} else {
				result.append(", ");
			}
			result.append(jsi.toString());
		}
		result.append("]");
		return result.toString();
	}
	public void truncate() {
		JointStateCoordinateType ct = getCoordinateType();
		if (ct.equals(JointStateCoordinateType.FLOAT_ABS_RANGE_OF_MOTION)) {
			double val = this.getCoordinateFloat(ct);
			if (val < 0.0) {
				this.setCoordinateFloat(ct, 0.000001);
			} else if (val > 1.0) {
				this.setCoordinateFloat(ct, 0.999999);
			}
		} else {
			throw new RuntimeException("Can't truncate value type " + ct + " on " + getJoint());
		}
	}
	public static double computeRateOfChange(JointStateCoordinateType inType,
			JointStateItem prevState, JointStateItem currState, double timeSec) {
		// both positions must be in inType
		double prevVal = prevState.getCoordinateFloat(inType);
		double currVal = currState.getCoordinateFloat(inType);
		double deltaVal = currVal - prevVal;
		double rate = deltaVal / timeSec;
		return rate;
	}
	public JointVelocityAROMPS asJointVelocity() {
		if (myCoordinateType == FLOAT_VEL_RANGE_OF_MOTION_PER_SEC) {
			return new JointVelocityAROMPS(myJoint, myFloatCoordinate);
		} else {
			throw new RuntimeException("Can't repackage as JointVelocity: " + this.toString());
		}
	}
}
