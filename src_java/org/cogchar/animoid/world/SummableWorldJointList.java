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


package org.cogchar.animoid.world;


import java.util.ArrayList;
import java.util.List;
import org.cogchar.animoid.protocol.SmallAngle;

/**
 *
 * @author Stu Baurmann
 */
public class SummableWorldJointList <WJ extends WorldJoint> {
	private		List<WJ>	myWorldJoints = new ArrayList<WJ>();
	public void addWorldJoint(WJ wj) {
		myWorldJoints.add(wj);
	}
	public boolean contains(WJ wj) {
		return myWorldJoints.contains(wj);
	}
	public double getMaxTotalWorldPosDeg() {
		double maxTotalDeg = 0.0;
		for (WJ wj : myWorldJoints) {
			maxTotalDeg += wj.getWorldMaxDegreesOffset();
		}
		return maxTotalDeg;
	}
	public double getMinTotalWorldPosDeg() {
		double minTotalDeg = 0.0;
		for (WJ wj : myWorldJoints) {
			minTotalDeg += wj.getWorldMinDegreesOffset();
		}
		return minTotalDeg;
	}
	public SmallAngle getMaxTotalWorldPosAngle() {
		return SmallAngle.makeFromDeg(getMaxTotalWorldPosDeg());
	}
	public SmallAngle getMinTotalWorldPosAngle() {
		return SmallAngle.makeFromDeg(getMinTotalWorldPosDeg());
	}

	public WorldGoalPosition getTruncatedGoalPosition(WorldGoalPosition wgp) {
		double maxDeg = getMaxTotalWorldPosDeg() - 0.001;
		if (wgp.degrees > maxDeg) {
			// Apply negative adjustment
			return wgp.makeAdjustedPosition(maxDeg - wgp.degrees);
		} else {
			double minDeg = getMinTotalWorldPosDeg() + 0.001;
			if (wgp.degrees < minDeg) {
				// apply positive adjustment
				return wgp.makeAdjustedPosition(minDeg - wgp.degrees);
			} else {
				return wgp;
			}
		}
	}
}
