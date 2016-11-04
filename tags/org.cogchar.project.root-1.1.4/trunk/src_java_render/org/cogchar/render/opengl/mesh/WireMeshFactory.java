

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

import com.jme3.math.Vector3f;
import com.jme3.scene.debug.WireBox;
import com.jme3.scene.debug.WireFrustum;
import com.jme3.scene.debug.WireSphere;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class WireMeshFactory {
	/*
	 * Are these extents "doubled" like the ones on regular Box?
	 */
	public WireBox makeWireBoxMesh(float xExt, float yExt, float zExt)  { 
		WireBox wbMesh = new WireBox(xExt, yExt, zExt);
		return wbMesh;
	}
	/*
	 * 
	 */	
	public WireSphere makeWireSphere(float radius) {
		WireSphere wsMesh = new WireSphere(radius);
		return wsMesh;
	}
	
	/*
	 * 
	 */
	public WireFrustum makeWireFrustum(Vector3f[] points) {
		WireFrustum wfMesh = new WireFrustum(points);
		return wfMesh;
	}
	
}
