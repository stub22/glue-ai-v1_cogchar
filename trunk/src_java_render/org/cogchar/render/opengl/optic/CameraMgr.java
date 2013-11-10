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
import java.util.Map.Entry;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.api.cinema.CameraConfig;
import org.cogchar.api.cinema.LightsCameraConfig;
import org.cogchar.name.cinema.LightsCameraAN;
import org.cogchar.render.app.entity.MainCameraBinding;
import org.cogchar.render.sys.context.CogcharRenderContext;
// import org.cogchar.render.app.humanoid.HumanoidRenderContext;
// import org.cogchar.render.model.humanoid.HumanoidFigureManager;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.cogchar.render.sys.task.Queuer;
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
	
	
	// This is what jME calls the flycam viewport. We won't mess with it in clearViewPorts and such.
	// private static final String DEFAULT_VIEWPORT_NAME = "Gui Default"; 
	
	private HeadCameraManager	myHeadCamMgr;
	private	Queuer				myQueuer;
	
	private Map<String, CameraBinding> myCameraBindingsByName = new HashMap<String, CameraBinding>();
	
	
	// This gets called from 
	// CoreFeatureAdapter.registerJMonkeyDefaultCameras()
	public void registerDefaultCamera(Camera defCam, Queuer aQueuer) { 
		theLogger.info("Registering DefaultCamera and Queuer");
		myQueuer = aQueuer;
		registerCommonCamera(CameraBinding.Kind.DEFAULT, defCam);

	}
	public CameraBinding getCommonCameraBinding(CameraBinding.Kind kind) {
		return getNamedCameraBinding(kind.name());
	}	
	public CameraBinding getDefaultCameraBinding() {
		return getCommonCameraBinding(CameraBinding.Kind.DEFAULT);
	}	

	public void setHeadCameraManager(HeadCameraManager hcm) {
		myHeadCamMgr = hcm;
	}

	public CameraBinding getNamedCameraBinding(String name) {
		CameraBinding camBinding = myCameraBindingsByName.get(name);
		// System.out.println("**********######################*********************###############*************** cameraMgr: " + this + " - found cam " + cam + " at " + name);		
		return camBinding;
	}

	private void registerCommonCamera(CameraBinding.Kind kind, Camera cam) {
		Ident kindID = new FreeIdent(LightsCameraAN.P_camera + "_" + kind.name());
		CameraBinding cb;
		if (kind == CameraBinding.Kind.DEFAULT) {
			cb = new MainCameraBinding(myQueuer, kindID);
		} else {
			cb = new CameraBinding(myQueuer, kindID);
		}
		cb.setCamera(cam);
		// System.out.println("**********######################*********************###############*************** cameraMgr: " + this + " - storing cam " + cam + " at " + name);
		myCameraBindingsByName.put(kind.name(), cb);		
	}

	private void addHeadCamera(Camera headCam, CameraConfig config, CogcharRenderContext crc) {
		if (myHeadCamMgr != null) {
			myHeadCamMgr.addHeadCamera(headCam, config, crc);
		} else {
			throw new RuntimeException("No HeadCameraMgr registered");
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
	public CameraBinding findOrMakeCameraBinding(String resolvedCamName) {
		CameraBinding camBind = getNamedCameraBinding(resolvedCamName); // First let's see if we can get a registered camera by this name
		if (camBind == null) {
			Ident	camID = new FreeIdent(LightsCameraAN.P_camera + "_" + resolvedCamName);			
			CameraBinding	defaultBinding = getDefaultCameraBinding();
			if (defaultBinding != null) {
				camBind = defaultBinding.makeClone(resolvedCamName, camID);
			} else {
				throw new RuntimeException("Default camera binding is unknown, so we have no default cam to clone()");
			}
			myCameraBindingsByName.put(resolvedCamName, camBind);
		}
		return camBind;
	}
	public void initCamerasFromConfig(LightsCameraConfig config, CogcharRenderContext crc) {
		RenderRegistryClient rrc = crc.getRenderRegistryClient();
		for (CameraConfig cc : config.myCCs) {
			theLogger.info("Building Camera for config: {}", cc);
			applyCameraConfig(cc, rrc, crc);
		}
	}
	
	private void applyCameraConfig(CameraConfig cConf, RenderRegistryClient rrc, CogcharRenderContext crc) { // CogcharRenderContext crc) {
		String resolvedCamName = resolveCameraName(cConf.myCamName);
		boolean flag_isHeadCam = resolvedCamName.equals(CameraBinding.Kind.HEAD_CAM.name());

		CameraBinding camBind = findOrMakeCameraBinding(resolvedCamName); 
		
		boolean assignDefaults = true;
		camBind.setValsFromConfig(cConf, assignDefaults);
		camBind.attachViewPort(rrc);
		camBind.applyInVWorld(Queuer.QueueingStyle.QUEUE_AND_RETURN);

		if (flag_isHeadCam) {
			Camera cam = camBind.getCamera();
			addHeadCamera(cam, cConf, crc); // Special handling for head cam
		} 
	}

	public void resetDefaultCamera() {
		CameraBinding defaultCamBinding = getDefaultCameraBinding();
		defaultCamBinding.resetToDefault();
		defaultCamBinding.applyInVWorld(Queuer.QueueingStyle.QUEUE_AND_RETURN);		
	}


	// Does not remove cameras from myCamerasByName, but removes additional viewports to "clear" RDF cameras
	public void clearViewPorts(RenderRegistryClient rrc) {
		theLogger.info("Clearing ViewPorts...");
		for (Entry<String, CameraBinding> e : myCameraBindingsByName.entrySet()) {
			CameraBinding cb = e.getValue();
			// Exclusion of default should happen based on class.
			cb.detachViewPort(rrc);
		}
	}

	/*
	 * public FlyByCamera fbc = app.getFlyByCamera(); fbc.setDragToRotate(true); fbc.setMoveSpeed(10f);
	 * app.setPauseOnLostFocus(false);
	 */
}
