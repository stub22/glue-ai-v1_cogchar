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

package org.cogchar.render.goody.basic;

import com.jme3.animation.*;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
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
import org.appdapter.core.name.Ident;
import org.cogchar.render.app.entity.GoodyAction;
import org.cogchar.render.app.entity.VWorldEntity;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */

// This will need some ongoing refactorings both to fix some oddness and bad form inherent in development of the concepts here,
// and to make sure the BasicGoodyImpl has the sorts of properties we want it to have 
public class BasicGoodyEntity extends VWorldEntity {

	protected Vector3f myPosition = new Vector3f(); // default: at origin
	protected Quaternion myRotation = new Quaternion(); // default: no rotation
	protected Vector3f myScale = new Vector3f(1f, 1f, 1f); // default: scale = 1 in all directions

	// This allows a single "thing" to have multiple switchable geometries
	List<BasicGoodyGeometry> myGeometries = new ArrayList<BasicGoodyGeometry>();
	int attachedIndex = NULL_INDEX; // The index of the currently attached geometry, or -1 if none
	final static int NULL_INDEX = -1;

	protected Node myNode;
	protected Node myRootNode;

	// May not want to allow this to be instantiated directly
	// Might make sense to set more instance variables in the constructor as well, including perhaps rootNode?
	protected BasicGoodyEntity(RenderRegistryClient aRenderRegCli, Ident uri) {
		myRenderRegCli = aRenderRegCli;
		myUri = uri;
		myNode = new Node("Goody Node of " + uri.getLocalName());
	}

	// It would be good for clarity to have this in a separate file, but by having it as an inner class we allow
	// access to getRenderRegistryClient() without awkwardness. And it seems it can be a private class. But we might
	// end up reconsidering this being a private inner class eventually.
	private class BasicGoodyGeometry {
		Geometry myGeometry;
		ColorRGBA myColor = ColorRGBA.Blue; // A default color
		RigidBodyControl myControl = null;
		Material myMaterial;
		Quaternion myRotationOffset;

		BasicGoodyGeometry(Mesh mesh, Material material, ColorRGBA color, 
				Quaternion rotation, CollisionShape shape, float mass) {
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
			myGeometry.setLocalScale(myScale);
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

	//This may not be a great thing to expose publically. For now it's used to attach a GoodyEntity to a CameraEntity
	public Node getNode() {
		return myNode;
	}
	
	// Returns geometry index
	// This method is intended to support physical objects
	protected int addGeometry(Mesh mesh, Material material, ColorRGBA color, Quaternion rotation, 
			CollisionShape shape, float mass) {
		myGeometries.add(new BasicGoodyGeometry(mesh, material, color, rotation, shape, mass));
		return myGeometries.size() - 1;
	}
	// For adding non-physical geometries
	protected int addGeometry(Mesh mesh, Material material, ColorRGBA color, Quaternion rotation) {
		return addGeometry(mesh, material, color, rotation, null, 0f);
	}
	// For adding non-physical geometries with default material
	protected int addGeometry(Mesh mesh, ColorRGBA color, Quaternion rotation) {
		return addGeometry(mesh, null, color, rotation, null, 0f);
	}
	// For adding non-physical geometries with default material and no rotation offset
	protected int addGeometry(Mesh mesh, ColorRGBA color) {
		return addGeometry(mesh, null, color, new Quaternion(), null, 0f);
	}

	// For attaching "default" (zero index) geometry
	@Override
	public void attachToVirtualWorldNode(Node rootNode) {
		attachToVirtualWorldNode(rootNode, 0);
	}
	
	// For attaching geometry by index
	// A bit messy now that we're using a myNode, especially in the multiple enqueueForJmeAndWait calls
	// in this method; perhaps has potental to be refactored into something more satisfying...
	protected void attachToVirtualWorldNode(Node rootNode, int geometryIndex) {
		if (rootNode != myRootNode) { 
			if (myRootNode != null) {
				enqueueForJmeAndWait(new Callable() { // Do this on main render thread
					@Override
					public Void call() throws Exception {
						myRootNode.detachChild(myNode);
						return null;
					}
				});
			}
			myRootNode = rootNode;
		}
		enqueueForJmeAndWait(new Callable() { // Do this on main render thread
			@Override
			public Void call() throws Exception {
				myRootNode.attachChild(myNode);
				return null;
			}
		});		
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
		final BasicGoodyGeometry geometryToAttach = myGeometries.get(geometryIndex);
		final Geometry jmeGeometry = geometryToAttach.getJmeGeometry();
		setGeometryPositionAndRotation(geometryToAttach);
		//myLogger.info("Attaching geometry {} for goody {}", geometryIndex, myUri); // TEST ONLY
		enqueueForJmeAndWait(new Callable() { // Do this on main render thread

			@Override
			public Void call() throws Exception {
				myNode.attachChild(jmeGeometry);
				if (geometryToAttach.myControl != null) {
					myRenderRegCli.getJme3BulletPhysicsSpace().add(jmeGeometry);
				}
				attachedIndex = geometryIndex;
				return null;
			}
		});
	}

	private void detachGeometryFromRootNode() {
		final BasicGoodyGeometry currentGeometry = getCurrentAttachedBasicGoodyGeometry();
		enqueueForJmeAndWait(new Callable<Void>() { // Do this on main render thread

			@Override
			public Void call() throws Exception {
				if (currentGeometry.myControl != null) {
					myRenderRegCli.getJme3BulletPhysicsSpace().remove(currentGeometry.myControl);
				}
				// Must detach by name; detaching by saved geometry does not work
				myNode.detachChildNamed(myUri.getLocalName()); 
				attachedIndex = NULL_INDEX;
				return null;
			}
		});
	}
	
	protected void setNewGeometryRotationOffset(int geomId, Quaternion offset) {
		BasicGoodyGeometry geom = myGeometries.get(geomId);
		geom.myRotationOffset = offset;
	}

	@Override
	public void setPosition(Vector3f newPosition) {
		setPositionAndRotation(newPosition, myRotation);
	}

	public void setRotation(Quaternion newRotation) {
		setPositionAndRotation(myPosition, newRotation); 
	}

	public void setPositionAndRotation(Vector3f newPosition, Quaternion newRotation) {
		setNewPositionAndRotationIfNonNull(newPosition, newRotation);
		if (attachedIndex != NULL_INDEX) {
			enqueueForJmeAndWait(new Callable() { // Do this on main render thread

				@Override
				public Void call() throws Exception {
					setGeometryPositionAndRotation(getCurrentAttachedBasicGoodyGeometry());
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

	private Quaternion getTotalRotation(BasicGoodyGeometry goodieGeometry) {
		return myRotation.mult(goodieGeometry.myRotationOffset);
	}
	
	private void setGeometryPositionAndRotation(BasicGoodyGeometry goodieGeometry) {
		Quaternion totalRotation = getTotalRotation(goodieGeometry);
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
	
	// Likely will eventually want to refactor this to use SpatialAnimMgr
	protected void moveViaAnimation(Vector3f newPosition, Quaternion newOrientation, Vector3f newScale, float duration) {
		final String moveAnimName = "BasicGoodyMoveFactory";
		AnimationFactory aniFactory = new AnimationFactory(duration, moveAnimName);
		// First add starting position/rotation/scale to timeline at index 0
		aniFactory.addKeyFrameTranslation(0, myPosition);
		aniFactory.addKeyFrameRotation(0, getTotalRotation(getCurrentAttachedBasicGoodyGeometry()));
		aniFactory.addKeyFrameScale(0, myScale);
		// Now add new position/rotation/scale at duration:
		setNewPositionAndRotationIfNonNull(newPosition, newOrientation);
		if (newScale != null) {
			myScale = newScale;
		}
		aniFactory.addTimeTranslation(duration, myPosition);
		aniFactory.addTimeRotation(duration, getTotalRotation(getCurrentAttachedBasicGoodyGeometry()));
		aniFactory.addTimeScale(duration, myScale);
		// Finally the Animation is generated and linked to the geometry via an AnimationControl
		Animation moveAnimation = aniFactory.buildAnimation();
		AnimControl goodyControl = new AnimControl(); // Should this be retained for reuse?
		goodyControl.addAnim(moveAnimation);
		getCurrentAttachedGeometry().addControl(goodyControl);
		AnimChannel moveChannel = goodyControl.createChannel();
		moveChannel.setAnim(moveAnimName, 0f);
		// Oddly, it seems this needs to be set *after* starting the animation with setAnim:
		moveChannel.setLoopMode(LoopMode.DontLoop);
	}
	
	protected void setNewPositionAndRotationIfNonNull(Vector3f newPosition, Quaternion newRotation) {
		if (newPosition != null) {
			myPosition = newPosition;
		}
		if (newRotation != null) {
			myRotation = newRotation;
		}
	}
	
	@Override
	public void setScale(final Float scaleFactor) {
		setVectorScale(new Vector3f(scaleFactor, scaleFactor, scaleFactor));
	}
	
	public void setVectorScale(final Vector3f scaleVector) {
		if (scaleVector != null) {
			enqueueForJmeAndWait(new Callable() { // Do this on main render thread

				@Override
				public Void call() throws Exception {
					for (BasicGoodyGeometry aGeometry : myGeometries) {
						Vector3f rotatedScale = aGeometry.myRotationOffset.mult(scaleVector);
						aGeometry.myGeometry.setLocalScale(rotatedScale);
					}
					return null;
				}
			});
			myScale = scaleVector;
		}
	}
	
	private void setPositionRotationAndScale(Vector3f position, Quaternion rotation, Vector3f scale, Float scalarScale) {
		setPositionAndRotation(position, rotation);
		if (scale != null) {
			setVectorScale(scale);
		} else if (scalarScale != null) {
			setScale(scalarScale);
		}
	}
	
	public void setCurrentGeometryColor(final ColorRGBA geoColor) {
		//myLogger.info("setting color with color {} to index {}", geoColor, attachedIndex); // TEST ONLY
		if ((geoColor != null) && (attachedIndex != NULL_INDEX)) {
			BasicGoodyGeometry currentGeometry = myGeometries.get(attachedIndex);
			currentGeometry.setMaterialColor(geoColor);
		}
	}
	
	// Override this method to add functionality; be sure to call this super method to apply standard Goody actions
	@Override
	public void applyAction(GoodyAction ga) {
		Vector3f newLocation = ga.getLocationVector();
		Quaternion newRotation = ga.getRotationQuaternion();
		Vector3f newVectorScale = ga.getVectorScale();
		Float scaleFactor = ga.getScale();
		ColorRGBA newColor = ga.getColor();
		switch (ga.getKind()) {
			case SET : {
				setPositionRotationAndScale(newLocation, newRotation, newVectorScale, scaleFactor);
				setCurrentGeometryColor(newColor);
				break;
			}
			case MOVE : {
				Float timeEnroute = ga.getTravelTime();
				if ((timeEnroute == null) || (Math.abs(timeEnroute-0f) < 0.001f)) {
					setPositionRotationAndScale(newLocation, newRotation, newVectorScale, scaleFactor);
				} else {
					if ((newVectorScale == null) && (scaleFactor != null)) {
						newVectorScale = new Vector3f(scaleFactor, scaleFactor, scaleFactor);
					}
					moveViaAnimation(newLocation, newRotation, newVectorScale, timeEnroute);
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
	public Geometry getCurrentAttachedGeometry() {
		Geometry currentGeometry = null;
		BasicGoodyGeometry currentGoodyGeometry = getCurrentAttachedBasicGoodyGeometry();
		if (currentGoodyGeometry != null) {
			currentGeometry = currentGoodyGeometry.getJmeGeometry();
		}
		return currentGeometry;
	}
	
	private BasicGoodyGeometry getCurrentAttachedBasicGoodyGeometry() {
		BasicGoodyGeometry currentGeometry = null;
		if (attachedIndex != NULL_INDEX) {
			currentGeometry = myGeometries.get(attachedIndex);
		}
		return currentGeometry;
	}
		
}
