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
package org.cogchar.render.opengl.bony.world;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Plane;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.texture.Texture;

/**
 *
 * @author normenhansen
 */
public class PhysicsStuffBuilder {

	public static final String GEOM_BOX = "Box";
	public static final String GEOM_FLOOR = "Floor";

	/**
	 * creates a simple physics test world with a floor, an obstacle and some test boxes
	 * @param rootNode
	 * @param assetManager
	 * @param space
	 */
	public static void createPhysicsTestWorld(Node rootNode, AssetManager assetManager, PhysicsSpace space) {
		LightMgr.addLightGrayAmbientLight(rootNode);
		
		Material floorMat = MatMgr.makeUnshadedMaterial(assetManager);
		
		addFloor(rootNode, space, true, floorMat);
	}


	public static void createPhysicsTestWorldSoccer(Node rootNode, AssetManager assetManager, PhysicsSpace space) {
		LightMgr.addLightGrayAmbientLight(rootNode);
		
		Material floorMat = MatMgr.makeJmonkeyLogoMaterial(assetManager);
		Material ballMat = floorMat;

		addFloor(rootNode, space, true, floorMat);
		//movable spheres
		makeSpheres(rootNode, assetManager, space, ballMat);
  
	}

	public static void addFloor(Node rootNode, PhysicsSpace space, boolean rigidBodyPhysFlag, Material floorMat) {
		LightMgr.addLightGrayAmbientLight(rootNode);

		Box floorBox = new Box(140, 0.25f, 140);
		Geometry floorGeometry = new Geometry(GEOM_FLOOR, floorBox);
		floorGeometry.setMaterial(floorMat);
		floorGeometry.setLocalTranslation(0, -5, 0);
		if (rigidBodyPhysFlag) {
			Plane plane = new Plane();
			plane.setOriginNormal(new Vector3f(0, 0.25f, 0), Vector3f.UNIT_Y);
			floorGeometry.addControl(new RigidBodyControl(new PlaneCollisionShape(plane), 0));
		}
		floorGeometry.addControl(new RigidBodyControl(0));
		rootNode.attachChild(floorGeometry);
		space.add(floorGeometry);
	}

	
	/**
	 * creates a box geometry with a RigidBodyControl
	 * @param assetManager
	 * @return
	 */
	public static Geometry createPhysicsTestBox(Node parentNode, AssetManager assetManager, PhysicsSpace space) {
		Material material = MatMgr.makeJmonkeyLogoMaterial(assetManager);
		Box box = new Box(0.25f, 0.25f, 0.25f);
// 		Box box = new Box(1, 1, 1);
		Geometry boxGeometry = new Geometry(GEOM_BOX, box);
		boxGeometry.setMaterial(material);
		//RigidBodyControl automatically uses box collision shapes when attached to single geometry with box mesh
		// boxGeometry.setLocalTranslation(4, 1, 2);
		boxGeometry.addControl(new RigidBodyControl(2));
// 		boxGeometry.addControl(new RigidBodyControl(new MeshCollisionShape(box), 0));
	        /*
	 * 	//movable boxes
	 * for (int i = 0; i < 12; i++) {
		Box box = new Box(0.25f, 0.25f, 0.25f);
		Geometry boxGeometry = new Geometry(GEOM_BOX, box);
		boxGeometry.setMaterial(material);
		boxGeometry.setLocalTranslation(i, 5, -3);
		//RigidBodyControl automatically uses box collision shapes when attached to single geometry with box mesh
		boxGeometry.addControl(new RigidBodyControl(2));
		rootNode.attachChild(boxGeometry);
		space.add(boxGeometry);
		 */	

		if (parentNode != null) {
			parentNode.attachChild(boxGeometry);
		}
		if (space != null) {
			space.add(boxGeometry);	
		}
		return boxGeometry;
	}



	/**
	 * creates an empty node with a RigidBodyControl
	 * @param manager
	 * @param shape
	 * @param mass
	 * @return
	 */
	public static Node createPhysicsTestNode(AssetManager manager, CollisionShape shape, float mass) {
		Node node = new Node("PhysicsNode");
		RigidBodyControl control = new RigidBodyControl(shape, mass);
		node.addControl(control);
		return node;
	}

	/**
	 * creates the necessary inputlistener and action to shoot balls from teh camera
	 * @param app
	 * @param rootNode
	 * @param space
	 */
	public static void createBallShooter(final Application app, final Node rootNode, final PhysicsSpace space) {
		ActionListener actionListener = new ActionListener() {
			public void onAction(String name, boolean keyPressed, float tpf) {
				Sphere bullet = new Sphere(32, 32, 0.4f, true, false);
				bullet.setTextureMode(TextureMode.Projected);
				Material mat2 = MatMgr.makeRockMaterial(app.getAssetManager());
				if (name.equals("shoot") && !keyPressed) {
					Geometry bulletg = new Geometry("bullet", bullet);
					bulletg.setMaterial(mat2);
					bulletg.setShadowMode(ShadowMode.CastAndReceive);
					bulletg.setLocalTranslation(app.getCamera().getLocation());
					RigidBodyControl bulletControl = new RigidBodyControl(1);
					bulletg.addControl(bulletControl);
					bulletControl.setLinearVelocity(app.getCamera().getDirection().mult(25));
					bulletg.addControl(bulletControl);
					rootNode.attachChild(bulletg);
					space.add(bulletControl);
				}
			}
		};
		InputMgr.attachShootTriggerAndListener(app.getInputManager(), actionListener);
	}

	public static void makeSpheres(Node rootNode, AssetManager assetManager, PhysicsSpace space, Material mat) {
		for (int i = 0; i < 5; i++) {
			RigidBodyControl rbc = new RigidBodyControl(.001f);
			Geometry ballGeometry = GeomMgr.makeSphereGeom(16, 16, .5f, GeomMgr.GEOM_SOCCER_BALL, mat, rbc);			
			//RigidBodyControl automatically uses Sphere collision shapes when attached to single geometry with sphere mesh
			ballGeometry.getControl(RigidBodyControl.class).setRestitution(1);
			ballGeometry.setLocalTranslation(i, 2, -3);
			rootNode.attachChild(ballGeometry);
			space.add(ballGeometry);
		}
	}
	public static void makeImmovableSphere(Node rootNode, AssetManager assetManager, PhysicsSpace space, Material mat) {	
		//immovable sphere with mesh collision shape
		Sphere sphere = new Sphere(8, 8, 1);
		RigidBodyControl rbc = new RigidBodyControl(new MeshCollisionShape(sphere), 0);
		Geometry sphereGeometry = GeomMgr.makeSphereGeom(sphere, GeomMgr.GEOM_SPHERE,  mat, rbc);
		sphereGeometry.setLocalTranslation(4, -4, 2);
		rootNode.attachChild(sphereGeometry);
		space.add(sphereGeometry);	
	}
	/**
	 * creates a sphere geometry with a RigidBodyControl
	 * @param assetManager
	 * @return
	 */
	public static Geometry createPhysicsTestSphere(AssetManager assetManager) {
		Material mat = MatMgr.makeJmonkeyLogoMaterial(assetManager);
		Geometry sphGeom = GeomMgr.makeSphereGeom(8, 8, 0.25f, GeomMgr.GEOM_SPHERE, mat, new RigidBodyControl(2));
		return sphGeom;
	}	

}
