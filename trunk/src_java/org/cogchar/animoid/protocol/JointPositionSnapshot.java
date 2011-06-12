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

import java.util.List;
import java.util.Map;
import org.cogchar.platform.util.TimeUtils;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class JointPositionSnapshot extends Frame {
	// private Map<Integer, JointPosition> myJointPositionsByID = new HashMap<Integer, JointPosition>();
	private long	mySnapshotTimestamp;
	
	public JointPositionSnapshot(){}
	public JointPositionSnapshot(Robot r, JointStateCoordinateType coordType,
				Map<Integer, Double>	positionValuesByJointID) {
		for (Integer jid : positionValuesByJointID.keySet()) {
			Joint j = r.getJointForOldLogicalNumber(jid);
			JointPosition jp = new JointPosition(j);
			// Note the coordinate system is vague at this point. We just have a map of doubles.
			Double absPos = positionValuesByJointID.get(jid);
			// Now we assert a coordinate system type
			jp.setCoordinateFloat(coordType, absPos);
			addPosition(jp);
			// myJointPositionsByID.put(jid, jp);
		}
		setSnapshotTimestamp(TimeUtils.currentTimeMillis());
	}
	public JointPositionSnapshot(List<JointPosition> jps){
		for(JointPosition jp : jps){
			addPosition(jp);
		}
		setSnapshotTimestamp(TimeUtils.currentTimeMillis());
	}
	/*  Please folks - no hardcoded muscle joint IDs!!!
	public static JointPositionSnapshot getGazeSnapshot(Frame f){
		List<Integer> jointIds = CollectionUtils.list(
				MuscleJoint.BothEyes_Up.getJointID(),
				MuscleJoint.BothEyes_TurnRight.getJointID(),
				MuscleJoint.Head_TurnRight.getJointID(),
				MuscleJoint.UpperNod_Forward.getJointID());
		f = f.copyAndConvert(JointStateCoordinateType.FLOAT_ABS_RANGE_OF_MOTION);
		JointPositionSnapshot jps = new JointPositionSnapshot();
				for(Integer i : jointIds){
			jps.addPosition(f.getJointPositionForOldLogicalJointNumber(i));
		}
		jps.setSnapshotTimestamp(TimeUtils.currentTimeMillis());
		return jps;
	}
	 */

	public long getSnapshotTimestamp() {
		return mySnapshotTimestamp;
	}

	public void setSnapshotTimestamp(long mySnapshotTimestamp) {
		this.mySnapshotTimestamp = mySnapshotTimestamp;
	}

	
}
