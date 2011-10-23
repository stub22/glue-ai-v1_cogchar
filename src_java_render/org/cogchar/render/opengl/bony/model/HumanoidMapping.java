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
package org.cogchar.render.opengl.bony.model;

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
public class HumanoidMapping {

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
