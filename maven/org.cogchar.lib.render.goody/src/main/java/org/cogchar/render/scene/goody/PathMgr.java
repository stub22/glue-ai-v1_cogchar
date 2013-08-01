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
package org.cogchar.render.scene.goody;

//import org.cogchar.render.sys.goody.SpatialGrabber;
import com.jme3.animation.LoopMode;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.appdapter.core.name.Ident;
import org.cogchar.api.cinema.AnimWaypointsConfig;
import org.cogchar.api.cinema.PathInstanceConfig;
import org.cogchar.api.cinema.SpatialActionConfig;
import org.cogchar.api.cinema.WaypointConfig;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class PathMgr extends AbstractThingCinematicMgr {
	private Map<Ident, MotionEvent> myPathsByUri = new HashMap<Ident, MotionEvent>();

	@Override
    public void buildAnimation(SpatialActionConfig sac) {
        myLogger.info("Building Path from RDF: {}", sac);
		if (sac.getClass() != PathInstanceConfig.class) {
			myLogger.warn("buildAnimation was passed the wrong class of configuration object! Aborting.");
			return;
		}
		PathInstanceConfig pic = (PathInstanceConfig) sac;
        Map<String, CameraNode> boundCameras = new HashMap<String, CameraNode>(); // To keep track of cameras bound to this cinematic
		SpatialGrabber grabber = new SpatialGrabber(myCRC);
		Spatial attachedSpatial = grabber.getSpatialForSpecifiedType(pic, boundCameras);
		//staticLogger.info("The attached spatial is {} with requested uri {}", attachedSpatial, pic.attachedItem); // TEST ONLY
		if (attachedSpatial != null) {
			MotionEvent event = getMotionEvent(pic, attachedSpatial);
			if (event != null) {
				myPathsByUri.put(pic.myUri, event);
			}
		}
    }

	// A class to extend MotionEvent to detach MotionEvent's spatial from the root node when the animation completes
	// This allows the flycam to operate normally after an animation if we animate the default camera
	class DetachingMotionEvent extends MotionEvent {

		Node myRootNode;

		DetachingMotionEvent(Spatial spatial, MotionPath path, float initialDuration, Node rootNode) {
			super(spatial, path, initialDuration);
			myRootNode = rootNode;
		}

		@Override
		public void onStop() {
			//super.onStop(); //maybe? -Matt (from MotionEvent: currentWaypoint = 0;)
			myRootNode.detachChild(this.getSpatial());
		}
	}

    private MotionEvent getMotionEvent(PathInstanceConfig track, Spatial attachedSpatial) {
        MotionPath path = new MotionPath();
        path.setCycle(track.cycle);
		AnimWaypointsConfig waypointInfo = AnimWaypointsConfig.getMainConfig();
        for (WaypointConfig waypoint : track.waypoints) {
            if (noPosition(waypoint.myCoordinates)) { // If we don't have coordinates for this waypoint...
                // First check to see if this waypoint refers to a stored waypoint previously defined
                Ident waypointReference = waypoint.myUri;
                if (waypointReference != null) {
                    waypoint = waypointInfo.myWCs.get(waypointReference); // Reset waypoint to the WaypointConfig declared separately by name
                    if (waypoint == null) { // If so, track is calling for a waypoint we don't know about
                        myLogger.error("Path has requested undefined waypoint: {}; track is {}", waypointReference, track);
                        break;
                    }
                } else {
                    myLogger.error("No coordinates or waypointName in waypoint contained in path: {}", track);
                    break; // If no coordinates and no waypointName, we don't really have a waypoint!
                }
            }
            //staticLogger.info("Making new waypoint: " + new Vector3f(waypoint.myCoordinates[0], waypoint.myCoordinates[1], waypoint.myCoordinates[2])); // TEST ONLY
            path.addWayPoint(new Vector3f(waypoint.myCoordinates[0], waypoint.myCoordinates[1], waypoint.myCoordinates[2]));
        }
		
		MotionEvent motionTrack = null;
		
		if (path.getNbWayPoints() < 2) {
			myLogger.warn("Less than two waypoints found in path {}; aborting build.", track.myUri);
		} else {
			path.setCurveTension(track.tension);

			MotionEvent.Direction directionJmeType = null;
			for (MotionEvent.Direction testType : MotionEvent.Direction.values()) {
				if (track.directionType.equals(testType.toString())) {
					directionJmeType = testType;
				}
			}
			if (directionJmeType == null) {
				myLogger.error("Specified MotionEvent direction type not in MotionEvent.Direction: {}", track.directionType);
				return null;
			}
			LoopMode loopJmeType = setLoopMode(track.loopMode);
			if (loopJmeType == null) {
				myLogger.error("Specified MotionEvent loop mode not in com.jme3.animation.LoopMode: {}", track.loopMode);
				return null;
			}
			motionTrack = new DetachingMotionEvent(attachedSpatial, path, track.duration, 
					myCRC.getRenderRegistryClient().getJme3RootDeepNode(null));
			motionTrack.setDirectionType(directionJmeType);
			motionTrack.setLookAt(new Vector3f(track.lookAtDirection[0], track.lookAtDirection[1], track.lookAtDirection[2]), Vector3f.UNIT_Y);
			motionTrack.setRotation(new Quaternion(track.lookAtDirection)); // Used for "Rotation" MotionEvent.Direction -- still experimental
			motionTrack.setLoopMode(loopJmeType);
		}
        return motionTrack;
	}

	@Override
    public boolean controlAnimationByName(final Ident uri, ControlAction action) { // Soon switching to controlPathByUri
        boolean validAction = true;
        final MotionEvent path = myPathsByUri.get(uri);
        if (path != null) {
            if (action.equals(PathMgr.ControlAction.PLAY)) {
                myLogger.info("Playing cinematic {}", uri);
				path.play();
            } else if (action.equals(PathMgr.ControlAction.STOP)) {
                // Wouldn't you know, this has to be done on main thread
                Future<Object> waitForThis = myCRC.enqueueCallable(new Callable<Boolean>() {

                    @Override
                    public Boolean call() throws Exception {
                        path.stop();
                        myLogger.info("Stopping cinematic {}", uri);
                        return true;
                    }
                });
                try {
                    // We call waitForThis.get (and discard the result) so that this method doesn't return until STOP is actially executed
                    waitForThis.get(3, java.util.concurrent.TimeUnit.SECONDS);
                } catch (Exception e) {
                    myLogger.error("Exception stopping cinematic: {}", e.toString());
                }
            } else if (action.equals(PathMgr.ControlAction.PAUSE)) { // NEEDS TO BE TESTED, MAY NEED TO BE EXECUTED ON MAIN jME RENDERING THREAD
                myLogger.info("Pausing cinematic {}", uri);
                myLogger.info("Pause has not been tested. If it throws an exception, we probably just need to add some code to cause it to be run on the main jME rendering thread");
                path.pause();
            } else {
                validAction = false;
            }
        } else {
            myLogger.error("No path found by URI {}", uri);
            validAction = false;
        }
        return validAction;
    }

	@Override
    public void clearAnimations() {
        myPathsByUri.clear();
		// Disable CameraNodes?
        myLogger.info("Paths cleared.");
    }

}