/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.demo.bony;

import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class ProjectileFuncs {
	public Geometry makeProjectileGeometry(String name, Sphere projectileSphereMesh) { 
		/*
	
		Geometry prjctlGeom = new Geometry(name, projectileSphereMesh);
		prjctlGeom.setMaterial(myProjectileMaterial);
		prjctlGeom.setLocalTranslation(cam.getLocation());
		prjctlGeom.setLocalScale(myProjectileSize);
		myProjectileCollisionShape = new SphereCollisionShape(myProjectileSize);
		RigidBodyControl prjctlNode = new RigidBodyControl(myProjectileCollisionShape, myProjectileSize * 10);
		prjctlNode.setCcdMotionThreshold(0.001f);
		prjctlNode.setLinearVelocity(cam.getDirection().mult(80));
		prjctlGeom.addControl(prjctlNode);
		//rootNode.attachChild(prjctlGeom);
		//getPhysicsSpace().add(prjctlNode);	
		 * 
		 */
		return null;
	}
}
