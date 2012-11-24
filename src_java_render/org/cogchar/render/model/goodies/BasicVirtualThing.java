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

import com.jme3.bullet.PhysicsSpace;
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
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */

// This will need some ongoing refactorings both to fix some oddness and bad form inherent in development of the concepts here,
// and to make sure the BasicVirtualThing has the sorts of properties we want it to have
// Might need to be abstract, but so far not necessary. 
public class BasicVirtualThing {
	
		private static Logger theLogger = LoggerFactory.getLogger(BasicVirtualThing.class);

		RenderRegistryClient myRenderRegCli;
		Ident uri;
		Vector3f position;
		boolean attached = false;
		
		// This allows a single "thing" to have multiple switchable geometries
		List<BasicThingGeometry> myGeometries = new ArrayList<BasicThingGeometry>();
		Geometry attachedGeometry;

		Node myRootNode;
		
		// May not want to allow this to be instantiated directly
		// Might make sense to set more instance variables in the constructor as well, including perhaps rootNode?
		BasicVirtualThing(RenderRegistryClient aRenderRegCli) {
			myRenderRegCli = aRenderRegCli;
		}
		
		// It would be good for clarity to have this in a separate file, but by having it as an inner class we allow
		// access to getRenderRegistryClient() without awkwardness. And it seems it can be a private class. But we might
		// end up reconsidering this being a private inner class eventually.
		private class BasicThingGeometry {
			Geometry myGeometry;
			ColorRGBA myColor = ColorRGBA.Blue; // A default color
			RigidBodyControl myControl = null;
			Material myMaterial;

			BasicThingGeometry(Mesh mesh, Material material, ColorRGBA color, 
					Quaternion rotation, CollisionShape shape, float mass) {
				if (color != null) {
					myColor = color;
				}
				if (shape != null) {
					myControl = new RigidBodyControl(shape, mass);
					myControl.setRestitution(0.5f);
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
				myGeometry = getRenderRegistryClient().getSceneGeometryFacade(null).makeGeom(uri.getLocalName(), mesh, myMaterial, myControl);
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
		// This class is intended to also support physical objects, though that functionality is not yet fleshed out
		int addGeometry(Mesh mesh, Material material, ColorRGBA color, Quaternion rotation, CollisionShape shape, float mass) {
			myGeometries.add(new BasicThingGeometry(mesh, material, color, rotation, shape, mass));
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
		void attachToVirtualWorldNode(Node rootNode) {
			attachToVirtualWorldNode(rootNode, 0);
		}
		// For attaching geometry by index
		void attachToVirtualWorldNode(Node rootNode, int geometryIndex) {
			myRootNode = rootNode;
			attachToRootNode(geometryIndex);
		}
		// For switching to geometry from a new index, attached to existing root node
		void setGeometryByIndex(int geometryIndex) {
			if (myRootNode != null) {
				attachToRootNode(geometryIndex);
			} else {
				theLogger.error("Attempting to set geometry by index, but no root node is set");
			}	
		}
		
		private void attachToRootNode(int geometryIndex) {
			detachIfAttached();
			if (myGeometries.size() > geometryIndex) {
				attachGeometryToRootNode(myGeometries.get(geometryIndex));
			} else {
				theLogger.error("Attempting to attach BasicVirtualThing {} with geometry index {}, but that geometry is not available",
						uri.getAbsUriString(), geometryIndex);
			}
		}
		
		void detachIfAttached() {
			if (attached)  {
				detachGeometryFromRootNode();
			}
		}
		
		private void attachGeometryToRootNode(final BasicThingGeometry geometryToAttach) {
			final PhysicsSpace physicsSpace = getRenderRegistryClient().getJme3BulletPhysicsSpace();
			getRenderRegistryClient().getWorkaroundAppStub().enqueue(new Callable<Void>() { // Do this on main render thread

				@Override
				public Void call() throws Exception {
					if (geometryToAttach.myControl != null) {
						//geometry.addControl(control); should be automatically done in geomFactory.makeGeom
						physicsSpace.add(geometryToAttach.myControl);
						geometryToAttach.myControl.setPhysicsLocation(position); // Need to review this to see if it's necessary/proper
					} else {
						geometryToAttach.getJmeGeometry().setLocalTranslation(position);
					}
					myRootNode.attachChild(geometryToAttach.getJmeGeometry());
					return null;
				}
			});
			attached = true;
		}
		
		private void detachGeometryFromRootNode() {
			getRenderRegistryClient().getWorkaroundAppStub().enqueue(new Callable<Void>() { // Do this on main render thread

				@Override
				public Void call() throws Exception {
					// Must detach by name; detaching by saved geometry does not work
					myRootNode.detachChildNamed(uri.getLocalName()); 
					return null;
				}
			});
			attached = false;
		}
		
}
