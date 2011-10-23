/*
 * 
 * Based on test code from  JME3-alpha4 code of JMonkey project
 * http://
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
package org.cogchar.demo.bony;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.Bone;
import com.jme3.animation.LoopMode;
import com.jme3.bullet.BulletAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.RagdollCollisionListener;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.KinematicRagdollControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.light.Light;
import com.jme3.material.Material;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;

import org.cogchar.demo.render.opengl.PhysicsTestHelper;
import org.cogchar.demo.render.opengl.ThrowableBombRigidBodyControl;

/**
 * JMonkey Team Comment as of about August 2011:
 * PHYSICS RAGDOLLS ARE NOT WORKING PROPERLY YET!
 */
public class BowlAtHumanoidApp extends SimpleApplication {
	private BulletAppState myPhysicsAppState;
	//private Node myHumanoidModelNode;
	//private KinematicRagdollControl myHumanoidKRC;
	//private AnimChannel myHumanoidAnimChannel;
	private HumanoidRagdollWrapper myHumanoidWrapper;
	private ProjectileMgr myPrjctlMgr;
	private	WorldMgr myWorldMgr;
	protected static String 
			GEOM_FLOOR = "Floor",
			//ANIM_STAND_FRONT = "StandUpFront",
			//ANIM_STAND_BACK = "StandUpBack",
			//ANIM_DANCE = "Dance",
			//ANIM_IDLE_TOP = "IdleTop",
			//PATH_HUMANOID_MESH = "Models/Sinbad/Sinbad.mesh.xml",
			PATH_DEFAULT_FONT = "Interface/Fonts/Default.fnt";

	public static void main(String[] args) {
		String lwjglRendererName = AppSettings.LWJGL_OPENGL_ANY; // LWJGL_OPENGL1
		BowlAtHumanoidApp app = new BowlAtHumanoidApp(lwjglRendererName);
		app.start();
	}
	public BowlAtHumanoidApp(String lwjglRendererName) {
		// Set to OpenGL - 1 mode for 915GM graphics controller
		AppSettings settings = new AppSettings(true);
		settings.setRenderer(lwjglRendererName);		
		setSettings(settings);
		myPrjctlMgr = new ProjectileMgr();
		myHumanoidWrapper = new HumanoidRagdollWrapper();
		myWorldMgr = new WorldMgr();
	}
	public void cmdToggleKinMode() {
		myHumanoidWrapper.toString();
	}

	public void cmdStandUp() {
		myHumanoidWrapper.standUp();
	}

	public void cmdBoogie() {
		myHumanoidWrapper.boogie();
//		myHumanoidAnimChannel.setAnim(ANIM_DANCE);
//		myHumanoidKRC.blendToKinematicMode(0.5f);
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

	private void initProjectileStuff() { 
		myPrjctlMgr.initStuff(assetManager);
	}
	public ProjectileMgr getProjectileMgr() { 
		return myPrjctlMgr;
	}
	private void initPhysicsStuff() { 
		myPhysicsAppState = new BulletAppState();
		myPhysicsAppState.setEnabled(true);
		stateManager.attach(myPhysicsAppState);

		// Turn on the blue wireframe collision bounds.
		myPhysicsAppState.getPhysicsSpace().enableDebug(assetManager);

		PhysicsTestHelper.createPhysicsTestWorld(rootNode, assetManager, myPhysicsAppState.getPhysicsSpace());
	}
	private void initCameraAndLights() {
		cam.setLocation(new Vector3f(0.26924422f, 6.646658f, 22.265987f));
		cam.setRotation(new Quaternion(-2.302544E-4f, 0.99302495f, -0.117888905f, -0.0019395084f));
		setAppSpeed(1.3f);
		flyCam.setMoveSpeed(50);
		addLightToRootNode(WorldMgr.makeDirectionalLight());		
	}
	private void initHumanoidStuff() { 
		myHumanoidWrapper.initStuff(assetManager, rootNode, getPhysicsSpace());
		/*
		myHumanoidModelNode = (Node) assetManager.loadModel(PATH_HUMANOID_MESH);

		// This was commented out in JMonkey code:
		//  myHumanoidModel.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));

		// Turn on the green bone skeleton debug.
		HumanoidMgr.attachDebugSkeleton(myHumanoidModelNode, getAssetManager());

		//Note: PhysicsRagdollControl is still TODO, constructor will change
		myHumanoidKRC = new KinematicRagdollControl(0.5f);
		HumanoidMgr.addHumanoidBonesToRagdoll(myHumanoidKRC);
		myHumanoidKRC.addCollisionListener(this);
		myHumanoidModelNode.addControl(myHumanoidKRC);

		HumanoidMgr.applyHumanoidJointLimits(myHumanoidKRC);

		getPhysicsSpace().add(myHumanoidKRC);

		rootNode.attachChild(myHumanoidModelNode);
		// rootNode.attachChild(skeletonDebug);

		AnimControl humanoidControl = myHumanoidModelNode.getControl(AnimControl.class);
		myHumanoidAnimChannel = humanoidControl.createChannel();
		humanoidControl.addListener(this);
		*/
	}
	public void simpleInitApp() {
		initProjectileStuff();
		guiFont = assetManager.loadFont(PATH_DEFAULT_FONT);
		WorldMgr.makeCrossHairs(assetManager, guiNode, guiFont, settings);
		initPhysicsStuff();
		initCameraAndLights();
		initHumanoidStuff();
		BowlAtHumanoidActions.setupActionListeners(inputManager, this);
		cmdBoogie();
	}

	private void setAppSpeed(float val) {
		speed = val;
	}

	protected void addLightToRootNode(Light l) {
		rootNode.addLight(l);

	}

	private PhysicsSpace getPhysicsSpace() {
		return myPhysicsAppState.getPhysicsSpace();
	}

	/*   
	float elTime = 0;
	boolean forward = true;
	AnimControl animControl;
	
	Vector3f direction = new Vector3f(0, 0, 1);
	Quaternion rotate = new Quaternion().fromAngleAxis(FastMath.PI / 8, Vector3f.UNIT_Y);
	boolean dance = true;
	 * */
	@Override
	public void simpleUpdate(float tpf) {
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
