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

import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class LightFactory {
	public static void addLightGrayAmbientLight(Node rootNode) {
		addAmbientLight(rootNode, ColorRGBA.LightGray);
	}
	public static void addAmbientLight(Node rootNode, ColorRGBA c) {
		AmbientLight light = new AmbientLight();
		light.setColor(c);
		rootNode.addLight(light);
	}
	
	public static AmbientLight makeWhiteAmbientLight() {
		AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1));
        return al;
	}
	public static DirectionalLight makeDirectionalLight() {

        //   rootNode.addLight(al);

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.1f, -0.7f, -1).normalizeLocal());
        dl.setColor(new ColorRGBA(1f, 1f, 1f, 1.0f));
        return dl;
    }	
}
