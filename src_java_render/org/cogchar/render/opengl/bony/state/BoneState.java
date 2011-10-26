/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.render.opengl.bony.state;

import com.jme3.math.Quaternion;

/**
 *
 * @author pow
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
