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

import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.CameraNode;
import com.jme3.scene.control.CameraControl.ControlDirection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cogchar.api.cinema.CameraConfig;
import org.cogchar.api.cinema.LightsCameraConfig;
import org.cogchar.name.cinema.LightsCameraAN;
import org.cogchar.render.sys.context.CogcharRenderContext;
// import org.cogchar.render.app.humanoid.HumanoidRenderContext;
// import org.cogchar.render.model.humanoid.HumanoidFigureManager;
import org.cogchar.render.sys.registry.RenderRegistryClient;
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
	public static interface HeadCameraManager {
		public void addHeadCamera(Camera headCam, CameraConfig config, CogcharRenderContext crc);
	}
	
	private Map<String, Camera> myCamerasByName = new HashMap<String, Camera>();
	// A list of names of cameras (viewports have the same names) which have an active ViewPort
	private List<String> attachedViewPortNames = new ArrayList<String>(); // A list of names of cameras (viewports have the same names)
	private Vector3f defaultPosition;
	private Vector3f defaultDirection;
	
	private static final String DEFAULT_VIEWPORT_NAME = "Gui Default"; // This is what jME calls the flycam viewport. We won't mess with it in clearViewPorts and such.
	private HeadCameraManager myHeadCamMgr;
	public Camera cloneCamera(Camera orig) {
		return orig.clone();
	}
	public void setHeadCameraManager(HeadCameraManager hcm) {
		myHeadCamMgr = hcm;
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
	
	// Needed for interim GoodySpace additions to allow control of cameras. Can be removed once Robosteps VirtualWorld
	// gets list of cameras directly from RDF.
	public Map<String, Camera> getCameraMap() {
		return myCamerasByName;
	}

	public void registerCommonCamera(CommonCameras id, Camera cam) {
		registerNamedCamera(id.name(), cam);
	}

	public Camera getCommonCamera(CommonCameras id) {
		return getNamedCamera(id.name());
	}
	public void addHeadCamera(Camera headCam, CameraConfig config, CogcharRenderContext crc) {
		if (myHeadCamMgr != null) {
			myHeadCamMgr.addHeadCamera(headCam, config, crc);
		} else {
			throw new RuntimeException("No HeadCameraMgr registered");
		}
	}
	public void initCamerasFromConfig(LightsCameraConfig config, CogcharRenderContext crc) {
		RenderRegistryClient rrc = crc.getRenderRegistryClient();
		for (CameraConfig cc : config.myCCs) {
			theLogger.info("Building Camera for config: {}", cc);
			String cameraName = cc.cameraName;
			// If the RDF camera name is camera_default (with any case), normalize name of this very special camera to "DEFAULT"
			if (cameraName.toLowerCase().replaceFirst(LightsCameraAN.partial_P_camera, "").equals("_" + LightsCameraAN.suffix_DEFAULT)) {
				cameraName = CommonCameras.DEFAULT.name();
			}
			if (cameraName.toLowerCase().replaceFirst(LightsCameraAN.partial_P_camera, "").equals("_" + LightsCameraAN.suffix_HEAD_CAM)) {
				cameraName = CommonCameras.HEAD_CAM.name();
			}
			Camera loadingCamera = getNamedCamera(cameraName); // First let's see if we can get a registered camera by this name
			if (loadingCamera == null) {
				loadingCamera = cloneCamera(getCommonCamera(CommonCameras.DEFAULT)); // otherwise we create a new one...
				registerNamedCamera(cameraName, loadingCamera); // and register it
			}
			float[] cameraViewPort = cc.cameraViewPort;
			loadingCamera.setViewPort(cameraViewPort[0], cameraViewPort[1], cameraViewPort[2], cameraViewPort[3]);
			if (cameraName.equals(CommonCameras.HEAD_CAM.name())) {
				addHeadCamera(loadingCamera, cc, crc); // Special handling for head cam
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
			if ((!attachedViewPortNames.contains(cameraName)) && (!CommonCameras.DEFAULT.name().equals(cameraName))) {
				theLogger.info("Camera with config: {} is new from RDF, creating new viewport...", cc);
				addViewPort(cameraName, loadingCamera, rrc);
				attachedViewPortNames.add(cameraName);
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


	
	// Moving this here from DeepSceneMgr. A judgement call, but it seeems invoking 3 additional facades
	// (CoreFeatureAdapter, DeepSceneMgr, ViewPortFacade) is needlessly complex when really the RenderRegistryClient
	// is all we need. We need RenderContext to get RenderRegistryClient, but we need it to get other facades anyway.
	// ViewPort stuff is inherently camera stuff, so why not have it live here for less confusion?
	public ViewPort addViewPort(String label, Camera c, RenderRegistryClient rrc) {
		ViewPort vp = rrc.getJme3RenderManager(null).createPostView(label, c); // PostView or MainView?
		vp.setClearFlags(true, true, true);
		vp.setBackgroundColor(ColorRGBA.LightGray); // This is set for main window right now in WorkaroundFuncsMustDie.setupCameraLightAndViewport - yuck. May want a more consistent way to do this in long run.
		vp.attachScene(rrc.getJme3RootDeepNode(null));
		return vp;
	}
	
	
	// Does not remove cameras from myCamerasByName, but removes additional viewports to "clear" RDF cameras
	public void clearViewPorts(RenderRegistryClient rrc) {
		RenderManager rm = rrc.getJme3RenderManager(null);
		List<ViewPort> attachedViewPorts = rm.getPostViews();
		theLogger.info("Clearing ViewPorts...");
		Object[] viewPortArray = attachedViewPorts.toArray(); 
		for (int i=0; i<viewPortArray.length; i++) {
			ViewPort viewPort = (ViewPort)viewPortArray[i];
			if (!DEFAULT_VIEWPORT_NAME.equals(viewPort.getName())) {
				theLogger.info("Removing ViewPort: {}", viewPort.getName());
				rm.removePostView(viewPort);
			}
		}
		attachedViewPortNames.clear();
		/* ...for some reason this method misses an instance
		for (ViewPort viewPort: attachedViewPorts) {
			theLogger.info("Checking ViewPort: " + viewPort.getName()); // TEST ONLY
			if (!CommonCameras.DEFAULT.name().equals(viewPort.getName())) {
				theLogger.info("Removing ViewPort: " + viewPort.getName());
				rm.removePostView(viewPort);
			}
		}
		*/
	
	}

	/*
	 * public FlyByCamera fbc = app.getFlyByCamera(); fbc.setDragToRotate(true); fbc.setMoveSpeed(10f);
	 * app.setPauseOnLostFocus(false);
	 */
}
