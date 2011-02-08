/*
 *  Copyright 2008 Hanson Robotics Inc.
 *  All Rights Reserved.
 */

package org.cogchar.animoid.protocol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Stu Baurmann
 */
public class Library implements Serializable {
	private	Map<String, Animation>	myAnimationsByName = new HashMap<String, Animation>();
	public void registerAnimation(Animation a) {
		myAnimationsByName.put(a.getName(), a);
	}
	public Animation getAnimationForName(String name) {
		return myAnimationsByName.get(name);
	}
	public List<String> getAnimationNames() {
		Set<String> nameSet  = myAnimationsByName.keySet();
		ArrayList<String> nameList = new ArrayList<String>(nameSet);
		return nameList;
	}	
}
