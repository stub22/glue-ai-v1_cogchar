/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.render.model.databalls;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import static java.lang.Math.*;
import java.util.*;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.api.scene.*;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.opengl.optic.CameraMgr;
import org.cogchar.render.opengl.scene.DeepSceneMgr;
import org.cogchar.render.opengl.scene.FlatOverlayMgr;
import org.cogchar.render.opengl.scene.GeomFactory;
import org.cogchar.render.opengl.scene.TextMgr;
import org.cogchar.render.sys.core.RenderRegistryClient;
import org.slf4j.Logger;

/**
 *
 * @author Ryan Biggs
 */
public class BallBuilder extends BasicDebugger {

	private static RenderRegistryClient rrc;
	private static GeomFactory factory;
	private static PhysicsSpace physics;
	private static DeepSceneMgr dsm;
	private static InputManager im;
	private static CameraMgr cameraMgr;
	private static Node ballsNode = new Node("ResourceBalls");
	private static final SphereCollisionShape sphereShape = new SphereCollisionShape(1.0f);
	private static Logger logger = getLoggerForClass(BallBuilder.class);
	private static CinematicConfig aConfigToDemo;
	private static TextMgr textMgr;
	private static FlatOverlayMgr flatOverlayMgr;
	private static final float[] PICK_TEXT_POSITION = {500f, 30f, 0f};
	private static final float[] BALL_INJECTION_POSITION = {-30f, 24f, 50f};
	private static boolean activated = false;
	private static Map<String, Ball> balls = new HashMap<String, Ball>();
	private static BitmapText screenText;

	public static void initialize(HumanoidRenderContext hrc) {
		rrc = hrc.getRenderRegistryClient();
		factory = rrc.getSceneGeometryFacade(null);
		physics = rrc.getJme3BulletPhysicsSpace();
		dsm = rrc.getSceneDeepFacade(null);
		im = rrc.getJme3InputManager(null);
		cameraMgr = rrc.getOpticCameraFacade(null);
		textMgr = rrc.getSceneTextFacade(null);
		//ballsNode = new Node("ResourceBalls");
		flatOverlayMgr = rrc.getSceneFlatFacade(null);
	}

	public static void storeCinematicConfig(CinematicConfig config) {
		aConfigToDemo = config;
	}

	static class CinematicModelBuilder {
		// These hold numbers to attach to the end of "Unnamed" track, waypoint, and rotation names

		static int incrementingTrack = 1;
		static int incrementingWaypoint = 1;
		static int incrementingRotation = 1;

		private static void buildModelFromCinematicConfig(CinematicConfig cc) {
			for (CinematicInstanceConfig cic : cc.myCICs) {
				logger.info("Adding instanceBall with uri " + cic.myURI_Fragment);
				Ball instanceBall = Ball.addBall(cic.myURI_Fragment, ColorRGBA.Green, 1.5f);
				for (CinematicTrack ct : cic.myTracks) {
					buildFromTrack(ct, instanceBall);
				}
			}
			for (CinematicTrack ct : cc.myCTs) {
				buildFromTrack(ct, null);
			}
			for (WaypointConfig wc : cc.myWCs) {
				buildFromWaypoint(wc, null);
			}
			for (RotationConfig rc : cc.myRCs) {
				buildFromRotation(rc, null);
			}
		}

		public static void buildFromTrack(CinematicTrack ct, Ball parentBall) {
			Ball trackBall;
			String trackName;
			if (ct.trackName.endsWith(CinematicConfigNames.suffix_unnamed)) {
				trackName = ct.trackName + incrementingTrack;
				incrementingTrack++;
			} else {
				trackName = ct.trackName;
			}
			if (balls.containsKey(trackName)) {
				logger.info("Updating trackBall with uri " + trackName);
				trackBall = balls.get(trackName);
			} else {
				logger.info("Adding trackBall with uri " + trackName);
				trackBall = Ball.addBall(trackName, ColorRGBA.Red);
			}
			if (parentBall != null) {
				parentBall.addConnection(trackName, "containsTrack");
			}
			for (WaypointConfig wc : ct.waypoints) {
				buildFromWaypoint(wc, trackBall);
			}
			if (ct.endRotation != null) {
				buildFromRotation(ct.endRotation, trackBall);
			}
		}

		public static void buildFromWaypoint(WaypointConfig wc, Ball parentBall) {
			String waypointName;
			if (wc.waypointName.endsWith(CinematicConfigNames.suffix_unnamed)) {
				waypointName = wc.waypointName + incrementingWaypoint;
				incrementingWaypoint++;
			} else {
				waypointName = wc.waypointName;
			}
			if (!balls.containsKey(waypointName)) {
				logger.info("Adding waypointBall with uri " + waypointName);
				Ball.addBall(waypointName, ColorRGBA.Yellow, 0.5f);
			}
			if (parentBall != null) {
				parentBall.addConnection(waypointName, "containsWaypoint", 2);
			}
		}

		public static void buildFromRotation(RotationConfig rc, Ball parentBall) {
			String rotationName;
			if (rc.rotationName.endsWith(CinematicConfigNames.suffix_unnamed)) {
				rotationName = rc.rotationName + incrementingRotation;
				incrementingRotation++;
			} else {
				rotationName = rc.rotationName;
			}
			if (!balls.containsKey(rotationName)) {
				logger.info("Adding waypointBall with uri " + rotationName);
				Ball.addBall(rotationName, ColorRGBA.Orange, 0.5f);
			}
			if (parentBall != null) {
				parentBall.addConnection(rotationName, "containsRotation", 2);
			}
		}
	}

	public static void runBalls(HumanoidRenderContext hrc) {
		if (balls.isEmpty()) {
			initialize(hrc);
			CinematicModelBuilder.buildModelFromCinematicConfig(aConfigToDemo);
			dsm.attachTopSpatial(ballsNode);
			activated = true;
		} else if (activated) {
			dsm.detachTopSpatial(ballsNode);
			flatOverlayMgr.detachOverlaySpatial(screenText);
			activated = false;
		} else {
			// Reset position and velocity
			for (Ball ball : balls.values()) {
				ball.reset();
			}
			dsm.attachTopSpatial(ballsNode);
			activated = true;
		}
	}

	static class Ball {

		String uri;
		Vector3f initialPosition;
		Map<String, Integer> connectionMap = new HashMap<String, Integer>();
		Map<String, Stick> stickMap = new HashMap<String, Stick>();
		Geometry geometry;
		RigidBodyControl control;
		float radius;

		Ball(String ballUri, Vector3f position, ColorRGBA color, float size) {
			uri = ballUri;
			initialPosition = position;
			radius = size;
			Sphere ball = new Sphere(20, 20, size);
			geometry = factory.makeColoredUnshadedGeom(uri, ball, color, null);
			control = new RigidBodyControl(sphereShape, 1f);
			control.setRestitution(0.5f);
			geometry.addControl(control);
			physics.add(control);
			ballsNode.attachChild(geometry);
			//dsm.attachTopSpatial(geometry);
			reset();
		}

		static Ball addBall(String ballUri) {
			return addBall(ballUri, ColorRGBA.Blue);
		}

		static Ball addBall(String ballUri, Vector3f position) {
			Ball newBall = new Ball(ballUri, position, ColorRGBA.Blue, 1f);
			balls.put(ballUri, newBall);
			return newBall;
		}

		static Ball addBall(String ballUri, ColorRGBA color) {
			return addBall(ballUri, color, 1.0f);
		}

		static Ball addBall(String ballUri, ColorRGBA color, float size) {
			Vector3f position = new Vector3f(BALL_INJECTION_POSITION[0] + 2 * balls.size(), BALL_INJECTION_POSITION[1], BALL_INJECTION_POSITION[2]);
			Ball newBall = new Ball(ballUri, position, color, size);
			balls.put(ballUri, newBall);
			// Give the balls a little random motion so they will shake down to stable equilibrium
			Random random = new Random(new Long(ballUri.hashCode()));
			newBall.control.setLinearVelocity(new Vector3f(random.nextFloat() - 0.5f, 0.25f * (random.nextFloat() - 0.5f), random.nextFloat() - 0.5f));
			return newBall;
		}

		void addConnection(String connectedBallUri, String stickUri) {
			addConnection(connectedBallUri, stickUri, 1);
		}

		void addConnection(String connectedBallUri, String stickUri, int strength) {
			if (connectionMap.containsKey(connectedBallUri)) {
				connectionMap.put(connectedBallUri, connectionMap.get(connectedBallUri) + strength);
			} else {
				connectionMap.put(connectedBallUri, strength);
				stickMap.put(connectedBallUri, new Stick(stickUri));
			}
		}

		final void reset() {
			control.setPhysicsLocation(initialPosition);
			control.setLinearVelocity(Vector3f.ZERO);
		}
	}

	static class Stick {

		String uri;
		Geometry geometry;
		Cylinder stickCylinder;

		Stick(String stickUri) {
			uri = stickUri;
			stickCylinder = new Cylinder(10, 20, 0.25f, 1f);
			geometry = factory.makeColoredUnshadedGeom(uri, stickCylinder, ColorRGBA.Black, null);
			ballsNode.attachChild(geometry);
			//dsm.attachTopSpatial(geometry);
		}
	}

	public static void applyUpdates() { // Called in ModularRenderContext.doUpdate - not sure if we want to hook in way "down" there or not
		if (activated) {
			for (Ball ball : balls.values()) {
				ball.control.setGravity(Vector3f.ZERO);
				Vector3f location = ball.geometry.getLocalTranslation();
				Vector3f velocity = ball.control.getLinearVelocity();

				// Compute forces due to other balls and connections
				Vector3f potentialForce = new Vector3f();
				Vector3f springForce = new Vector3f();
				for (Ball otherBall : balls.values()) {
					if (!ball.equals(otherBall)) {
						Vector3f otherLocation = otherBall.geometry.getLocalTranslation();
						Vector3f vectorToOther = otherLocation.subtract(location);
						Vector3f directionToOther = vectorToOther.normalize();
						float distanceToOther = vectorToOther.length();
						Stick connectingStick = null;

						// Compute potential force
						float potentialForceMagnitude = -20f * ((float) pow(ball.radius, 3) + (float) pow(otherBall.radius, 3)) / (distanceToOther * distanceToOther); // Change potential force physics here. Physics to move to separate method?
						potentialForce = potentialForce.add(directionToOther.mult(potentialForceMagnitude));

						// Compute spring force due to other ball's connections to this ball
						if (otherBall.connectionMap.containsKey(ball.uri)) {
							float springForceMagnitude = 0.4f * otherBall.connectionMap.get(ball.uri) * distanceToOther; // Change spring force physics here...
							springForce = springForce.add(directionToOther.mult(springForceMagnitude));
							connectingStick = otherBall.stickMap.get(ball.uri);
						}
						// Compute spring force due to this ball's connections to other ball
						if (ball.connectionMap.containsKey(otherBall.uri)) {
							float springForceMagnitude = 0.4f * ball.connectionMap.get(otherBall.uri) * distanceToOther; // ... also change spring force physics here
							springForce = springForce.add(directionToOther.mult(springForceMagnitude));
							connectingStick = ball.stickMap.get(otherBall.uri); // Note only one stick is chosen if there are duplicates
						}

						// Update stick to otherBall, if any
						if ((connectingStick != null) && (distanceToOther > 0.1)) {
							connectingStick.geometry.setLocalTranslation(FastMath.interpolateLinear(.5f, location, otherLocation));
							Quaternion rotation = new Quaternion();
							rotation.lookAt(vectorToOther, Vector3f.UNIT_Y);
							connectingStick.geometry.setLocalRotation(rotation);
							connectingStick.stickCylinder.updateGeometry(10, 20, 0.25f, 0.25f, distanceToOther, true, false);
						}
					}
				}

				// Compute damping force
				Vector3f dampingForce = velocity.mult(-0.5f); // Change damping force physics here

				// Apply forces
				Vector3f totalForce = potentialForce.add(springForce).add(dampingForce);
				//logger.info("potentialForce is " + potentialForce.toString());
				//logger.info("springForce is " + springForce.toString());
				//logger.info("dampingForce is " + dampingForce.toString());
				//logger.info("totalForce is " + totalForce.toString());
				Float forceMagnitude = totalForce.length();
				//logger.info("forceMagnitude is " + forceMagnitude);
				// At the first update, locations may not yet be initialized, so the balls may both have position at the origin resulting in infinite force
				if ((!forceMagnitude.isInfinite()) && (!forceMagnitude.isNaN())) {
					ball.control.applyCentralForce(totalForce);
				} else {
					logger.info("Invalid force in BallBuilder.applyUpdates");
				}

			}
		}
	}

	public static void pick() {
		// Reset results list.
		CollisionResults results = new CollisionResults();
		// Convert screen click to 3d position
		Vector2f click2d = im.getCursorPosition();
		Vector3f click3d = cameraMgr.getCommonCamera(CameraMgr.CommonCameras.DEFAULT).getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
		Vector3f dir = cameraMgr.getCommonCamera(CameraMgr.CommonCameras.DEFAULT).getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
		// Aim the ray from the clicked spot forwards.
		Ray ray = new Ray(click3d, dir);
		// Collect intersections between ray and all nodes in results list.
		ballsNode.collideWith(ray, results);
		// (Print the results so we see what is going on:)
		for (int i = 0; i < results.size(); i++) {
			// (For each “hit”, we know distance, impact point, geometry.)
			float dist = results.getCollision(i).getDistance();
			Vector3f pt = results.getCollision(i).getContactPoint();
			String target = results.getCollision(i).getGeometry().getName();
			logger.info("Selection #" + i + ": " + target + " at " + pt + ", " + dist + " WU away.");
		}
		// Use the results
		if (results.size() > 0) {
			// The closest result is the target that the player picked:
			Geometry target = results.getClosestCollision().getGeometry();
			// Here comes the action:
			for (Ball ball : balls.values()) {
				if (target.equals(ball.geometry)) {
					showPickText(ball.uri);
				}
				for (Stick stick : ball.stickMap.values()) {
					if (target.equals(stick.geometry)) {
						showPickText(stick.uri);
					}
				}
			}
		}
	}

	private static void showPickText(String uri) {
		logger.info("Looks like you picked " + uri);
		if (screenText != null) {
			flatOverlayMgr.detachOverlaySpatial(screenText);
		}
		screenText = textMgr.getScaledBitmapText("Picked: " + uri, 1.0f);
		screenText.setLocalTranslation(PICK_TEXT_POSITION[0], PICK_TEXT_POSITION[1], PICK_TEXT_POSITION[2]);
		screenText.setColor(ColorRGBA.Black);
		flatOverlayMgr.attachOverlaySpatial(screenText);
	}
}
