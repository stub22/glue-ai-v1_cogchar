

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
import com.jme3.scene.debug.Arrow;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class FancyMeshFactory {
	
	/*
	 * Line from origin toward the tip, where there is a pointy arrowhead shape.
	 * Default lineWidth is 1.0f pixels. 
	 */
	public Arrow makeArrowMesh(Vector3f tipPos, Float lineWidthPixels) { 
		Arrow arrowMesh = new Arrow(tipPos);
		if (lineWidthPixels != null) {
	        arrowMesh.setLineWidth(lineWidthPixels); // make arrow thicker
		}
		return arrowMesh;
	}
	
/*
    public void putGrid(Vector3f pos, ColorRGBA color){
        putShape(new Grid(6, 6, 0.2f), color).center().move(pos);
    }

    public void putSphere(Vector3f pos, ColorRGBA color){
        putShape(new WireSphere(1), color).setLocalTranslation(pos);
	
*/	
}
