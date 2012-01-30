

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
import com.jme3.scene.shape.Sphere;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class ShapeMeshFactory {
	/** Box	A box with solid (filled) faces.
Curve	A Curve is a visual, line-based representation of a Spline.
Cylinder	A simple cylinder, defined by it's height and radius.
Dome	A hemisphere.
Line	A simple line implementation with a start and an end.
PQTorus	A parameterized torus, also known as a pq torus.
Quad	Quad represents a rectangular plane in space defined by 4 vertices.
Sphere	Sphere represents a 3D object with all points equidistance from a center point.
StripBox	A box with solid (filled) faces.
Surface	This class represents a surface described by knots, weights and control points.
Torus	An
	 */
	
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
			*  radius2 = what?
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
	
	
	
}
