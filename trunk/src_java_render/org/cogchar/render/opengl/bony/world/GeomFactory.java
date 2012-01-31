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
package org.cogchar.render.opengl.bony.world;

import org.cogchar.render.opengl.optic.MatFactory;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import org.cogchar.render.opengl.mesh.ShapeMeshFactory;

/**
 * "A geometry has a mesh to define its form, and a material to define the appearance".
 * http://jmonkeyengine.org/wiki/doku.php/jme3:scenegraph_for_dummies - Slide 14 retrieved 2012-01-29.
 * @author Stu B. <www.texpedient.com>
 */
public class GeomFactory {
	public static final String GEOM_SPHERE = "Sphere";
	public static final String GEOM_SOCCER_BALL = "Soccer ball";
	
	private		ShapeMeshFactory	myShapeFactory;
			
	public MatFactory	myMatMgr;
	
	public GeomFactory (MatFactory matMgr) {
		myMatMgr = matMgr;
	}
	public Geometry makeGeom(String name, Mesh mesh) {
		return new Geometry(name, mesh);
	}
	public Geometry makeGeom(String name, Mesh mesh, Material optMat, RigidBodyControl optRBC) {
		Geometry g = makeGeom(name, mesh);
		if (optMat != null) {
			g.setMaterial(optMat);
		}
		if (optRBC != null) {
			g.addControl(optRBC);
		}
		return g;
	}
	public Geometry makeSphereGeom(String geomName, Material optMat,  RigidBodyControl optRBC, 
					int zSamples, int radialSamples, float radius) {	
		
		Sphere sphereMesh = new Sphere(zSamples, radialSamples, radius);
		return makeGeom(geomName, sphereMesh, optMat, optRBC);
	}
	public Geometry makeColoredUnshadedSphereGeom(String geomName, ColorRGBA color, RigidBodyControl optRBC, 
					int zSamples, int radialSamples, float radius) {
		
		Material mat = myMatMgr.makeColoredUnshadedMat(color);
		return makeSphereGeom(geomName, mat, optRBC, zSamples, radialSamples, radius);
	}
	public Geometry makeBoxGeom(String geomName, Material optMat, RigidBodyControl optRBC, float centerX, float centerY, float centerZ, 
				float width, float height, float depth) {
		
		Box boxMesh = myShapeFactory.makeBoxMesh(centerX, centerY, centerZ, width, height, depth);
		return makeGeom(geomName, boxMesh, optMat, optRBC);
	}
	public Geometry makeColoredUnshadedBoxGeom(String name, ColorRGBA color, RigidBodyControl optRBC, 
				float centerX, float centerY, float centerZ, float width, float height, float depth) {
		
		Material boxMat = myMatMgr.makeColoredUnshadedMat(color);		
		return makeBoxGeom(name, boxMat, optRBC, centerX, centerY, centerZ, width, height, depth);
	}
	
	public Geometry makeCubeGeom(String name, Material optMat, RigidBodyControl optRBC, 
					float centerX, float centerY, float centerZ, float edgeLen) {
		
		return makeBoxGeom(name, optMat, optRBC, centerX, centerY, centerZ, edgeLen, edgeLen, edgeLen);
	}
	
	/** X */
	public Geometry makeColoredUnshadedCubeGeom(String name, ColorRGBA color, RigidBodyControl optRBC, 
				float centerX, float centerY, float centerZ, float edgeLen) {
		
		Material cubeMat = myMatMgr.makeColoredUnshadedMat(color);		
		return makeCubeGeom(name, cubeMat, optRBC, centerX, centerY, centerZ, edgeLen);
	}

	/** A floor to show that the "shot" can go through several objects. */
	public Geometry makeFloor() {
		Material floorMat = myMatMgr.makeColoredUnshadedMat(ColorRGBA.Gray);
		Geometry floorGeom = makeBoxGeom("the_floor", floorMat, null, 0.0f, -4.0f, -5.0f, 7.5f, 0.2f, 7.5f);
		return floorGeom;
	}

	/** A red ball that marks the last spot that was "hit" by the "shot". */
	public Geometry makeRedBallMark() {
		Material mat = myMatMgr.makeColoredUnshadedMat(ColorRGBA.Red);
		Geometry sphGeom = makeSphereGeom ("hit_mark", mat, null, 30, 30, 0.2f);
		return sphGeom;
	}	
}
