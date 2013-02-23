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
package org.cogchar.render.model.goodies;

import org.cogchar.name.goody.DataballStrings;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import java.io.InputStream;
import static java.lang.Math.pow;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.bind.lift.LiftAmbassador;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.opengl.optic.CameraMgr;
import org.cogchar.render.opengl.optic.MatFactory;
import org.cogchar.render.opengl.scene.DeepSceneMgr;
import org.cogchar.render.opengl.scene.FlatOverlayMgr;
import org.cogchar.render.opengl.scene.GeomFactory;
import org.cogchar.render.opengl.scene.TextMgr;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.slf4j.Logger;

/**
 *
 * @author Ryan Biggs
 */
public class BallBuilder extends BasicDebugger {
	
	private static BallBuilder theBallBuilder;
	
	private static final float LOW_DAMPING_COEFFICIENT = 0.4f;
	private static final float HIGH_DAMPING_COEFFICIENT = 0.98f;
	private static final float MASS_COEFFICIENT = 1f;
	private static final float[] PICK_TEXT_POSITION = {300f, 30f, 0f};
	private static final float[] BALL_INJECTION_POSITION = {0f, 24f, 50f};
	private static final float[] BALL_INJECTION_BOX_SIZE = {20f, 10f, 10f};
	private static final float BALL_PADDING = 0.1f;
	private static final int DAMPING_TRIM_CONSTANT = (int) pow(40, 4);
	private static final float MINIMUM_DAMPING_COEFFICIENT = 0.33f;
	private static final int RELEASE_DAMPING_PERIOD = 60;
	private static final float RELEASE_DAMPING = 0.99f;
	private static final int INFLATION_PERIOD = 250;
	private static final float INFLATION_DAMPING = MINIMUM_DAMPING_COEFFICIENT;
	private HumanoidRenderContext myRenderContext;
	private RenderRegistryClient myRRC;
	private GeomFactory myFactory;
	private PhysicsSpace myPhysics;
	//private BulletAppState bulletState;
	private DeepSceneMgr myDSM;
	private InputManager myIM;
	private CameraMgr myCameraMgr;
	private Node myBallsNode = new Node("Databalls");
	private Logger myLogger = getLoggerForClass(BallBuilder.class);
	//private CinematicConfig myDemoCinematicConfig;
	private TextMgr myTextMgr;
	private FlatOverlayMgr myFlatOverlayMgr;
	private ClassLoader myResourceCl;
	private MatFactory myMaterialFactory;
	private Map<String, ClassLoader> myClassloaders = new HashMap<String, ClassLoader>();
	private boolean thisActivated = false;
	private Map<String, Ball> myBalls = new HashMap<String, Ball>();
	private BitmapText myScreenText;
	private float myDamping = LOW_DAMPING_COEFFICIENT;
	private Material myStandardMaterial;
	private Model myLastModel; // May be only temporary; holds last model loaded so we can run SPARQL queries on it
	// Probably it makes sense to retain an instance variable for the LiftAmbassador since it is used in several methods.
	// However, it's probably even better to add an interface for BallBuilder->Lifter interactions
	private LiftAmbassador myLiftAmbassador; 
	//private CinematicModelBuilder myCinematicModelBuilder;

	// Empty private default constructor to prevent outside instantiation
	private BallBuilder() {}
	
	// Provides a getter for the singleton instance. This method provides lazy initialization and is automatically thread-safe.
	// Probably not a concern for BallBuilder, but not a bad practice just in case.
	private static final class SingletonHolder {
		private static final BallBuilder theBallBuilder = new BallBuilder();
	}
	public static BallBuilder getTheBallBuilder() {
		return SingletonHolder.theBallBuilder;
	}	
	
	public void initialize(HumanoidRenderContext hrc) {
		myRenderContext = hrc;
		myRRC = hrc.getRenderRegistryClient();
		myFactory = myRRC.getSceneGeometryFacade(null);
		myPhysics = myRRC.getJme3BulletPhysicsSpace();
		myDSM = myRRC.getSceneDeepFacade(null);
		myIM = myRRC.getJme3InputManager(null);
		myCameraMgr = myRRC.getOpticCameraFacade(null);
		myTextMgr = myRRC.getSceneTextFacade(null);
		//ballsNode = new Node("ResourceBalls");
		myFlatOverlayMgr = myRRC.getSceneFlatFacade(null);
		myMaterialFactory = myRRC.getOpticMaterialFacade(null, null);
		myStandardMaterial = myMaterialFactory.makeMatWithOptTexture("Common/MatDefs/Light/Lighting.j3md", "SpecularMap", null);
		// Below: an experiment in Bullet multithreading (http://jmonkeyengine.org/wiki/doku.php/jme3:advanced:bullet_multithreading)
		// Currently throwing an NPE
		//bulletState = myRRC.getJme3BulletAppState(null);
		// This *may* improve performance
		//bulletState.setThreadingType(BulletAppState.ThreadingType.PARALLEL); // Does the bulletState need to be attached to the state manager, or is it already?
	}
	
	private LiftAmbassador getLiftAmbassador() {
		if (myLiftAmbassador == null) {
			myLiftAmbassador = LiftAmbassador.getLiftAmbassador();
		}
		return myLiftAmbassador;
	}

	class Ball {

		String uri;
		Vector3f initialPosition;
		Map<String, Integer> connectionMap = new HashMap<String, Integer>();
		Map<String, Stick> stickMap = new HashMap<String, Stick>();
		Geometry geometry;
		RigidBodyControl control;
		Material material;
		float radius;

		Ball(String ballUri, Vector3f position, ColorRGBA color, float size) {
			uri = ballUri;
			initialPosition = position;
			radius = size;
			Sphere ball = new Sphere(20, 20, size);
			material = myStandardMaterial.clone();
			material.setBoolean("UseMaterialColors", true);
			material.setColor("Diffuse", color);
			material.setColor("Ambient", color);
			material.setColor("Specular", color);
			material.setFloat("Shininess", 25f);
			control = new RigidBodyControl(sphereShape(size), (float) (pow(size, 3) * MASS_COEFFICIENT));
			control.setRestitution(0.5f);
			geometry = myFactory.makeGeom(uri, ball, material, control);
			reset();
			myRenderContext.enqueueCallable(new Callable<Void>() { // Do this on main render thread

				@Override
				public Void call() throws Exception {
					//geometry.addControl(control);
					myPhysics.add(control);
					myBallsNode.attachChild(geometry);
					control.setPhysicsLocation(initialPosition); // Probably unnecessary - setting this here, in reset() above, and using resetAllBalls in buildModelFromTurtle because they don't want to go to the initial position! Probably some sort of jME concurrency thing...
					return null;
				}
			});
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
			// Give the balls a little random motion so they will shake down to stable equilibrium
			Random random = new Random(new Long(uri.hashCode()));
			control.setLinearVelocity(new Vector3f(random.nextFloat() - 0.5f, 0.25f * (random.nextFloat() - 0.5f), random.nextFloat() - 0.5f));
		}
		
		private SphereCollisionShape lastShape;
		private float lastRadius = -1f;
		private SphereCollisionShape sphereShape(float radius) {
			if (radius == lastRadius) {
				return lastShape; // Recycle old sphereShape if possible for efficiency
			} else {
				SphereCollisionShape newShape = new SphereCollisionShape(radius);
				lastShape = newShape;
				lastRadius = radius;
				return newShape;
			}
		}
		
	}
	
	Ball addBall(String ballUri) {
		return addBall(ballUri, ColorRGBA.Blue);
	}

	Ball addBall(String ballUri, Vector3f position) {
		Ball newBall;
		if (!myBalls.containsKey(ballUri)) {
			newBall = new Ball(ballUri, position, ColorRGBA.Blue, 1f);
			myBalls.put(ballUri, newBall);
		} else {
			newBall = myBalls.get(ballUri);
		}
		return newBall;
	}

	Ball addBall(String ballUri, ColorRGBA color) {
		return addBall(ballUri, color, 1.0f);
	}

	Ball addBall(String ballUri, ColorRGBA color, float size) {
		Ball newBall;
		if (!myBalls.containsKey(ballUri)) {
			Vector3f position = assignStartingLocation(size);
			newBall = new Ball(ballUri, position, color, size);
			myBalls.put(ballUri, newBall);
		} else {
			newBall = myBalls.get(ballUri);
		}
		return newBall;
	}

	

	class Stick {

		String uri;
		Geometry geometry;
		Cylinder stickCylinder;
		Material material;

		Stick(String stickUri) {
			uri = stickUri;
			stickCylinder = new Cylinder(10, 20, 0.25f, 1f);
			material = myStandardMaterial;
			material.setBoolean("UseMaterialColors", true);
			material.setColor("Diffuse", ColorRGBA.Black);
			material.setColor("Ambient", ColorRGBA.Black);
			material.setColor("Specular", ColorRGBA.Black);
			material.setFloat("Shininess", 100f);
			geometry = myFactory.makeGeom(uri, stickCylinder, material, null);
			myRenderContext.enqueueCallable(new Callable<Void>() { // Do this on main render thread

				@Override
				public Void call() throws Exception {
					myBallsNode.attachChild(geometry);
					return null;
				}
			});
		}
	}
	
	class Cloud {
		
		int cloudNum = 1; // Just a temporary way to discretely name these
		static final String CLOUD_NAME_PREFIX = "Cloud";

		Cloud(float radius, Vector3f position, ColorRGBA color) {
			Sphere cloud = new Sphere(40, 40, radius);
			Material material = myMaterialFactory.makeMatWithOptNamedTexture("Common/MatDefs/Light/Lighting.j3md", "AlphaMap", "Textures/Uniform/DataCloudAlphaMap.png");
			material.setBoolean("UseMaterialColors", true);
			material.setColor("Diffuse", color);
			material.setColor("Ambient", color);
			material.setColor("Specular", color);
			material.setFloat("Shininess", 25f);
			material.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
			
			final Geometry geometry = myFactory.makeGeom(CLOUD_NAME_PREFIX + cloudNum, cloud, material, null);
			final RigidBodyControl control = new RigidBodyControl(CollisionShapeFactory.createMeshShape(geometry), 0f);
			geometry.addControl(control); // Has to be done after the makeGeom method since the control CollisionShape is based on the geometry
			control.setPhysicsLocation(position);
			geometry.setQueueBucket(Bucket.Transparent);
			myRenderContext.enqueueCallable(new Callable<Void>() { // Do this on main render thread

				@Override
				public Void call() throws Exception {
					myPhysics.add(control);
					myBallsNode.attachChild(geometry);
					return null;
				}
			});
		}
	}
	
	
	private float[] newPosition = new float[3];
	private Float lastRadius = Float.NaN;
	private float biggestRadiusThisLine;
	private float biggestRadiusThisPlane;

	private Vector3f assignStartingLocation(float ballRadius) {
		if (lastRadius.isNaN()) { // If so, we are starting a fresh set of balls
			for (int i = 0; i < newPosition.length; i++) {
				newPosition[i] = firstPosition(i);
			}
			lastRadius = 0f;
			biggestRadiusThisLine = ballRadius;
			biggestRadiusThisPlane = ballRadius;
		}
		newPosition[0] += lastRadius + ballRadius + BALL_PADDING;
		if (exceedsBound(0)) { // Time for a new line!
			newPosition[0] = firstPosition(0);
			newPosition[1] += biggestRadiusThisLine + ballRadius + BALL_PADDING;
			if (exceedsBound(1)) { // Time for a new plane!
				newPosition[1] = firstPosition(1);
				newPosition[2] += biggestRadiusThisPlane + ballRadius + BALL_PADDING;
				if (exceedsBound(2)) {
					myLogger.warn("Balls are overflowing from injection box!");
				}
				biggestRadiusThisPlane = ballRadius;
			} else {
				biggestRadiusThisLine = ballRadius;
			}
		}
		lastRadius = ballRadius;
		if (ballRadius > biggestRadiusThisLine) {
			newPosition[1] += (ballRadius - biggestRadiusThisLine); // We need to shift this ball (and rest of line) up to clear last line now that we have bigger radii
			biggestRadiusThisLine = ballRadius;
		}
		if (ballRadius > biggestRadiusThisPlane) {
			newPosition[2] += (ballRadius - biggestRadiusThisPlane); // We need to shift this ball (and rest of plane) back to clear last plane now that we have bigger radii
			biggestRadiusThisPlane = ballRadius;
		}
		return new Vector3f(newPosition[0], newPosition[1], newPosition[2]);
	}

	private boolean exceedsBound(int dimension) {
		return (newPosition[dimension] > BALL_INJECTION_POSITION[dimension] + BALL_INJECTION_BOX_SIZE[dimension] / 2);
	}

	private float firstPosition(int dimension) {
		return BALL_INJECTION_POSITION[dimension] - BALL_INJECTION_BOX_SIZE[dimension] / 2;
	}

	/*
	public void storeCinematicConfig(CinematicConfig config) {
		myDemoCinematicConfig = config;
	}
	*/
	
	/* No longer applicable since Cinematics are no more - may be convertable to Paths, but we probably don't care...
	class CinematicModelBuilder {
		// These hold numbers to attach to the end of "Unnamed" track, waypoint, and rotation names

		int incrementingTrack = 1;
		int incrementingWaypoint = 1;
		int incrementingRotation = 1;
		
		private void buildModelFromCinematicConfig(CinematicConfig cc) {
			for (CinematicInstanceConfig cic : cc.myCICs) {
				myLogger.info("Adding instanceBall with uri {}", cic.getName());
				Ball instanceBall = addBall(cic.getName(), ColorRGBA.Green, 1.5f);
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
			myDamping = computeIdealDamping();
		}

		public void buildFromTrack(CinematicTrack ct, Ball parentBall) {
			Ball trackBall;
			String trackName;
			if (ct.getName().endsWith(CinemaAN.suffix_unnamed)) {
				trackName = ct.getName() + incrementingTrack;
				incrementingTrack++;
			} else {
				trackName = ct.getName();
			}
			if (myBalls.containsKey(trackName)) {
				myLogger.info("Updating trackBall with uri {}", trackName);
				trackBall = myBalls.get(trackName);
			} else {
				myLogger.info("Adding trackBall with uri {}", trackName);
				trackBall = addBall(trackName, ColorRGBA.Red);
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

		public void buildFromWaypoint(WaypointConfig wc, Ball parentBall) {
			String waypointName;
			if (wc.getName().endsWith(CinemaAN.suffix_unnamed)) {
				waypointName = wc.getName() + incrementingWaypoint;
				incrementingWaypoint++;
			} else {
				waypointName = wc.getName();
			}
			if (!myBalls.containsKey(waypointName)) {
				myLogger.info("Adding waypointBall with uri {}", waypointName);
				addBall(waypointName, ColorRGBA.Yellow, 0.5f);
			}
			if (parentBall != null) {
				parentBall.addConnection(waypointName, "containsWaypoint", 2);
			}
		}

		public void buildFromRotation(RotationConfig rc, Ball parentBall) {
			String rotationName;
			if (rc.getName().endsWith(CinemaAN.suffix_unnamed)) {
				rotationName = rc.getName() + incrementingRotation;
				incrementingRotation++;
			} else {
				rotationName = rc.getName();
			}
			if (!myBalls.containsKey(rotationName)) {
				myLogger.info("Adding waypointBall with uri {}", rotationName);
				addBall(rotationName, ColorRGBA.Orange, 0.5f);
			}
			if (parentBall != null) {
				parentBall.addConnection(rotationName, "containsRotation", 2);
			}
		}
	}
	
	private CinematicModelBuilder getMyCinematicModelBuilder() {
		if (myCinematicModelBuilder == null) {
			myCinematicModelBuilder = new CinematicModelBuilder();
		}
		return myCinematicModelBuilder;
	}

	public void showCinematicConfig() {
		if (thisActivated) {
			stop();
		}
		getMyCinematicModelBuilder().buildModelFromCinematicConfig(myDemoCinematicConfig);
		start();
	}
	*/

	public void runBalls() {
		if (myBalls.isEmpty() && myRenderContext != null) {
			//showCinematicConfig();
			start();
		} else if (thisActivated) {
			stop();
		} else {
			start();
		}
	}

	public void resetAllBalls() {
		// Reset position and velocity
		for (Ball ball : myBalls.values()) {
			ball.reset();
		}
	}

	public void buildModelFromJena(Model rdfModel, boolean ballsForAllObjects) {
		final float NORMAL_RADIUS = 1.25f;
		final float ENDPOINT_RADIUS = 0.75f;
		final float BLANK_NODE_RADIUS = 0.5f;
		/*
		 * NodeIterator objects = rdfModel.listObjects(); while (objects.hasNext()) { RDFNode node = objects.next();
		 * logger.info("Node read: " + node.toString()); }
		 */
		if (thisActivated) {
			stop();
		}
		resetAllBalls();
		ResIterator res = rdfModel.listSubjects();
		while (res.hasNext()) {
			Resource node = res.nextResource();
			//logger.info("Subject read: " + node.toString()); // TEST ONLY
			Ball newBall;
			if (node.isAnon()) {
				// A blank node!
				newBall = addBall(node.toString(), ColorRGBA.Blue, BLANK_NODE_RADIUS);
			} else {
				// A regular node
				newBall = addBall(node.toString(), ColorRGBA.Red, NORMAL_RADIUS);
			}
			StmtIterator statements = node.listProperties();
			while (statements.hasNext()) {
				Statement statement = statements.nextStatement();
				RDFNode rdfObject = statement.getObject();
				//logger.info("Adding connection: " + node.toString() +" via " + statement.getPredicate() + " to " + statement.getObject()); // TEST ONLY
				newBall.addConnection(rdfObject.toString(), statement.getPredicate().toString());
			}
		}
		if (ballsForAllObjects) { // If this true, balls will be generated for all Objects, even if they are not subjects
			NodeIterator objects = rdfModel.listObjects();
			while (objects.hasNext()) {
				RDFNode node = objects.next();
				if (!myBalls.containsKey(node.toString())) {
					addBall(node.toString(), ColorRGBA.Green, ENDPOINT_RADIUS);
				}
			}
		}
		resetAllBalls();
		myDamping = computeIdealDamping();
		start();
	}

	public Model loadModelFromTurtle(ClassLoader loader, String configPath) {
		Model rdfModel = ModelFactory.createDefaultModel();
		try {
			InputStream stream = loader.getResourceAsStream(configPath);
			rdfModel.read(stream, null, "TURTLE");
		} catch (Exception e) {
			showErrorInLift("Exception attemping to read Turtle file: " + e);
			return null;
		}
		myLastModel = rdfModel;
		return rdfModel;
	}

	public boolean buildModelFromTurtle(ClassLoader loader, String configPath, boolean ballsForAllObjects) {
		boolean success = false;
		Model rdfModel = loadModelFromTurtle(loader, configPath);
		if (rdfModel != null) {
			buildModelFromJena(rdfModel, ballsForAllObjects);
			success = true;
		}
		return success;
	}

	public boolean buildModelFromTurtle(ClassLoader loader, String configPath) {
		return buildModelFromTurtle(loader, configPath, false);
	}

	public boolean buildModelFromTurtleUsingLiftSettings(String configPath) {
		boolean success = false;
		myResourceCl = null;
		String classloaderKey = getLiftAmbassador().getLiftVariable(DataballStrings.classloaderKey);
		if (classloaderKey != null) {
			if (myClassloaders.containsKey(classloaderKey)) {
				myResourceCl = myClassloaders.get(classloaderKey);
			}
		}
		boolean showAllObjects = false;
		String liftShowAllObjectsString = getLiftAmbassador().getLiftVariable(DataballStrings.showAllObjects);
		if (liftShowAllObjectsString != null) {
			showAllObjects = Boolean.valueOf(liftShowAllObjectsString);
		}
		if (myResourceCl != null) {

			success = buildModelFromTurtle(myResourceCl, configPath, showAllObjects);

		} else {
			showErrorInLift("Databalls graph using Lift settings requested, but could not find classloader with key " + classloaderKey);
		}
		return success;
	}

	public boolean buildModelFromSparql(Model modelToQuery, String queryString) {
		boolean success = false;
		Query query = QueryFactory.create(queryString);
		//Model model = loadModelFromTurtle(loader, configPath); // Not sure if we want this to build its own model or not...
		//if (model != null) {
		QueryExecution qexec = QueryExecutionFactory.create(query, modelToQuery);
		Model resultModel = qexec.execDescribe();
		qexec.close();
		buildModelFromJena(resultModel, true);
		success = true;
		//}
		return success;
	}

	public boolean buildModelFromSpaqrlUsingLiftSettings(String queryString) {
		if (myLastModel != null) {
			return buildModelFromSparql(myLastModel, queryString);
		} else {
			showErrorInLift("Can't build model from Sparql - no model for query loaded");
			return false;
		}
	}
	
	public void buildCloudFromSparql(Model modelToQuery, String queryString) {
		clear(); // Seems best for now
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, modelToQuery);
		try {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				Iterator<String> varNames = soln.varNames();
				RDFNode lastNode = null;
				for (; varNames.hasNext(); ) {
					RDFNode newNode = soln.get(varNames.next());
					Ball nodeBall = addBall(newNode.toString(), ColorRGBA.Green);
					if (lastNode != null) {
						nodeBall.addConnection(lastNode.toString(), "Shares solution");
					}
					lastNode=newNode;
				}      
			}
		} finally {
			qexec.close();
		}
		float cloudRadius = 0;
		for (int i=0; i < BALL_INJECTION_BOX_SIZE.length; i++) {
			//if (BALL_INJECTION_BOX_SIZE[i]/2 > cloudRadius) cloudRadius = BALL_INJECTION_BOX_SIZE[i]/2;
			cloudRadius += pow(BALL_INJECTION_BOX_SIZE[i]/2, 2);
		}
		cloudRadius = (float)(Math.sqrt(new Double(cloudRadius))*1.2);
		Vector3f cloudPosition = new Vector3f(BALL_INJECTION_POSITION[0], BALL_INJECTION_POSITION[1], BALL_INJECTION_POSITION[2]);
		new Cloud(cloudRadius, cloudPosition, ColorRGBA.Blue);
		myDamping = MINIMUM_DAMPING_COEFFICIENT;
		start();
	}

	public boolean buildCloudFromSpaqrlUsingLiftSettings(String queryString) {
		if (myLastModel != null) {
			buildCloudFromSparql(myLastModel, queryString);
			return true;
		} else {
			showErrorInLift("Can't build model from Sparql - no model for query loaded");
			return false;
		}
	}
	
	public void setClassLoader(ClassLoader loader) {
		myResourceCl = loader;
	}

	public void setClassLoader(String key, ClassLoader loader) {
		myClassloaders.put(key, loader);
	}
	
	void showErrorInLift(String errorText) {
		myLogger.error(errorText);
		getLiftAmbassador().displayError(DataballStrings.liftErrorCode, errorText);
	}

	public boolean performAction(String action, String text) {
		boolean success = true;
		// Clear error shown in Lift, if any
		getLiftAmbassador().displayError(DataballStrings.liftErrorCode, "");
		if (action.equals(DataballStrings.viewRdfGraph)) { // Oh, Java 6 and your non-String supporting case statements...
			success = buildModelFromTurtleUsingLiftSettings(text);
		} else if (action.equals(DataballStrings.onOff)) {
			runBalls();
		} else if (action.startsWith(DataballStrings.setDamping)) {
			String dampingString = action.replaceAll(DataballStrings.setDamping + "_", "");
			myDamping = DataballStrings.highDamping.equals(dampingString) ? HIGH_DAMPING_COEFFICIENT : LOW_DAMPING_COEFFICIENT;
		} else if (action.equals(DataballStrings.clear)) {
			clear();
		} else if (action.equals(DataballStrings.demo)) {
			//showCinematicConfig();
			myLogger.warn("Cinematic demo is depreciated.");
		} else if (action.equals(DataballStrings.viewSparqlQuery)) {
			success = buildModelFromSpaqrlUsingLiftSettings(text);
		} else if (action.equals(DataballStrings.viewSparqlQueryCloud)) {
			success = buildCloudFromSpaqrlUsingLiftSettings(text);
		} else {
			myLogger.error("Action sent to Databalls, but not recognized: {}", action);
			success = false;
		}
		return success;
	}

	public void clear() {

		class Delayed {

			private Void clearBalls() {
				// Wait a while after stop() to be sure updates are complete
				FutureTask<Void> future =
						new FutureTask<Void>(new Callable<Void>() {

					@Override
					public Void call() {
						myBalls.clear();
						myBallsNode = new Node("Databalls");
						return null;
					}
				});
				ExecutorService executor = Executors.newFixedThreadPool(1);
				executor.execute(future);
				// Making this a noncancelable task (see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html)
				boolean interrupted = false;
				try {
					while (true) {
						try {
							return future.get();
						} catch (InterruptedException e) { // ... really want to check to be sure this is an interrupted exception
							interrupted = true;
							// fall through and retry
						} catch (ExecutionException e) {
							myLogger.error("Execution Exception encountered in BallBuilder.clear() - other problems may follow: {}", e);
							return null;
						}
					}
				} finally {
					if (interrupted) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}

		stop();
		new Delayed().clearBalls();
		lastRadius = Float.NaN; // make sure assignStartingLocation knows we are starting over
	}
	
	public void stop() {
		thisActivated = false;
		Future<Object> detachFuture = myRenderContext.enqueueCallable(new Callable<Boolean>() { // Do this on main render thread in case this is being run from a different one - oh no!

			@Override
			public Boolean call() throws Exception {
				myDSM.detachTopSpatial(myBallsNode);
				if (myScreenText != null) {
					myFlatOverlayMgr.detachOverlaySpatial(myScreenText);
				}
				return true;
			}
		});
		try { // Wait until call is complete before returning
			detachFuture.get(5, java.util.concurrent.TimeUnit.SECONDS);
		} catch (Exception e) {
			myLogger.error("Future for detaching ballsNode did not return! Info: {}", e);
		}
		// Reset startMode for next "inflation" period
		startMode = true;
		updateCount = 0;
	}

	public void start() {
		resetAllBalls();

		myRenderContext.enqueueCallable(new Callable<Void>() { // Do this on main render thread in case this is being run from a different one - oh no!

			@Override
			public Void call() throws Exception {
				myDSM.attachTopSpatial(myBallsNode);
				thisActivated = true;
				return null;
			}
		});
	}

	private float computeIdealDamping() {
		double maxInstabilityScore = 0;
		Map<String, Integer> ballConnectionStrength = new HashMap<String, Integer>();
		for (String ballUri : myBalls.keySet()) {
			int thisConnectivity = 0;
			if (ballConnectionStrength.containsKey(ballUri)) {
				thisConnectivity += ballConnectionStrength.get(ballUri);
			}
			Ball ball = myBalls.get(ballUri);
			for (String connectedUri : ball.connectionMap.keySet()) {
				int connectionStrength = ball.connectionMap.get(connectedUri);
				thisConnectivity += connectionStrength;
				int thatConnectivity = 0;
				if (ballConnectionStrength.containsKey(connectedUri)) {
					thatConnectivity += ballConnectionStrength.get(connectedUri);
				}
				thatConnectivity += connectionStrength;
				ballConnectionStrength.put(connectedUri, thatConnectivity);
			}
			ballConnectionStrength.put(ballUri, thisConnectivity);
		}
		for (String ballUri : ballConnectionStrength.keySet()) {
			if (myBalls.containsKey(ballUri)) {
				Ball ball = myBalls.get(ballUri);
				double instabilityScore = ballConnectionStrength.get(ballUri) / pow(ball.radius, 4); // Basically this goes as connectionStrength/mass^1.33
				if (instabilityScore > maxInstabilityScore) {
					maxInstabilityScore = instabilityScore;
				}
			}
		}
		float idealDamping = new Float(1 - DAMPING_TRIM_CONSTANT / pow(maxInstabilityScore, 4));
		idealDamping = Math.max(idealDamping, MINIMUM_DAMPING_COEFFICIENT);
		myLogger.info("Damping coefficient of {} computed for current configuration. Maximum instabilityScore was {}", idealDamping, maxInstabilityScore);
		return idealDamping;
	}
	private static boolean startMode = true;
	private static int updateCount = 0;

	public void applyUpdates(float tpf) { // Called in ModularRenderContext.doUpdate - not sure if we want to hook in way "down" there or not
		//boolean temp = activated; //TEST ONLY
		//activated = false; //LOCK OFF! FOR TEST ONLY!!
		if (thisActivated) {
			// Set special dampings to allow for quick "inflation" during start-up period
			float currentDamping = myDamping;
			if (startMode) {
				updateCount += 1;
				if (updateCount < RELEASE_DAMPING_PERIOD) {
					currentDamping = RELEASE_DAMPING;
				} else if (updateCount < (RELEASE_DAMPING_PERIOD + INFLATION_PERIOD)) {
					currentDamping = INFLATION_DAMPING;
				} else {
					startMode = false;
					myLogger.info("Startup damping mode complete; 1/tpf is {}; damping = {}", 1 / tpf , currentDamping);
				}

			}
			for (Ball ball : myBalls.values()) {
				ball.control.setGravity(Vector3f.ZERO);
				Vector3f location = ball.geometry.getLocalTranslation();
				Vector3f velocity = ball.control.getLinearVelocity();

				// "Auto-brakes": slow this way down if it's getting so fast that the 60Hz physics won't converge
				if (velocity.length() / 60 > 1) {
					ball.control.setLinearVelocity(velocity.mult(0.01f));
					myLogger.warn("Ball velocity at {}; auto-braking!", velocity.length());
					velocity = velocity.mult(0.01f); //In case we use it later;
				}

				// Compute forces due to other balls and connections
				Vector3f potentialForce = new Vector3f();
				Vector3f springForce = new Vector3f();
				for (Ball otherBall : myBalls.values()) {
					if (!ball.equals(otherBall)) {
						Vector3f otherLocation = otherBall.geometry.getLocalTranslation();
						Vector3f vectorToOther = otherLocation.subtract(location);
						Vector3f directionToOther = vectorToOther.normalize();
						float distanceToOther = vectorToOther.length();
						Stick connectingStick = null;

						// Compute potential force
						float potentialForceMagnitude = -20f * ((float) pow(ball.radius, 3) * (float) pow(otherBall.radius, 3)) / (distanceToOther * distanceToOther); // Change potential force physics here. Physics to move to separate method?
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

				//Vector3f dampingForce = velocity.mult(damping); // Change damping force physics here
				Vector3f dampingForce = Vector3f.ZERO; // Or not. At moment built-in jME/bullet damping seems to work a little better


				// Apply forces
				Vector3f totalForce = potentialForce.add(springForce).add(dampingForce);
				// TEST ONLY Force logging:
				//logger.info("potentialForce is ", potentialForce.toString());
				//logger.info("springForce is ", springForce.toString());
				//logger.info("dampingForce is ", dampingForce.toString());
				//logger.info("totalForce is ", totalForce.toString());
				Float forceMagnitude = totalForce.length();
				//logger.info("forceMagnitude is ", forceMagnitude);
				// At the first update, locations may not yet be initialized, so the balls may both have position at the origin resulting in infinite force
				if ((!forceMagnitude.isInfinite()) && (!forceMagnitude.isNaN())) {
					// Apply conservative forces
					ball.control.applyCentralForce(totalForce);
					// Set damping force
					ball.control.setLinearDamping(currentDamping);
				} else {
					// May not want this always enabled to avoid a chunk of messages during startup (when updates have been enabled but initialization isn't quite complete on main render thread)
					//logger.info("Invalid force in BallBuilder.applyUpdates (normal during initial startup)");
				}

			}
		}
		//activated = temp; //TEST ONLY
	}

	public void pick() {
		// Reset results list.
		CollisionResults results = new CollisionResults();
		// Convert screen click to 3d position
		Vector2f click2d = myIM.getCursorPosition();
		Vector3f click3d = myCameraMgr.getCommonCamera(CameraMgr.CommonCameras.DEFAULT).getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
		Vector3f dir = myCameraMgr.getCommonCamera(CameraMgr.CommonCameras.DEFAULT).getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
		// Aim the ray from the clicked spot forwards.
		Ray ray = new Ray(click3d, dir);
		// Collect intersections between ray and all nodes in results list.
		myBallsNode.collideWith(ray, results);
		//rrc.getJme3RootDeepNode(null).collideWith(ray, results); // ONLY FOR TESTING
		
		/*
		// (Print the results so we see what is going on:)
		for (int i = 0; i < results.size(); i++) { // (For each “hit”, we know distance, impact point, geometry.)
		float dist = results.getCollision(i).getDistance();
		Vector3f pt = results.getCollision(i).getContactPoint();
		String target = results.getCollision(i).getGeometry().getName(); logger.info("Selection #" + i + ": " + target + " at " + pt + ", " + dist + " WU away."); }
		*/
		
		// Use the results
		if (results.size() > 0) {
			// The closest result is the target that the player picked:
			Geometry target = results.getClosestCollision().getGeometry();
			// ... Unless it's a DataCloud surface
			if (target.getName().startsWith(Cloud.CLOUD_NAME_PREFIX)) {
				target = results.getCollision(1).getGeometry();
			}
			// Here comes the action:
			for (Ball ball : myBalls.values()) {
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

	private void showPickText(String uri) {
		myLogger.info("Looks like you picked {}", uri);
		if (myScreenText != null) {
			myFlatOverlayMgr.detachOverlaySpatial(myScreenText);
		}
		myScreenText = myTextMgr.getScaledBitmapText("Picked: " + uri, 0.8f);
		myScreenText.setLocalTranslation(PICK_TEXT_POSITION[0], PICK_TEXT_POSITION[1], PICK_TEXT_POSITION[2]);
		myScreenText.setColor(ColorRGBA.Black);
		myFlatOverlayMgr.attachOverlaySpatial(myScreenText);
	}
}
