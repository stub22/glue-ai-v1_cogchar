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

package org.cogchar.api.animoid.protocol;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class Robot implements Serializable {
	private	String					myName;
	private Map<String, Joint>		myJointsByName = new HashMap<String, Joint>();
	private Map<Integer, Joint>		myJointsByOldLogicalJointNumber = new HashMap<Integer, Joint>();
	public Robot(String name) {
		myName = name;
	}
	public String getName() {
		return myName;
	}
	public void registerJoint(Joint j) {
		myJointsByName.put(j.getJointName(), j);
		myJointsByOldLogicalJointNumber.put(j.oldLogicalJointNumber, j);
	}
	public Joint getJointForName(String n) {
		return myJointsByName.get(n);
	}
	public Joint getJointForOldLogicalNumber(Integer oln) { 
		return myJointsByOldLogicalJointNumber.get(oln);
	}
	public Set<String> getJointNameSet() {
		Set<String> jointNameSet = myJointsByName.keySet();
		return jointNameSet;
	}
	public Collection<Joint> getJoints() {
		return myJointsByName.values();
	}
}
