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
import com.jme3.scene.Node;
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

	// Yuck - to be replaced with general (node-)AttachedCameraBinding
	public static interface AttachmentNodeFinder {
		public Node findNode(CameraConfig config, CogcharRenderContext crc);
	}

	private AttachmentNodeFinder	myAttachmentNodeFinder;
	private	Queuer					myQueuer;
	
	// private Map<String, CameraBinding> myCameraBindingsByName = new HashMap<String, CameraBinding>();
	private Map<Ident, CameraBinding> myCameraBindingsByID = new HashMap<Ident, CameraBinding>();
	
	private static Ident		DEF_CAM_ID = new FreeIdent(LightsCameraAN.URI_defaultCam);
	// This is what jME calls the flycam viewport. We won't mess with it in clearViewPorts and such.
	// private static final String DEFAULT_VIEWPORT_NAME = "Gui Default"; 
	

	public void registerDefaultCamera(Camera defCam, Queuer aQueuer) { 
		// This method gets called from  CoreFeatureAdapter.registerJMonkeyDefaultCameras()		
		theLogger.info("Registering DefaultCamera and Queuer");
		myQueuer = aQueuer;
		CameraBinding defCB = new MainCameraBinding(myQueuer, DEF_CAM_ID);
		defCB.setCamera(defCam);
		myCameraBindingsByID.put(DEF_CAM_ID, defCB);
	}
	public CameraBinding getCameraBinding(Ident id) {
		return myCameraBindingsByID.get(id);
	}
	public CameraBinding getDefaultCameraBinding() {
		return getCameraBinding(DEF_CAM_ID);
	}	

	public void setAttachmentNodeFinder(AttachmentNodeFinder hcm) {
		myAttachmentNodeFinder = hcm;
	}

	public CameraBinding findOrMakeCameraBinding(Ident camID) {
		CameraBinding camBind = getCameraBinding(camID); 
		if (camBind == null) {	
			CameraBinding	defaultBinding = getDefaultCameraBinding();
			if (defaultBinding != null) {
				camBind = defaultBinding.makeClone(camID);
			} else {
				throw new RuntimeException("Default camera binding is unknown, so we have no default cam to clone()");
			}
			myCameraBindingsByID.put(camID, camBind);
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
	
	public void applyCameraConfig(CameraConfig cConf, RenderRegistryClient rrc, CogcharRenderContext crc) { // CogcharRenderContext crc) {
		
		Ident camID = cConf.myCamID;
		CameraBinding camBind = findOrMakeCameraBinding(camID); 
		if (camBind != null) {
			boolean assignDefaults = true;
			camBind.setValsFromConfig(cConf, assignDefaults);
			camBind.attachViewPort(rrc);
			camBind.applyInVWorld(Queuer.QueueingStyle.QUEUE_AND_RETURN);
		}
		// TODO:  Use rdf:type
		// Old way:
		// boolean flag_isHeadCam = camID.getLocalName().contains(LightsCameraAN.suffix_HEAD_CAM);
		boolean flag_isHeadCam = cConf.myBoneAttachmentFlag;
		if (flag_isHeadCam) {
			if (myAttachmentNodeFinder != null) {
				Node attachmentNode = myAttachmentNodeFinder.findNode(cConf, crc);
				if (attachmentNode != null) {
					camBind.attachToNode(attachmentNode);
				}
			}
			else {
				throw new RuntimeException("No AttachmentNodeFinder registered");
			}			
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
		for (Entry<Ident, CameraBinding> e : myCameraBindingsByID.entrySet()) {
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
