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

/*
import com.jme3.animation.LoopMode;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.cinematic.Cinematic;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.events.AbstractCinematicEvent;
import com.jme3.cinematic.events.MotionTrack;
import com.jme3.cinematic.events.PositionTrack;
import com.jme3.cinematic.events.RotationTrack;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.api.cinema.*;
import org.cogchar.render.model.databalls.BallBuilder;
import org.cogchar.render.model.goodies.BasicGoody;
import org.cogchar.render.model.goodies.BasicGoodyImpl;
import org.cogchar.render.model.goodies.GoodyFactory;
import org.cogchar.render.opengl.optic.CameraMgr;
import org.cogchar.render.sys.context.CogcharRenderContext;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.slf4j.Logger;
*/

/**
 *
 * @author Ryan Biggs
 */

public class CinematicMgr /*extends BasicDebugger*/ {
	
	/* CinematicMgr is currently disabled due to cinematics classes being depreciated in current jMonkey revision
	 * This will eventually be replaced with a new spatial animation system
	
	
	 
	 
	// The currently used (and depreciated) version of jMonkey doesn't appear to correctly apply durations
	// The reported duration matches the value set via setInitialDuration, but the actual observed duration is shorter
	// The result is this unfortunate trim factor to which we set the speed, so that the observed motion duration matches
	// what we expect to see. Totally prone to variation and problems; hopefully we'll be able to get rid of this in the 
	// not-too-distant future with a new version of jMonkey or etc!
	final static float SPEED_TRIM_FACTOR = 0.77f; 

	private Map<String, Cinematic> myCinematicsByName = new HashMap<String, Cinematic>();
	private Map<String, CinematicTrack> myTracksByName = new HashMap<String, CinematicTrack>();
	private Map<String, WaypointConfig> myWaypointsByName = new HashMap<String, WaypointConfig>();
	private Map<String, RotationConfig> myRotationsByName = new HashMap<String, RotationConfig>();
	private Logger staticLogger = getLoggerForClass(CinematicMgr.class);
	private CogcharRenderContext myCRC;

	public void storeCinematicsFromConfig(CinematicConfig config, CogcharRenderContext crc) {
		BallBuilder.getTheBallBuilder().storeCinematicConfig(config); // Temporary for BallBuilder demo

		myCRC = crc;

		// Store any items defined outside cinematic instances to the maps
		storeLooseComponents(config);

		// Next, we build the cinematics, using named tracks/waypoints/rotations if required.
		for (CinematicInstanceConfig cic : config.myCICs) {
			buildCinematic(cic);
		}
			
	}
	
	private boolean noPosition(float[] waypointDef) {
		return (new Float(waypointDef[0]).isNaN()) || (new Float(waypointDef[1]).isNaN()) || (new Float(waypointDef[2]).isNaN());
	}
	
	private void storeLooseComponents(CinematicConfig config) {
		// First, any named waypoints defined outside track definitions are stored for later use
		for (WaypointConfig wc : config.myWCs) {
			staticLogger.info("Storing Named Waypoint from RDF: {}", wc);
			myWaypointsByName.put(wc.getName(), wc);
		}

		// Also "first", any named rotations are stored
		for (RotationConfig rc : config.myRCs) {
			staticLogger.info("Storing Named Rotation from RDF: {}", rc);
			myRotationsByName.put(rc.getName(), rc);
		}

		// Second, any named tracks defined outside cinematics definitions are stored for later use
		for (CinematicTrack ct : config.myCTs) {
			staticLogger.info("Storing Named Track from RDF: {}", ct);
			myTracksByName.put(ct.getName(), ct);
		}
	}

	// Public so that Goodies can build Cinematics for move operations, although a flat-out public scope is a little
	// dangerous and may be ammended
	public void buildCinematic(CinematicInstanceConfig cic) {
		staticLogger.info("Building Cinematic from RDF: {}", cic);
		CogcharRenderContext crc = myCRC;
		RenderRegistryClient rrc = crc.getRenderRegistryClient();
		final Cinematic cinematic = new Cinematic(rrc.getJme3RootDeepNode(null), cic.duration);
		Map<String, CameraNode> boundCameras = new HashMap<String, CameraNode>(); // To keep track of cameras bound to this cinematic
		// Add each track
		trackLoop:
		for (CinematicTrack track : cic.myTracks) {
			if (track.trackType == CinematicTrack.TrackType.NULLTYPE) { // This usually indicates that the cinematic contains a reference to a named track defined elsewhere
				String trackReference = track.getName();
				if (!trackReference.equals(CinemaAN.unnamedTrackName)) {
					track = myTracksByName.get(trackReference); // Reset track to the CinematicTrack declared separately by name
					if (track == null) { // If so, cinematic is calling for a track we don't know about
						staticLogger.error("Cinematic has requested undefined track: {}; cinematic is {}", trackReference, cic);
						break trackLoop;
					}
				} else {
					staticLogger.error("No trackType or trackName in track contained in cinematic: {}", cic);
					break trackLoop; // If no trackType and no trackName, we don't really have a track!
				}
			}
			Spatial attachedSpatial = null;
			switch (track.attachedItemType) {
				case CAMERA: {
					attachedSpatial = getSpatialForAttachedCamera(track, cinematic, boundCameras);
					break;
				}
				case GOODY: {
					attachedSpatial = getSpatialForAttachedGoody(track);
					break;
				}
				default: {
					staticLogger.error("Unsupported attached item type in track: {}", track.attachedItemType);
				}
			}
			if (attachedSpatial == null) {
				break trackLoop;
			}
			AbstractCinematicEvent event = null;
			if (track.trackType == CinematicTrack.TrackType.MOTIONTRACK) {
				event = getMotionTrackEvent(track, attachedSpatial);
			} else if (track.trackType == CinematicTrack.TrackType.POSITIONTRACK) {
				event = getPositionTrackEvent(track, attachedSpatial);
			} else if (track.trackType == CinematicTrack.TrackType.ROTATIONTRACK) {
				event = getRotationTrackEvent(track, attachedSpatial);
			} else {
				staticLogger.error("Unsupported track type: {}", track.trackType);
			}
			if (event != null) {
				event.setSpeed(SPEED_TRIM_FACTOR);
				cinematic.addCinematicEvent(track.startTime, event);
			}
		}
		// Attach resulting cinematic and store it in map
		rrc.getJme3AppStateManager(null).attach(cinematic);
		myCinematicsByName.put(cic.getName(), cinematic);
	}
	
	private Spatial getSpatialForAttachedCamera(CinematicTrack track, final Cinematic cinematic, 
			Map<String, CameraNode> boundCameras) {
		Spatial attachedSpatial;
		CameraMgr cm = myCRC.getRenderRegistryClient().getOpticCameraFacade(null);
		final String cameraLocalName = track.attachedItem.getLocalName();
		if (boundCameras.containsKey(cameraLocalName)) {
			// Hey, we already bound this to the cinematic! We'll just get the node to attach to the track.
			staticLogger.info("Attached camera already bound, reusing in track: {}", track);
			attachedSpatial = boundCameras.get(cameraLocalName);
		} else {
			final Camera cineCam = cm.getNamedCamera(cameraLocalName);
			if (cineCam != null) {
				// The following is not necessary if we use only cameras already loaded from RDF, as these have already had their viewports added
				//CoreFeatureAdapter.addViewPort(rrc, track.attachedItem, cineCam);
				// Bind the camera to the cinematic to make a CameraNode - this must be done on the main render thread
				Future<Object> camNodeFuture = myCRC.enqueueCallable(new Callable<CameraNode>() {

					@Override
					public CameraNode call() throws Exception {
						CameraNode camNode = cinematic.bindCamera(cameraLocalName, cineCam);
						return camNode;
					}
				});
				try {
					attachedSpatial = (Node) camNodeFuture.get(3, java.util.concurrent.TimeUnit.SECONDS);
				} catch (Exception e) {
					staticLogger.error("Exception binding camera to cinematic: {}", e.toString());
					return null;
				}
				cinematic.activateCamera(0, cameraLocalName); // Attached at time zero, because who knows what other track might use this camera?
				//cinematic.setActiveCamera(cameraName); // Can also do this, but it messes with initial camera position
				boundCameras.put(cameraLocalName, (CameraNode) attachedSpatial); // Could just use Node as type of HashMap and not cast back and forth, but this makes it explicit
			} else {
				staticLogger.error("Specified Camera not found for Cinematic config from RDF: {}", track.attachedItem);
				return null;
			}
		}
		return attachedSpatial;
	}
	
	private Spatial getSpatialForAttachedGoody(CinematicTrack track) {
		Spatial attachedSpatial = null;
		try {
			BasicGoody desiredGoody = GoodyFactory.getTheFactory().getTheGoodySpace().getGoody(track.attachedItem);
			if (desiredGoody instanceof BasicGoodyImpl) {
				BasicGoodyImpl goody3d = (BasicGoodyImpl) desiredGoody;
				attachedSpatial = goody3d.getCurrentGeometry();
			} else {
				staticLogger.warn("Attempting to attach goody of improper type to cinematic,: {}. Aborting.",
						desiredGoody.getUri().getLocalName());
			}
		} catch (Exception e) {
			staticLogger.error("Exception binding goody to cinematic: {}", e.toString());
			attachedSpatial = null;
		}
		return attachedSpatial;
	}
	
	private AbstractCinematicEvent getMotionTrackEvent(CinematicTrack track, Spatial attachedSpatial) {
		MotionPath path = new MotionPath();
		path.setCycle(track.cycle);
		for (WaypointConfig waypoint : track.waypoints) {
			if (noPosition(waypoint.myCoordinates)) { // If we don't have coordinates for this waypoint...
				// First check to see if this waypoint refers to a stored waypoint previously defined
				String waypointReference = waypoint.getName();
				if (!waypointReference.equals(CinemaAN.unnamedWaypointName)) {
					waypoint = myWaypointsByName.get(waypointReference); // Reset waypoint to the WaypointConfig declared separately by name
					if (waypoint == null) { // If so, track is calling for a waypoint we don't know about
						staticLogger.error("Track has requested undefined waypoint: {}; track is {}", waypointReference, track);
						break;
					}
				} else {
					staticLogger.error("No coordinates or waypointName in waypoint contained in track: {}", track);
					break; // If no coordinates and no waypointName, we don't really have a waypoint!
				}
			}
			//staticLogger.info("Making new waypoint: " + new Vector3f(waypoint.myCoordinates[0], waypoint.myCoordinates[1], waypoint.myCoordinates[2])); // TEST ONLY
			path.addWayPoint(new Vector3f(waypoint.myCoordinates[0], waypoint.myCoordinates[1], waypoint.myCoordinates[2]));
		}
		path.setCurveTension(track.tension);

		MotionTrack.Direction directionJmeType = null;
		for (MotionTrack.Direction testType : MotionTrack.Direction.values()) {
			if (track.directionType.equals(testType.toString())) {
				directionJmeType = testType;
			}
		}
		if (directionJmeType == null) {
			staticLogger.error("Specified MotionTrack direction type not in MotionTrack.Direction: {}", track.directionType);
			return null;
		}
		LoopMode loopJmeType = setLoopMode(track.loopMode);
		if (loopJmeType == null) {
			staticLogger.error("Specified MotionTrack loop mode not in com.jme3.animation.LoopMode: {}", track.loopMode);
			return null;
		}
		MotionTrack motionTrack = new MotionTrack(attachedSpatial, path);
		motionTrack.setDirectionType(directionJmeType);
		motionTrack.setLookAt(new Vector3f(track.direction[0], track.direction[1], track.direction[2]), Vector3f.UNIT_Y);
		motionTrack.setLoopMode(loopJmeType);
		return motionTrack;
	}
	
	private AbstractCinematicEvent getPositionTrackEvent(CinematicTrack track, Spatial attachedSpatial) {
		float[] endPositionArray;
		// PositionTrack only supports one waypoint. Let's check to be sure there is only one waypoint, and that it is valid.
		if (track.waypoints.isEmpty()) {
			staticLogger.error("PositionTrack requested, but no waypoint provided for track: {}", track);
			return null;
		} else if (track.waypoints.size() != 1) {
			staticLogger.warn("PositionTrack requested, but more than one waypoint provided for track: {}", track);
			staticLogger.warn("Extra waypoints discarded for Positiontrack");
		}
		endPositionArray = track.waypoints.get(0).myCoordinates;
		if (noPosition(endPositionArray)) { // If we don't have coordinates for this waypoint...
			// First check to see if this waypoint refers to a stored waypoint previously defined
			String waypointReference = track.waypoints.get(0).getName();
			if (!waypointReference.equals(CinemaAN.unnamedWaypointName)) {
				WaypointConfig waypoint = myWaypointsByName.get(waypointReference); // Set waypoint to the WaypointConfig declared separately by name
				if (waypoint == null) { // If so, track is calling for a waypoint we don't know about
					staticLogger.error("Track has requested undefined waypoint: {}; track is {}", waypointReference, track);
					return null;
				} else {
					endPositionArray = waypoint.myCoordinates;
				}

			} else {
				staticLogger.error("No coordinates or waypointName in waypoint contained in track: {}", track);
				return null; // If no coordinates and no waypointName, we don't really have a waypoint!
			}
		}
		Vector3f endPosition = new Vector3f(endPositionArray[0], endPositionArray[1], endPositionArray[2]);
		LoopMode loopJmeType = setLoopMode(track.loopMode);
		if (loopJmeType == null) {
			staticLogger.error("Specified PositionTrack loop mode not in com.jme3.animation.LoopMode: {}", track.loopMode);
			return null;
		}
		if (track.trackDuration <= 0) {
			staticLogger.warn("Warning: PositionTrack contains no positive cc:trackDuration, setting to zero");
			track.trackDuration = 0; // Just in case it is set to a negative number in RDF
		}
		return new PositionTrack(attachedSpatial, endPosition, track.trackDuration, loopJmeType);
	}
	
	private AbstractCinematicEvent getRotationTrackEvent(CinematicTrack track, Spatial attachedSpatial) {
		// First make sure rotation is valid
		RotationConfig rotation = track.endRotation;
		float[] rotationInArray = {rotation.yaw, rotation.roll, rotation.pitch};
		if (noPosition(rotationInArray)) { // If we don't have values for this rotation...
			// First check to see if this rotation refers to a stored rotation previously defined
			String rotationReference = rotation.getName();
			if (!rotationReference.equals(CinemaAN.unnamedRotationName)) {
				rotation = myRotationsByName.get(rotationReference); // Reset rotation to the RotationConfig declared separately by name
				if (rotation == null) { // If so, track is calling for a rotation we don't know about
					staticLogger.error("Track has requested undefined rotation: {}; track is {}", rotationReference, track);
					return null;
				} else {
					// Reset rotationInArray to values from loaded rotation
					rotationInArray[0] = rotation.yaw;
					rotationInArray[1] = rotation.roll;
					rotationInArray[2] = rotation.pitch;
				}
			} else {
				staticLogger.error("No valid rotation angles or rotationName in rotation contained in track: {}", track);
				return null; // If no coordinates and no rotationName, we don't really have a rotation!
			}
		}
		// Convert angles from degrees to radians
		for (int i = 0; i < rotationInArray.length; i++) { // Really Java? All this is required just to multiply an array by a constant?
			rotationInArray[i] = new Float(rotationInArray[i] * Math.PI / 180);
		}
		LoopMode loopJmeType = setLoopMode(track.loopMode);
		if (loopJmeType == null) {
			staticLogger.error("Specified RotationTrack loop mode not in com.jme3.animation.LoopMode: {}", track.loopMode);
			return null;
		}
		if (track.trackDuration <= 0) {
			staticLogger.warn("Warning: RotationTrack contains no positive cc:trackDuration, setting to zero");
			track.trackDuration = 0; // Just in case it is set to a negative number in RDF
		}
		Quaternion endRotation = new Quaternion(rotationInArray);
		staticLogger.info("Adding new RotationTrack: yaw, pitch, roll: {}, {}, {}; duration {}, loop type {}", new Object[]{rotation.yaw, rotation.pitch, rotation.roll, track.trackDuration, track.loopMode}); // TEST ONLY
		return new RotationTrack(attachedSpatial, endRotation, track.trackDuration, loopJmeType);
	}
	
	private LoopMode setLoopMode(String modeString) {
		LoopMode loopJmeType = null;
		for (LoopMode testType : LoopMode.values()) {
			if (modeString.equals(testType.toString())) {
				loopJmeType = testType;
			}
		}
		return loopJmeType;
	}

	public boolean controlCinematicByName(final String name, CinematicMgr.ControlAction action) {
		boolean validAction = true;
		final Cinematic cinematic = myCinematicsByName.get(name);
		if (cinematic != null) {
			if (action.equals(CinematicMgr.ControlAction.PLAY)) {
				staticLogger.info("Playing cinematic {}", name);
				cinematic.play();
			} else if (action.equals(CinematicMgr.ControlAction.STOP)) {
				// Wouldn't you know, this has to be done on main thread
				Future<Object> waitForThis = myCRC.enqueueCallable(new Callable<Boolean>() {

					@Override
					public Boolean call() throws Exception {
						cinematic.stop();
						staticLogger.info("Stopping cinematic {}", name);
						return true;
					}
				});
				try {
					// We call waitForThis.get (and discard the result) so that this method doesn't return until STOP is actially executed
					waitForThis.get(3, java.util.concurrent.TimeUnit.SECONDS);
				} catch (Exception e) {
					staticLogger.error("Exception stopping cinematic: {}", e.toString());
				}
			} else if (action.equals(CinematicMgr.ControlAction.PAUSE)) { // NEEDS TO BE TESTED, MAY NEED TO BE EXECUTED ON MAIN jME RENDERING THREAD
				staticLogger.info("Pausing cinematic {}", name);
				staticLogger.info("Pause has not been tested. If it throws an exception, we probably just need to add some code to cause it to be run on the main jME rendering thread");
				cinematic.pause();
			} else {
				validAction = false;
			}
		} else {
			staticLogger.error("No cinematic found by name {}", name);
			validAction = false;
		}
		return validAction;
	}

	public void clearCinematics(CogcharRenderContext crc) {
		final AppStateManager manager = crc.getRenderRegistryClient().getJme3AppStateManager(null);

		// Keeps getting AppStates of class Cinematic and detaching them until there are no more
		Future<Object> detachFuture = myCRC.enqueueCallable(new Callable<Integer>() { // Do this on main render thread to detach successfully

			@Override
			public Integer call() throws Exception {
				int counter = 0;
				AppState state;
				do {
					state = manager.getState(Cinematic.class);
					if (state != null) {
						manager.detach(state);
						counter++;
					}
				} while (state != null);
				return counter;
			}
		});
		int detachCounter = 0;
		try {
			// Gets the detached items counter, and waits to be sure everything is really detached
			detachCounter = (Integer) detachFuture.get(3, java.util.concurrent.TimeUnit.SECONDS);
		} catch (Exception e) {
			staticLogger.error("Exception getting number of cinematics detached: {}", e.toString());
		}
		myCinematicsByName.clear();
		myTracksByName.clear();
		myWaypointsByName.clear();
		myRotationsByName.clear();
		staticLogger.info("Cinematics cleared. Number detached: {}", detachCounter);
	}
	*/ 

	public enum ControlAction {

		PLAY, STOP, PAUSE
	}
}
