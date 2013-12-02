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

import com.jme3.scene.Node;

import org.cogchar.render.sys.context.CogcharRenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *	@author RyanB
 *	@author StuB22
 */
public class HominoidCameraManager implements CameraMgr.AttachmentNodeFinder {
	static Logger theLogger = LoggerFactory.getLogger(HominoidCameraManager.class);
	
	@Override public Node findNode(CameraConfig config, CogcharRenderContext crc) {
		Node attachmentNode = null;
		if (crc != null) {
			HumanoidRenderContext hrc = (HumanoidRenderContext) crc;
			HumanoidFigureManager hfm = hrc.getHumanoidFigureManager();
			attachmentNode = hfm.findHumanoidBone(hrc, config.myAttachedRobotID, config.myAttachedBoneName);
		} else {
			theLogger.warn("Attempting to add head camera, but HumanoidRenderContext has not been set!");
		}
		return attachmentNode;
	}	


}
