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
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import java.util.concurrent.Callable;
import org.appdapter.core.name.Ident;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.opengl.optic.MatFactory;
import org.cogchar.render.opengl.scene.GeomFactory;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */

// This will need some ongoing refactorings both to fix some oddness and bad form inherent in development of the concepts here,
// and to make sure the BasicVirtualThing has the sorts of properties we want it to have
// Might need to be abstract, but so far not necessary. However this shouldn't be instantiated directly, so ultimately
// we'll want to make sure it can't be
public class BasicVirtualThing {

		// Some of these may not really need to live in fields, and some of these properties may duplicate other implied
		// information in methods -- definitely something to polish up as this is refactored and refined
		Ident uri;
		HumanoidRenderContext myHRC;
		Vector3f position;
		Mesh mesh;
		Geometry geometry;
		ColorRGBA color = ColorRGBA.Blue; // A default color
		RigidBodyControl control = null;
		Material material;
		Node myRootNode;

		// This class is intended to also support physical objects, though that functionality is not yet fleshed out
		// Both addToVirtualWorld methods really need to be protected against exceptions if they are called when the 
		// Thing's fields aren't set up properly 
		void addToVirtualWorld(HumanoidRenderContext hrc, Node rootNode, CollisionShape shape, float mass) {
			control = new RigidBodyControl(shape, mass);
			control.setRestitution(0.5f);
			addToVirtualWorld(hrc, rootNode);
		}
		void addToVirtualWorld(HumanoidRenderContext hrc, final Node rootNode) {
			myHRC = hrc;
			myRootNode = rootNode;
			RenderRegistryClient rrc = hrc.getRenderRegistryClient();
			GeomFactory geomFactory = rrc.getSceneGeometryFacade(null);
			MatFactory materialFactory = rrc.getOpticMaterialFacade(null, null);
			
			if (material == null) {
				// Set "standard" material; these hard coded values probably won't live here for long
				material = materialFactory.makeMatWithOptTexture("Common/MatDefs/Light/Lighting.j3md", "SpecularMap", null);
				material.setBoolean("UseMaterialColors", true);
				material.setFloat("Shininess", 25f);
				setMaterialColor(color);
			}

			geometry = geomFactory.makeGeom(uri.getLocalName(), mesh, material, control);
			attachGeometryToRootNode();
		}
		
		void setMaterialColor(ColorRGBA newColor) {
			color = newColor;
			material.setColor("Diffuse", newColor);
			material.setColor("Ambient", newColor);
			material.setColor("Specular", newColor);	
		}
		
		private void attachGeometryToRootNode() {
			final PhysicsSpace physicsSpace = myHRC.getRenderRegistryClient().getJme3BulletPhysicsSpace();
			myHRC.enqueueCallable(new Callable<Void>() { // Do this on main render thread

				@Override
				public Void call() throws Exception {
					if (control != null) {
						//geometry.addControl(control); should be automatically done in geomFactory.makeGeom
						physicsSpace.add(control);
						control.setPhysicsLocation(position); // Need to review this to see if it's necessary/proper
					} else {
						geometry.setLocalTranslation(position);
					}
					myRootNode.attachChild(geometry);
					return null;
				}
			});
		}
		
		private void detachGeometryFromRootNode() {
			myHRC.enqueueCallable(new Callable<Void>() { // Do this on main render thread

				@Override
				public Void call() throws Exception {
					// Must detach by name; detaching by geometry saved in field does not work
					myRootNode.detachChildNamed(uri.getLocalName()); 
					return null;
				}
			});
		}
		
		void changeMesh(Mesh newMesh) {
			detachGeometryFromRootNode();
			mesh = newMesh;
			geometry = myHRC.getRenderRegistryClient().getSceneGeometryFacade(null).makeGeom(uri.getLocalName(), newMesh, material, control);
			attachGeometryToRootNode();
		}
}
