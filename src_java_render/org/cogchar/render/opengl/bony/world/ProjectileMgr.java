/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.render.opengl.bony.world;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
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
import com.jme3.texture.Texture;


/**
 * @author Stu B. <www.texpedient.com>
 */
public class ProjectileMgr {
	private float myProjectileSize = 1f;
	private Material myProjectileMaterial;
	private Sphere myProjectileSphereMesh;
	
	private static String 						
			GEOM_PRJCT = "projectile",
			GEOM_BOOM = "boom",
			PATH_PRJCT_MAT = "Common/MatDefs/Misc/Unshaded.j3md",
			PATH_ROCK_TEXTURE = "Textures/Terrain/Rock/Rock.PNG";
	
	public void initStuff(AssetManager asstMgr) { 
		initProjectileMaterial(asstMgr);
		myProjectileSphereMesh = ProjectileMgr.makeProjectileSphere();
		// myProjectileCollisionShape = new SphereCollisionShape(1.0f);
	}
	public void initProjectileMaterial(AssetManager asstMgr) {

		myProjectileMaterial = new Material(asstMgr, PATH_PRJCT_MAT);
		TextureKey key2 = new TextureKey(PATH_ROCK_TEXTURE);
		key2.setGenerateMips(true);
		Texture tex2 = asstMgr.loadTexture(key2);
		myProjectileMaterial.setTexture("ColorMap", tex2);
	}	
	public static Sphere makeProjectileSphere () {
		Sphere projSphere = new Sphere(32, 32, 1.0f, true, false);
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
		RigidBodyControl prjctlRBC = new RigidBodyControl(projCollisionShape, myProjectileSize * 10);
		prjctlRBC.setCcdMotionThreshold(0.001f);
		
		return prjctlRBC;
	}
	public void cmdBiggerProjectile() {
		myProjectileSize *= 1.1f;
	}

	public void cmdSmallerProjectile() {
		myProjectileSize *= 0.9f;
	}	
}
