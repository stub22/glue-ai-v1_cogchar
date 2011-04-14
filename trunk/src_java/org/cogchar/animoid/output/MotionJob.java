/*
 *  Copyright 2008 Hanson Robotics Inc.
 *  All Rights Reserved.
 */

package org.cogchar.animoid.output;

import java.util.Collection;
import java.util.Set;
import org.cogchar.animoid.calc.estimate.TimeKeeper;
import org.cogchar.animoid.config.AnimoidConfig;
import org.cogchar.animoid.protocol.Frame;
import org.cogchar.animoid.protocol.JVFrame;
import org.cogchar.animoid.protocol.Joint;

/**
 *
 * @author Stu Baurmann
 */
public abstract class MotionJob extends AnimoidJob {
	private		transient		TimeKeeper		myTimeKeeper;

	public MotionJob(AnimoidConfig aconf) {
		super(aconf);
	}
	public Collection<Joint> getCautionJoints() {
		return null;
	}
	public abstract JVFrame contributeVelFrame(Frame prevPosAbsRomFrame, JVFrame prevVelRomFrame,
				Set<Joint> cautionJoints);
	
	public void setTimeKeeper(TimeKeeper tk) {
		myTimeKeeper = tk;
	}
	public TimeKeeper getTimeKeeper() {
		return myTimeKeeper;
	}
}
