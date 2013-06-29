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
package org.cogchar.render.opengl.scene;

import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection;
import java.util.Map;
import java.util.concurrent.Callable;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.api.cinema.SpatialActionConfig;
import org.cogchar.render.app.entity.VWorldEntity;
import org.cogchar.render.goody.basic.BasicGoodyEntity;
import org.cogchar.render.app.entity.GoodyFactory;
import org.cogchar.render.opengl.optic.CameraMgr;
import org.cogchar.render.sys.context.CogcharRenderContext;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.slf4j.Logger;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class SpatialGrabber extends BasicDebugger {
	
	private static Logger theLogger = getLoggerForClass(SpatialGrabber.class);
	
	private CogcharRenderContext myCRC;
	
	SpatialGrabber(CogcharRenderContext crc) {
		myCRC = crc;
	}
	
	Spatial getSpatialForSpecifiedType(SpatialActionConfig track, Map<String, CameraNode> boundCameras) {
		Spatial attachedSpatial = null;
		switch (track.attachedItemType) {
			case CAMERA: {
				attachedSpatial = getSpatialForAttachedCamera(track, boundCameras);
				break;
			}
			case GOODY: {
				attachedSpatial = getSpatialForAttachedGoody(track);
				break;
			}
			default: {
				theLogger.error("Unsupported attached item type in animation: {}", track.attachedItemType);
			}
		}
		return attachedSpatial;
	}
	
	Spatial getSpatialForAttachedCamera(SpatialActionConfig track, Map<String, CameraNode> boundCameras) {
		Spatial attachedSpatial;
		final RenderRegistryClient rrc = myCRC.getRenderRegistryClient();
		CameraMgr cm = rrc.getOpticCameraFacade(null);
		final String cameraLocalName = track.attachedItem.getLocalName();
		if (boundCameras.containsKey(cameraLocalName)) {
			// Hey, we already bound this to the cinematic! We'll just get the node to attach to the track.
			getLogger().info("Attached camera already bound, reusing in track: {}", track);
			attachedSpatial = boundCameras.get(cameraLocalName);
			// Must reattach since probably was unattached in PathMgr.DetachingMotionEvent.onStop
			rrc.getJme3RootDeepNode(null).attachChild(attachedSpatial);
		} else {
			final Camera cineCam = cm.getNamedCamera(cameraLocalName);
			if (cineCam != null) {
				final CameraNode camNode = new CameraNode(cameraLocalName, cineCam);
				rrc.getWorkaroundAppStub().enqueue(new Callable() {
					@Override
					public Void call() throws Exception {
						camNode.setLocalTranslation(cineCam.getLocation());
						camNode.setLocalRotation(cineCam.getRotation());
						camNode.setControlDir(ControlDirection.SpatialToCamera);
						rrc.getJme3RootDeepNode(null).attachChild(camNode);
						return null;
					}
				});
				camNode.setEnabled(true);
				attachedSpatial = camNode;
				boundCameras.put(cameraLocalName, (CameraNode) attachedSpatial); // Could just use Node as type of HashMap and not cast back and forth, but this makes it explicit
			} else {
				getLogger().error("Specified Camera not found for Cinematic config from RDF: {}", track.attachedItem);
				return null;
			}
		}
		return attachedSpatial;
	}
	
	Spatial getSpatialForAttachedGoody(SpatialActionConfig track) {
		Spatial attachedSpatial = null;
		try {
			VWorldEntity desiredGoody = GoodyFactory.getTheFactory().getActionConsumer().getGoody(track.attachedItem);
			if (desiredGoody instanceof BasicGoodyEntity) {
				BasicGoodyEntity goody3d = (BasicGoodyEntity) desiredGoody;
				attachedSpatial = goody3d.getCurrentAttachedGeometry();
			} else {
				getLogger().warn("Attempting to attach goody of improper type to cinematic: {}. Aborting.",
						desiredGoody.getUri().getLocalName());
			}
		} catch (Exception e) {
			getLogger().error("Exception binding goody to cinematic: {}", e.toString());
			attachedSpatial = null;
		}
		return attachedSpatial;
	}
}
