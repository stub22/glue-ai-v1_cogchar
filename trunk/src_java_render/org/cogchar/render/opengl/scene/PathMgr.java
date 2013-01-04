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

import com.jme3.animation.LoopMode;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.events.MotionEvent;
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
import org.cogchar.api.cinema.AnimWaypointsConfig;
import org.cogchar.api.cinema.PathConfig;
import org.cogchar.api.cinema.PathInstanceConfig;
import org.cogchar.api.cinema.WaypointConfig;
import org.cogchar.render.sys.context.CogcharRenderContext;
import org.slf4j.Logger;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class PathMgr extends BasicDebugger {
	private Map<Ident, MotionEvent> myPathsByUri = new HashMap<Ident, MotionEvent>();
    private Map<Ident, WaypointConfig> myWaypointsByUri = new HashMap<Ident, WaypointConfig>();
    private Logger staticLogger = getLoggerForClass(PathMgr.class);
    private CogcharRenderContext myCRC;

    public void storePathsFromConfig(PathConfig config, CogcharRenderContext crc) {

        myCRC = crc;

        // Store any items defined outside cinematic instances to the maps
		AnimWaypointsConfig awc = AnimWaypointsConfig.getMainConfig();
        storeLooseComponents(awc);

        // Next, we build the cinematics, using named tracks/waypoints/rotations if required.
        for (PathInstanceConfig pic : config.myPICs) {
            buildPath(pic);
        }

    }

    private boolean noPosition(float[] waypointDef) {
        return (new Float(waypointDef[0]).isNaN()) || (new Float(waypointDef[1]).isNaN()) || (new Float(waypointDef[2]).isNaN());
    }

    private void storeLooseComponents(AnimWaypointsConfig config) {
		if (config != null) {
			for (WaypointConfig wc : config.myWCs) {
				staticLogger.info("Storing Named Waypoint from RDF: {}", wc);
				myWaypointsByUri.put(wc.myUri, wc);
			}
		}
    }

    private void buildPath(PathInstanceConfig pic) {
        staticLogger.info("Building Path from RDF: {}", pic);
        Map<String, CameraNode> boundCameras = new HashMap<String, CameraNode>(); // To keep track of cameras bound to this cinematic
		Spatial attachedSpatial = null;
		SpatialGrabber grabber = new SpatialGrabber(myCRC);
		switch (pic.attachedItemType) {
			case CAMERA: {
				attachedSpatial = grabber.getSpatialForAttachedCamera(pic, boundCameras);
				break;
			}
			case GOODY: {
				attachedSpatial = grabber.getSpatialForAttachedGoody(pic);
				break;
			}
			default: {
				staticLogger.error("Unsupported attached item type in track: {}", pic.attachedItemType);
			}
		}
		//staticLogger.info("The attached spatial is {} with requested uri {}", attachedSpatial, pic.attachedItem); // TEST ONLY
		if (attachedSpatial != null) {
			MotionEvent event = getMotionEvent(pic, attachedSpatial);
			if (event != null) {
				myPathsByUri.put(pic.myUri, event);
			}
		}
       
    }

    private MotionEvent getMotionEvent(PathInstanceConfig track, Spatial attachedSpatial) {
        MotionPath path = new MotionPath();
        path.setCycle(track.cycle);
        for (WaypointConfig waypoint : track.waypoints) {
            if (noPosition(waypoint.myCoordinates)) { // If we don't have coordinates for this waypoint...
                // First check to see if this waypoint refers to a stored waypoint previously defined
                Ident waypointReference = waypoint.myUri;
                if (waypointReference != null) {
                    waypoint = myWaypointsByUri.get(waypointReference); // Reset waypoint to the WaypointConfig declared separately by name
                    if (waypoint == null) { // If so, track is calling for a waypoint we don't know about
                        staticLogger.error("Path has requested undefined waypoint: {}; track is {}", waypointReference, track);
                        break;
                    }
                } else {
                    staticLogger.error("No coordinates or waypointName in waypoint contained in path: {}", track);
                    break; // If no coordinates and no waypointName, we don't really have a waypoint!
                }
            }
            //staticLogger.info("Making new waypoint: " + new Vector3f(waypoint.myCoordinates[0], waypoint.myCoordinates[1], waypoint.myCoordinates[2])); // TEST ONLY
            path.addWayPoint(new Vector3f(waypoint.myCoordinates[0], waypoint.myCoordinates[1], waypoint.myCoordinates[2]));
        }
		
		MotionEvent motionTrack = null;
		
		if (path.getNbWayPoints() < 2) {
			staticLogger.warn("Less than two waypoints found in path {}; aborting build.", track.myUri);
		} else {
			path.setCurveTension(track.tension);

			MotionEvent.Direction directionJmeType = null;
			for (MotionEvent.Direction testType : MotionEvent.Direction.values()) {
				if (track.directionType.equals(testType.toString())) {
					directionJmeType = testType;
				}
			}
			if (directionJmeType == null) {
				staticLogger.error("Specified MotionEvent direction type not in MotionEvent.Direction: {}", track.directionType);
				return null;
			}
			LoopMode loopJmeType = setLoopMode(track.loopMode);
			if (loopJmeType == null) {
				staticLogger.error("Specified MotionEvent loop mode not in com.jme3.animation.LoopMode: {}", track.loopMode);
				return null;
			}
			motionTrack = new MotionEvent(attachedSpatial, path, track.duration);
			motionTrack.setDirectionType(directionJmeType);
			motionTrack.setLookAt(new Vector3f(track.lookAtDirection[0], track.lookAtDirection[1], track.lookAtDirection[2]), Vector3f.UNIT_Y);
			motionTrack.setLoopMode(loopJmeType);
		}
        return motionTrack;
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

	static final String PATH_URI_PREFIX = "http://www.cogchar.org/schema/path/definition#"; // Temporary
    public boolean controlPathByName(final String localName, PathMgr.ControlAction action) { // Soon switching to controlPathByUri
        boolean validAction = true;
		final Ident uri = new FreeIdent(PATH_URI_PREFIX + localName); // Just temporary until we upgrade the food chain to send URI directly from lifter
        final MotionEvent path = myPathsByUri.get(uri);
        if (path != null) {
            if (action.equals(PathMgr.ControlAction.PLAY)) {
                staticLogger.info("Playing cinematic {}", uri);
				path.play();
            } else if (action.equals(PathMgr.ControlAction.STOP)) {
                // Wouldn't you know, this has to be done on main thread
                Future<Object> waitForThis = myCRC.enqueueCallable(new Callable<Boolean>() {

                    @Override
                    public Boolean call() throws Exception {
                        path.stop();
                        staticLogger.info("Stopping cinematic {}", uri);
                        return true;
                    }
                });
                try {
                    // We call waitForThis.get (and discard the result) so that this method doesn't return until STOP is actially executed
                    waitForThis.get(3, java.util.concurrent.TimeUnit.SECONDS);
                } catch (Exception e) {
                    staticLogger.error("Exception stopping cinematic: {}", e.toString());
                }
            } else if (action.equals(PathMgr.ControlAction.PAUSE)) { // NEEDS TO BE TESTED, MAY NEED TO BE EXECUTED ON MAIN jME RENDERING THREAD
                staticLogger.info("Pausing cinematic {}", uri);
                staticLogger.info("Pause has not been tested. If it throws an exception, we probably just need to add some code to cause it to be run on the main jME rendering thread");
                path.pause();
            } else {
                validAction = false;
            }
        } else {
            staticLogger.error("No cinematic found by name {}", uri);
            validAction = false;
        }
        return validAction;
    }

    public void clearCinematics(CogcharRenderContext crc) {
        myPathsByUri.clear();
        myWaypointsByUri.clear();
		// Disable CameraNodes?
        staticLogger.info("Paths cleared.");
    }

    public enum ControlAction {

        PLAY, STOP, PAUSE
    }
}
