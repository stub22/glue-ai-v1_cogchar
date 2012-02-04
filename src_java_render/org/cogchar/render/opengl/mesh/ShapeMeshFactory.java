

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

package org.cogchar.render.opengl.mesh;

import com.jme3.math.Spline;
import com.jme3.math.Vector3f;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Curve;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Dome;
import com.jme3.scene.shape.PQTorus;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Torus;

/**
 * Factory which knows how to create the platonic solid primitives provided by JME3.
 * <br/>See <a href="http://jmonkeyengine.org/wiki/doku.php/jme3:advanced:shape">JME3 Shapes Tutorial</a> as 
 * well as <a href="http://jmonkeyengine.org/javadoc/com/jme3/scene/shape/package-summary.html">shape package Javadocs</a>.
 * <br/>
 * <br/>Box	A box with solid (filled) faces.
 * <br/>Curve	A Curve is a visual, line-based representation of a Spline.
 * <br/>Cylinder	A simple cylinder, defined by it's height and radius.
 * <br/>Dome	A hemisphere.
 * <br/>Line	A simple line implementation with a start and an end.
 * <br/>PQTorus	A parameterized torus, also known as a pq torus.
 * <br/>Quad	Quad represents a rectangular plane in space defined by 4 vertices.
 * <br/>Sphere	Sphere represents a 3D object with all points equidistance from a center point.
 * <br/>StripBox	A box with solid (filled) faces.
 * <br/>Surface	This class represents a surface described by knots, weights and control points.
 * <br/>Torus	An

 * @author Stu B. <www.texpedient.com>
 */
public class ShapeMeshFactory {
	
	
	public Box makeBoxMesh(Vector3f center, float width, float height, float depth) { 
		
		Box boxMesh = new Box(center, width / 2.0f, height / 2.0f, depth / 2.0f);
		return boxMesh;
	}
		
	public Box makeBoxMesh(float centerX, float centerY, float centerZ, 
				float width, float height, float depth) {
		
		return makeBoxMesh(new Vector3f(centerX, centerY, centerZ), width, height, depth);
	}
	public Box makeBoxMesh(Vector3f minCorner, Vector3f maxCorner) {
		
		Box boxMesh = new Box(minCorner, maxCorner);
		return boxMesh;
	}
    
	public Curve makeCurveMesh(Spline spline, int nbSubSegments) {
		Curve curveMesh = new Curve(spline, nbSubSegments);
		return curveMesh;
	}
	public Curve makeCurveMesh(Vector3f[] controlPoints, int nbSubSegments) {
		Curve curveMesh = new Curve(controlPoints, nbSubSegments);
		return curveMesh;
	}
			/**
			 * closed - true to create a cylinder with top and bottom surface
			*	inverted - true to create a cylinder that is meant to be viewed from the interior.
			*
			 */
	public Cylinder makeCylinderMesh(int axisSamples, int radialSamples, float radiusBottom, float radiusTop, 
				float height, boolean closedAtEnds, boolean insideView) {
		Cylinder cylMesh = new Cylinder(axisSamples, radialSamples, radiusBottom, radiusTop, height, 
						closedAtEnds, insideView);
		return cylMesh;
	}
	/*
	 * Hemispheric dome.
	 */
	public Dome makeDomeMesh(Vector3f center, int planes, int radialSamples, float radius, boolean insideView) {
		Dome domeMesh = new Dome(center, planes, radialSamples, radius, insideView);
		return domeMesh;
	}
	
	public Sphere makeSphereMesh(int zSamples, int radialSamples, float radius) {
		Sphere sphereMesh = new Sphere(zSamples, radialSamples, radius);
		return sphereMesh;
	}
	public Sphere makeSphereMesh(int zSamples, int radialSamples, float radius, boolean useEvenSlices, boolean interior) {
		Sphere sphereMesh = new Sphere(zSamples, radialSamples, radius, useEvenSlices, interior);
		return sphereMesh;
	}	
	
	public PQTorus makePQTorusMesh(float p, float q, float radius, float width, int steps, int radialSamples) {
		PQTorus pqtMesh = new PQTorus( p,  q,  radius,  width,  steps,  radialSamples);
		return pqtMesh;
	}
	public Torus makeTorusMesh (int circleSamples, int radialSamples, float innerRadius, float outerRadius)  {
		Torus torusMesh = new Torus( circleSamples,  radialSamples,  innerRadius,  outerRadius);
		return torusMesh;
	}
	
}
