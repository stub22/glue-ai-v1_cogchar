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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.appdapter.core.name.Ident;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */

// This will need some ongoing refactorings both to fix some oddness and bad form inherent in development of the concepts here,
// and to make sure the BasicGoodyImpl has the sorts of properties we want it to have
public abstract class BasicGoodyImpl {
	
		private static Logger theLogger = LoggerFactory.getLogger(BasicGoodyImpl.class);
		
		// Number of ms this Impl will wait for goody to attach or detach from jMonkey root node before timing out
		// Currently we don't wait, because futures never return! This creates potential for concurrency issues
		// and is something it'd be very nice to sort out.
		private final static long ATTACH_DETACH_TIMEOUT = 3000;

		RenderRegistryClient myRenderRegCli;
		Ident myUri;
		Vector3f myPosition = new Vector3f(); // default: at origin
		Quaternion myRotation = new Quaternion(); // default: no rotation
		
		// This allows a single "thing" to have multiple switchable geometries
		List<BasicGoodieGeometry> myGeometries = new ArrayList<BasicGoodieGeometry>();
		int attachedIndex = NULL_INDEX; // The index of the currently attached geometry, or -1 if none
		final static int NULL_INDEX = -1;

		Node myRootNode;
		
		// May not want to allow this to be instantiated directly
		// Might make sense to set more instance variables in the constructor as well, including perhaps rootNode?
		BasicGoodyImpl(RenderRegistryClient aRenderRegCli, Ident uri) {
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
					Quaternion rotation, CollisionShape shape, float mass) {
				myRotationOffset = rotation;
				if (color != null) {
					myColor = color;
				}
				if (material == null) {
					// Set "standard" material; these hard coded values probably won't live here for long
					myMaterial = getRenderRegistryClient().getOpticMaterialFacade(null, null)
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
				myGeometry = getRenderRegistryClient().getSceneGeometryFacade(null)
						.makeGeom(myUri.getLocalName(), mesh, myMaterial, myControl);
				//myGeometry.addControl(new RigidBodyControl(0)); // TEST ONLY -- in here only until I can figure out what's wrong with goody floor
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
		
		protected RenderRegistryClient getRenderRegistryClient() {
			return myRenderRegCli;
		}
		
		// Returns geometry index
		// This method is intended to support physical objects
		int addGeometry(Mesh mesh, Material material, ColorRGBA color, Quaternion rotation, CollisionShape shape, float mass) {
			myGeometries.add(new BasicGoodieGeometry(mesh, material, color, rotation, shape, mass));
			return myGeometries.size() - 1;
		}
		// For adding non-physical geometries
		int addGeometry(Mesh mesh, Material material, ColorRGBA color, Quaternion rotation) {
			return addGeometry(mesh, material, color, rotation, null, 0f);
		}
		// For adding non-physical geometries with default material
		int addGeometry(Mesh mesh, ColorRGBA color, Quaternion rotation) {
			return addGeometry(mesh, null, color, rotation, null, 0f);
		}
		// For adding non-physical geometries with default material and no rotation offset
		int addGeometry(Mesh mesh, ColorRGBA color) {
			return addGeometry(mesh, null, color, new Quaternion(), null, 0f);
		}
		
		// For attaching "default" (zero index) geometry
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
					theLogger.error("Attempting to attach BasicVirtualThing {} with geometry index {}, but that geometry is not available",
						myUri.getAbsUriString(), geometryIndex);
				}
			} else {
				theLogger.error("Attempting to set geometry by index, but no root node is set");
			}	
		}
		
		private void attachGeometryToRootNode(final int geometryIndex) {
			if (attachedIndex != -1)  {
				detachGeometryFromRootNode();
			}
			final BasicGoodieGeometry geometryToAttach = myGeometries.get(geometryIndex);
			final Geometry jmeGeometry = geometryToAttach.getJmeGeometry();
			setGeometryPositionAndRotation(geometryToAttach);
			Future<Void> attachFuture = getRenderRegistryClient().getWorkaroundAppStub().enqueue(new Callable<Void>() { // Do this on main render thread

				@Override
				public Void call() throws Exception {
					myRootNode.attachChild(jmeGeometry);
					if (geometryToAttach.myControl != null) {
						getRenderRegistryClient().getJme3BulletPhysicsSpace().add(jmeGeometry);
					}
					attachedIndex = geometryIndex;
					return null;
				}
			});
			// Method should block until attach completes to avoid collision with subsequent detach
			//waitForJmeFuture(attachFuture);
		}
		
		private void detachGeometryFromRootNode() {
			final BasicGoodieGeometry currentGeometry = myGeometries.get(attachedIndex);
			Future<Void> detachFuture = getRenderRegistryClient().getWorkaroundAppStub().enqueue(new Callable<Void>() { // Do this on main render thread

				@Override
				public Void call() throws Exception {
					if (currentGeometry.myControl != null) {
						getRenderRegistryClient().getJme3BulletPhysicsSpace().remove(currentGeometry.myControl);
					}
					// Must detach by name; detaching by saved geometry does not work
					myRootNode.detachChildNamed(myUri.getLocalName()); 
					attachedIndex = NULL_INDEX;
					return null;
				}
			});
			// Method should block until detach completes to avoid collision with subsequent attaches
			//waitForJmeFuture(detachFuture);
		}
		
		// This would be nice if it worked, but futures don't return for some reason...
		private void waitForJmeFuture(Future jmeFuture) {
			try {
				jmeFuture.get(ATTACH_DETACH_TIMEOUT, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				theLogger.warn("Exception attempting to attach or detach goody: ", e);
			}
		}
		
		public void setPosition(Vector3f newPosition) {
			myPosition = newPosition;
			if (attachedIndex != NULL_INDEX) {
				setGeometryPositionAndRotation(myGeometries.get(attachedIndex));
			} 
		}
		
		public void setRotation(Quaternion newRotation) {
			myRotation = newRotation;
			if (attachedIndex != NULL_INDEX) {
				setGeometryPositionAndRotation(myGeometries.get(attachedIndex));
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
			RigidBodyControl jmeControl = goodieGeometry.myControl;
			if (jmeControl != null) {
				jmeControl.setPhysicsLocation(myPosition); // Need to review this to see if it's necessary/proper
				jmeControl.setPhysicsRotation(totalRotation);
				goodieGeometry.getJmeGeometry().setLocalTranslation(myPosition); // TEST ONLY
			} else {
				Geometry jmeGeometry = goodieGeometry.getJmeGeometry();
				jmeGeometry.setLocalTranslation(myPosition);
				jmeGeometry.setLocalRotation(totalRotation);
			}
		}
		
		public abstract void applyAction(GoodyAction ga);
		
}
