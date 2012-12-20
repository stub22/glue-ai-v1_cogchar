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

package org.cogchar.render.model.goodies;

import com.jme3.animation.LoopMode;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.events.MotionTrack;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.api.cinema.*;
import org.cogchar.render.opengl.scene.CinematicMgr;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */

// This will need some ongoing refactorings both to fix some oddness and bad form inherent in development of the concepts here,
// and to make sure the BasicGoodyImpl has the sorts of properties we want it to have 
public class BasicGoodyImpl extends BasicGoody {
	
	// The currently used (and depreciated) version of jMonkey doesn't appear to correctly apply durations for MotionTracks
	// The reported duration matches the value set via setInitialDuration, but the actual observed duration is shorter
	// The result is this unfortunate trim factor to which we set the speed, so that the observed motion duration matches
	// what we expect to see. Totally prone to variation and problems; hopefully we'll be able to get rid of this in the 
	// not-too-distant future with a new version of jMonkey or etc!
	// Update 19 Dec 2012: Seems the whole Cinematic system in jMonkey is depreciated. Probably using the AnimationFactory
	// will produce better results and allow this to be eliminated:
	final static float SPEED_TRIM_FACTOR = 0.77f; 

	protected Vector3f myPosition = new Vector3f(); // default: at origin
	protected Quaternion myRotation = new Quaternion(); // default: no rotation

	// This allows a single "thing" to have multiple switchable geometries
	List<BasicGoodieGeometry> myGeometries = new ArrayList<BasicGoodieGeometry>();
	int attachedIndex = NULL_INDEX; // The index of the currently attached geometry, or -1 if none
	final static int NULL_INDEX = -1;

	protected Node myRootNode;

	// May not want to allow this to be instantiated directly
	// Might make sense to set more instance variables in the constructor as well, including perhaps rootNode?
	protected BasicGoodyImpl(RenderRegistryClient aRenderRegCli, Ident uri) {
		myRenderRegCli = aRenderRegCli;
		myUri = uri;
	}

	// It would be good for clarity to have this in a separate file, but by having it as an inner class we allow
	// access to getRenderRegistryClient() without awkwardness. And it seems it can be a private class. But we might
	// end up reconsidering this being a private inner class eventually.
	private class BasicGoodieGeometry {
		Geometry myGeometry;
		ColorRGBA myColor = ColorRGBA.Blue; // A default color
		RigidBodyControl myControl = null;
		Material myMaterial;
		Quaternion myRotationOffset;

		BasicGoodieGeometry(Mesh mesh, Material material, ColorRGBA color, 
				Quaternion rotation, float scale, CollisionShape shape, float mass) {
			myRotationOffset = rotation;
			if (color != null) {
				myColor = color;
			}
			if (material == null) {
				// Set "standard" material; these hard coded values probably won't live here for long
				myMaterial = myRenderRegCli.getOpticMaterialFacade(null, null)
						.makeMatWithOptTexture("Common/MatDefs/Light/Lighting.j3md", "SpecularMap", null);
				myMaterial.setBoolean("UseMaterialColors", true);
				myMaterial.setFloat("Shininess", 25f);
				setMaterialColor(myColor);
			} else {
				myMaterial = material;
			}
			if (shape != null) {
				myControl = new RigidBodyControl(shape, mass);
				//myGeometry.addControl(myControl); should be automatically done in geomFactory.makeGeom
			}
			myGeometry = myRenderRegCli.getSceneGeometryFacade(null)
					.makeGeom(myUri.getLocalName(), mesh, myMaterial, myControl);
			//myGeometry.addControl(new RigidBodyControl(0)); // TEST ONLY -- in here only until I can figure out what's wrong with goody floor
			myGeometry.setLocalScale(scale);
			myGeometry.setLocalRotation(rotation);
		}

		final void setMaterialColor(ColorRGBA newColor) {
			myColor = newColor;
			myMaterial.setColor("Diffuse", newColor);
			myMaterial.setColor("Ambient", newColor);
			myMaterial.setColor("Specular", newColor);	
		}

		Geometry getJmeGeometry() {
			return myGeometry;
		}
	}

	// Returns geometry index
	// This method is intended to support physical objects
	protected int addGeometry(Mesh mesh, Material material, ColorRGBA color, Quaternion rotation, 
			float scale, CollisionShape shape, float mass) {
		myGeometries.add(new BasicGoodieGeometry(mesh, material, color, rotation, scale, shape, mass));
		return myGeometries.size() - 1;
	}
	// For adding non-physical geometries
	protected int addGeometry(Mesh mesh, Material material, ColorRGBA color, Quaternion rotation, float scale) {
		return addGeometry(mesh, material, color, rotation, scale, null, 0f);
	}
	// For adding non-physical geometries with default material
	protected int addGeometry(Mesh mesh, ColorRGBA color, Quaternion rotation, float scale) {
		return addGeometry(mesh, null, color, rotation, scale, null, 0f);
	}
	// For adding non-physical geometries with default material and no rotation offset
	protected int addGeometry(Mesh mesh, ColorRGBA color, float scale) {
		return addGeometry(mesh, null, color, new Quaternion(), scale, null, 0f);
	}

	// For attaching "default" (zero index) geometry
	@Override
	public void attachToVirtualWorldNode(Node rootNode) {
		attachToVirtualWorldNode(rootNode, 0);
	}
	// For attaching geometry by index
	protected void attachToVirtualWorldNode(Node rootNode, int geometryIndex) {
		myRootNode = rootNode;
		attachGeometryToRootNode(geometryIndex);
	}
	// For switching to geometry from a new index, attached to existing root node
	public void setGeometryByIndex(int geometryIndex) {
		if (myRootNode != null) {
			if (myGeometries.size() > geometryIndex) {
				attachGeometryToRootNode(geometryIndex);
			} else {
				myLogger.error("Attempting to attach BasicVirtualThing {} with geometry index {}, but that geometry is not available",
					myUri.getAbsUriString(), geometryIndex);
			}
		} else {
			myLogger.error("Attempting to set geometry by index, but no root node is set");
		}	
	}

	@Override
	public void detachFromVirtualWorldNode() {
		if (attachedIndex != -1)  {
			detachGeometryFromRootNode();
		}
	}

	private void attachGeometryToRootNode(final int geometryIndex) {
		detachFromVirtualWorldNode();
		final BasicGoodieGeometry geometryToAttach = myGeometries.get(geometryIndex);
		final Geometry jmeGeometry = geometryToAttach.getJmeGeometry();
		setGeometryPositionAndRotation(geometryToAttach);
		//myLogger.info("Attaching geometry {} for goody {}", geometryIndex, myUri); // TEST ONLY
		enqueueForJmeAndWait(new Callable() { // Do this on main render thread

			@Override
			public Void call() throws Exception {
				myRootNode.attachChild(jmeGeometry);
				if (geometryToAttach.myControl != null) {
					myRenderRegCli.getJme3BulletPhysicsSpace().add(jmeGeometry);
				}
				attachedIndex = geometryIndex;
				return null;
			}
		});
	}

	private void detachGeometryFromRootNode() {
		final BasicGoodieGeometry currentGeometry = myGeometries.get(attachedIndex);
		enqueueForJmeAndWait(new Callable<Void>() { // Do this on main render thread

			@Override
			public Void call() throws Exception {
				if (currentGeometry.myControl != null) {
					myRenderRegCli.getJme3BulletPhysicsSpace().remove(currentGeometry.myControl);
				}
				// Must detach by name; detaching by saved geometry does not work
				myRootNode.detachChildNamed(myUri.getLocalName()); 
				attachedIndex = NULL_INDEX;
				return null;
			}
		});
	}

	@Override
	public void setPosition(Vector3f newPosition) {
		setPositionAndRotation(newPosition, myRotation);
	}

	public void setRotation(Quaternion newRotation) {
		setPositionAndRotation(myPosition, newRotation); 
	}

	public void setPositionAndRotation(Vector3f newPosition, Quaternion newRotation) {
		if (newPosition != null) {
			myPosition = newPosition;
		}
		if (newRotation != null) {
			myRotation = newRotation;
		}
		if (attachedIndex != NULL_INDEX) {
			enqueueForJmeAndWait(new Callable() { // Do this on main render thread

				@Override
				public Void call() throws Exception {
					setGeometryPositionAndRotation(myGeometries.get(attachedIndex));
					return null;
				}
			});
		}
	}

	public Vector3f getPosition() {
		return myPosition;
	}

	public Quaternion getRotation() {
		return myRotation;
	}

	private void setGeometryPositionAndRotation(BasicGoodieGeometry goodieGeometry) {
		Quaternion totalRotation = myRotation.mult(goodieGeometry.myRotationOffset);
		//myLogger.info("Setting Goody position {}, rotation {} with offset {} for total rotation {}", // TEST ONLY
		//	new Object[]{myPosition, myRotation, goodieGeometry.myRotationOffset, totalRotation}); // TEST ONLY
		RigidBodyControl jmeControl = goodieGeometry.myControl;
		if (jmeControl != null) {
			jmeControl.setPhysicsLocation(myPosition); // Need to review this to see if it's necessary/proper
			jmeControl.setPhysicsRotation(totalRotation);
		} else {
			Geometry jmeGeometry = goodieGeometry.getJmeGeometry();
			jmeGeometry.setLocalTranslation(myPosition);
			jmeGeometry.setLocalRotation(totalRotation);
		}
	}

	protected void translateToPosition(Vector3f newPosition, float timeEnroute) {
		MotionPath path = new MotionPath();
		path.addWayPoint(myPosition);
		path.addWayPoint(newPosition);
		//path.setCurveTension(0.0f);
		// MotionTrack is depreciated in new jMonkey, but we must use it since we're using an older version:
		MotionTrack event = new MotionTrack(myGeometries.get(attachedIndex).getJmeGeometry(), path);
		// Current jMonkey uses this instead:
		//MotionEvent event = new MotionEvent(myGeometries.get(attachedIndex).getJmeGeometry(), path);
		event.setSpeed(SPEED_TRIM_FACTOR);
		event.setDirectionType(MotionTrack.Direction.None);
		event.setInitialDuration(timeEnroute);
		event.play();
		myPosition = newPosition;
	}
	
	/*
	// In somewhat ugly fashion, we build a CinematicInstanceConfig for the move operation
	// Likely can be improved
	// Currently rotations and timing need some work
	// Oops, Cinematics ae depreciated! May want to do this via AnimationFactory instead.
	private void moveViaCinematic(Vector3f newPosition, Quaternion newOrientation, float duration) {
		Quaternion totalRotation = newOrientation.mult(myGeometries.get(attachedIndex).myRotationOffset);
		Ident endWaypointUri = makeIdentForLocalCinematicEntity("MoveEndPoint");
		Ident endRotationUri = makeIdentForLocalCinematicEntity("MoveEndRotation");
		Ident motionTrackUri = makeIdentForLocalCinematicEntity("MoveTranslation");
		Ident rotationTrackUri = makeIdentForLocalCinematicEntity("MoveRotation");
		Ident moveCinematicUri = makeIdentForLocalCinematicEntity("MoveCinematic");
		WaypointConfig endWaypoint = new WaypointConfig(endWaypointUri, newPosition.toArray(new float[3]));
		RotationConfig endRotation = new RotationConfig(endRotationUri, totalRotation.toAngles(new float[3]));
		CinematicTrack motionTrack = new CinematicTrack(motionTrackUri);
		setBasicCinematicTrackProperties(motionTrack, duration);
		motionTrack.trackType = CinematicTrack.TrackType.POSITIONTRACK;
		motionTrack.waypoints.add(endWaypoint);
		CinematicTrack rotationTrack = new CinematicTrack(rotationTrackUri);
		setBasicCinematicTrackProperties(rotationTrack, duration);
		rotationTrack.trackType = CinematicTrack.TrackType.ROTATIONTRACK;
		rotationTrack.endRotation = endRotation;
		CinematicInstanceConfig moveCinematic = new CinematicInstanceConfig(moveCinematicUri);
		moveCinematic.duration = duration;
		moveCinematic.myTracks.add(motionTrack);
		moveCinematic.myTracks.add(rotationTrack);
		CinematicMgr cineMgr = myRenderRegCli.getSceneCinematicsFacade(null);
		cineMgr.buildCinematic(moveCinematic);
		cineMgr.controlCinematicByName(moveCinematic.getName(), CinematicMgr.ControlAction.PLAY);
		myPosition = newPosition;
		myRotation = newOrientation;
	}
		
	private Ident makeIdentForLocalCinematicEntity(String uriSuffix) {
		String uriPrefixString = CinemaCN.CCRT + myUri.getLocalName();
		return new FreeIdent(uriPrefixString + uriSuffix);
	}
	
	private void setBasicCinematicTrackProperties(CinematicTrack track, float duration) {
		track.attachedItem = myUri;
		track.attachedItemType = CinematicTrack.AttachedItemType.GOODY;
		track.trackDuration = duration;
		track.startTime = 0.0f;
		track.loopMode = "DontLoop";
	}
	*/
	
	@Override
	public void setScale(final Float scaleFactor) {
		if (scaleFactor != null) {
			enqueueForJmeAndWait(new Callable() { // Do this on main render thread

				@Override
				public Void call() throws Exception {
					for (BasicGoodieGeometry aGeometry : myGeometries) {
						aGeometry.myGeometry.setLocalScale(scaleFactor);
					}
					return null;
				}
			});
		}
	}

	// Override this method to add functionality; be sure to call this super method to apply standard Goody actions
	@Override
	public void applyAction(GoodyAction ga) {
		Vector3f newLocation = ga.getLocationVector();
		Quaternion newRotation = ga.getRotationQuaternion();
		Float scaleFactor = ga.getScale();
		switch (ga.getKind()) {
			case SET : {
				setPositionAndRotation(newLocation, newRotation);
				setScale(scaleFactor);
				break;
			}
			case MOVE : {
				Float timeEnroute = ga.getTravelTime();
				if ((timeEnroute == null) || (Math.abs(timeEnroute-0f) < 0.001f)) {
					setPositionAndRotation(newLocation, newRotation);
				} else {
					translateToPosition(newLocation, timeEnroute);
					// Soon will also add rotation, scale via AnimationFactory techniques
				}
				break;
			}
			default: {
				myLogger.error("Unknown action requested in Goody {}: {}", myUri.getLocalName(), ga.getKind().name());
			}
		}
	};

	// Not clear whether this is a good thing to expose publically, especially since goodies can change their geometry
	// Adding it to provide goody cinematic capabilities on a trial basis
	public Geometry getCurrentGeometry() {
		Geometry currentGeometry = null;
		if (attachedIndex != NULL_INDEX) {
			currentGeometry = myGeometries.get(attachedIndex).getJmeGeometry();
		}
		return currentGeometry;
	}
		
}
