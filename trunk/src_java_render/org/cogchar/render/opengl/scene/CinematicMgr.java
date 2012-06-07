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
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.cinematic.Cinematic;
import com.jme3.animation.LoopMode;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.events.*;
import com.jme3.scene.Node;
import com.jme3.scene.CameraNode;
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
	private static Logger staticLogger = getLoggerForClass(CinematicMgr.class);
	private static HumanoidRenderContext myHrc;

	public static void storeCinematicsFromConfig(CinematicConfig config, HumanoidRenderContext hrc) {
		myHrc = hrc;
		RenderRegistryClient rrc = hrc.getRenderRegistryClient();
		Node jmeRootNode = rrc.getJme3RootDeepNode(null);
		for (CinematicInstanceConfig cic : config.myCICs) {
			staticLogger.info("Building Cinematic from RDF: " + cic);
			final Cinematic cinematic = new Cinematic(jmeRootNode, cic.duration);
			// Add each track
			for (CinematicTrack track : cic.myTracks) {
				AbstractCinematicEvent event;
				if (track.trackType == CinematicTrack.TrackType.MOTIONTRACK) { // Which is all we support initially...
					MotionPath path = new MotionPath();
					Node attachedNode = null;
					path.setCycle(track.cycle);

					for (float[] waypoint : track.waypoints) {
						staticLogger.info("Making new waypoint: " + new Vector3f(waypoint[0], waypoint[1], waypoint[2]));
						path.addWayPoint(new Vector3f(waypoint[0], waypoint[1], waypoint[2]));
					}
					path.setCurveTension(track.tension);
					if (track.attachedItemType == CinematicTrack.AttachedItemType.CAMERA) { // Which is all we support initally...
						CameraMgr cm = rrc.getOpticCameraFacade(null);
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
							cinematic.activateCamera(track.startTime, cameraName);  // Results are very disappointing if we don't do this!
							//cinematic.setActiveCamera(cameraName); // Can also do this, but it messes with initial camera position
						} else {
							staticLogger.error("Specified Camera not found for Cinematic config from RDF: " + track.attachedItem);
							break;
						}
					} else {
						staticLogger.error("Unsupported attached item type in track: " + track.attachedItemType);
						break;
					}
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
					LoopMode loopJmeType = null;
					for (LoopMode testType : LoopMode.values()) {
						if (track.loopMode.equals(testType.toString())) {
							loopJmeType = testType;
						}
					}
					if (loopJmeType == null) {
						staticLogger.error("Specified MotionTrack loop mode not in com.jme3.animation.LoopMode: " + track.loopMode);
						break;
					}
					MotionTrack motionTrack = new MotionTrack(attachedNode, path);
					motionTrack.setDirectionType(directionJmeType);
					motionTrack.setLookAt(new Vector3f(track.direction[0], track.direction[1], track.direction[2]), Vector3f.UNIT_Y);
					motionTrack.setLoopMode(loopJmeType);
					event = motionTrack;
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

	public static boolean controlCinematicByName(String name, CinematicMgr.ControlAction action) {
		boolean validAction = true;
		final Cinematic cinematic = myCinematicsByName.get(name);
		if (cinematic != null) {
			if (action.equals(CinematicMgr.ControlAction.PLAY)) {
				staticLogger.info("Playing cinematic " + name);
				cinematic.play();
			} else if (action.equals(CinematicMgr.ControlAction.STOP)) {
				// Wouldn't you know, this has to be done on main thread
				Future<Object> waitForThis = WorkaroundFuncsMustDie.enqueueCallableReturn(myHrc, new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						cinematic.stop();
						staticLogger.info("Stopping cinematic");
						return null;
					}
				});
				try {
					Object testThis = waitForThis.get(3, java.util.concurrent.TimeUnit.SECONDS); // Not working with the Void call, I think this will be easy to fix next thing, but for now I'll commit this error because the timeout makes things work anyhow!
				} catch (Exception e) {
					staticLogger.error("Exception stopping cinematic: " + e.toString());
				}
			} else if (action.equals(CinematicMgr.ControlAction.PAUSE)) {
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
