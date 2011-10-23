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

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.Bone;
import com.jme3.animation.LoopMode;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.RagdollCollisionListener;
import com.jme3.bullet.control.KinematicRagdollControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

import org.cogchar.render.opengl.bony.world.PhysicsStuffBuilder;
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
public class HumanoidRagdollWrapper implements RagdollCollisionListener, AnimEventListener {
	private Node myHumanoidModelNode;
	private KinematicRagdollControl myHumanoidKRC;
	private AnimChannel myHumanoidAnimChannel;
	private static String 	
			ANIM_STAND_FRONT = "StandUpFront",
			ANIM_STAND_BACK = "StandUpBack",
			ANIM_DANCE = "Dance",
			ANIM_IDLE_TOP = "IdleTop",
			PATH_HUMANOID_MESH = "Models/Sinbad/Sinbad.mesh.xml";
	
	public void initStuff(AssetManager asstMgr, Node parentNode, PhysicsSpace ps) {
		myHumanoidModelNode = (Node) asstMgr.loadModel(PATH_HUMANOID_MESH);

		// This was commented out in JMonkey code:
		//  myHumanoidModel.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));

		// Turn on the green bone skeleton debug.
		attachDebugSkeleton(myHumanoidModelNode, asstMgr);

		//Note: PhysicsRagdollControl is still TODO, constructor will change
		myHumanoidKRC = new KinematicRagdollControl(0.5f);
		HumanoidMapping.addHumanoidBonesToRagdoll(myHumanoidKRC);
		myHumanoidKRC.addCollisionListener(this);
		myHumanoidModelNode.addControl(myHumanoidKRC);

		HumanoidMapping.applyHumanoidJointLimits(myHumanoidKRC);

		ps.add(myHumanoidKRC);

		parentNode.attachChild(myHumanoidModelNode);
		// rootNode.attachChild(skeletonDebug);

		AnimControl humanoidControl = myHumanoidModelNode.getControl(AnimControl.class);
		myHumanoidAnimChannel = humanoidControl.createChannel();
		humanoidControl.addListener(this);
		
	}
	public void toggleKinMode() {
		myHumanoidKRC.setEnabled(!myHumanoidKRC.isEnabled());
		myHumanoidKRC.setRagdollMode();
	}
	public void standUp() { 
				Vector3f v = new Vector3f();
		v.set(myHumanoidModelNode.getLocalTranslation());
		v.y = 0;
		myHumanoidModelNode.setLocalTranslation(v);
		Quaternion q = new Quaternion();
		float[] angles = new float[3];
		myHumanoidModelNode.getLocalRotation().toAngles(angles);
		q.fromAngleAxis(angles[1], Vector3f.UNIT_Y);
		myHumanoidModelNode.setLocalRotation(q);
		if (angles[0] < 0) {
			myHumanoidAnimChannel.setAnim(ANIM_STAND_BACK);
			myHumanoidKRC.blendToKinematicMode(0.5f);
		} else {
			myHumanoidAnimChannel.setAnim(ANIM_STAND_FRONT);
			myHumanoidKRC.blendToKinematicMode(0.5f);
		}
	}

	public void collide(Bone bone, PhysicsCollisionObject pco, PhysicsCollisionEvent pce) {
		Object userObj = pco.getUserObject();
		if ((userObj != null) && (userObj instanceof Geometry)) {
			Geometry geom = (Geometry) userObj;
			if (PhysicsStuffBuilder.GEOM_FLOOR.equals(geom.getName())) {
				return;
			}
		}
		myHumanoidKRC.setRagdollMode();
	}

	public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
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

	public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
	}
	
	public void boogie() { 
		myHumanoidAnimChannel.setAnim(ANIM_DANCE);
		myHumanoidKRC.blendToKinematicMode(0.5f);
	}
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
}
