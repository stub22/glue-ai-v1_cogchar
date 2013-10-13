/*
 *  Copyright 2013 by The Friendularity Project (www.friendularity.org).
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
package org.cogchar.render.goody.dynamic;

import org.appdapter.core.name.Ident;
import org.cogchar.render.opengl.scene.GeomFactory;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import java.util.concurrent.Callable;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;

import org.appdapter.core.name.Ident;
import org.cogchar.render.goody.basic.DataballGoodyBuilder;
import org.cogchar.render.opengl.optic.CameraMgr;
import org.cogchar.render.opengl.optic.MatFactory;
import org.cogchar.render.opengl.scene.DeepSceneMgr;
import org.cogchar.render.opengl.scene.FlatOverlayMgr;
import org.cogchar.render.opengl.scene.GeomFactory;
import org.cogchar.render.opengl.scene.TextMgr;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 * @author Owner
 */
public class VizShape {
	private		Ident		myIdent;
	private		Vector3f	myPosVec;
	private		float		myRadius;
	// Strangely it seems RGBA is the only colorspace directly supported by JME3 core API - true? (No HSV, YUV)
	private		ColorRGBA	myColor;
	private		Geometry	myGeom;
	// RigidBodyControl	myRigidBodyControl;
	private		Material	myMaterial;

	public VizShape(Ident id, Vector3f initPos, float initRadius, ColorRGBA initColor) {
		myIdent = id;
		myPosVec = initPos;
		myRadius = initRadius;
		myColor = initColor;
	}
	public Ident getIdent() { 
		return myIdent;
	}
	public Geometry getGeom() { 
		return myGeom;
	}

	protected void applyColorsToMat() {
		myMaterial.setColor("Diffuse", myColor);
		myMaterial.setColor("Ambient", myColor);
		myMaterial.setColor("Specular", myColor);
		myMaterial.setFloat("Shininess", 25f);
	}

	public void setupGeom(ShapeAnimator sa, RenderRegistryClient rrc) {
		int zSamp = 20;
		int rSamp = 20;
		// Copied+modified from DataballGoodyBuilder
		Sphere sphereMesh = new Sphere(zSamp, rSamp, myRadius);
		myMaterial = sa.getStandardMat().clone();
		myMaterial.setBoolean("UseMaterialColors", true);
		applyColorsToMat();
		// 		material.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
		//  geometry.setQueueBucket(Bucket.Transparent);
		RigidBodyControl optRBC = null;
		// myRigidBodyControl = new RigidBodyControl(sphereShape(size), (float) (pow(size, 3) * MASS_COEFFICIENT));
		// control.setRestitution(0.5f);
		String emptySelector = null;
		GeomFactory geomFactory = rrc.getSceneGeometryFacade(emptySelector);
		myGeom = geomFactory.makeGeom(myIdent.getLocalName(), sphereMesh, myMaterial, optRBC);
		myGeom.setLocalTranslation(myPosVec);
	}

	public void setPosition(Vector3f pos) {
		myPosVec = pos;
		if (myGeom != null) {
			myGeom.setLocalTranslation(myPosVec);
		}
	}

	public void setColor(ColorRGBA col) {
		myColor = col;
		if (myMaterial != null) {
			applyColorsToMat();
		}
	}
	
}
/*
myRenderContext.enqueueCallable(new Callable<Void>() { // Do this on main render thread
@Override
public Void call() throws Exception {
//geometry.addControl(control);
myPhysics.add(control);
myBallsNode.attachChild(geometry);
control.setPhysicsLocation(initialPosition); // Probably unnecessary - setting this here, in reset() above, and using resetAllBalls in buildModelFromTurtle because they don't want to go to the initial position! Probably some sort of jME concurrency thing...
return null;
}
});
}
 */
