/*
 *  Copyright 2008 Hanson Robotics Inc.
 *  All Rights Reserved.
 */

package org.cogchar.animoid.protocol;

import java.util.List;
import java.util.Map;
import org.cogchar.platform.util.TimeUtils;

/**
 * @author Stu Baurmann
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
