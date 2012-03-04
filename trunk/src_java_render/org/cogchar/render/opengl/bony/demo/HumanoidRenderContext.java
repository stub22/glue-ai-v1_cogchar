/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.render.opengl.bony.demo;

import com.jme3.asset.AssetManager;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import org.cogchar.blob.emit.BonyConfigEmitter;
import org.cogchar.render.opengl.app.AppStub;
import org.cogchar.render.opengl.bony.app.BonyStickFigureContext;
import org.cogchar.render.opengl.bony.model.HumanoidBoneConfig;
import org.cogchar.render.opengl.bony.model.HumanoidFigureModule;
import org.cogchar.render.opengl.bony.model.HumanoidFigure;
import org.cogchar.render.opengl.bony.model.SpatialManipFuncs;
import org.cogchar.render.opengl.bony.world.ProjectileLauncher;
import org.cogchar.render.opengl.optic.CameraMgr;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class HumanoidRenderContext extends BonyStickFigureContext {
	private HumanoidFigure		myHumanoidWrapper;
	private ProjectileLauncher			myPrjctlMgr;
	
	public HumanoidRenderContext(BonyConfigEmitter bce) {
		super(bce);
		myHumanoidWrapper = new HumanoidFigure(bce);
	}
	@Override public void completeInit() { 
		super.completeInit();
		
		myPrjctlMgr = makeProjectileLauncher();
		myPrjctlMgr.initStuff();  // Can be done at any time in this startup seq
		
		AppSettings someSettings = getJMonkeyAppSettings();
		initCrossHairs(someSettings);
		initBasicTestPhysics();
		initCameraAndLights();
		initHumanoidStuff();
		
		InputManager inputManager = findJme3InputManager(null);

		HumanoidPuppetActions.setupActionListeners(inputManager, this);
        SimulatorActions.setupActionListeners(inputManager, this);
		myHumanoidWrapper.boogie();
		myHumanoidWrapper.becomePuppet();		
	}
	public ProjectileLauncher getProjectileMgr() { 
		return myPrjctlMgr;
	}
	public HumanoidFigure getHumdWrap()  {
		return myHumanoidWrapper;
	}


	private void initHumanoidStuff() { 
		AssetManager amgr = findJme3AssetManager(null);
		HumanoidBoneConfig hbc = new HumanoidBoneConfig(true);
		BonyConfigEmitter bce = getBonyConfigEmitter();
		String humanoidMeshPath = bce.getHumanoidMeshPath();
		if (humanoidMeshPath != null) {
			
			Node rootNode = findJme3RootDeepNode(null);
			myHumanoidWrapper.initStuff(hbc, amgr, rootNode, getPhysicsSpace(), humanoidMeshPath);
			//VirtCharPanel vcp = getVCPanel();
			//vcp.setMaxChannelNum(hbc.getConfiguredBoneCount() - 1);

			HumanoidFigureModule hfm = new HumanoidFigureModule(myHumanoidWrapper, this);
			attachModule(hfm);			
		} else {
			getLogger().warn("Skipping humanoid mesh load");
		}
		
		String extraRobotMeshPath = bce.getExtraRobotMeshPath();
		if (extraRobotMeshPath != null) {
			getLogger().info("Loading extra-robot mesh from: " + extraRobotMeshPath);
			Node extraRobotNode = loadModelOrNull(amgr, extraRobotMeshPath);
			if (extraRobotNode != null) {
				SpatialManipFuncs.dumpNodeTree(extraRobotNode, "   ");
				Node rootNode = findJme3RootDeepNode(null);
				rootNode.attachChild(extraRobotNode);
			}
		} else {
			getLogger().warn("Skipping extra-robot mesh load");
		}

	}

	private void initCameraAndLights() {
		AppStub stub = getAppStub();
        setDefaultCameraLocation();
		stub.setAppSpeed(1.3f);
		FlyByCamera fbCam = stub.getFlyCam();
		fbCam.setMoveSpeed(50);
		setupLight();	
	}
    protected void setDefaultCameraLocation(){
		CameraMgr cmgr = findOrMakeOpticCameraFacade(null);
		Camera defCam = cmgr.getCommonCamera(CameraMgr.CommonCameras.DEFAULT);
		defCam.setLocation(new Vector3f(0.26924422f, 6.646658f, 22.265987f));
		defCam.setRotation(new Quaternion(-2.302544E-4f, 0.99302495f, -0.117888905f, -0.0019395084f));
    }

	
	public void cmdShoot() {
		CameraMgr cm = findOrMakeOpticCameraFacade(null);
		Camera defCam = cm.getCommonCamera(CameraMgr.CommonCameras.DEFAULT);
		Node rootNode = findJme3RootDeepNode(null);
		myPrjctlMgr.fireProjectileFromCamera(defCam, rootNode, getPhysicsSpace());	
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
}
