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

import org.appdapter.core.log.BasicDebugger;
import org.cogchar.api.scene.*;
import org.cogchar.render.app.core.CoreFeatureAdapter;
import org.cogchar.render.sys.core.WorkaroundFuncsMustDie;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.sys.core.RenderRegistryClient;
import org.cogchar.render.opengl.optic.CameraMgr;
import org.cogchar.api.scene.SceneConfigNames;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.animation.LoopMode;
import com.jme3.cinematic.*;
import com.jme3.cinematic.events.*;
import com.jme3.scene.Node;
import com.jme3.scene.CameraNode;
import java.lang.Float;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.slf4j.Logger;

/**
 *
 * @author Ryan Biggs
 */
public class CinematicMgr extends BasicDebugger {

	private static Map<String, Cinematic> myCinematicsByName = new HashMap<String, Cinematic>();
	private static Map<String, CinematicTrack> myTracksByName = new HashMap<String, CinematicTrack>();
	private static Map<String, WaypointConfig> myWaypointsByName = new HashMap<String, WaypointConfig>();
	private static Map<String, RotationConfig> myRotationsByName = new HashMap<String, RotationConfig>();
	//private static Map<String, Boolean> myCinematicsReadyStatusByName = new HashMap<String, Boolean>(); // May not need this, woohoo!
	private static Logger staticLogger = getLoggerForClass(CinematicMgr.class);
	private static HumanoidRenderContext myHrc;

	public static void storeCinematicsFromConfig(CinematicConfig config, HumanoidRenderContext hrc) {
		myHrc = hrc;
		RenderRegistryClient rrc = hrc.getRenderRegistryClient();
		Node jmeRootNode = rrc.getJme3RootDeepNode(null);

		// First, any named waypoints defined outside track definitions are stored for later use
		for (WaypointConfig wc : config.myWCs) {
			staticLogger.info("Storing Named Waypoint from RDF: " + wc);
			myWaypointsByName.put(wc.waypointName, wc);
		}

		// Also "first", any named rotations are stored
		for (RotationConfig rc : config.myRCs) {
			staticLogger.info("Storing Named Rotation from RDF: " + rc);
			myRotationsByName.put(rc.rotationName, rc);
		}

		// Second, any named tracks defined outside cinematics definitions are stored for later use
		for (CinematicTrack ct : config.myCTs) {
			staticLogger.info("Storing Named Track from RDF: " + ct);
			myTracksByName.put(ct.trackName, ct);
		}

		// Next, we build the cinematics, using named tracks/waypoints/rotations if required.
		for (CinematicInstanceConfig cic : config.myCICs) {
			staticLogger.info("Building Cinematic from RDF: " + cic);
			final Cinematic cinematic = new Cinematic(jmeRootNode, cic.duration);
			Map<String, CameraNode> boundCameras = new HashMap<String, CameraNode>(); // To keep track of cameras bound to this cinematic
			// Add each track
			for (CinematicTrack track : cic.myTracks) {
				AbstractCinematicEvent event;
				if (track.trackType == CinematicTrack.TrackType.NULLTYPE) { // This usually indicates that the cinematic contains a reference to a named track defined elsewhere
					String trackReference = track.trackName;
					if (!trackReference.equals(CinematicConfigNames.unnamedTrackName)) {
						track = myTracksByName.get(trackReference); // Reset track to the CinematicTrack declared separately by name
						if (track == null) { // If so, cinematic is calling for a track we don't know about
							staticLogger.error("Cinematic has requested undefined track: " + trackReference + "; cinematic is " + cic);
							break;
						}
					} else {
						staticLogger.error("No trackType or trackName in track contained in cinematic: " + cic);
						break; // If no trackType and no trackName, we don't really have a track!
					}
				}
				Node attachedNode = null;
				if (track.attachedItemType == CinematicTrack.AttachedItemType.CAMERA) { // Which is all we support initally...
					CameraMgr cm = rrc.getOpticCameraFacade(null);
					if (boundCameras.containsKey(track.attachedItem)) {
						// Hey, we already bound this to the cinematic! We'll just get the node to attach to the track.
						staticLogger.info("Attached camera already bound, reusing in track: " + track);
						attachedNode = (Node) boundCameras.get(track.attachedItem);
					} else {
						final Camera cineCam = cm.getNamedCamera(track.attachedItem);
						if (cineCam != null) {
							// The following is not necessary if we use only cameras already loaded from RDF, as these have already had their viewports added
							//CoreFeatureAdapter.addViewPort(rrc, track.attachedItem, cineCam);
							// Bind the camera to the cinematic to make a CameraNode - this must be done on the main render thread
							final String cameraName = track.attachedItem;
							Future<Object> camNodeFuture = WorkaroundFuncsMustDie.enqueueCallableReturn(hrc, new Callable<CameraNode>() {

								@Override
								public CameraNode call() throws Exception {
									CameraNode camNode = cinematic.bindCamera(cameraName, cineCam);
									return camNode;
								}
							});
							try {
								attachedNode = (Node) camNodeFuture.get(3, java.util.concurrent.TimeUnit.SECONDS);
							} catch (Exception e) {
								staticLogger.error("Exception binding camera to cinematic: " + e.toString());
								break;
							}
							cinematic.activateCamera(0, cameraName); // Attached at time zero, because who knows what other track might use this camera?
							//cinematic.setActiveCamera(cameraName); // Can also do this, but it messes with initial camera position
							boundCameras.put(track.attachedItem, (CameraNode) attachedNode); // Could just use Node as type of HashMap and not cast back and forth, but this makes it explicit
						} else {
							staticLogger.error("Specified Camera not found for Cinematic config from RDF: " + track.attachedItem);
							break;
						}
					}
				} else {
					staticLogger.error("Unsupported attached item type in track: " + track.attachedItemType);
					break;
				}
				if (track.trackType == CinematicTrack.TrackType.MOTIONTRACK) {
					MotionPath path = new MotionPath();
					path.setCycle(track.cycle);
					for (WaypointConfig waypoint : track.waypoints) {
						if (noPosition(waypoint.waypointCoordinates)) { // If we don't have coordinates for this waypoint...
							// First check to see if this waypoint refers to a stored waypoint previously defined
							String waypointReference = waypoint.waypointName;
							if (!waypointReference.equals(CinematicConfigNames.unnamedWaypointName)) {
								waypoint = myWaypointsByName.get(waypointReference); // Reset waypoint to the WaypointConfig declared separately by name
								if (waypoint == null) { // If so, track is calling for a waypoint we don't know about
									staticLogger.error("Track has requested undefined waypoint: " + waypointReference + "; track is " + track);
									break;
								}
							} else {
								staticLogger.error("No coordinates or waypointName in waypoint contained in track: " + track);
								break; // If no coordinates and no waypointName, we don't really have a waypoint!
							}
						}
						//staticLogger.info("Making new waypoint: " + new Vector3f(waypoint.waypointCoordinates[0], waypoint.waypointCoordinates[1], waypoint.waypointCoordinates[2])); // TEST ONLY
						path.addWayPoint(new Vector3f(waypoint.waypointCoordinates[0], waypoint.waypointCoordinates[1], waypoint.waypointCoordinates[2]));
					}
					path.setCurveTension(track.tension);

					MotionTrack.Direction directionJmeType = null;
					for (MotionTrack.Direction testType : MotionTrack.Direction.values()) {
						if (track.directionType.equals(testType.toString())) {
							directionJmeType = testType;
						}
					}
					if (directionJmeType == null) {
						staticLogger.error("Specified MotionTrack direction type not in MotionTrack.Direction: " + track.directionType);
						break;
					}
					LoopMode loopJmeType = setLoopMode(track.loopMode);
					if (loopJmeType == null) {
						staticLogger.error("Specified MotionTrack loop mode not in com.jme3.animation.LoopMode: " + track.loopMode);
						break;
					}
					MotionTrack motionTrack = new MotionTrack(attachedNode, path);
					motionTrack.setDirectionType(directionJmeType);
					motionTrack.setLookAt(new Vector3f(track.direction[0], track.direction[1], track.direction[2]), Vector3f.UNIT_Y);
					motionTrack.setLoopMode(loopJmeType);
					event = motionTrack;
				} else if (track.trackType == CinematicTrack.TrackType.POSITIONTRACK) {
					float[] endPositionArray;
					// PositionTrack only supports one waypoint. Let's check to be sure there is only one waypoint, and that it is valid.
					if (track.waypoints.isEmpty()) {
						staticLogger.error("PositionTrack requested, but no waypoint provided for track: " + track);
						break;
					} else if (track.waypoints.size() != 1) {
						staticLogger.warn("PositionTrack requested, but more than one waypoint provided for track: " + track);
						staticLogger.warn("Extra waypoints discarded for Positiontrack");
					}
					endPositionArray = track.waypoints.get(0).waypointCoordinates;
					if (noPosition(endPositionArray)) { // If we don't have coordinates for this waypoint...
						// First check to see if this waypoint refers to a stored waypoint previously defined
						String waypointReference = track.waypoints.get(0).waypointName;
						if (!waypointReference.equals(CinematicConfigNames.unnamedWaypointName)) {
							WaypointConfig waypoint = myWaypointsByName.get(waypointReference); // Set waypoint to the WaypointConfig declared separately by name
							if (waypoint == null) { // If so, track is calling for a waypoint we don't know about
								staticLogger.error("Track has requested undefined waypoint: " + waypointReference + "; track is " + track);
								break;
							} else {
								endPositionArray = waypoint.waypointCoordinates;
							}

						} else {
							staticLogger.error("No coordinates or waypointName in waypoint contained in track: " + track);
							break; // If no coordinates and no waypointName, we don't really have a waypoint!
						}
					}
					Vector3f endPosition = new Vector3f(endPositionArray[0], endPositionArray[1], endPositionArray[2]);
					LoopMode loopJmeType = setLoopMode(track.loopMode);
					if (loopJmeType == null) {
						staticLogger.error("Specified PositionTrack loop mode not in com.jme3.animation.LoopMode: " + track.loopMode);
						break;
					}
					if (track.trackDuration <= 0) {
						staticLogger.warn("Warning: PositionTrack contains no positive cc:trackDuration, setting to zero");
						track.trackDuration = 0; // Just in case it is set to a negative number in RDF
					}
					event = new PositionTrack(attachedNode, endPosition, track.trackDuration, loopJmeType);
				} else if (track.trackType == CinematicTrack.TrackType.ROTATIONTRACK) {
					// First make sure rotation is valid
					RotationConfig rotation = track.endRotation;
					float[] rotationInArray = {rotation.pitch, rotation.yaw, rotation.roll};
					if (noPosition(rotationInArray)) { // If we don't have values for this rotation...
						// First check to see if this rotation refers to a stored rotation previously defined
						String rotationReference = rotation.rotationName;
						if (!rotationReference.equals(CinematicConfigNames.unnamedRotationName)) {
							rotation = myRotationsByName.get(rotationReference); // Reset rotation to the RotationConfig declared separately by name
							if (rotation == null) { // If so, track is calling for a rotation we don't know about
								staticLogger.error("Track has requested undefined rotation: " + rotationReference + "; track is " + track);
								break;
							} else {
								// Reset rotationInArray to values from loaded rotation
								rotationInArray[0] = rotation.pitch;
								rotationInArray[1] = rotation.yaw;
								rotationInArray[2] = rotation.roll;
							}
						} else {
							staticLogger.error("No valid rotation angles or rotationName in rotation contained in track: " + track);
							break; // If no coordinates and no rotationName, we don't really have a rotation!
						}
					}
					// Convert angles from degrees to radians
					for (int i = 0; i < rotationInArray.length; i++) { // Really Java? All this is required just to multiply an array by a constant?
						rotationInArray[i] = new Float(rotationInArray[i] * Math.PI / 180);
					}
					LoopMode loopJmeType = setLoopMode(track.loopMode);
					if (loopJmeType == null) {
						staticLogger.error("Specified RotationTrack loop mode not in com.jme3.animation.LoopMode: " + track.loopMode);
						break;
					}
					if (track.trackDuration <= 0) {
						staticLogger.warn("Warning: RotationTrack contains no positive cc:trackDuration, setting to zero");
						track.trackDuration = 0; // Just in case it is set to a negative number in RDF
					}
					Quaternion endRotation = new Quaternion(rotationInArray);
					event = new RotationTrack(attachedNode, endRotation, track.trackDuration, loopJmeType);
				} else {
					staticLogger.error("Unsupported track type: " + track.trackType);
					break;
				}
				cinematic.addCinematicEvent(track.startTime, event);
			}
			// Attach resulting cinematic and store it in map
			rrc.getJme3AppStateManager(null).attach(cinematic);
			myCinematicsByName.put(cic.myURI_Fragment, cinematic);
		}
	}

	private static boolean noPosition(float[] waypointDef) {
		return (new Float(waypointDef[0]).isNaN()) || (new Float(waypointDef[1]).isNaN()) || (new Float(waypointDef[2]).isNaN());
	}

	private static LoopMode setLoopMode(String modeString) {
		LoopMode loopJmeType = null;
		for (LoopMode testType : LoopMode.values()) {
			if (modeString.equals(testType.toString())) {
				loopJmeType = testType;
			}
		}
		return loopJmeType;
	}

	public static boolean controlCinematicByName(final String name, CinematicMgr.ControlAction action) {
		boolean validAction = true;
		final Cinematic cinematic = myCinematicsByName.get(name);
		if (cinematic != null) {
			if (action.equals(CinematicMgr.ControlAction.PLAY)) {
				staticLogger.info("Playing cinematic " + name);
				cinematic.play();
			} else if (action.equals(CinematicMgr.ControlAction.STOP)) {
				// Wouldn't you know, this has to be done on main thread
				Future<Object> waitForThis = WorkaroundFuncsMustDie.enqueueCallableReturn(myHrc, new Callable<Boolean>() {

					@Override
					public Boolean call() throws Exception {
						cinematic.stop();
						staticLogger.info("Stopping cinematic" + name);
						return true;
					}
				});
				try {
					// We call waitForThis.get (and discard the result) so that this method doesn't return until STOP is actially executed
					waitForThis.get(3, java.util.concurrent.TimeUnit.SECONDS);
				} catch (Exception e) {
					staticLogger.error("Exception stopping cinematic: " + e.toString());
				}
			} else if (action.equals(CinematicMgr.ControlAction.PAUSE)) { // NEEDS TO BE TESTED, MAY NEED TO BE EXECUTED ON MAIN jME RENDERING THREAD
				staticLogger.info("Pausing cinematic " + name);
				staticLogger.info("Pause has not been tested. If it throws an exception, we probably just need to add some code to cause it to be run on the main jME rendering thread");
				cinematic.pause();
			} else {
				validAction = false;
			}
		} else {
			staticLogger.error("No cinematic found by name " + name);
			validAction = false;
		}
		return validAction;
	}

	public enum ControlAction {

		PLAY, STOP, PAUSE
	}
}
