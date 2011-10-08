/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.demo.bony;

import com.jme3.animation.AnimControl;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.control.KinematicRagdollControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Node;
import com.jme3.scene.debug.SkeletonDebugger;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class HumanoidFuncs {
	public static void attachDebugSkeleton(Node humanoidModel, AssetManager assetMgr) { 
		  
        AnimControl humanoidControl = humanoidModel.getControl(AnimControl.class);
        SkeletonDebugger humanoidSkeletonDebug = new SkeletonDebugger("skeleton", humanoidControl.getSkeleton());
        Material mat2 = new Material(assetMgr, "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.getAdditionalRenderState().setWireframe(true);
        mat2.setColor("Color", ColorRGBA.Green);
        mat2.getAdditionalRenderState().setDepthTest(false);
        humanoidSkeletonDebug.setMaterial(mat2);
        humanoidSkeletonDebug.setLocalTranslation(humanoidModel.getLocalTranslation());
		
		humanoidModel.attachChild(humanoidSkeletonDebug);
	}
    public static void addHumanoidBonesToRagdoll(KinematicRagdollControl krc) {
        krc.addBoneName("Ulna.L");
        krc.addBoneName("Ulna.R");
        krc.addBoneName("Chest");
        krc.addBoneName("Foot.L");
        krc.addBoneName("Foot.R");
        krc.addBoneName("Hand.R");
        krc.addBoneName("Hand.L");
        krc.addBoneName("Neck");
        krc.addBoneName("Root");
        krc.addBoneName("Stomach");
        krc.addBoneName("Waist");
        krc.addBoneName("Humerus.L");
        krc.addBoneName("Humerus.R");
        krc.addBoneName("Thigh.L");
        krc.addBoneName("Thigh.R");
        krc.addBoneName("Calf.L");
        krc.addBoneName("Calf.R");
        krc.addBoneName("Clavicle.L");
        krc.addBoneName("Clavicle.R");

    }
	public static void applyHumanoidJointLimits(KinematicRagdollControl krc) { 
		float eighth_pi = FastMath.PI * 0.125f;
        krc.setJointLimit("Waist", eighth_pi, eighth_pi, eighth_pi, eighth_pi, eighth_pi, eighth_pi);
        krc.setJointLimit("Chest", eighth_pi, eighth_pi, 0, 0, eighth_pi, eighth_pi);
		//JMonkey original commented out line and comment...
        //  Oto's head is almost rigid
        //    myHumanoidKRC.setJointLimit("head", 0, 0, eighth_pi, -eighth_pi, 0, 0);
	}
}
