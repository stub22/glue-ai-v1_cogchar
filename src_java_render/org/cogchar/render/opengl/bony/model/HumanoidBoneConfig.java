/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.render.opengl.bony.model;

import java.util.ArrayList;
import java.util.List;
import com.jme3.bullet.control.KinematicRagdollControl;
import com.jme3.math.FastMath;

/**
 *
 * @author pow
 */
public class HumanoidBoneConfig {
	private	List<HumanoidBoneDesc> myBoneDescs;
	public HumanoidBoneConfig(boolean useSindbadDefaultDescs) {
		if (useSindbadDefaultDescs) {
			makeSinbadDefaultBoneDescs();
		}
	}
	public void addBoneDesc(String spatialName) {
		HumanoidBoneDesc hbd = new HumanoidBoneDesc(spatialName);
		myBoneDescs.add(hbd);
	}
	public void attachRagdollBones(HumanoidRagdollWrapper hrw) { 
		for (HumanoidBoneDesc hbd : myBoneDescs) {
			hrw.attachRagdollBone(hbd);
		}
	}
	public List<HumanoidBoneDesc> getBoneDescs() { 
		return myBoneDescs;
	}
	public int getConfiguredBoneCount() { 
		return myBoneDescs.size();
	}
	private void makeSinbadDefaultBoneDescs() { 
		myBoneDescs = new ArrayList<HumanoidBoneDesc>();
		
		// Explicitly bound to KRD in Bowl-At-Sinbad demo
        addBoneDesc("Ulna.L");
        addBoneDesc("Ulna.R");
        addBoneDesc("Chest");
        addBoneDesc("Foot.L");
        addBoneDesc("Foot.R");
        addBoneDesc("Hand.R");
        addBoneDesc("Hand.L");
        addBoneDesc("Neck");
        addBoneDesc("Root");
        addBoneDesc("Stomach");
        addBoneDesc("Waist");
        addBoneDesc("Humerus.L");
        addBoneDesc("Humerus.R");
        addBoneDesc("Thigh.L");
        addBoneDesc("Thigh.R");
        addBoneDesc("Calf.L");
        addBoneDesc("Calf.R");
        addBoneDesc("Clavicle.L");
        addBoneDesc("Clavicle.R");	
		
		// extras
        addBoneDesc("Neck");
        addBoneDesc("Head");
        addBoneDesc("Eye.L");
        addBoneDesc("Eye.R");
        addBoneDesc("Brow.C");
        addBoneDesc("Brow.L");
        addBoneDesc("Brow.R");
        addBoneDesc("Cheek.L");
        addBoneDesc("Cheek.R");
        addBoneDesc("UpperLip");
        addBoneDesc("Jaw");
        addBoneDesc("TongueBase");
        addBoneDesc("LowerLip");
	}
	public static void applyHumanoidJointLimits(KinematicRagdollControl krc) { 
		float eighth_pi = FastMath.PI * 0.125f;
        krc.setJointLimit("Waist", eighth_pi, eighth_pi, eighth_pi, eighth_pi, eighth_pi, eighth_pi);
        krc.setJointLimit("Chest", eighth_pi, eighth_pi, 0, 0, eighth_pi, eighth_pi);
		/*
		krc.setJointLimit("Foot.L", eighth_pi, eighth_pi, 0, 0, eighth_pi, eighth_pi);
		krc.setJointLimit("Thigh.L", eighth_pi, eighth_pi, 0, 0, eighth_pi, eighth_pi);
		krc.setJointLimit("Calf.L", eighth_pi, eighth_pi, 0, 0, eighth_pi, eighth_pi);
		krc.setJointLimit("Hand.L", eighth_pi, eighth_pi, 0, 0, eighth_pi, eighth_pi);
		krc.setJointLimit("Humerus.L", eighth_pi, eighth_pi, 0, 0, eighth_pi, eighth_pi);
		krc.setJointLimit("Ulna.L", eighth_pi, eighth_pi, 0, 0, eighth_pi, eighth_pi);
		 * 
		 */
		//JMonkey original commented out line and comment...
        //  Oto's head is almost rigid
        //    myHumanoidKRC.setJointLimit("head", 0, 0, eighth_pi, -eighth_pi, 0, 0);
	}	
}
