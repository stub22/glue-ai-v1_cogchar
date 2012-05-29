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

package org.cogchar.api.animoid.world;

import java.util.ArrayList;
import java.util.List;

import org.cogchar.api.animoid.protocol.JVFrame;
import org.cogchar.api.animoid.protocol.Joint;
import org.cogchar.api.animoid.protocol.JointVelocityAROMPS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class SummableWJTrajectoryList<Traj extends WorldJointTrajectory>  {
	private static Logger		theLogger = LoggerFactory.getLogger(SummableWJTrajectoryList.class.getName());

	private		List<Traj>			myTrajectories;
	public SummableWJTrajectoryList() {
		myTrajectories = new ArrayList<Traj>();
	}
	public Traj getTrajectoryForJoint(Joint j) {
		for (Traj t: myTrajectories) {
			if (t.getWorldJoint().getJoint().equals(j)) {
				return t;
			}
		}
		return null;
	}
	public void addTrajectory(Traj t) {
		myTrajectories.add(t);
	}
	public List<Traj> getTrajectories() {
		return myTrajectories;
	}
	public double getTotalInitialWorldPositionDeg() {
		double totalPos = 0.0;
		for (Traj t : myTrajectories) {
			double twpd = t.getInitialJointStateSnap().getWorldPosDeg();
			totalPos += twpd;
		}
		return totalPos;
	}
	public double getTotalWorldRoomDegAboveInitPos() {
		double totalRoom = 0.0;
		for (Traj t : myTrajectories) {
			double troom = t.getInitialJointStateSnap().getRoomAbovePosInWorldDeg();
			totalRoom += troom;
		}
		return totalRoom;
	}
	public double getTotalWorldRoomDegBelowInitPos() {
		double totalRoom = 0.0;
		for (Traj t : myTrajectories) {
			double troom = t.getInitialJointStateSnap().getRoomBelowPosInWorldDeg();
			totalRoom += troom;
		}
		return totalRoom;
	}
	public boolean isGoalInRange(WorldGoalPosition wgp, boolean warnOnFailure) {
		double worldGoalDeltaDeg  = wgp.deltaDegrees;
		if (worldGoalDeltaDeg > 0.0) {
			double totalRoomAbove = getTotalWorldRoomDegAboveInitPos();
			if (worldGoalDeltaDeg > totalRoomAbove) {
				if (warnOnFailure) {
					theLogger.warn("Goal is out of range on high side[goalDeltaDeg=" + worldGoalDeltaDeg
							+ ", totalRoomAbove=" + totalRoomAbove);
				}
				return false;
			}
		} else {
			double totalRoomBelow = getTotalWorldRoomDegBelowInitPos();
			if (worldGoalDeltaDeg < -1.0 * totalRoomBelow) {
				if (warnOnFailure) {
					theLogger.warn("Goal is out of range on low side[goalDeltaDeg=" + worldGoalDeltaDeg
							+ ", totalRoomBelow=" + totalRoomBelow);
				}
				return false;
			}
		}
		return true;
	}
	/** @return frame containing the practical velocities needed to ramp from initial
	 *  pos on all the trajectories to a particular modeled position.
	 */
	public JVFrame getVelocityFrameForJumpFromStartToTime(double targetOffsetSec) {
		JVFrame velFrame = new JVFrame();
		for (Traj t :  myTrajectories) {
			JointVelocityAROMPS jvel = t.getJointVelocityForJumpFromStartToTarget(targetOffsetSec);
			velFrame.addPosition(jvel);
		}
		return velFrame;
	}
}
