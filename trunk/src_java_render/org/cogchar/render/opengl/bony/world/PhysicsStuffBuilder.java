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

import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.material.Material;
import com.jme3.math.Plane;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import org.cogchar.render.opengl.mesh.MeshFactoryFacade;

/**
 *
 * @author normenhansen
 */
public class PhysicsStuffBuilder {

	public static final String GEOM_BOX = "Box";
	public static final String GEOM_FLOOR = "Floor";
	
	public static String		SHOOT_ACTION_NAME	= "shoot";

	private		Node			myRootNode;
	private		PhysicsSpace	myPhysSpc;
	private		MatFactory			myMatMgr;
	private		GeomFactory				myGeomMgr;
	private		MeshFactoryFacade	myMeshFF;
	
	public 	PhysicsStuffBuilder(Node rootNode, MatFactory matMgr, PhysicsSpace physSpc) {
		myRootNode = rootNode;
		myMatMgr = matMgr;
		myPhysSpc = physSpc;
	}
	
	public Node getRootNode() { 
		return myRootNode;
	}

	public PhysicsSpace getPhysicsSpace() { 
		return myPhysSpc;
	}

	protected MeshFactoryFacade getMeshFF() { 
		return myMeshFF;
	}
	
	/**
	 * creates a simple physics test world with a floor, an obstacle and some test boxes
	 * @param rootNode
	 * @param assetManager
	 * @param space
	 */
	public void createPhysicsTestWorld() {
		LightFactory.addLightGrayAmbientLight(myRootNode);
		
		Material floorMat = myMatMgr.makeUnshadedMat();
		
		addFloor( true, floorMat);
	}


	public void createPhysicsTestWorldSoccer() {
		LightFactory.addLightGrayAmbientLight(myRootNode);
		
		Material floorMat = myMatMgr.makeJmonkeyLogoMat();
		Material ballMat = floorMat;

		addFloor(true, floorMat);
		//movable spheres
		makeSpheres(ballMat);
  
	}

	public void addFloor(boolean rigidBodyPhysFlag, Material floorMat) {

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
		myRootNode.attachChild(floorGeometry);
		myPhysSpc.add(floorGeometry);
	}

	
	/**
	 * creates a box geometry with a RigidBodyControl
	 * @param assetManager
	 * @return
	 */
	public Geometry createPhysicsTestBox() {
		Material material = myMatMgr.makeJmonkeyLogoMat();
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

		if (myRootNode != null) {
			myRootNode.attachChild(boxGeometry);
		}
		if (myPhysSpc != null) {
			myPhysSpc.add(boxGeometry);	
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
	public Node createPhysicsTestNode(CollisionShape shape, float mass) {
		Node node = new Node("PhysicsNode");
		RigidBodyControl control = new RigidBodyControl(shape, mass);
		node.addControl(control);
		return node;
	}

	/*
	public fireBallFromCamera() { 
					Geometry bulletg = new Geometry("bullet", bullet);
					bulletg.setMaterial(mat2);
					bulletg.setShadowMode(ShadowMode.CastAndReceive);
					bulletg.setLocalTranslation(cam.getLocation());
					RigidBodyControl bulletControl = new RigidBodyControl(1);
					bulletg.addControl(bulletControl);
					bulletControl.setLinearVelocity(cam.getDirection().mult(25));
					bulletg.addControl(bulletControl);
					myRootNode.attachChild(bulletg);
					myPhysSpc.add(bulletControl);
	 * }
	 *
	 */

	/**
	 * creates the necessary inputlistener and action to shoot balls from teh camera
	 * @param app
	 * @param rootNode
	 * @param space
	 */
	public void createBallShooter(final Camera cam, InputMgr inputMgr, String actionName) {
		ActionListener actionListener = new ActionListener() {
			public void onAction(String name, boolean keyPressed, float tpf) {
				Sphere bullet = new Sphere(32, 32, 0.4f, true, false);
				bullet.setTextureMode(TextureMode.Projected);
				Material mat2 = myMatMgr.makeRockMat();
				if (name.equals("shoot") && !keyPressed) {
					Geometry bulletg = new Geometry("bullet", bullet);
					bulletg.setMaterial(mat2);
					bulletg.setShadowMode(ShadowMode.CastAndReceive);
					bulletg.setLocalTranslation(cam.getLocation());
					RigidBodyControl bulletControl = new RigidBodyControl(1);
					bulletg.addControl(bulletControl);
					bulletControl.setLinearVelocity(cam.getDirection().mult(25));
					bulletg.addControl(bulletControl);
					myRootNode.attachChild(bulletg);
					myPhysSpc.add(bulletControl);
				}
			}
		};
		inputMgr.attachMouseButtonTriggerAndListener(actionListener, SHOOT_ACTION_NAME,  MouseInput.BUTTON_LEFT);
	}

	public void makeSpheres(Material mat) {
		for (int i = 0; i < 5; i++) {
			RigidBodyControl rbc = new RigidBodyControl(.001f);
			Sphere s = getMeshFF().getShapeMF().makeSphereMesh(16, 16, 0.5f);
			Geometry ballGeometry = myGeomMgr.makeGeom( GeomFactory.GEOM_SOCCER_BALL, s, mat, rbc);			
			//RigidBodyControl automatically uses Sphere collision shapes when attached to single geometry with sphere mesh
			ballGeometry.getControl(RigidBodyControl.class).setRestitution(1);
			ballGeometry.setLocalTranslation(i, 2, -3);
			myRootNode.attachChild(ballGeometry);
			myPhysSpc.add(ballGeometry);
		}
	}
	public void makeImmovableSphere(Material mat) {	
		//immovable sphere with mesh collision shape
		Sphere sphMesh = getMeshFF().getShapeMF().makeSphereMesh(8, 8, 1);
		// Note here we are constructing the collision shape explicitly, and with 0 mass = "static" RBC.
		RigidBodyControl rbc = new RigidBodyControl(new MeshCollisionShape(sphMesh), 0);
		Geometry sphereGeometry = myGeomMgr.makeGeom(GeomFactory.GEOM_SPHERE, sphMesh,  mat, rbc);
		sphereGeometry.setLocalTranslation(4, -4, 2);
		myRootNode.attachChild(sphereGeometry);
		myPhysSpc.add(sphereGeometry);	
	}
	/**
	 * creates a sphere geometry with a RigidBodyControl
	 * @param assetManager
	 * @return
	 */
	public Geometry createPhysicsTestSphere() {
		Material mat = myMatMgr.makeJmonkeyLogoMat();
		Sphere sphMesh = getMeshFF().getShapeMF().makeSphereMesh(8, 8, 0.25f);		
		RigidBodyControl sphRBC = new RigidBodyControl(2.0f);
		Geometry sphGeom = myGeomMgr.makeGeom(GeomFactory.GEOM_SPHERE, sphMesh, mat, sphRBC);
		return sphGeom;
	}	

}
