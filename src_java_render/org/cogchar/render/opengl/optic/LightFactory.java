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
package org.cogchar.render.opengl.optic;

import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import org.cogchar.api.scene.LightConfig;
import org.cogchar.api.scene.LightsCameraConfig;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class LightFactory {
        
        static Logger theLogger = LoggerFactory.getLogger(CameraMgr.class);
    
	public static void addLightGrayAmbientLight(Node rootNode) {
		addAmbientLight(rootNode, ColorRGBA.LightGray);
	}
	public static void addAmbientLight(Node rootNode, ColorRGBA c) {
		AmbientLight light = new AmbientLight();
		light.setColor(c);
		rootNode.addLight(light);
	}
        
        public static AmbientLight makeAmbientLight(ColorRGBA c) {
		AmbientLight light = new AmbientLight();
		light.setColor(c);
		return light;
	}
	
	public static AmbientLight makeWhiteAmbientLight() {
		AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1));
        return al;
	}
	public DirectionalLight makeDirectionalLight(Vector3f direction, ColorRGBA color) {
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(direction.normalizeLocal());
        dl.setColor(color);
        return dl;
    }	
	public  DirectionalLight makeWhiteOpaqueDirectionalLight(Vector3f direction) {
        //   rootNode.addLight(al);
		ColorRGBA whiteOpaqueLight = new ColorRGBA(1f, 1f, 1f, 1.0f);
		return makeDirectionalLight(direction, whiteOpaqueLight);
    }	
        
	public void initLightsFromConfig(LightsCameraConfig config, HumanoidRenderContext hrc) {
		for (LightConfig lc : config.myLCs) {
			theLogger.info("Building Light for config: " + lc);
			ColorRGBA color = new ColorRGBA(lc.lightColor[0], lc.lightColor[1], lc.lightColor[2], lc.lightColor[3]);
			if (lc.lightType.equals(LightConfig.LightType.DIRECTIONAL)) {
				Vector3f direction = new Vector3f(lc.lightDirection[0], lc.lightDirection[1], lc.lightDirection[2]);
				//hrc.addNewLightToJME3RootNode(makeDirectionalLight(direction, color)); //Temporarily disabled until thread issues resolved
			}
			if (lc.lightType.equals(LightConfig.LightType.AMBIENT)) {
				//hrc.addNewLightToJME3RootNode(makeAmbientLight(color)); //Temporarily disabled until thread issues resolved
			}
		}        
	}

}
