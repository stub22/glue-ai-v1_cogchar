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
 *
 */
package org.cogchar.render.opengl.bony.state;


import com.jme3.math.FastMath;

import com.jme3.math.Quaternion;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BoneState {
	public	String		myBoneName;
	// JME3 docs are messed up regarding "yaw, pitch, roll" terminology.
	// However, our current understanding is that rots are applied in this order,
	// with this meaning for an airplane flying towards X-infinity (at our right)
	// with Y straight up, and Z towards us.
	
	public	float		rot_X_bank_A3rd, rot_Y_heading_A1st, rot_Z_attitude_A2nd;
	
	public BoneState(String name) {
		myBoneName = name;
	}
	public String getBoneName() { 
		return myBoneName;
	}
	public Quaternion getRotQuat() { 
		Quaternion q = new Quaternion();
		/* 
		 * http://jmonkeyengine.org/groups/general-2/forum/topic/definition-of-pitch-yaw-roll-in-jmonkeyengine/?topic_page=2&num=15
		 * 
		 * Rotations are applied in the order
		 * 1) heading=yRot
		 * 2) attitude=zRot
		 * 3) bank=xRot
		 */
		
		q.fromAngles(rot_X_bank_A3rd, rot_Y_heading_A1st, rot_Z_attitude_A2nd);
		return q;
	}
 
}
