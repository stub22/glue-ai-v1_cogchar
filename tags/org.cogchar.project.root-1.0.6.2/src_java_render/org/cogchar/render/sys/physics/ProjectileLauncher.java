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
package org.cogchar.render.sys.physics;

import org.cogchar.render.opengl.optic.MatFactory;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import org.cogchar.render.opengl.mesh.ShapeMeshFactory;


/**
 * @author Stu B. <www.texpedient.com>
 */
public class ProjectileLauncher {
	private MatFactory		myMatFactory;
	
	private float myProjectileSize = 1f;
	private String myProjectileTexturePath;
	private Material myProjectileMaterial;
	private Sphere myProjectileSphereMesh;
	
	private ShapeMeshFactory	myShapeMeshFactory;
	
	private static String 						
			GEOM_PRJCT = "projectile",
			// GEOM_BOOM = "boom",
			PATH_PRJCT_MAT = "Common/MatDefs/Misc/Unshaded.j3md",
			PATH_ROCK_TEXTURE = "Textures/Terrain/Rock/Rock.PNG";
	
	private static float	CCD_MOTION_THRESH = 0.001f,
							PRJCTL_GROWTH_FACTOR = 1.1f;
					
	
	public ProjectileLauncher(ShapeMeshFactory smf, MatFactory mf) {
		myShapeMeshFactory = smf;
		myMatFactory = mf;
	}
	
	public void initStuff() { 
		myProjectileMaterial = myMatFactory.makeRockMat();
		myProjectileSphereMesh = makeProjectileSphere();
		// myProjectileCollisionShape = new SphereCollisionShape(1.0f);
	}
	public Sphere makeProjectileSphere () {
		Sphere projSphere = myShapeMeshFactory.makeSphereMesh(32, 32, 1.0f, true, false);
		projSphere.setTextureMode(TextureMode.Projected);
		return projSphere;
	}
	public RigidBodyControl fireProjectileFromCamera(Camera cam, 
			Node parentNode, PhysicsSpace ps) { 
		Vector3f prjLoc = cam.getLocation();
		Vector3f prjVel = cam.getDirection().mult(80);  // bomb was 180
		RigidBodyControl prjctlRBC = makeProjectileGeometryAndRBC(GEOM_PRJCT, 
				myProjectileSphereMesh, parentNode, prjLoc, prjVel);
		ps.add(prjctlRBC);
		return prjctlRBC;
	}
	
	public void fireBombFromCamera(Camera cam, 
			Node parentNode, PhysicsSpace ps) { 
		// ThrowableBombRigidBodyControl throwableBombRBC= new ThrowableBombRigidBodyControl(assetManager, myProjectileCollisionShape, 1);

		// RigidBodyControl prjctlRBC = fireProjectileFromCamera(cam, parentNode, ps);
		// 		prjctlRBC.setForceFactor(8);
		// prjctlRBC.setExplosionRadius(20);
	}
		
	private RigidBodyControl makeProjectileGeometryAndRBC(String name, 
			Sphere projectileSphereMesh, Node parentNode,
			Vector3f loc, Vector3f vel) { 
		
		Geometry prjctlGeom = new Geometry(name, projectileSphereMesh);
		prjctlGeom.setMaterial(myProjectileMaterial);
		prjctlGeom.setLocalTranslation(loc);
		prjctlGeom.setLocalScale(myProjectileSize);

		RigidBodyControl prjctlRBC = makeRegularProjectileCollider();
		prjctlRBC.setLinearVelocity(vel);
		prjctlGeom.addControl(prjctlRBC);
		parentNode.attachChild(prjctlGeom);
		return prjctlRBC;
		 
		// return prjctlGeom;
	}
	private RigidBodyControl makeRegularProjectileCollider() {
		SphereCollisionShape projCollisionShape = new SphereCollisionShape(myProjectileSize);
		float projectileMass = 10 * myProjectileSize;
		RigidBodyControl prjctlRBC = new RigidBodyControl(projCollisionShape, projectileMass);
		prjctlRBC.setCcdMotionThreshold(CCD_MOTION_THRESH);
		
		return prjctlRBC;
	}
	public void cmdBiggerProjectile() {
		myProjectileSize *= PRJCTL_GROWTH_FACTOR;
	}

	public void cmdSmallerProjectile() {
		myProjectileSize /= PRJCTL_GROWTH_FACTOR;
	}	
}
