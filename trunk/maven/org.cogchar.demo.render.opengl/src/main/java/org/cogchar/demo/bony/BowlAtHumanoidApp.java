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
public class BowlAtHumanoidApp extends SimpleApplication implements RagdollCollisionListener, AnimEventListener {

	private Node myHumanoidModelNode;
	private BulletAppState myPhysicsAppState;
	private KinematicRagdollControl myHumanoidKRC;
	private float myProjectileSize = 1f;
	private Material myProjectileMaterial;
	private Sphere myProjectileSphereMesh;
	private SphereCollisionShape myProjectileCollisionShape;
	private AnimChannel myHumanoidAnimChannel;
	private static String 
			GEOM_FLOOR = "Floor",
			GEOM_PRJCT = "projectile",
			GEOM_BOOM = "boom",
			ANIM_STAND_FRONT = "StandUpFront",
			ANIM_STAND_BACK = "StandUpBack",
			ANIM_DANCE = "Dance",
			ANIM_IDLE_TOP = "IdleTop",
			PATH_HUMANOID_MESH = "Models/Sinbad/Sinbad.mesh.xml",
			PATH_PRJCT_MAT = "Common/MatDefs/Misc/Unshaded.j3md",
			PATH_ROCK_TEXTURE = "Textures/Terrain/Rock/Rock.PNG",
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
	}
	public void cmdToggleKinMode() {
		myHumanoidKRC.setEnabled(!myHumanoidKRC.isEnabled());
		myHumanoidKRC.setRagdollMode();
	}

	public void cmdStandUp() {
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

	public void cmdBoogie() {
		myHumanoidAnimChannel.setAnim(ANIM_DANCE);
		myHumanoidKRC.blendToKinematicMode(0.5f);
	}

	public void cmdShoot() {
		Geometry prjctlGeom = new Geometry(GEOM_PRJCT, myProjectileSphereMesh);
		prjctlGeom.setMaterial(myProjectileMaterial);
		prjctlGeom.setLocalTranslation(cam.getLocation());
		prjctlGeom.setLocalScale(myProjectileSize);
		myProjectileCollisionShape = new SphereCollisionShape(myProjectileSize);
		RigidBodyControl prjctlNode = new RigidBodyControl(myProjectileCollisionShape, myProjectileSize * 10);
		prjctlNode.setCcdMotionThreshold(0.001f);
		prjctlNode.setLinearVelocity(cam.getDirection().mult(80));
		prjctlGeom.addControl(prjctlNode);
		rootNode.attachChild(prjctlGeom);
		getPhysicsSpace().add(prjctlNode);
	}

	public void cmdBoom() {
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
	}

	public void cmdBiggerProjectile() {
		myProjectileSize *= 1.1f;
	}

	public void cmdSmallerProjectile() {
		myProjectileSize *= 0.9f;
	}

	public void simpleInitApp() {
		initCrossHairs();
		initProjectileMaterial();

		cam.setLocation(new Vector3f(0.26924422f, 6.646658f, 22.265987f));
		cam.setRotation(new Quaternion(-2.302544E-4f, 0.99302495f, -0.117888905f, -0.0019395084f));


		myPhysicsAppState = new BulletAppState();
		myPhysicsAppState.setEnabled(true);
		stateManager.attach(myPhysicsAppState);
		myProjectileSphereMesh = new Sphere(32, 32, 1.0f, true, false);
		myProjectileSphereMesh.setTextureMode(TextureMode.Projected);
		myProjectileCollisionShape = new SphereCollisionShape(1.0f);

		// Turn on the blue wireframe collision bounds.
		myPhysicsAppState.getPhysicsSpace().enableDebug(assetManager);

		PhysicsTestHelper.createPhysicsTestWorld(rootNode, assetManager, myPhysicsAppState.getPhysicsSpace());
		addLightToRootNode(WorldFuncs.makeDirectionalLight());
		setAppSpeed(1.3f);
		flyCam.setMoveSpeed(50);

		myHumanoidModelNode = (Node) assetManager.loadModel(PATH_HUMANOID_MESH);

		// This was commented out in JMonkey code:
		//  myHumanoidModel.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));

		// Turn on the green bone skeleton debug.
		HumanoidFuncs.attachDebugSkeleton(myHumanoidModelNode, getAssetManager());

		//Note: PhysicsRagdollControl is still TODO, constructor will change
		myHumanoidKRC = new KinematicRagdollControl(0.5f);
		HumanoidFuncs.addHumanoidBonesToRagdoll(myHumanoidKRC);
		myHumanoidKRC.addCollisionListener(this);
		myHumanoidModelNode.addControl(myHumanoidKRC);

		HumanoidFuncs.applyHumanoidJointLimits(myHumanoidKRC);

		getPhysicsSpace().add(myHumanoidKRC);

		rootNode.attachChild(myHumanoidModelNode);
		// rootNode.attachChild(skeletonDebug);

		AnimControl humanoidControl = myHumanoidModelNode.getControl(AnimControl.class);
		myHumanoidAnimChannel = humanoidControl.createChannel();
		humanoidControl.addListener(this);
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

	public void initProjectileMaterial() {

		myProjectileMaterial = new Material(assetManager, PATH_PRJCT_MAT);
		TextureKey key2 = new TextureKey(PATH_ROCK_TEXTURE);
		key2.setGenerateMips(true);
		Texture tex2 = assetManager.loadTexture(key2);
		myProjectileMaterial.setTexture("ColorMap", tex2);
	}

	protected void initCrossHairs() {
		guiFont = assetManager.loadFont(PATH_DEFAULT_FONT);
		BitmapText ch = new BitmapText(guiFont, false);
		ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
		ch.setText("+"); // crosshairs
		ch.setLocalTranslation( // center
				settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
				settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
		guiNode.attachChild(ch);
	}

	public void collide(Bone bone, PhysicsCollisionObject object, PhysicsCollisionEvent event) {
		if (object.getUserObject() != null && object.getUserObject() instanceof Geometry) {
			Geometry geom = (Geometry) object.getUserObject();
			if (GEOM_FLOOR.equals(geom.getName())) {
				return;
			}
		}
		myHumanoidKRC.setRagdollMode();

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
}
