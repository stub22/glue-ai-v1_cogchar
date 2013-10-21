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
import org.cogchar.render.app.entity.GoodyActionExtractor;
import org.cogchar.render.app.entity.VWorldEntity;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.cogchar.render.sys.goody.GoodyRenderRegistryClient;
/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */

// This will need some ongoing refactorings both to fix some oddness and bad form inherent in development of the concepts here,
// and to make sure the BasicGoodyImpl has the sorts of properties we want it to have 
public class BasicGoodyEntity extends VWorldEntity {

	private Vector3f		myPosition = new Vector3f(); // default: at origin (relative to parentNode)
	private Quaternion		myRotation = new Quaternion(); // default: no rotation (relative to parentNode)
	private Vector3f		myScaleVec = new Vector3f(1f, 1f, 1f); // default: scale = 1 in all directions

	// This collection allows a single "thing" to have multiple switchable geometry bindings.
	// Only one of these geometries is attached to our JME3 node at any particular time.
	private List<BasicGoodyGeomBinding> myGeomBindings = new ArrayList<BasicGoodyGeomBinding>();
	private int myCurrAttachedBindingIndex = NULL_INDEX; // The index of the currently attached geom-binding, or -1 if none
	private final static int NULL_INDEX = -1;

	private Node myContentNode;
	private Node myParentNode;

	// May not want to allow this to be instantiated directly
	// Might make sense to set more instance variables in the constructor as well, including perhaps rootNode?
	protected BasicGoodyEntity(GoodyRenderRegistryClient aRenderRegCli, Ident uri) {
		super(aRenderRegCli, uri);
		myContentNode = new Node("Goody Node of " + uri.getLocalName());
	}
	protected Quaternion getRotation() {
		return myRotation;
	}
	protected Vector3f getPosition() {
		return myPosition;
	}
	protected Vector3f getScale() {
		return myScaleVec;
	}
	@Override public void setUniformScaleFactor(final Float scaleFactor, QueueingStyle qStyle) {
		setVectorScale(new Vector3f(scaleFactor, scaleFactor, scaleFactor), qStyle);
	}
	// It would be good for clarity to have this in a separate file, but by having it as an inner class we allow
	// access to getRenderRegistryClient() without awkwardness. And it seems it can be a private class. But we might
	// end up reconsidering this being a private inner class eventually.
	private class BasicGoodyGeomBinding {
		Geometry myGeometry;
		ColorRGBA myColor = ColorRGBA.Blue; // A default color
		RigidBodyControl myControl = null;
		Material myMaterial;
		Quaternion myRotationOffset;

		BasicGoodyGeomBinding(Mesh mesh, Material material, ColorRGBA color, 
				Quaternion rotation, CollisionShape shape, float mass) {
			myRotationOffset = rotation;
			if (color != null) {
				myColor = color;
			}
			if (material == null) {
				// Set "standard" material; these hard coded values probably won't live here for long
				myMaterial = getRenderRegCli().getOpticMaterialFacade(null, null)
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
			myGeometry = getRenderRegCli().getSceneGeometryFacade(null)
					.makeGeom(getUri().getLocalName(), mesh, myMaterial, myControl);
			myGeometry.setLocalScale(myScaleVec);
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
	public Node getContentNode() {
		return myContentNode;
	}
	protected Node getParentNode() { 
		return myParentNode;
	}
	// Returns geometry index
	// This method is intended to support physical objects
	protected int addGeometry(Mesh mesh, Material material, ColorRGBA color, Quaternion rotation, 
			CollisionShape shape, float mass) {
		myGeomBindings.add(new BasicGoodyGeomBinding(mesh, material, color, rotation, shape, mass));
		return myGeomBindings.size() - 1;
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
	@Override public void attachToVirtualWorldNode(Node parentNode, QueueingStyle style) {
		attachToVirtualWorldNode(parentNode, 0, style);
	}
	
	// For attaching geometry by index
	// A bit messy now that we're using a myNode, especially in the multiple enqueueForJmeAndWait calls
	// in this method; perhaps has potental to be refactored into something more satisfying...
	
	// Note that if we are already on the JME3 render thread, and we try to block synchronously, we will deadlock!
	protected void attachToVirtualWorldNode(Node parentNode, int geometryIndex, QueueingStyle style) {
		if (parentNode != myParentNode) { 
			if (myParentNode != null) {
				enqueueForJme(new Callable() { // Do this on main render thread
					@Override public Void call() throws Exception {
						myParentNode.detachChild(myContentNode);
						return null;
					}
				}, style);
			}
			myParentNode = parentNode;
		}
		enqueueForJme(new Callable() { // Do this on main render thread
			@Override public Void call() throws Exception {
				myParentNode.attachChild(myContentNode);
				return null;
			}
		}, style);		
		setActiveBoundGeomIndex(geometryIndex, style);
	}
	// For switching to geometry from a new index, attached to existing root node
	public void setGeometryByIndex(int geometryIndex, QueueingStyle style) {
		if (myParentNode != null) {
			if (myGeomBindings.size() > geometryIndex) {
				setActiveBoundGeomIndex(geometryIndex, style);
			} else {
				getLogger().error("Attempting to attach BasicVirtualThing {} with geometry index {}, but that geometry is not available",
					getUri().getAbsUriString(), geometryIndex);
			}
		} else {
			getLogger().error("Attempting to set geometry by index, but no root node is set");
		}	
	}

	@Override public void detachFromVirtualWorldNode(QueueingStyle style) {
		if (myCurrAttachedBindingIndex != -1)  {
			detachActiveBoundGeom(style);
		}
	}

	private void setActiveBoundGeomIndex(final int geomBindIndex, QueueingStyle style) {
		detachFromVirtualWorldNode(style);
		final BasicGoodyGeomBinding geomBindingToAttach = myGeomBindings.get(geomBindIndex);
		final Geometry jmeGeometry = geomBindingToAttach.getJmeGeometry();
		setGeometryPositionAndRotation(geomBindingToAttach);
		//myLogger.info("Attaching geometry {} for goody {}", geometryIndex, myUri); // TEST ONLY
		enqueueForJme(new Callable() { // Do this on main render thread

			@Override public Void call() throws Exception {
				myContentNode.attachChild(jmeGeometry);
				if (geomBindingToAttach.myControl != null) {
					getRenderRegCli().getJme3BulletPhysicsSpace().add(jmeGeometry);
				}
				myCurrAttachedBindingIndex = geomBindIndex;
				return null;
			}
		}, style);
	}

	private void detachActiveBoundGeom(QueueingStyle style) {
		final BasicGoodyGeomBinding currentGeomBinding = getCurrentAttachedGeomBinding();
		enqueueForJme(new Callable<Void>() { // Do this on main render thread
			@Override public Void call() throws Exception {
				if (currentGeomBinding.myControl != null) {
					getRenderRegCli().getJme3BulletPhysicsSpace().remove(currentGeomBinding.myControl);
				}
				// Must detach by name; detaching by saved geometry does not work
				myContentNode.detachChildNamed(getUri().getLocalName()); 
				myCurrAttachedBindingIndex = NULL_INDEX;
				return null;
			}
		}, style);
	}
	
	protected void setNewGeometryRotationOffset(int geomId, Quaternion offset) {
		BasicGoodyGeomBinding geomBinding = myGeomBindings.get(geomId);
		geomBinding.myRotationOffset = offset;
	}

	@Override public void setPosition(Vector3f newPosition, QueueingStyle qStyle) {
		setPositionAndRotation(newPosition, myRotation, qStyle);
	}

	@Override public void setRotation(Quaternion newRotation, QueueingStyle qStyle) {
		setPositionAndRotation(myPosition, newRotation, qStyle); 
	}

	public void setPositionAndRotation(Vector3f newPosition, Quaternion newRotation, QueueingStyle style) {
		setNewPositionAndRotationIfNonNull(newPosition, newRotation);
		if (myCurrAttachedBindingIndex != NULL_INDEX) {
			enqueueForJme(new Callable() { // Do this on main render thread
				@Override public Void call() throws Exception {
					setGeometryPositionAndRotation(getCurrentAttachedGeomBinding());
					return null;
				}
			}, style);
		}
	}


	private Quaternion getTotalRotation(BasicGoodyGeomBinding goodieGeometry) {
		return myRotation.mult(goodieGeometry.myRotationOffset);
	}
	
	private void setGeometryPositionAndRotation(BasicGoodyGeomBinding goodieGeometry) {
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
		aniFactory.addKeyFrameRotation(0, getTotalRotation(getCurrentAttachedGeomBinding()));
		aniFactory.addKeyFrameScale(0, myScaleVec);
		// Now add new position/rotation/scale at duration:
		setNewPositionAndRotationIfNonNull(newPosition, newOrientation);
		if (newScale != null) {
			myScaleVec = newScale;
		}
		aniFactory.addTimeTranslation(duration, myPosition);
		aniFactory.addTimeRotation(duration, getTotalRotation(getCurrentAttachedGeomBinding()));
		aniFactory.addTimeScale(duration, myScaleVec);
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
	

	
	@Override public void setVectorScale(final Vector3f scaleVector, QueueingStyle style) {
		if (scaleVector != null) {
			enqueueForJme(new Callable() { // Do this on main render thread

				@Override
				public Void call() throws Exception {
					for (BasicGoodyGeomBinding aGeomBinding : myGeomBindings) {
						Vector3f rotatedScale = aGeomBinding.myRotationOffset.mult(scaleVector);
						aGeomBinding.myGeometry.setLocalScale(rotatedScale);
					}
					return null;
				}
			}, style);
			myScaleVec = scaleVector;
		}
	}
	
	private void setPositionRotationAndScale(Vector3f position, Quaternion rotation, Vector3f scale, Float scalarScale,
				QueueingStyle qStyle) {
		setPositionAndRotation(position, rotation, qStyle);
		if (scale != null) {
			setVectorScale(scale, qStyle);
		} else if (scalarScale != null) {
			setUniformScaleFactor(scalarScale, qStyle);
		}
	}
	
	public void setCurrentGeometryColor(final ColorRGBA geoColor) {
		//myLogger.info("setting color with color {} to index {}", geoColor, attachedIndex); // TEST ONLY
		if ((geoColor != null) && (myCurrAttachedBindingIndex != NULL_INDEX)) {
			BasicGoodyGeomBinding currentGeometry = myGeomBindings.get(myCurrAttachedBindingIndex);
			currentGeometry.setMaterialColor(geoColor);
		}
	}
	
	// Override this method to add functionality for particular goodies, but  be sure to call this super method.
	@Override public void applyAction(GoodyActionExtractor ga, QueueingStyle qStyle) {
		Vector3f newLocation = ga.getLocationVec3f();
		Quaternion newRotation = ga.getRotationQuaternion();
		Vector3f newVectorScale = ga.getScaleVec3f();
		Float scaleFactor = ga.getScaleUniform();
		ColorRGBA newColor = ga.getColor();
		switch (ga.getKind()) {
			case SET : {
				setPositionRotationAndScale(newLocation, newRotation, newVectorScale, scaleFactor, qStyle);
				setCurrentGeometryColor(newColor);
				break;
			}
			case MOVE : {
				Float timeEnroute = ga.getTravelTime();
				if ((timeEnroute == null) || (Math.abs(timeEnroute-0f) < 0.001f)) {
					setPositionRotationAndScale(newLocation, newRotation, newVectorScale, scaleFactor, qStyle);
				} else {
					if ((newVectorScale == null) && (scaleFactor != null)) {
						newVectorScale = new Vector3f(scaleFactor, scaleFactor, scaleFactor);
					}
					moveViaAnimation(newLocation, newRotation, newVectorScale, timeEnroute);
				}
				break;
			}
			default: {
				getLogger().error("Unknown action requested in Goody {}: {}", getUri().getLocalName(), ga.getKind().name());
			}
		}
	};

	// Not clear whether this is a good thing to expose publically, especially since goodies can change their geometry
	// Adding it to provide goody cinematic capabilities on a trial basis
	public Geometry getCurrentAttachedGeometry() {
		Geometry currentGeometry = null;
		BasicGoodyGeomBinding currGeomBind = getCurrentAttachedGeomBinding();
		if (currGeomBind != null) {
			currentGeometry = currGeomBind.getJmeGeometry();
		}
		return currentGeometry;
	}
	
	private BasicGoodyGeomBinding getCurrentAttachedGeomBinding() {
		BasicGoodyGeomBinding currentGeometry = null;
		if (myCurrAttachedBindingIndex != NULL_INDEX) {
			currentGeometry = myGeomBindings.get(myCurrAttachedBindingIndex);
		}
		return currentGeometry;
	}
		
}
