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
import org.cogchar.api.scene.CameraConfig;
import org.cogchar.api.scene.LightsCameraConfig;
import org.cogchar.api.scene.SceneConfigNames;
import org.cogchar.render.app.core.CoreFeatureAdapter;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.sys.core.RenderRegistryClient;
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
	private Vector3f defaultPosition;
	private Vector3f defaultDirection;

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
			boolean newFromRdf = false; // Used to trigger new viewport creation for new cameras loaded from RDF - probably not the way we want to handle this in long run
			String cameraName = cc.cameraName;
			// If the RDF camera name is camera_default (with any case), normalize name of this very special camera to "DEFAULT"
			if (cameraName.toLowerCase().replaceFirst(SceneConfigNames.partial_P_camera, "").equals("_default")) {
				cameraName = "DEFAULT";
			}
			Camera loadingCamera = getNamedCamera(cameraName); // First let's see if we can get a registered camera by this name
			if (loadingCamera == null) {
				newFromRdf = true; // Trigger new viewport creation for a new camera from RDF
				loadingCamera = cloneCamera(getCommonCamera(CommonCameras.DEFAULT)); // otherwise we create a new one...
				registerNamedCamera(cameraName, loadingCamera); // and register it
			}
			float[] cameraPos = cc.cameraPosition;
			loadingCamera.setLocation(new Vector3f(cameraPos[0], cameraPos[1], cameraPos[2]));
			float[] cameraDir = cc.cameraPointDir;
			loadingCamera.lookAtDirection(new Vector3f(cameraDir[0], cameraDir[1], cameraDir[2]), Vector3f.UNIT_Y);
			float[] cameraViewPort = cc.cameraViewPort;
			loadingCamera.setViewPort(cameraViewPort[0], cameraViewPort[1], cameraViewPort[2], cameraViewPort[3]);
			if (newFromRdf) {
				theLogger.info("Camera with config: " + cc + " is new from RDF, creating new viewport...");
				RenderRegistryClient rrc = hrc.getRenderRegistryClient();
				CoreFeatureAdapter.addViewPort(rrc, cameraName, loadingCamera);
			}
			if (cameraName.equals("DEFAULT")) {// If we are setting default camera info, save position/direction for later reset
				defaultPosition = new Vector3f(cameraPos[0], cameraPos[1], cameraPos[2]);
				defaultDirection = new Vector3f(cameraDir[0], cameraDir[1], cameraDir[2]);
			}
		}
	}

	public void resetDefaultCamera() {
		Camera defaultCamera = getCommonCamera(CommonCameras.DEFAULT);
		if ((defaultPosition != null) && (defaultDirection != null)) {
			defaultCamera.setLocation(defaultPosition);
			defaultCamera.lookAtDirection(defaultDirection, Vector3f.UNIT_Y);
		}
	}
	/*
	 * public FlyByCamera fbc = app.getFlyByCamera(); fbc.setDragToRotate(true); fbc.setMoveSpeed(10f);
	 * app.setPauseOnLostFocus(false);
	 */
}
