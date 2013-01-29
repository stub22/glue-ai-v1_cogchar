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

import org.cogchar.api.humanoid.HumanoidBoneDesc;
import org.cogchar.api.humanoid.HumanoidBoneConfig;
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

import org.cogchar.render.sys.physics.PhysicsStuffBuilder;
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
public class HumanoidFigure extends BasicDebugger implements RagdollCollisionListener, AnimEventListener {
	private static final Logger theLogger = LoggerFactory.getLogger(HumanoidFigure.class);
	
	private		Node						myHumanoidModelNode;
	private		HumanoidRagdollControl		myHumanoidKRC;
	private		AnimChannel					myHumanoidAnimChannel;

	// Skeleton is used for direct access to the graphic "spatial" bones of JME3 (bypassing JBullet physics bindings). 
	private	Skeleton						myHumanoidSkeleton;
	private	SkeletonDebugger				myHumanoidSkeletonDebugger;

	
	private	FigureState						myFigureState;
	
	private	HumanoidFigureConfig			myConfig;
	
	private HumanoidFigureModule			myModule;


	public static String 	
			ANIM_STAND_FRONT = "StandUpFront",
			ANIM_STAND_BACK = "StandUpBack",
			ANIM_DANCE = "Dance",
			ANIM_IDLE_TOP = "IdleTop",
			SKEL_DEBUG_NAME = "hrwSkelDebg";
	
	private static float DEFAULT_ANIM_BLEND_RATE = 0.5f;
	private static float KRC_WEIGHT_THRESHOLD = 0.5f;
        
        // "Special" face bone "rotation" max/min angles assumed to be set to +/-FACE_ANGLE_LIMIT
        // for purposes of "hack" mappings of rotations to translations, etc. (see getNormalizedTranslation)
        private static float FACE_ANGLE_LIMIT = (float)Math.PI/2; 


	public HumanoidFigure(HumanoidFigureConfig hfc) { 
		myConfig = hfc;
		
	}
	protected HumanoidRagdollControl getRagdollControl() { 
		return myHumanoidKRC;
	}
	protected Ident getCharIdent() { 
		return myConfig.myCharIdent;
	}
	protected String getNickname() { 
		return myConfig.myNickname;
	}	
	protected HumanoidBoneConfig getHBConfig() {
		return myConfig.myBoneConfig;
	}
	protected Bone getSpatialBone(String boneName) {
		Bone b = myHumanoidSkeleton.getBone(boneName);
		return b;
	}
	protected Bone getRootBone() {
		return myHumanoidSkeleton.getRoots()[0];
	}
	
	// We provide getter/setter for the HumanoidFigureModule associated with this HumanoidFigure here.
	// This allows us to detach the module on character "deinit"
	public HumanoidFigureModule getModule() {
		return myModule;
	}
	public void setModule(HumanoidFigureModule module) {
		myModule = module;
	}

	public boolean loadMeshAndSkeletonIntoVWorld(AssetManager assetMgr, Node parentNode, PhysicsSpace ps) {
		try {
			myHumanoidModelNode = (Node) assetMgr.loadModel(myConfig.myMeshPath);
		} catch (Throwable t) {
			getLogger().warn("Caught exception trying to load 3D mesh model at [{}]", myConfig.myMeshPath, t);
			return false;
		}
		
		myHumanoidModelNode.setLocalScale(myConfig.myScale); 
		
		// This was commented out in JMonkey code:
		//  myHumanoidModel.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));

		AnimControl humanoidControl = myHumanoidModelNode.getControl(AnimControl.class);
		myHumanoidSkeleton = humanoidControl.getSkeleton();
		// Prepare the green bone skeleton debugger, but don't activate it.
		initDebugSkeleton(assetMgr);

		myHumanoidKRC = new HumanoidRagdollControl(KRC_WEIGHT_THRESHOLD);
		
		attachRagdollBones();

		myHumanoidModelNode.addControl(myHumanoidKRC);

		applyHumanoidJointLimits(myHumanoidKRC);

		if (myConfig.myPhysicsFlag && (ps != null)) {
			myHumanoidKRC.addCollisionListener(this);
			ps.add(myHumanoidKRC);
		}

		parentNode.attachChild(myHumanoidModelNode);
		
		Vector3f pos = new Vector3f(myConfig.myInitX, myConfig.myInitY, myConfig.myInitZ);
		moveToPosition(pos);

		myHumanoidAnimChannel = humanoidControl.createChannel();
		humanoidControl.addListener(this);
		return true;
	}
	
	public void detachFromVirtualWorld(final Node parentNode, PhysicsSpace ps) {
		ps.remove(myHumanoidKRC);
		parentNode.detachChild(myHumanoidModelNode);
	}

	protected void becomeKinematicPuppet() { 
		myHumanoidKRC.setKinematicMode();
	}
	protected void becomeFloppyRagdoll() { 
		myHumanoidKRC.setEnabled(true);
		myHumanoidKRC.setRagdollMode();
	}	
	/*
	private void togglePhysicsKinematicModeEnabled() {
		myHumanoidKRC.setEnabled(!myHumanoidKRC.isEnabled());
		myHumanoidKRC.setRagdollMode();
	}
	* 
	*/ 
	public void makeSinbadStandUp() { 
		Vector3f v = new Vector3f();
		v.set(myHumanoidModelNode.getLocalTranslation());
		v.y = myConfig.myInitY;
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
	protected void moveToPosition(Vector3f pos) {
		myHumanoidModelNode.setLocalTranslation(pos);		
	}
	protected void movePosition(float deltaX, float deltaY, float deltaZ) {
		Vector3f v = new Vector3f();
		v.set(myHumanoidModelNode.getLocalTranslation());
		v.x += deltaX;
		v.y += deltaY;
		v.z += deltaZ;
		myHumanoidModelNode.setLocalTranslation(v);		
	}

	@Override public void collide(Bone bone, PhysicsCollisionObject pco, PhysicsCollisionEvent pce) {
		Object userObj = pco.getUserObject();
		if ((userObj != null) && (userObj instanceof Geometry)) {
			Geometry geom = (Geometry) userObj;
			// Floor name is now being set from config - need to revisit this area
			// if (PhysicsStuffBuilder.GEOM_FLOOR.equals(geom.getName())) {
			if (geom.getName().toLowerCase().contains(PhysicsStuffBuilder.GEOM_FLOOR.toLowerCase())) {
				return;
			}
			theLogger.info("Bone {} collided with userObj-geom named {}, which is not the floor", bone.getName(), geom.getName());
		} else {
			theLogger.info("Bone {} collided with something, userObj is {}", bone.getName(), userObj);
		}
		myHumanoidKRC.setRagdollMode();
	}

	@Override public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
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

	@Override public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
	}
	
	public void runSinbadBoogieAnim() { 
		try {
			myHumanoidAnimChannel.setAnim(ANIM_DANCE);
			myHumanoidKRC.blendToKinematicMode(DEFAULT_ANIM_BLEND_RATE);
		} catch (Throwable t) {
			getLogger().warn("Character cannot boogie, nickname is: {}; {}", getNickname(), t);
		}
	}
	public void initDebugSkeleton(AssetManager assetMgr) { 
		// AnimControl humanoidControl = myHumanoidModelNode.getControl(AnimControl.class);
		if (myHumanoidSkeletonDebugger == null) {
			myHumanoidSkeletonDebugger = new SkeletonDebugger(SKEL_DEBUG_NAME, myHumanoidSkeleton);
			String unshadedMatPath = myConfig.myDebugSkelMatPath;
			Material mat2 = new Material(assetMgr, unshadedMatPath);
			mat2.getAdditionalRenderState().setWireframe(true);
			mat2.setColor("Color", ColorRGBA.Green);
			mat2.getAdditionalRenderState().setDepthTest(false);
			myHumanoidSkeletonDebugger.setMaterial(mat2);
		}
        myHumanoidSkeletonDebugger.setLocalTranslation(myHumanoidModelNode.getLocalTranslation());	
	}
	public void toggleDebugSkeleton() {
		if (myHumanoidModelNode != null) {
			if (myHumanoidModelNode.hasChild(myHumanoidSkeletonDebugger)) {
				myHumanoidModelNode.detachChild(myHumanoidSkeletonDebugger);
			} else {
				myHumanoidModelNode.attachChild(myHumanoidSkeletonDebugger);
			}
		}
	}
	protected void attachRagdollBone(HumanoidBoneDesc hbd) {
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
		// theLogger.info("Applying figureState " + fs + " to boneDescs[" + boneDescs + "]"); // TEST ONLY
		int debugModulator = 0;
		for (HumanoidBoneDesc hbd : boneDescs) {
			
			String boneName = hbd.getSpatialName();
			BoneState bs = fs.getBoneState(boneName);
			Bone tgtBone = getSpatialBone(boneName);
			if ((bs != null) && (tgtBone != null)) {
				
				// For hinged bones, generally all we need is the rotation.
				Quaternion boneRotQuat = bs.getRotQuat();	// Can cancel this with null or Quaternion.IDENTITY

				Vector3f boneTranslateVec = null; // Same as Vector3f.ZERO = no local translation
                                Vector3f boneScaleVec = null;   // Same as Vector3f.UNIT_XYZ = new Vector3f(1.0, 1.0, 1.0); = scale by 1 in all 3 directions
                                
				StickFigureTwister.applyBoneTransforms(tgtBone, boneTranslateVec, boneRotQuat, boneScaleVec);
			}
		}
	
	}
        
        // Convenience method to convert from rotation about an axis of +/- FACE_ANGLE_LIMIT 
        // to a normalized linear mapping with a domain of {0,1} (symmetric=false) or {-1,1} (symmetric=true)
        private float getNormalizedLinearMap(float rotation, boolean symmetric) {
            return symmetric? rotation/FACE_ANGLE_LIMIT : (rotation/FACE_ANGLE_LIMIT + 1)/2;
        }

	public void attachRagdollBones() {
		HumanoidBoneConfig	hbc = getHBConfig();
		List<HumanoidBoneDesc> boneDescs = hbc.getBoneDescs();
		for (HumanoidBoneDesc hbd : boneDescs) {
			attachRagdollBone(hbd);
		}
	}
	
	// Provided so we can do such things as attach a camera node to bone attachment nodes
	// We can't do the attaching here, because we need access to the main jME app to enqueue attach on main thread
	public Node getBoneAttachmentsNode(String boneName) {
		if (myHumanoidModelNode != null) {
			SkeletonControl sc = myHumanoidModelNode.getControl(SkeletonControl.class); 
			return sc.getAttachmentsNode(boneName);
		} else {
			return null;
		}
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
