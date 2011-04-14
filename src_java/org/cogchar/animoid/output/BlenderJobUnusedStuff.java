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

package org.cogchar.animoid.output;

import java.util.Map;
import java.util.Set;
import org.cogchar.animoid.protocol.Frame;
import org.cogchar.animoid.protocol.Joint;

/**
 *
 * @author Stu Baurmann
 */
public class BlenderJobUnusedStuff {
		/* More disabled stuff:
		// Frame deltaV = accelFrame.integrate(mySecondsPerFrame);
		// Frame nextVelFrame = Frame.sumCompatibleFrames(prevVelFrame, deltaV);
		// multiplyByScalar mutates the values in place, so deltaV "becomes" halfDeltaV
		Frame halfDeltaV = deltaV;   halfDeltaV.multiplyByScalar(0.5);
		Frame averageV = Frame.sumCompatibleFrames(myPrevVelFrame, halfDeltaV);
		// Averaging the velocity of last frame and this frame provides some smoothing
		// and damping of oscillations , in exchange for
		// increased rise time (TODO: quantify assumptions under which this is true).
		// But doing this increases the complexity of sending the robot to a position
		// by forcing an immediate move, which the simplified impulse-acceleration below
		// facilitiates.
		*/
	private void updateContribCounts(Map<Joint, Integer> contribCounts, Frame velFrame) {
		Set<Joint> jobJointsUsed = velFrame.getUsedJointSet();
		if (jobJointsUsed != null) {
			for (Joint j : jobJointsUsed) {
				Integer cc = contribCounts.get(j);
				if (cc == null) {
					cc = 1;
				} else {
					cc = cc + 1;
				}
				contribCounts.put(j, cc);
			}
		}
	}
	/*
	private void scaleSummedVelocitiesIntoAverages(Frame velSumFrame, Map<Joint, Integer> contribCounts) {
		// Scale the summed velocities by the number of contributions, to produce
		// an average.
		for (Joint j : velSumFrame.getUsedJointSet()) {
			Integer cc = contribCounts.get(j);
			double multiplier = 1.0 / cc.doubleValue();
			JointPosition velJP = velSumFrame.getJointPositionForJoint(j);
			velJP.multiplyByScalar(multiplier);
		}
	}
	 */
}
