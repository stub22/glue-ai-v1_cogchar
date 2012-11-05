/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.bind.rk.aniconv;

import java.util.HashMap;
import java.util.Map;
import org.cogchar.api.skeleton.config.BoneRotationAxis;

/**
 * A temporary data structure in poor style and taste if there ever was one!
 * Contains our "wild guesses" about the correspondence between Axx anim channel names and bone names / rotation axes
 * in the VW Bone config
 * 
 * In the event this lookup table takes on any kind of more permanent utility, it will most certainly move to our semantic repo
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class JunkyConversionTable {
	
	public Map<String, JointInfo> lookupTable = new HashMap<String, JointInfo>();
	
	class JointInfo {
		public String boneName;
		public BoneRotationAxis axis;
		
		public JointInfo(String name, BoneRotationAxis bra) {
			boneName = name;
			axis = bra;
		}
	}
	
	// How junky is this? We'll fill in the data here in the constuctor, that's how junky!
	public JunkyConversionTable() {
		/*
		lookupTable.put("cc_l_TorsoCtrl_01_rotateY", new JointInfo("rig_torso_01", BoneRotationAxis.X_ROT));
		lookupTable.put("cc_headCtrl_01_rotateX", new JointInfo("rig_neck_02", BoneRotationAxis.X_ROT));
		lookupTable.put("cc_headCtrl_01_rotateZ", new JointInfo("rig_neck01", BoneRotationAxis.Z_ROT));
		lookupTable.put("rig_r_eyebrow_ctrl_01_rotateZ1", new JointInfo("rig_r_eyebrow_ctrl_01", BoneRotationAxis.Z_ROT));
		lookupTable.put("cc_l_eyeCtrl_01_TopLid", new JointInfo("jnt_l_eyeTopLid_01", BoneRotationAxis.X_ROT));
		lookupTable.put("cc_r_eyeCtrl_01_TopLid", new JointInfo("jnt_r_eyeTopLid_01", BoneRotationAxis.Z_ROT));
		lookupTable.put("cc_jawCtrl_01_rotateX", new JointInfo("rig_jaw_01", BoneRotationAxis.Z_ROT));
		lookupTable.put("rig_l_lipCorner_01_rotateZ", new JointInfo("rig_l_lipCorner_01", BoneRotationAxis.X_ROT));
		lookupTable.put("rig_r_lipCorner_01_rotateZ", new JointInfo("rig_r_lipCorner_01", BoneRotationAxis.Z_ROT));
		lookupTable.put("cc_l_armRotZCtrl_01_rotateZ", new JointInfo("rig_l_shoulder_01", BoneRotationAxis.X_ROT));
		lookupTable.put("cc_l_elbowRotXCtrl_01_rotateX", new JointInfo("rig_l_elbow_yaw_01", BoneRotationAxis.X_ROT));
		lookupTable.put("cc_l_elbowRotYCtrl_01_rotateY", new JointInfo("rig_l_elbow_pitch_01", BoneRotationAxis.Y_ROT));
		lookupTable.put("cc_l_wristRotXCtrl_01_rotateX", new JointInfo("rig_l_wrist_01", BoneRotationAxis.Y_ROT));
		lookupTable.put("cc_r_armRotZCtrl_01_rotateZ", new JointInfo("rig_r_shoulder_01", BoneRotationAxis.X_ROT));
		lookupTable.put("cc_r_elbowRotXCtrl_01_rotateX", new JointInfo("rig_r_elbow_yaw_01", BoneRotationAxis.X_ROT));
		lookupTable.put("cc_r_elbowRotYCtrl_01_rotateX", new JointInfo("rig_r_elbow_pitch_01", BoneRotationAxis.Y_ROT));
		lookupTable.put("cc_r_wristRotXCtrl_01_rotateX", new JointInfo("rig_r_wrist_01", BoneRotationAxis.X_ROT));
		lookupTable.put("cc_hipCtrl_01_rotateZ", new JointInfo("rig_l_hip_rotY_01", BoneRotationAxis.X_ROT));
		lookupTable.put("cc_l_legRotYCtrl_01_rotateZ", new JointInfo("rig_l_leg_01", BoneRotationAxis.Z_ROT));
		lookupTable.put("cc_l_footCtrl_01_rotateX", new JointInfo("rig_l_foot_01", BoneRotationAxis.Z_ROT));
		lookupTable.put("cc_r_legRotYCtrl_01_rotateY", new JointInfo("rig_r_leg_01", BoneRotationAxis.Y_ROT));
		lookupTable.put("cc_r_footCtrl_01_rotateX", new JointInfo("rig_r_foot_01", BoneRotationAxis.Z_ROT));
		*/
		lookupTable.put("rig_r_elbow_01_rotateY", new JointInfo("rig_r_elbow_pitch_01", BoneRotationAxis.Y_ROT));
		lookupTable.put("rig_r_elbow_01_rotateX", new JointInfo("rig_r_elbow_yaw_01", BoneRotationAxis.X_ROT));
		lookupTable.put("rig_r_arm_01_rotateZ", new JointInfo("rig_r_arm_01", BoneRotationAxis.Y_ROT));
		lookupTable.put("rig_r_arm_01_rotateY", new JointInfo("rig_r_arm_01", BoneRotationAxis.X_ROT));
		lookupTable.put("rig_r_arm_01_rotateX", new JointInfo("rig_r_arm_01", BoneRotationAxis.Z_ROT));
		lookupTable.put("rig_l_lipCorner_01_rotateX", new JointInfo("rig_l_lipCorner_01", BoneRotationAxis.Z_ROT)); // To prevent earlier rig_l_lipCorner_01/X_ROT match
		lookupTable.put("rig_l_lipCorner_01_rotateZ", new JointInfo("rig_l_lipCorner_01", BoneRotationAxis.X_ROT)); // To match r_lipcorner
		lookupTable.put("jnt_l_eyeTopLid_01_rotateZ", new JointInfo("jnt_l_eyeTopLid_01", BoneRotationAxis.X_ROT));
		lookupTable.put("rig_neck01_rotateX", new JointInfo("rig_neck_02", BoneRotationAxis.X_ROT)); // Neck Yaw
		lookupTable.put("rig_neck01_rotateY", new JointInfo("rig_neck01", BoneRotationAxis.X_ROT)); // Neck Pitch
		//lookupTable.put("rig_neck01_rotateZ", new JointInfo("rig_neck01", BoneRotationAxis.Z_ROT)); // Neck Roll
		lookupTable.put("jnt_l_eye_01_rotateX", new JointInfo("jnt_l_eye_01", BoneRotationAxis.Z_ROT)); //To prevent earlier jnt_l_eye_01/X_ROT match
		lookupTable.put("jnt_l_eye_01_rotateZ", new JointInfo("jnt_l_eye_01", BoneRotationAxis.X_ROT)); // Eyes Yaw
	}
	
}
