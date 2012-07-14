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
import com.jme3.scene.CameraNode;
import com.jme3.scene.control.CameraControl.ControlDirection;
import java.util.HashMap;
import java.util.Map;
import org.cogchar.api.scene.CameraConfig;
import org.cogchar.api.scene.LightsCameraConfig;
import org.cogchar.api.scene.SceneConfigNames;
import org.cogchar.render.app.core.CogcharRenderContext;
import org.cogchar.render.app.core.CoreFeatureAdapter;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.sys.core.RenderRegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com> & Ryan Biggs
 */
public class CameraMgr {

	static Logger theLogger = LoggerFactory.getLogger(CameraMgr.class);

	public enum CommonCameras {

		DEFAULT,
		TOP_VIEW,
		WIDE_VIEW,
		HEAD_CAM
	}
	private Map<String, Camera> myCamerasByName = new HashMap<String, Camera>();
	private Vector3f defaultPosition;
	private Vector3f defaultDirection;
	private HumanoidRenderContext hrc;

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

	public void initCamerasFromConfig(LightsCameraConfig config, CogcharRenderContext hrc) { // We could just use the static hrc set by setHumanoidRenderContext, but leaving this for now since the future of that method isn't assured
		for (CameraConfig cc : config.myCCs) {
			theLogger.info("Building Camera for config: " + cc);
			boolean newFromRdf = false; // Used to trigger new viewport creation for new cameras loaded from RDF - probably not the way we want to handle this in long run
			String cameraName = cc.cameraName;
			// If the RDF camera name is camera_default (with any case), normalize name of this very special camera to "DEFAULT"
			if (cameraName.toLowerCase().replaceFirst(SceneConfigNames.partial_P_camera, "").equals("_" + SceneConfigNames.suffix_DEFAULT)) {
				cameraName = CommonCameras.DEFAULT.name();
			}
			if (cameraName.toLowerCase().replaceFirst(SceneConfigNames.partial_P_camera, "").equals("_" + SceneConfigNames.suffix_HEAD_CAM)) {
				cameraName = CommonCameras.HEAD_CAM.name();
			}
			Camera loadingCamera = getNamedCamera(cameraName); // First let's see if we can get a registered camera by this name
			if (loadingCamera == null) {
				newFromRdf = true; // Trigger new viewport creation for a new camera from RDF
				loadingCamera = cloneCamera(getCommonCamera(CommonCameras.DEFAULT)); // otherwise we create a new one...
				registerNamedCamera(cameraName, loadingCamera); // and register it
			}
			float[] cameraViewPort = cc.cameraViewPort;
			loadingCamera.setViewPort(cameraViewPort[0], cameraViewPort[1], cameraViewPort[2], cameraViewPort[3]);
			if (cameraName.equals(CommonCameras.HEAD_CAM.name())) {
				addHeadCamera(loadingCamera, cc); // Special handling for head cam
			} else {
				float[] cameraPos = cc.cameraPosition;
				loadingCamera.setLocation(new Vector3f(cameraPos[0], cameraPos[1], cameraPos[2]));
				float[] cameraDir = cc.cameraPointDir;
				loadingCamera.lookAtDirection(new Vector3f(cameraDir[0], cameraDir[1], cameraDir[2]), Vector3f.UNIT_Y);
				if (cameraName.equals(CommonCameras.DEFAULT.name())) {// If we are setting default camera info, save position/direction for later reset
					defaultPosition = new Vector3f(cameraPos[0], cameraPos[1], cameraPos[2]);
					defaultDirection = new Vector3f(cameraDir[0], cameraDir[1], cameraDir[2]);
				}
			}
			if (newFromRdf) {
				theLogger.info("Camera with config: " + cc + " is new from RDF, creating new viewport...");
				RenderRegistryClient rrc = hrc.getRenderRegistryClient();
				CoreFeatureAdapter.addViewPort(rrc, cameraName, loadingCamera);
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

	public void addHeadCamera(Camera headCam, CameraConfig config) {
		if (hrc != null) {
			CameraNode headCamNode = new CameraNode(CommonCameras.HEAD_CAM.name() + "_NODE", headCam);
			headCamNode.setControlDir(ControlDirection.SpatialToCamera);
			CoreFeatureAdapter.attachToHumanoidBone(hrc, headCamNode, config.attachedItem);
			float[] cameraPos = config.cameraPosition;
			headCamNode.setLocalTranslation(new Vector3f(cameraPos[0], cameraPos[1], cameraPos[2]));
		} else {
			theLogger.warn("Attempting to add head camera, but HumanoidRenderContext has not been set!");
		}
	}

	public void setHumanoidRenderContext(HumanoidRenderContext hrcInstance) {
		hrc = hrcInstance;
	}
	/*
	 * public FlyByCamera fbc = app.getFlyByCamera(); fbc.setDragToRotate(true); fbc.setMoveSpeed(10f);
	 * app.setPauseOnLostFocus(false);
	 */
}
