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
 * ------------------------------------------------------------------------------
 *
 *		This file contains code copied from the JMonkeyEngine project.
 *		You may not use this file except in compliance with the
 *		JMonkeyEngine license.  See full notice at bottom of this file. 
 */


package org.cogchar.render.model.humanoid;

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

import org.cogchar.render.sys.physics.PhysicsStuffBuilder;
import com.jme3.animation.AnimControl;
import com.jme3.animation.Skeleton;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.control.KinematicRagdollControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.debug.SkeletonDebugger;
import java.util.List;
import org.cogchar.blob.emit.BonyConfigEmitter;
import org.cogchar.render.model.bony.StickFigureTwister;
import org.cogchar.render.model.bony.BoneState;
import org.cogchar.render.model.bony.FigureState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class HumanoidFigure implements RagdollCollisionListener, AnimEventListener {
	static Logger theLogger = LoggerFactory.getLogger(HumanoidFigure.class);

	private Node						myHumanoidModelNode;
	protected  KinematicRagdollControl	myHumanoidKRC;
	private AnimChannel					myHumanoidAnimChannel;
	protected	HumanoidBoneConfig		myHumanoidBoneConfig;
	// Skeleton is used for direct access to the graphic "spatial" bones of JME3 (bypassing JBullet physics bindings). 
	private	Skeleton					myHumanoidSkeleton;
	private BonyConfigEmitter			myBonyConfigEmitter;
	
	private	FigureState					myFigureState;
	
	private String						myCharURI;
	private String						myNickname;

	public static String 	
			ANIM_STAND_FRONT = "StandUpFront",
			ANIM_STAND_BACK = "StandUpBack",
			ANIM_DANCE = "Dance",
			ANIM_IDLE_TOP = "IdleTop",
			SKEL_DEBUG_NAME = "hrwSkelDebg";
	
	private static float DEFAULT_ANIM_BLEND_RATE = 0.5f;
	private static float KRC_WEIGHT_THRESHOLD = 0.5f;

	public HumanoidFigure(BonyConfigEmitter bce, String charURI) { 
		myBonyConfigEmitter = bce;
		myCharURI = charURI;
		myNickname = bce.getNicknameForChar(charURI);
	}
	public HumanoidBoneConfig getHBConfig() {
		return myHumanoidBoneConfig;
	}
	public Bone getSpatialBone(String boneName) {
		Bone b = myHumanoidSkeleton.getBone(boneName);
		return b;
	}
	public Bone getRootBone() {
		return myHumanoidSkeleton.getRoots()[0];
	}
	public void initStuff(HumanoidBoneConfig hbc, AssetManager assetMgr, Node parentNode, PhysicsSpace ps, String humanoidMeshPath) {
		myHumanoidBoneConfig = hbc;
		myHumanoidModelNode = (Node) assetMgr.loadModel(humanoidMeshPath);

		// This was commented out in JMonkey code:
		//  myHumanoidModel.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));

		AnimControl humanoidControl = myHumanoidModelNode.getControl(AnimControl.class);
		myHumanoidSkeleton = humanoidControl.getSkeleton();
		// Turn on the green bone skeleton debug.
		attachDebugSkeleton(myHumanoidModelNode, assetMgr);

		//Note: PhysicsRagdollControl is still TODO, constructor will change
		myHumanoidKRC = new KinematicRagdollControl(KRC_WEIGHT_THRESHOLD);
		
		myHumanoidBoneConfig.attachRagdollBones(this);

		myHumanoidModelNode.addControl(myHumanoidKRC);

		HumanoidBoneConfig.applyHumanoidJointLimits(myHumanoidKRC);

		if (ps != null) {
			myHumanoidKRC.addCollisionListener(this);
			ps.add(myHumanoidKRC);
		}

		parentNode.attachChild(myHumanoidModelNode);

		myHumanoidAnimChannel = humanoidControl.createChannel();
		humanoidControl.addListener(this);
		
	}

	public void becomePuppet() { 
		myHumanoidKRC.setKinematicMode();
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
			myHumanoidKRC.blendToKinematicMode(DEFAULT_ANIM_BLEND_RATE);
		} else {
			myHumanoidAnimChannel.setAnim(ANIM_STAND_FRONT);
			myHumanoidKRC.blendToKinematicMode(DEFAULT_ANIM_BLEND_RATE);
		}
	}
	
	public void movePosition(float deltaX, float deltaY, float deltaZ) {
		Vector3f v = new Vector3f();
		v.set(myHumanoidModelNode.getLocalTranslation());
		v.x += deltaX;
		v.y += deltaY;
		v.z += deltaZ;
		myHumanoidModelNode.setLocalTranslation(v);		
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
		try {
			myHumanoidAnimChannel.setAnim(ANIM_DANCE);
			myHumanoidKRC.blendToKinematicMode(DEFAULT_ANIM_BLEND_RATE);
		} catch (Throwable t) {
			theLogger.warn("Character cannot boogie, nickname is: " + myNickname, t);
		}
	}
	public void attachDebugSkeleton(Node humanoidModel, AssetManager assetMgr) { 
		// AnimControl humanoidControl = myHumanoidModelNode.getControl(AnimControl.class);
        SkeletonDebugger humanoidSkeletonDebug = 
				new SkeletonDebugger(SKEL_DEBUG_NAME, myHumanoidSkeleton);
		String unshadedMatPath = myBonyConfigEmitter.getMaterialPath();
        Material mat2 = new Material(assetMgr, unshadedMatPath);
        mat2.getAdditionalRenderState().setWireframe(true);
        mat2.setColor("Color", ColorRGBA.Green);
        mat2.getAdditionalRenderState().setDepthTest(false);
        humanoidSkeletonDebug.setMaterial(mat2);
        humanoidSkeletonDebug.setLocalTranslation(humanoidModel.getLocalTranslation());
		
		humanoidModel.attachChild(humanoidSkeletonDebug);
	}
	public void attachRagdollBone(HumanoidBoneDesc hbd) {
		myHumanoidKRC.addBoneName(hbd.getSpatialName());
	}

	public FigureState getFigureState() {
		return myFigureState;
	}
	public void setFigureState(FigureState fs) { 
		myFigureState = fs;
	}
	
	public void applyFigureState(FigureState fs) {
		if (fs == null) {
			return;
		}
		HumanoidBoneConfig hbc = getHBConfig();
		List<HumanoidBoneDesc> boneDescs = hbc.getBoneDescs();
		// theLogger.info("Applying figureState " + fs + " to boneDescs[" + boneDescs + "]");
		int debugModulator = 0;
		for (HumanoidBoneDesc hbd : boneDescs) {
			
			String boneName = hbd.getSpatialName();
			BoneState bs = fs.getBoneState(boneName);
			Bone tgtBone = getSpatialBone(boneName);
			if ((bs != null) && (tgtBone != null)) {
				Quaternion boneRotQuat = bs.getRotQuat();
		//		if (debugModulator++ %5 == 0)  {
		//			theLogger.info("Applying " + boneRotQuat + " to " + tgtBone);
		//		}
				StickFigureTwister.applyBoneRotQuat(tgtBone, boneRotQuat);
			}
		}
	
	}	
	/*
	 * 
	 * 		// Unused bone lookups
		Bone headBone = myHumanoidSkeleton.getBone("Head");
		Bone footLeftBone = myHumanoidSkeleton.getBone("Foot.L");

	 *        
	 joint.getRotationalLimitMotor(0).setHiLimit(maxX);
        joint.getRotationalLimitMotor(0).setLoLimit(minX);
        joint.getRotationalLimitMotor(1).setHiLimit(maxY);
        joint.getRotationalLimitMotor(1).setLoLimit(minY);
        joint.getRotationalLimitMotor(2).setHiLimit(maxZ);
        joint.getRotationalLimitMotor(2).setLoLimit(minZ);
	 */
}


/*
 * 
 * Contains code copied and modified from the JMonkeyEngine.com project,
 * under the following terms:
 * 
 * -----------------------------------------------------------------------
 * 
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
