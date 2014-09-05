/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.render.model.humanoid;

import org.cogchar.api.humanoid.HumanoidFigureConfig;
import org.cogchar.api.humanoid.FigureBoneDesc;
import org.cogchar.api.humanoid.FigureBoneConfig;
import org.cogchar.api.humanoid.HumanoidFigureConfig;

import org.appdapter.core.name.Ident;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.Bone;
import com.jme3.animation.LoopMode;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.RagdollCollisionListener;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.math.FastMath;

//import org.cogchar.render.sys.physics.PhysicsStuffBuilder;
import org.cogchar.render.goody.physical.GoodyPhysicsStuffBuilder;
import com.jme3.animation.AnimControl;
import com.jme3.animation.Skeleton;
import com.jme3.animation.SkeletonControl;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.control.KinematicRagdollControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.debug.SkeletonDebugger;
import java.util.List;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.render.model.bony.StickFigureTwister;
import org.cogchar.render.model.bony.BoneState;
import org.cogchar.render.model.bony.FigureState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class HumanoidFigure_SinbadTest extends HumanoidFigure {
	public static String 	
			ANIM_STAND_FRONT = "StandUpFront",
			ANIM_STAND_BACK = "StandUpBack",
			ANIM_DANCE = "Dance",
			ANIM_IDLE_TOP = "IdleTop";

	private static float DEFAULT_ANIM_BLEND_RATE = 0.5f;
	
	public HumanoidFigure_SinbadTest(HumanoidFigureConfig hfc) { 
		super(hfc);
	}
	public void makeSinbadStandUp() { 
		Vector3f v = new Vector3f();
		Node sceneNode = getFigureNode();
		HumanoidFigureConfig hfc = getFigureConfig();
		v.set(sceneNode.getLocalTranslation());
		v.y = hfc.getInitY();
		sceneNode.setLocalTranslation(v);
		Quaternion q = new Quaternion();
		float[] angles = new float[3];
		sceneNode.getLocalRotation().toAngles(angles);
		q.fromAngleAxis(angles[1], Vector3f.UNIT_Y);
		sceneNode.setLocalRotation(q);
		AnimChannel animChan = getFigureAnimChannel();
		HumanoidRagdollControl ragdollControl = getRagdollControl();
	
		if (angles[0] < 0) {
			animChan.setAnim(ANIM_STAND_BACK);
			ragdollControl.blendToKinematicMode(DEFAULT_ANIM_BLEND_RATE);
		} else {
			animChan.setAnim(ANIM_STAND_FRONT);
			ragdollControl.blendToKinematicMode(DEFAULT_ANIM_BLEND_RATE);
		}
	}
	public void runSinbadBoogieAnim() { 
		try {
			AnimChannel animChan = getFigureAnimChannel();
			HumanoidRagdollControl ragdollControl = getRagdollControl();
			animChan.setAnim(ANIM_DANCE);
			ragdollControl.blendToKinematicMode(DEFAULT_ANIM_BLEND_RATE);
		} catch (Throwable t) {
			getLogger().warn("Character cannot boogie, nickname is: {}; {}", getNickname(), t);
		}
	}	
	@Override public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
		super.onAnimCycleDone(control, channel, animName);
//        if(channel.getAnimationName().equals("StandUpFront")){
//            channel.setAnim("Dance");
//        }

		if (channel.getAnimationName().equals(ANIM_STAND_BACK) || channel.getAnimationName().equals(ANIM_STAND_FRONT)) {
			channel.setLoopMode(LoopMode.DontLoop);
			channel.setAnim(ANIM_IDLE_TOP, 5);
			channel.setLoopMode(LoopMode.Loop);
		}
//        if(channel.getAnimationName().equals("IdleTop")){
//            channel.setAnim("StandUpFront");
//        }

	}	
}
