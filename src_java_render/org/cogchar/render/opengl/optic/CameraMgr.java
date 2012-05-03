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

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import java.util.HashMap;
import java.util.Map;
import org.cogchar.api.skeleton.config.BoneRobotConfig;
import org.cogchar.api.scene.CameraConfig;
import org.cogchar.api.scene.LightsCameraConfig;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class CameraMgr {

        static Logger theLogger = LoggerFactory.getLogger(CameraMgr.class);
    
	public enum CommonCameras {

		DEFAULT,
		TOP_VIEW,
		WIDE_VIEW
	}
	private Map<String, Camera> myCamerasByName = new HashMap<String, Camera>();

	public Camera cloneCamera(Camera orig) {
		return orig.clone();
	}

	public void registerNamedCamera(String name, Camera cam) {
		// System.out.println("**********######################*********************###############*************** cameraMgr: " + this + " - storing cam " + cam + " at " + name);
		myCamerasByName.put(name, cam);
	}

	public Camera getNamedCamera(String name) {
		Camera cam = myCamerasByName.get(name);
		// System.out.println("**********######################*********************###############*************** cameraMgr: " + this + " - found cam " + cam + " at " + name);		
		return cam;
	}

	public void registerCommonCamera(CommonCameras id, Camera cam) {
		registerNamedCamera(id.name(), cam);
	}

	public Camera getCommonCamera(CommonCameras id) {
		return getNamedCamera(id.name());
	}
        
        public void initCamerasFromConfig(LightsCameraConfig config, HumanoidRenderContext hrc) {
                for (CameraConfig cc : config.myCCs) {
			theLogger.info("Building Camera for config: " + cc);
                        String cameraName = cc.cameraName;
			Camera loadingCamera = getNamedCamera(cameraName); // First let's see if we can get a registered camera by this name
                        if (loadingCamera == null) {
                            loadingCamera = hrc.registerNewCameraUsingJME3Settings(cameraName); // otherwise we create a new one - note this method (in CogcharRenderContext) registers the camera for us
                        }
                        float[] cameraPos = cc.cameraPosition;
                        loadingCamera.setLocation(new Vector3f(cameraPos[0], cameraPos[1], cameraPos[2]));
                        float[] cameraDir = cc.cameraPointDir;
                        loadingCamera.lookAtDirection(new Vector3f(cameraDir[0], cameraDir[1], cameraDir[2]), Vector3f.UNIT_Y);
                }        
        }
        
	/*
	public  		
	 * FlyByCamera fbc = app.getFlyByCamera();
        fbc.setDragToRotate(true);
		fbc.setMoveSpeed(10f);
		app.setPauseOnLostFocus(false);
	*/
        
}
