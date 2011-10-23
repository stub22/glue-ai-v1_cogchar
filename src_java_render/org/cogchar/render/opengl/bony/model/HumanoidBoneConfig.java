/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.render.opengl.bony.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pow
 */
public class HumanoidBoneConfig {
	private	List<HumanoidBoneDesc> myBoneDescs;
	public HumanoidBoneConfig() { 
		makeSinbadDefaultBoneDescs();
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
	private void makeSinbadDefaultBoneDescs() { 
		myBoneDescs = new ArrayList<HumanoidBoneDesc>();
		
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
	}
}
