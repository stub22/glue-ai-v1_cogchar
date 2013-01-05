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

import com.jme3.animation.*;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.api.cinema.*;
import org.cogchar.render.sys.context.CogcharRenderContext;
import org.slf4j.Logger;

/**
 * Generates and controls jMonkey spatial animations for virtual world "things"
 *
 * @author Ryan Biggs
 */

// TODO:
// - Add vector scale provisions
// - Add LoopMode
// - Implement pause
// - Factor out common bits with PathMgr

public class SpatialAnimMgr extends BasicDebugger {

	private Logger staticLogger = getLoggerForClass(SpatialAnimMgr.class);
	private CogcharRenderContext myCRC;
	private Map<Ident, AnimChannel> myChannelsByUri = new HashMap<Ident, AnimChannel>();
	
	private boolean noPosition(float[] waypointDef) {
        return (new Float(waypointDef[0]).isNaN()) || (new Float(waypointDef[1]).isNaN()) || (new Float(waypointDef[2]).isNaN());
    }

	public void storeAnimationsFromConfig(ThingAnimConfig config, CogcharRenderContext crc) {

		myCRC = crc;

		// Next, we build the cinematics, using named tracks/waypoints/rotations if required.
		for (ThingAnimInstanceConfig taic : config.myTAICs) {
			buildAnimation(taic);
		}
			
	}

	// Public so that Thing API can build animations, although a flat-out public scope is a little dangerous and may be ammended
	public void buildAnimation(ThingAnimInstanceConfig aic) {
		staticLogger.info("Building Thing Spatial Animation from RDF: {}", aic);
		CogcharRenderContext crc = myCRC;
		Map<String, CameraNode> boundCameras = new HashMap<String, CameraNode>(); // To keep track of cameras bound to this cinematic
		
		Spatial attachedSpatial = null;
		SpatialGrabber grabber = new SpatialGrabber(myCRC);
		switch (aic.attachedItemType) {
			case CAMERA: {
				attachedSpatial = grabber.getSpatialForAttachedCamera(aic, boundCameras);
				break;
			}
			case GOODY: {
				attachedSpatial = grabber.getSpatialForAttachedGoody(aic);
				break;
			}
			default: {
				staticLogger.error("Unsupported attached item type in animation: {}", aic.attachedItemType);
			}
		}
		
		AnimWaypointsConfig waypointInfo = AnimWaypointsConfig.getMainConfig();
		
		if (attachedSpatial != null) {
			final String animName = aic.myUri.getLocalName();
			AnimationFactory aniFactory = new AnimationFactory(aic.duration, animName);
			Vector3f lastPosition = attachedSpatial.getLocalTranslation();
			Quaternion lastOrientation = attachedSpatial.getLocalRotation();
			Vector3f lastScale = attachedSpatial.getLocalScale();
			// Set "zero frame" to original spatial parameters. Can be overriden by a repo defined frame with t=0.
			aniFactory.addKeyFrameTranslation(0, lastPosition);
			aniFactory.addKeyFrameRotation(0, lastOrientation);
			aniFactory.addKeyFrameScale(0, lastScale);
			// Add key frames from repo definition
			for (ThingAnimInstanceConfig.KeyFrameConfig kfc : aic.myKeyFrameDefinitions) {
				float time = kfc.myTime;
				// Determine location for the key frame, or use last location if none
				WaypointConfig frameLocationConfig = waypointInfo.myWCs.get(kfc.myLocation);
				Vector3f frameLocation;
				if (frameLocationConfig == null) {
					frameLocation = lastPosition;
				} else {
					float[] frameLocationVector = frameLocationConfig.myCoordinates;
					frameLocation = new Vector3f(frameLocationVector[0], frameLocationVector[1], frameLocationVector[2]);
				}
				// Determine orientation for the key frame, or use last orientation if none
				RotationConfig frameRotationConfig = waypointInfo.myRCs.get(kfc.myOrientation);
				Quaternion frameRotation;
				if (frameRotationConfig == null) {
					frameRotation = lastOrientation;
				} else {
					Vector3f rotationAxis = new Vector3f(frameRotationConfig.rotX, frameRotationConfig.rotY, frameRotationConfig.rotZ);
					frameRotation = new Quaternion().fromAngleAxis(frameRotationConfig.rotMag, rotationAxis);
				}
				// Determine scale for the key frame, or use last scale if none
				float[] frameScaleVector = kfc.getScaleVector();
				Vector3f frameScale;
				if (noPosition(frameScaleVector)) {
					frameScale = lastScale;
				} else {
					frameScale = new Vector3f(frameScaleVector[0], frameScaleVector[1], frameScaleVector[2]);
				}
				// if this is a t=0 frame, so we'll put it at index 0 so AnimationFactory will replace the default identity transforms
				// Probably want to modify this so that parameters are not specified at key frames unless specified in repo
				if (time != Float.NaN) {
					if (time < 0.01f) { 
						aniFactory.addKeyFrameTranslation(0, frameLocation);
						aniFactory.addKeyFrameRotation(0, frameRotation);
						aniFactory.addKeyFrameScale(0, frameScale);
					} else {
						aniFactory.addTimeTranslation(time, frameLocation);
						aniFactory.addTimeRotation(time, frameRotation);
						aniFactory.addTimeScale(time, frameScale);
					}
				} else {
					getLogger().warn("Detected a key frame with unspecified time in animation {}; ignoring...", aic.myUri);
				}
			}
			// Now the Animation is generated and linked to the geometry via an AnimationControl
			Animation newAnimation = aniFactory.buildAnimation();
			// Get existing AnimControl from Spatial if one already exists
			AnimControl animControl = attachedSpatial.getControl(AnimControl.class);
			if (animControl == null) {
				animControl = new AnimControl();
			}
			animControl.addAnim(newAnimation);
			attachedSpatial.addControl(animControl);
			AnimChannel animChannel = animControl.createChannel();
			myChannelsByUri.put(aic.myUri, animChannel);
		}
	}

	static final String PATH_URI_PREFIX = "http://www.cogchar.org/schema/thinganim/definition#"; // Temporary
	public boolean controlAnimByName(final String localName, SpatialAnimMgr.ControlAction action) { // Soon switching to controlAnimByUri
		boolean validAction = true;
		final Ident uri = new FreeIdent(PATH_URI_PREFIX + localName); // Just temporary until we upgrade the food chain to send URI directly from lifter
		final AnimChannel channel = myChannelsByUri.get(uri);
		if (channel != null) {
			if (action.equals(SpatialAnimMgr.ControlAction.PLAY)) {
				staticLogger.info("Playing thing animation {}", uri);
				channel.setAnim(localName, 0f);
				// Oddly, it seems this needs to be set *after* starting the animation with setAnim:
				// Likely we want to add the ability to make a looping anim via config
				channel.setLoopMode(LoopMode.DontLoop);
			} else if (action.equals(SpatialAnimMgr.ControlAction.STOP)) {
				// Wouldn't you know, this has to be done on main thread
				Future<Object> waitForThis = myCRC.enqueueCallable(new Callable<Boolean>() {

					@Override
					public Boolean call() throws Exception {
						staticLogger.info("Stopping thing animation {}", uri);
						channel.reset(true);
						return true;
					}
				});
				try {
					// We call waitForThis.get (and discard the result) so that this method doesn't return until STOP is actially executed
					waitForThis.get(3, java.util.concurrent.TimeUnit.SECONDS);
				} catch (Exception e) {
					staticLogger.error("Exception stopping animation: {}", e.toString());
				}
			} else if (action.equals(SpatialAnimMgr.ControlAction.PAUSE)) {
				staticLogger.info("Pausing thing animation {}", uri);
				staticLogger.info("Pause has not yet been implemented, ignoring...");
				// Probably need to channel.reset(false) and retain time for restart?
			} else {
				validAction = false;
			}
		} else {
			staticLogger.error("No thing animation found by URI: {}", uri);
			validAction = false;
		}
		return validAction;
	}

	public void clearAnims() {
		myChannelsByUri.clear();
		staticLogger.info("Animations cleared.");
	}

	public enum ControlAction {

		PLAY, STOP, PAUSE
	}
}
