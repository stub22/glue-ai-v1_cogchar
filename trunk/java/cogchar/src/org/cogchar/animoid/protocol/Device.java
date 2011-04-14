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
