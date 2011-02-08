/*
 *  Copyright 2008 Hanson Robotics Inc.
 *  All Rights Reserved.
 */

package org.cogchar.animoid.protocol;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Stu Baurmann
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
