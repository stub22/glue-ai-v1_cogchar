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

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class GeomMgr {
	public static final String GEOM_SPHERE = "Sphere";
	public static final String GEOM_SOCCER_BALL = "Soccer ball";
	
	public static Geometry makeSphereGeom(int zSamples, int radialSamples, float radius, String geomName, Material optionalMat,  
					RigidBodyControl optRigidBodyCtrl) {
		Sphere sphere = new Sphere(zSamples, radialSamples, radius);
		return makeSphereGeom(sphere, geomName, optionalMat, optRigidBodyCtrl);
	}
	public static Geometry makeSphereGeom(Sphere s, String geomName, Material optionalMat, RigidBodyControl optRigidBodyCtrl) {
		Geometry sphereGeometry = new Geometry(geomName, s);
		if (optionalMat != null) {
			sphereGeometry.setMaterial(optionalMat);
		}
		if (optRigidBodyCtrl != null) {
			//RigidBodyControl automatically uses sphere collision shapes when attached to single geometry with sphere mesh
			sphereGeometry.addControl(optRigidBodyCtrl);
		}
		return sphereGeometry;
	}	
}
