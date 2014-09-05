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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import static org.cogchar.api.animoid.protocol.JointStateCoordinateType.*;
/**
 *
 * @param <JP> 
 * @author Stu B. <www.texpedient.com>
 */
public class Frame<JP extends JointPosition> implements Serializable {
	private static Logger	theLogger = LoggerFactory.getLogger(Frame.class.getName());

	private List<JP>		myPositions = new ArrayList<JP>();
	public Frame() {}

	public void addPosition(JP jp) {
		if (jp == null) {
			throw new RuntimeException ("Cannot add null jp");
		}
		myPositions.add(jp);
	}
	public List<JP> getAllPositions() {
		return myPositions;
	}
	public List<JP> getNonzeroPositions() {
		List<JP> nzps = new ArrayList<JP>();
		for (JP jp: myPositions) {
			if (!jp.isZero()) {
				nzps.add(jp);
			}
		}
		return nzps;
	}	
	public JP getJointPositionForJoint(Joint j) {
		for (JP jp: myPositions) {
			if (jp.getJoint().equals(j)) {
				return jp;
			}
		}
		return null;
	}
	public JP getJointPositionForOldLogicalJointNumber(Integer jointID) {
		for (JP jp: myPositions) {
			if (jp.getJoint().oldLogicalJointNumber.equals(jointID)) {
				return jp;
			}
		}
		return null;
	}
	public Set<Joint> getUsedJointSet() {
		HashSet<Joint> usedJoints = new HashSet<Joint>();
		for (JP jp : myPositions) {
			usedJoints.add(jp.getJoint());
		}
		return usedJoints;
	}
	public String dumpAllPositions() {
		return JointStateItem.dumpStateList(myPositions);
	}
	public String dumpNonzeroPositions() {
		return JointStateItem.dumpStateList(getNonzeroPositions());
	}	
	public void transformOtherFrame(Frame targetFrame, Set<Joint> permittedJointMask, boolean okToAddDim) {
		/****** Only used by OldExecutingAnimation *****/
		//  Modify targetFrame using replacement (for absolute
		// joint pos) or translation (for relative joint pos) along the permittedJointMask axes.
		// if okToAddDim is true, then we may add joints to the result frame (if they are in
		// permittedJointMask, but not in targetFrame).  Otherwise we do not add positions for
		// joints which are not already present in targetFrame.  
		// The coordinate system of targetFrame is always unchanged, except for
		// joints we have added, where we have discretion (in theory).
		for (JointPosition sourceJP: myPositions) {
			JointStateCoordinateType targetCT, sourceCT;
			Joint sourceJ = sourceJP.getJoint();
			sourceCT = sourceJP.getCoordinateType();
			if (permittedJointMask.contains(sourceJ)) {
				JointPosition targetJP = targetFrame.getJointPositionForJoint(sourceJ);
				if (targetJP != null) {
					targetCT = targetJP.getCoordinateType();
					if (targetCT != FLOAT_ABS_RANGE_OF_MOTION) {
						theLogger.warn("Cannot play animation into JP of coordinate type: " + targetCT);
						continue;
					}
					if (sourceCT != FLOAT_REL_RANGE_OF_MOTION) {
						theLogger.warn("Cannot play animation from JP of coordinate type: " + sourceCT);
						continue;
					}
					double targetPrePos = targetJP.getCoordinateFloat(FLOAT_ABS_RANGE_OF_MOTION);
					double increment = sourceJP.getCoordinateFloat(FLOAT_REL_RANGE_OF_MOTION);
					// Range Center is nominal middle of range of motion - NOT same as "default pos"
					// Max "dist" is 0.5
					// Apply compression as we near the boundary.					
					double distFromRangeCenter = Math.abs(targetPrePos - 0.5);
					double compressionMultiplier = 1.0 - 2.0 * distFromRangeCenter;
					// But compression is applied only in the OUTWARD direction!
					if (((targetPrePos - 0.5) * increment) < 0.0) {
						compressionMultiplier = 1.0;
					}
					double resultPos = targetPrePos + increment * compressionMultiplier;
					// Hard limit to range of motion, of course (and this is also limited at lower layers).
					if (resultPos <= 0.0) {
						resultPos = 0.0;
						theLogger.warn("Truncated motion at 0.0 ABS_ROM position of " + sourceJ);
					} 
					if (resultPos >= 1.0) {
						resultPos = 1.0;
						theLogger.warn("Truncated motion at 1.0 ABS_ROM position of " + sourceJ);
					}
					targetJP.setCoordinateFloat(targetCT, resultPos);
				} else {
					if (okToAddDim) {
						theLogger.warn("Dimension adding not implemented yet!");
					}					
				}
			}
		}
	}
	public Frame copyAndConvert(JointStateCoordinateType targetCoordinateType) {
		Frame resultFrame = null;
		if ((targetCoordinateType == FLOAT_ABS_RANGE_OF_MOTION) || 
					(targetCoordinateType == FLOAT_ABS_LOPSIDED_PIECEWISE_LINEAR)) {
			resultFrame = new Frame();
			for (JointPosition sourceJP: myPositions) {
				JointPosition convertedJP = sourceJP.convertToCooordinateType(targetCoordinateType);
				if (convertedJP != null) {
					resultFrame.addPosition(convertedJP);
				}
			}
		} else  {
			throw new RuntimeException("Cannot convert to coordinate type: " + targetCoordinateType);
		}
		return resultFrame;
	}
	public Frame copy() {
		Frame<JointPosition> resultFrame = new Frame<JointPosition>();
		for (JP sourceJP: myPositions) {
			JointPosition destJP = sourceJP.copy();
			resultFrame.addPosition(destJP);
		}
		return resultFrame;
	}
	public void shallowCopyPositionsFrom(Frame<JP> source) {
		for (JP sourceJP: source.myPositions) {
			addPosition(sourceJP);
		}
	}
	/*
	public static  <FT extends Frame> FT sumCompatibleFrameJoints(FT f1, FT f2, FT result) {
		if (f1 == null) {
			result.shallowCopyPositionsFrom(f2);
		} else if (f2 == null) {
			result.shallowCopyPositionsFrom(f1);
		}

		Set<Joint> js1 = f1.getUsedJointSet();
		Set<Joint> js2 = f2.getUsedJointSet();
		Set<Joint> allJoints = new HashSet<Joint>(js1);
		allJoints.addAll(js2);
		return new FT();
		// weightedSumSelectedJoints(f1, 1.0, f2, 1.0, allJoints);
	}
	 */
	public static Frame sumCompatibleFrames(Frame f1, Frame f2) {
		if (f1 == null) {
			return f2;
		} else if (f2 == null) {
			return f1;
		}
		
		Set<Joint> js1 = f1.getUsedJointSet();
		Set<Joint> js2 = f2.getUsedJointSet();
		Set<Joint> allJoints = new HashSet<Joint>(js1);
		allJoints.addAll(js2);
		return weightedSumSelectedJoints(f1, 1.0, f2, 1.0, allJoints);
	}
	public static Frame weightedSumCommonJoints(Frame f1, double w1, Frame f2, double w2) {
		if ((f1 == null) || (f2 == null)) {
			return null;
		}
		Set<Joint> js1 = f1.getUsedJointSet();
		Set<Joint> js2 = f2.getUsedJointSet();
		Set<Joint> commonJoints = new HashSet<Joint>(js1);
		commonJoints.retainAll(js2);
		return weightedSumSelectedJoints(f1, w1, f2, w2, commonJoints);
	}

	public static Frame weightedSumSelectedJoints(Frame f1, double w1,
				Frame f2, double w2, Set<Joint> selJoints) {
		Frame result = new Frame();
		for (Joint j: selJoints) {
			JointPosition jp1 = f1.getJointPositionForJoint(j);
			JointPosition jp2 = f2.getJointPositionForJoint(j);
			JointPosition sumJP = JointPosition.weightedSumJointPositions(jp1, w1, jp2, w2);
			result.addPosition(sumJP);
		}
		return result;
	}
	public static boolean verifySameJointsUsed(Frame f1, Frame f2) {
		Set<Joint> used1 = f1.getUsedJointSet();
		Set<Joint> used2 = f2.getUsedJointSet();
		if (!used1.equals(used2)) {
			theLogger.error("Joint sets are not equal.  #1=" + used1 + ", #2=" + used2);
			throw new RuntimeException("Joint sets are not equal - see warning msg");
		}
		return true;
	}
	// Frames must contain same joints, and all coords must be differentiable to produce outType.
	public static Frame computeDerivativeFrame(JointStateCoordinateType outType, Frame prevPosAR, Frame currPosAR, double timeSec) {
		if (!verifySameJointsUsed(prevPosAR, currPosAR)) {
			theLogger.warn("Used joints differ for prev=" + prevPosAR + " and curr=" + currPosAR);
			return null;
		}
		Frame result = new Frame();
		Set<Joint> usedJoints = prevPosAR.getUsedJointSet();
		for (Joint j: usedJoints) {
			JointPosition prevJP = prevPosAR.getJointPositionForJoint(j);
			JointPosition currJP = currPosAR.getJointPositionForJoint(j);
			JointPosition derivJP = JointPosition.differentiate(outType, prevJP, currJP, timeSec);
			result.addPosition(derivJP);
		}
		return result;
	}



	public Frame integrate(double time) {
		Frame result = new Frame();
		for (JointPosition jp: this.getAllPositions()) {
			JointPosition ijp = jp.integrate(time);
			result.addPosition(ijp);
		}
		return result;
	}
	public void multiplyByScalar(double scalar) {
		for (JointPosition jp: this.getAllPositions()) {
			jp.multiplyByScalar(scalar);
		}
	}
	public void addDeltaFrame(Frame delta) {
		// Mutates this frame in place, and ignores any JPs in delta which are not in this Frame
		for (JointPosition jp: this.getAllPositions()) {
			Joint j = jp.getJoint();
			JointPosition deltaJP = delta.getJointPositionForJoint(j);
			// Requires jp in Abs-ROM and delta in Rel-ROM
			if (deltaJP != null) {
				jp.addDelta(deltaJP);
			}
		}		
	}
	public void truncate() {
		for (JointPosition jp: getAllPositions()) {
			jp.truncate();
		}
	}
	public void verifyCoordinateTypeCompatibility(JointStateCoordinateType ctype) {
		for (JointPosition jp: getAllPositions()) {
			jp.verifyCoordinateTypeCompatibility(ctype);
		}		
	}
	public Frame getSubframe(Set<Joint> joints, boolean ignoreMissingPositions) {
		Frame resultF = new Frame();
		for (Joint j: joints) {
			JointPosition jp = this.getJointPositionForJoint(j);
			if (jp != null) {
				resultF.addPosition(jp);
			} else {
				if (!ignoreMissingPositions) {
					throw new  RuntimeException("Missing expected jointPosition for " + j);
				}
			}
		}
		return resultF;
	}
	public double computeNorm(double normPower, JointStateCoordinateType ctype) {
		double sum = 0.0;
		for (JP jp : getAllPositions()) {
			double jv = jp.getCoordinateFloat(ctype);
			double term = Math.abs(Math.pow(jv, normPower));
			sum += term;
		}
		double powInv = 1.0 / normPower;
		double norm = Math.pow(sum, powInv);
		return norm;
	}
	public String toString() {
		return "Frame[" + myPositions + "]";
	}
}
