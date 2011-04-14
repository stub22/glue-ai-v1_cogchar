/*
 *  Copyright 2008 Hanson Robotics Inc.
 *  All Rights Reserved.
 */

package org.cogchar.animoid.config;

import org.cogchar.animoid.world.WorldJoint;


/**
 * @author Stu Baurmann
 */

public class GazeJoint extends WorldJoint {

	public enum Direction {
		LEFT,
		RIGHT,
		UP,
		DOWN
		// Other directions could be added here, e.g. to represent head-tilt, without affecting the rest of the class.
		// (But GazePlan would need some work to restore consistency, since it assumes all links are either horiz or vert).
	}
	private		Direction	positiveDirection;

	public Direction getPositiveDirection() {
		return positiveDirection;
	}

	public boolean isPixelNumberSensePositive() {
		return ((positiveDirection == Direction.RIGHT) || (positiveDirection == Direction.DOWN));
	}
	public boolean isHorizontal() {
		return ((positiveDirection == Direction.LEFT) || (positiveDirection == Direction.RIGHT));
	}
	public boolean isEgocentricDirectionSensePositive() {
		return ((positiveDirection == Direction.RIGHT) || (positiveDirection == Direction.UP));
	}

	@Override public boolean isWorldSenseInverted() {
		return !isEgocentricDirectionSensePositive();
	}


	public String toString() {
		return "\nGazeJoint["
				+ "\npositiveDirection=" + getPositiveDirection()
				+ "\nsuper=" + super.toString()
				+ "]";
	}
}
