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

import org.cogchar.render.app.entity.CameraBinding;
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

	public static interface HeadCameraManager {

		public void addHeadCamera(Camera headCam, CameraConfig config, CogcharRenderContext crc);
	}
	private Map<String, Camera> myCamerasByName = new HashMap<String, Camera>();
	// A list of names of cameras (viewports have the same names) which have an active ViewPort
	private List<String> attachedViewPortNames = new ArrayList<String>(); // A list of names of cameras (viewports have the same names)
	private Vector3f myDefCamPosVec3f;
	private Vector3f myDefCamPointDirVec3f;
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

	public void registerCommonCamera(CameraBinding.Kind kind, Camera cam) {
		registerNamedCamera(kind.name(), cam);
	}

	public Camera getCommonCamera(CameraBinding.Kind kind) {
		return getNamedCamera(kind.name());
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
			applyCameraConfig(cc, rrc, crc);
		}
	}

	private String resolveCameraName(String rawName) {
		String resolvedName = rawName;
		String condensedCamName = rawName.toLowerCase().replaceFirst(LightsCameraAN.partial_P_camera, "");

		// If the RDF camera name is camera_default (with any case), normalize name of this very special camera to "DEFAULT"
		if (condensedCamName.equals("_" + LightsCameraAN.suffix_DEFAULT)) {
			resolvedName = CameraBinding.Kind.DEFAULT.name();
		}
		if (condensedCamName.equals("_" + LightsCameraAN.suffix_HEAD_CAM)) {
			resolvedName = CameraBinding.Kind.HEAD_CAM.name();
		}
		return resolvedName;
	}
	public Camera findOrMakeCamera(String resolvedCamName) {
		Camera cam = getNamedCamera(resolvedCamName); // First let's see if we can get a registered camera by this name
		if (cam == null) {
			cam = cloneCamera(getCommonCamera(CameraBinding.Kind.DEFAULT)); // otherwise we create a new one...
			registerNamedCamera(resolvedCamName, cam); // and register it
		}
		return cam;
	}
	public void applyCameraConfig(CameraConfig cConf, RenderRegistryClient rrc, CogcharRenderContext crc) { // CogcharRenderContext crc) {
		String resolvedCamName = resolveCameraName(cConf.myCamName);
		boolean flag_isHeadCam = resolvedCamName.equals(CameraBinding.Kind.HEAD_CAM.name());
		boolean flag_isDefaultCam = resolvedCamName.equals(CameraBinding.Kind.DEFAULT.name());

		Camera cam = findOrMakeCamera(resolvedCamName); 
		
		applyViewportRect(cam, cConf);

		if (flag_isHeadCam) {
			addHeadCamera(cam, cConf, crc); // Special handling for head cam
		} else {
			applyCamConfVectors(cam, cConf, flag_isDefaultCam);
		}
		if ((!attachedViewPortNames.contains(resolvedCamName)) && (!flag_isDefaultCam)) {
			theLogger.info("Camera with config: {} is new from RDF, creating new viewport...", cConf);
			addViewPort(resolvedCamName, cam, rrc);
			attachedViewPortNames.add(resolvedCamName);
		}
	}
	public void applyViewportRect(Camera cam, CameraConfig cConf) {
		float[] cameraViewPort = cConf.myDisplayRect;
		cam.setViewPort(cameraViewPort[0], cameraViewPort[1], cameraViewPort[2], cameraViewPort[3]);		
	}

	public void applyCamConfVectors(Camera camToMove, CameraConfig cConf, boolean flag_setDefVals) {
		float[] cameraPos = cConf.myCamPos;
		Vector3f camLocVec3f = new Vector3f(cameraPos[0], cameraPos[1], cameraPos[2]);
		camToMove.setLocation(camLocVec3f);
		float[] cameraDir = cConf.myCamPointDir;
		Vector3f camPointDirVec3f = new Vector3f(cameraDir[0], cameraDir[1], cameraDir[2]);
		camToMove.lookAtDirection(camPointDirVec3f, Vector3f.UNIT_Y);
		if (flag_setDefVals) {// If we are setting default camera info, save position/direction for later reset
			myDefCamPosVec3f = camLocVec3f;
			myDefCamPointDirVec3f = camPointDirVec3f;
		}
	}

	public void resetDefaultCamera() {
		Camera defaultCamera = getCommonCamera(CameraBinding.Kind.DEFAULT);
		if ((myDefCamPosVec3f != null) && (myDefCamPointDirVec3f != null)) {
			defaultCamera.setLocation(myDefCamPosVec3f);
			defaultCamera.lookAtDirection(myDefCamPointDirVec3f, Vector3f.UNIT_Y);
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
		for (int i = 0; i < viewPortArray.length; i++) {
			ViewPort viewPort = (ViewPort) viewPortArray[i];
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
