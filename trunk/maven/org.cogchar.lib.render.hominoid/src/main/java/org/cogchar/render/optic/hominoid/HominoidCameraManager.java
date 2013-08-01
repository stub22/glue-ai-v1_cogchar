/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.render.optic.hominoid;

import org.cogchar.api.cinema.CameraConfig;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.model.humanoid.HumanoidFigureManager;
import org.cogchar.render.opengl.optic.CameraMgr;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.CameraNode;
import com.jme3.scene.control.CameraControl.ControlDirection;
import org.cogchar.render.sys.context.CogcharRenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Owner
 */
public class HominoidCameraManager implements CameraMgr.HeadCameraManager {
	static Logger theLogger = LoggerFactory.getLogger(HominoidCameraManager.class);
	
	@Override public void addHeadCamera(Camera headCam, CameraConfig config, CogcharRenderContext crc) {
	
		if (crc != null) {
			HumanoidRenderContext hrc = (HumanoidRenderContext) crc;
			HumanoidFigureManager hfm = hrc.getHumanoidFigureManager();
			CameraNode headCamNode = new CameraNode(CameraMgr.CommonCameras.HEAD_CAM.name() + "_NODE", headCam);
			headCamNode.setControlDir(ControlDirection.SpatialToCamera);
			//theLogger.info("Attaching head cam to robot ident: " + config.attachedRobot + " bone " + config.attachedItem); // TEST ONLY
			hfm.attachNodeToHumanoidBone(hrc, headCamNode, config.attachedRobot, config.attachedItem);
			float[] cameraPos = config.cameraPosition;
			float[] cameraDir = config.cameraPointDir;
			headCamNode.setLocalTranslation(new Vector3f(cameraPos[0], cameraPos[1], cameraPos[2]));
			headCamNode.setLocalRotation(new Quaternion().fromAngles(cameraDir));
		} else {
			theLogger.warn("Attempting to add head camera, but HumanoidRenderContext has not been set!");
		}
	}	


}
