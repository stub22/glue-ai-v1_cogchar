/*
 *  Copyright 2008 Hanson Robotics Inc.
 *  All Rights Reserved.
 */

package org.cogchar.animoid.protocol;

import org.cogchar.animoid.protocol.Frame;
import org.cogchar.animoid.protocol.JPARFrame;
import org.cogchar.animoid.protocol.JointPositionSnapshot;

/**
 * @author Stu Baurmann
 */
public interface IServoMonitor {
	// At most one of these snapshots may be null!
	public void servoSnapshotUpdate(JointPositionSnapshot lopsidedSnapshot, JPARFrame absRomSnapshot);
}
