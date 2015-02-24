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
 * Primary state object for organizing the depiction of a mesh+skeleton in JME3.
 * The "Humanoid" part is not essential to the current implementation.
 * 
 * @author Stu B. <www.texpedient.com>
 */
public class HumanoidFigure extends BasicDebugger implements RagdollCollisionListener, AnimEventListener {
	private static final Logger theLogger = LoggerFactory.getLogger(HumanoidFigure.class);
	
	private		Node						myJME3ModelSceneNode;
	private		HumanoidRagdollControl		myRagdollKinematicControl;
	private		AnimChannel					myJME3AnimChannel;

	// Skeleton is used for direct access to the graphic "spatial" bones of JME3 (bypassing JBullet physics bindings). 
	private	Skeleton						myJMESkeleton;
	private	SkeletonDebugger				myJMESkeletonDebugger;

	
	// Holds the runtime state of the bones, which it is our job to render into OpenGL skeleton updates
	// in the metho
	private	FigureState						myFigureState;
	
	// Holds the detailed configuration of the bone mapping
	private	HumanoidFigureConfig			myHFConfig;
	
	// Supplies render callbacks on the OpenGL update thread, allowing us to update the OpenGL skeleton model.
	private HumanoidFigureModule			myHFModule;


	public static String
			SKEL_DEBUG_NAME = "hrwSkelDebg";
	

	private static float KRC_WEIGHT_THRESHOLD = 0.5f;
        
        // "Special" face bone "rotation" max/min angles assumed to be set to +/-FACE_ANGLE_LIMIT
        // for purposes of "hack" mappings of rotations to translations, etc. (see getNormalizedTranslation)
        private static float FACE_ANGLE_LIMIT = (float)Math.PI/2; 


	public HumanoidFigure(HumanoidFigureConfig hfc) { 
		myHFConfig = hfc;
	}
	protected Node getFigureNode() { 
		return myJME3ModelSceneNode;
	}
	protected HumanoidFigureConfig getFigureConfig() { 
		return myHFConfig;
	}
	protected AnimChannel getFigureAnimChannel() { 
		return myJME3AnimChannel;
	}
	protected HumanoidRagdollControl getRagdollControl() { 
		return myRagdollKinematicControl;
	}
	protected Ident getCharIdent() { 
		return myHFConfig.getFigureID();
	}
	protected String getNickname() { 
		return myHFConfig.getNickname();
	}	
	protected FigureBoneConfig getHBConfig() {
		return myHFConfig.getFigureBoneConfig();
	}
	protected Bone getFigureBone(String boneName) {
		Bone b = myJMESkeleton.getBone(boneName);
		return b;
	}
	protected Bone getFigureRootBone() {
		return myJMESkeleton.getRoots()[0];
	}
	
	// We provide getter/setter for the HumanoidFigureModule associated with this HumanoidFigure here.
	// This allows us to detach the module on character "deinit"
	public HumanoidFigureModule getModule() {
		return myHFModule;
	}
	public void setModule(HumanoidFigureModule module) {
		myHFModule = module;
	}

	public boolean loadMeshAndSkeletonIntoVWorld(AssetManager assetMgr, Node parentNode, PhysicsSpace ps) {
		String meshPath = myHFConfig.getMeshPath();
		try {
			myJME3ModelSceneNode = (Node) assetMgr.loadModel(meshPath);
		} catch (Throwable t) {
			getLogger().warn("Caught exception trying to load 3D mesh model at [{}]", meshPath, t);
			return false;
		}
		
		myJME3ModelSceneNode.setLocalScale(myHFConfig.getScale()); 
		
		// This was commented out in JMonkey code:
		//  myHumanoidModel.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));

		AnimControl humanoidControl = myJME3ModelSceneNode.getControl(AnimControl.class);
		myJMESkeleton = humanoidControl.getSkeleton();
		// Prepare the green bone skeleton debugger, but don't activate it.
		initDebugSkeleton(assetMgr);

		myRagdollKinematicControl = new HumanoidRagdollControl(KRC_WEIGHT_THRESHOLD);
		
		attachRagdollBones();

		myJME3ModelSceneNode.addControl(myRagdollKinematicControl);

		applyHumanoidJointLimits(myRagdollKinematicControl);

		if (myHFConfig.getPhysicsFlag() && (ps != null)) {
			myRagdollKinematicControl.addCollisionListener(this);
			ps.add(myRagdollKinematicControl);
		}

		parentNode.attachChild(myJME3ModelSceneNode);
		
		Vector3f pos = new Vector3f(myHFConfig.getInitX(), myHFConfig.getInitY(), myHFConfig.getInitZ());
		moveToPosition_onSceneThread(pos);

		myJME3AnimChannel = humanoidControl.createChannel();
		humanoidControl.addListener(this);
		return true;
	}
	
	public void detachFromVirtualWorld(final Node parentNode, PhysicsSpace ps) {
		ps.remove(myRagdollKinematicControl);
		parentNode.detachChild(myJME3ModelSceneNode);
	}

	protected void becomeKinematicPuppet() { 
		myRagdollKinematicControl.setKinematicMode();
	}
	protected void becomeFloppyRagdoll() { 
		myRagdollKinematicControl.setEnabled(true);
		myRagdollKinematicControl.setRagdollMode();
	}	
	/*
	private void togglePhysicsKinematicModeEnabled() {
		myHumanoidKRC.setEnabled(!myHumanoidKRC.isEnabled());
		myHumanoidKRC.setRagdollMode();
	}
	* 
	*/ 

	protected void moveToPosition_onSceneThread(Vector3f pos) {
		myJME3ModelSceneNode.setLocalTranslation(pos);		
	}
	protected void movePosition_onSceneThread(float deltaX, float deltaY, float deltaZ) {
		Vector3f v = new Vector3f();
		v.set(myJME3ModelSceneNode.getLocalTranslation());
		v.x += deltaX;
		v.y += deltaY;
		v.z += deltaZ;
		myJME3ModelSceneNode.setLocalTranslation(v);		
	}
	
	public Node getNode() {
		return myJME3ModelSceneNode;
	}

	@Override public void collide(Bone bone, PhysicsCollisionObject pco, PhysicsCollisionEvent pce) {
		Object userObj = pco.getUserObject();
		if ((userObj != null) && (userObj instanceof Geometry)) {
			Geometry geom = (Geometry) userObj;
			// Floor name is now being set from config - need to revisit this area
			// if (PhysicsStuffBuilder.GEOM_FLOOR.equals(geom.getName())) {
			if (geom.getName().toLowerCase().contains(GoodyPhysicsStuffBuilder.GEOM_FLOOR.toLowerCase())) {
				return;
			}
			theLogger.info("Bone {} collided with userObj-geom named {}, which is not the floor", bone.getName(), geom.getName());
		} else {
			theLogger.info("Bone {} collided with something, userObj is {}", bone.getName(), userObj);
		}
		myRagdollKinematicControl.setRagdollMode();
	}

	@Override public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
		theLogger.info("AnimCycleDone {}", animName);
	}

	@Override public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
	}
	

	public void initDebugSkeleton(AssetManager assetMgr) { 
		// AnimControl humanoidControl = myHumanoidModelNode.getControl(AnimControl.class);
		if (myJMESkeletonDebugger == null) {
			myJMESkeletonDebugger = new SkeletonDebugger(SKEL_DEBUG_NAME, myJMESkeleton);
			String unshadedMatPath = myHFConfig.getDebugSkelMatPath();
			Material mat2 = new Material(assetMgr, unshadedMatPath);
			mat2.getAdditionalRenderState().setWireframe(true);
			mat2.setColor("Color", ColorRGBA.Green);
			mat2.getAdditionalRenderState().setDepthTest(false);
			myJMESkeletonDebugger.setMaterial(mat2);
		}
        myJMESkeletonDebugger.setLocalTranslation(myJME3ModelSceneNode.getLocalTranslation());	
	}
	public void toggleDebugSkeleton_onSceneThread() {
		if (myJME3ModelSceneNode != null) {
			if (myJME3ModelSceneNode.hasChild(myJMESkeletonDebugger)) {
				myJME3ModelSceneNode.detachChild(myJMESkeletonDebugger);
			} else {
				myJME3ModelSceneNode.attachChild(myJMESkeletonDebugger);
			}
		}
	}
	protected void attachRagdollBone(FigureBoneDesc hbd) {
		myRagdollKinematicControl.addBoneName(hbd.getBoneName());
	}

	public FigureState getFigureState() {
		return myFigureState;
	}
	public void setFigureState(FigureState fs) { 
		myFigureState = fs;
	}
	
	public void applyFigureState_onSceneThread(FigureState fs) {
		if (fs == null) {
			return;
		}
		FigureBoneConfig hbc = getHBConfig();
		List<FigureBoneDesc> boneDescs = hbc.getBoneDescs();
		if (boneDescs.size() == 0) {
			theLogger.warn("Found 0 boneDescs to map to for figure {}", myHFConfig);
		}
		theLogger.trace("Applying figureState {} to {} boneDescs[{}]", fs, boneDescs.size(), boneDescs); 
		int debugModulator = 0;
		for (FigureBoneDesc hbd : boneDescs) {
			
			String boneName = hbd.getBoneName();
			BoneState bs = fs.getBoneState(boneName);
			Bone tgtBone = getFigureBone(boneName);
			if ((bs != null) && (tgtBone != null)) {
				
				// For hinged bones, generally all we need is the rotation.
				Quaternion boneRotQuat = bs.getRotQuat();	// Can cancel this with null or Quaternion.IDENTITY

				Vector3f boneTranslateVec = null; // Same as Vector3f.ZERO = no local translation
                                Vector3f boneScaleVec = null;   // Same as Vector3f.UNIT_XYZ = new Vector3f(1.0, 1.0, 1.0); = scale by 1 in all 3 directions
                                
				StickFigureTwister.applyBoneTransforms_onSceneThread(tgtBone, boneTranslateVec, boneRotQuat, boneScaleVec);
			} else {
				theLogger.debug("Skipping boneState {} and tgtBone {}, for boneName {}", bs, tgtBone, boneName);
			}
		}
	
	}
        
        // Convenience method to convert from rotation about an axis of +/- FACE_ANGLE_LIMIT 
        // to a normalized linear mapping with a domain of {0,1} (symmetric=false) or {-1,1} (symmetric=true)
        private float getNormalizedLinearMap(float rotation, boolean symmetric) {
            return symmetric? rotation/FACE_ANGLE_LIMIT : (rotation/FACE_ANGLE_LIMIT + 1)/2;
        }

	public void attachRagdollBones() {
		FigureBoneConfig	hbc = getHBConfig();
		List<FigureBoneDesc> boneDescs = hbc.getBoneDescs();
		for (FigureBoneDesc hbd : boneDescs) {
			attachRagdollBone(hbd);
		}
	}
	
	// Provided so we can do such things as attach a camera node to bone attachment nodes
	// We can't do the attaching here, because we need access to the main jME app to enqueue attach on main thread
	public Node getBoneAttachmentsNode(String boneName) {
		if (myJME3ModelSceneNode != null) {
			SkeletonControl sc = myJME3ModelSceneNode.getControl(SkeletonControl.class); 
			return sc.getAttachmentsNode(boneName);
		} else {
			return null;
		}
	}
	

	/**
	 * Unused(?) stub of an idea
	 * @param krc 
	 */
	private static void applyHumanoidJointLimits(KinematicRagdollControl krc) {
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
