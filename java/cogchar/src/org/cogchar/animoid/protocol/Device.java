/*
 *  Copyright 2008 Hanson Robotics Inc.
 *  All Rights Reserved.
 */

package org.cogchar.animoid.protocol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Stu Baurmann
 */
public class Device implements Serializable {
	public enum Type {
		SSC32_V20
	};
	private		String			myName;
	private		Type			myType;
	private		List<Joint>		myJoints;
	public Device(String name, Type t) {
		myName = name;
		myJoints = new ArrayList<Joint>();
	}
	public String getName() {
		return myName;
	}
	public Type getType() {
		return myType;
	}
	public void registerJoint(Joint j) {
		// should check for joint name uniqueness
		myJoints.add(j);
	}
	public Joint getJointForChannelID(String channelID) {
		for (Joint j: myJoints) {
			if (j.getDeviceChannelID().equals(channelID)) {
				return j;
			}
		}
		return null;
	}

	public List<Joint> getAllJoints() {
		// should return immutable copy
		return myJoints;
	}
}
