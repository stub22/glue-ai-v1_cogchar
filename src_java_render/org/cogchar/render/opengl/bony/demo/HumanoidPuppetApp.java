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
 * 
 * This file also contains fragments copied from the JMonkeyEngine test code.
 * See http://www.jmonkeyengine.org
 */

/*
 * 
 * Based on test code from  JME3-alpha4 code of JMonkey project
 * http://www.jmonkeyengine.org
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
package org.cogchar.render.opengl.bony.demo;

import com.jme3.animation.Bone;
import org.cogchar.render.opengl.bony.model.HumanoidRagdollWrapper;
import org.cogchar.render.opengl.bony.world.WorldMgr;
import org.cogchar.render.opengl.bony.world.ProjectileLauncher;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import com.jme3.scene.Node;
import org.cogchar.render.opengl.bony.model.SpatialManipFuncs;

import java.util.List;
import org.cogchar.blob.emit.BonyConfigEmitter;
import org.cogchar.render.opengl.bony.app.BonyStickFigureApp;
import org.cogchar.render.opengl.bony.model.HumanoidBoneConfig;
import org.cogchar.render.opengl.bony.model.HumanoidBoneDesc;
import org.cogchar.render.opengl.bony.state.BoneState;
import org.cogchar.render.opengl.bony.state.FigureState;
import org.cogchar.render.opengl.bony.sys.BonyRenderContext;
import org.cogchar.render.opengl.bony.gui.VirtualCharacterPanel;
import org.cogchar.render.opengl.bony.world.LightFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JMonkey Team Comment as of about August 2011:
 * PHYSICS RAGDOLLS ARE NOT WORKING PROPERLY YET!
 */
public class HumanoidPuppetApp extends BonyStickFigureApp { // DemoApp {
    private final static Logger		theLogger = LoggerFactory.getLogger(HumanoidPuppetApp.class);
	private HumanoidRagdollWrapper	myHumanoidWrapper;
	private ProjectileLauncher			myPrjctlMgr;
	private	WorldMgr				myWorldMgr;

	
	// private	String					myHumanoidMeshPath;
	


	public static void main(String[] args) {
		BonyConfigEmitter bce = new BonyConfigEmitter();
		HumanoidPuppetApp app = new HumanoidPuppetApp(bce);
		app.start();
	}
	public HumanoidPuppetApp(BonyConfigEmitter bce) { 
		super(bce); // lwjglRendererName, canvWidth, canvHeight, null, 1.0f);
		// myHumanoidMeshPath = pathToHumanoidMesh;
		myPrjctlMgr = new ProjectileLauncher();
		myHumanoidWrapper = new HumanoidRagdollWrapper(bce);
		myWorldMgr = new WorldMgr();
		// assetManager, rootNode, etc. are not available yet!
		// So, we wait for "simpleInitApp()" to do the bulk of our init.
	}
	@Override public void simpleInitApp() {
		theLogger.info("simpleInitApp() - START");
		super.simpleInitApp();
		initFonts();
		WorldMgr.makeCrossHairs(assetManager, guiNode, guiFont, settings);
		initBasicTestPhysics();
		initCameraAndLights();
		initHumanoidStuff();
		initProjectileStuff();  // Can be done at any time in this startup seq
		HumanoidPuppetActions.setupActionListeners(inputManager, this);
        SimulatorActions.setupActionListeners(inputManager, this);
		myHumanoidWrapper.boogie();
		myHumanoidWrapper.becomePuppet();
		theLogger.info("simpleInitApp() - END");

	}
	private void initProjectileStuff() {
		myPrjctlMgr.initStuff(getMatMgr());
	}
	public HumanoidRagdollWrapper getHumdWrap()  {
		return myHumanoidWrapper;
	}
	public ProjectileLauncher getProjectileMgr() { 
		return myPrjctlMgr;
	}	
	public void cmdShoot() {
		myPrjctlMgr.fireProjectileFromCamera(cam, rootNode, getPhysicsSpace());	
	}
	public void cmdBoom() {
		/*
		Geometry prjctlGeom = new Geometry(GEOM_BOOM, myProjectileSphereMesh);
		prjctlGeom.setMaterial(myProjectileMaterial);
		prjctlGeom.setLocalTranslation(cam.getLocation());
		prjctlGeom.setLocalScale(myProjectileSize);
		myProjectileCollisionShape = new SphereCollisionShape(myProjectileSize);
		ThrowableBombRigidBodyControl prjctlNode = new ThrowableBombRigidBodyControl(assetManager, myProjectileCollisionShape, 1);
		prjctlNode.setForceFactor(8);
		prjctlNode.setExplosionRadius(20);
		prjctlNode.setCcdMotionThreshold(0.001f);
		prjctlNode.setLinearVelocity(cam.getDirection().mult(180));
		prjctlGeom.addControl(prjctlNode);
		rootNode.attachChild(prjctlGeom);
		getPhysicsSpace().add(prjctlNode);
		 * */
		
	}
	private void initHumanoidStuff() { 
		HumanoidBoneConfig hbc = new HumanoidBoneConfig(true);
		BonyConfigEmitter bce = getBonyConfigEmitter();
		String humanoidMeshPath = bce.getHumanoidMeshPath();
		if (humanoidMeshPath != null) {
			myHumanoidWrapper.initStuff(hbc, assetManager, rootNode, getPhysicsSpace(), humanoidMeshPath);
			//VirtCharPanel vcp = getVCPanel();
			//vcp.setMaxChannelNum(hbc.getConfiguredBoneCount() - 1);
		} else {
			theLogger.warn("Skipping humanoid mesh load");
		}
		
		String extraRobotMeshPath = bce.getExtraRobotMeshPath();
		if (extraRobotMeshPath != null) {
			theLogger.info("Loading extra-robot mesh from: " + extraRobotMeshPath);
			Node extraRobotNode = (Node) assetManager.loadModel(extraRobotMeshPath);
			SpatialManipFuncs.dumpNodeTree(extraRobotNode, "   ");
			rootNode.attachChild(extraRobotNode);
		} else {
			theLogger.warn("Skipping extra-robot mesh load");
		}
		
		
	}


	private void initCameraAndLights() {
        setDefaultCameraLocation();
		setAppSpeed(1.3f);
		flyCam.setMoveSpeed(50);
		addLightToRootNode(LightFactory.makeDirectionalLight());		
	}
    protected void setDefaultCameraLocation(){
		cam.setLocation(new Vector3f(0.26924422f, 6.646658f, 22.265987f));
		cam.setRotation(new Quaternion(-2.302544E-4f, 0.99302495f, -0.117888905f, -0.0019395084f));
    }
	public VirtualCharacterPanel getVCPanel() { 
		BonyRenderContext ctx = getBonyRenderContext();
		VirtualCharacterPanel vcp = ctx.getPanel();
		return vcp;
	}
	public void applyTwisting(float tpf) { 
		/*VirtCharPanel vcp = getVCPanel();
		int testChannelNum = vcp.getTestChannelNum();
		String direction = vcp.getTestDirection();
		HumanoidBoneConfig hbc = myHumanoidWrapper.getHBConfig();
		List<HumanoidBoneDesc> boneDescs = hbc.getBoneDescs();
		HumanoidBoneDesc hbd = boneDescs.get(testChannelNum);
		String boneName = hbd.getSpatialName();
		Bone tgtBone = myHumanoidWrapper.getSpatialBone(boneName);
		myTwister.twistBone(tpf, tgtBone, direction);
		
		Bone rootBone = myHumanoidWrapper.getRootBone();*/
	}
	/**
	 * Called from SimpleUpdate to propagate position state from the figure state in the
	 * BonyRenderContext to the OpenGL-rendered bones of the humanoid puppet, as mediated
	 * by the myTwister delegate (which doesn't do anything smart, yet)..  
	 */
	public void applyFigureState() {
		BonyRenderContext ctx = getBonyRenderContext();
		FigureState fs = ctx.getFigureState();
		if (fs == null) {
			return;
		}
		HumanoidBoneConfig hbc = myHumanoidWrapper.getHBConfig();
		List<HumanoidBoneDesc> boneDescs = hbc.getBoneDescs();
		// theLogger.info("Applying figureState " + fs + " to boneDescs[" + boneDescs + "]");
		int debugModulator = 0;
		for (HumanoidBoneDesc hbd : boneDescs) {
			
			String boneName = hbd.getSpatialName();
			BoneState bs = fs.getBoneState(boneName);
			Bone tgtBone = myHumanoidWrapper.getSpatialBone(boneName);
			if ((bs != null) && (tgtBone != null)) {
				Quaternion boneRotQuat = bs.getRotQuat();
		//		if (debugModulator++ %5 == 0)  {
		//			theLogger.info("Applying " + boneRotQuat + " to " + tgtBone);
		//		}
				myTwister.applyBoneRotQuat(tgtBone, boneRotQuat);
			}
		}
	
	}

	/*   
	float elTime = 0;
	boolean forward = true;
	AnimControl animControl;
	
	Vector3f direction = new Vector3f(0, 0, 1);
	Quaternion rotate = new Quaternion().fromAngleAxis(FastMath.PI / 8, Vector3f.UNIT_Y);
	boolean dance = true;
	 * */
    private long myLastUpdateTime = System.currentTimeMillis();
    private void logUpdateTime(){
        long prev = myLastUpdateTime;
        long now = System.currentTimeMillis();
        long elapsed = now - prev;
        theLogger.info("Updating Robot.  " + elapsed + "msec since last update.  Cur time: " + now);
        myLastUpdateTime = now;
    }
    
	@Override
	public void simpleUpdate(float tpf) {
        //logUpdateTime();
		super.simpleUpdate(tpf);
		//applyTwisting(tpf);
		applyFigureState();
		// myHumanoidWrapper.wiggle(tpf);
		//  Below is JMonkey test code from TestBoneRagdoll, which is commented out in JMonkey trunk as of about 
		// 2011-08-01.
		// System.out.println(((BoundingBox) myHumanoidModel.getWorldBound()).getYExtent());
//        elTime += tpf;
//        if (elTime > 3) {
//            elTime = 0;
//            if (dance) {
//                rotate.multLocal(direction);
//            }
//            if (Math.random() > 0.80) {
//                dance = true;
//                myHumanoidAnimChannel.setAnim("Dance");
//            } else {
//                dance = false;
//                myHumanoidAnimChannel.setAnim("RunBase");
//                rotate.fromAngleAxis(FastMath.QUARTER_PI * ((float) Math.random() - 0.5f), Vector3f.UNIT_Y);
//                rotate.multLocal(direction);
//            }
//        }
//        if (!myHumanoidKRC.hasControl() && !dance) {
//            if (myHumanoidModel.getLocalTranslation().getZ() < -10) {
//                direction.z = 1;
//                direction.normalizeLocal();
//            } else if (myHumanoidModel.getLocalTranslation().getZ() > 10) {
//                direction.z = -1;
//                direction.normalizeLocal();
//            }
//            if (myHumanoidModel.getLocalTranslation().getX() < -10) {
//                direction.x = 1;
//                direction.normalizeLocal();
//            } else if (myHumanoidModel.getLocalTranslation().getX() > 10) {
//                direction.x = -1;
//                direction.normalizeLocal();
//            }
//            myHumanoidModel.move(direction.multLocal(tpf * 8));
//            direction.normalizeLocal();
//            myHumanoidModel.lookAt(myHumanoidModel.getLocalTranslation().add(direction), Vector3f.UNIT_Y);
//        }
	}


}
