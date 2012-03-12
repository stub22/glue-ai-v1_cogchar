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

import java.util.List;

import com.jme3.math.Quaternion;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BoneState {
	public	String		myBoneName;
	public	float		rot_X_pitch, rot_Y_roll, rot_Z_yaw;
	
	public BoneState(String name) {
		myBoneName = name;
	}
	public String getBoneName() { 
		return myBoneName;
	}
	public Quaternion getRotQuat() { 
		Quaternion q = new Quaternion();
		q.fromAngles(rot_X_pitch, rot_Y_roll, rot_Z_yaw);
		return q;
	}

}
