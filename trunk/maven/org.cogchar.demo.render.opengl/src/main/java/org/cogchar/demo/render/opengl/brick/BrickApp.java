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
package org.cogchar.demo.render.opengl.brick;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.BasicShadowRenderer;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.shadow.PssmShadowRenderer.CompareMode;
import com.jme3.shadow.PssmShadowRenderer.FilterMode;
import org.cogchar.render.app.core.CogcharPresumedApp;
import org.cogchar.render.app.core.CogcharRenderContext;
import org.cogchar.render.opengl.optic.MatFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BrickApp extends CogcharPresumedApp {

	protected Material myBrickMat, myRockMat, myPondMat;
	protected BulletAppState myPhysAppState;
	protected SceneProcessor myShadowRenderer;

	@Override protected CogcharRenderContext makeCogcharRenderContext() {
		BA_RenderContext rc = new BA_RenderContext();
		return rc;
	}
//		@Override public void simpleInitApp() {
//		super.simpleInitApp();

	class BA_RenderContext extends CogcharRenderContext {

		@Override public void completeInit() {

			myPhysAppState = new BulletAppState();

			initMaterials();

			//bulletAppState.getPhysicsSpace().enableDebug(assetManager);
		}

		/** Initialize the materials used in this scene. */
		protected void initMaterials() {
			MatFactory mf = findOrMakeOpticMaterialFacade(null, null);

			myBrickMat = mf.getBrickWallMat();
			myRockMat = mf.makeRockMat();
			myPondMat = mf.getPondMat();

		}
	}

	protected PhysicsSpace getPhysicsSpace() {
		return myPhysAppState.getPhysicsSpace();
	}

	protected void addShadowProcessor(SceneProcessor shadowProcessor) {
		rootNode.setShadowMode(ShadowMode.Off);
		viewPort.addProcessor(shadowProcessor);
		myShadowRenderer = shadowProcessor;
	}

	protected void initPssmShadowRenderer() {
		PssmShadowRenderer shadowRenderer = new PssmShadowRenderer(assetManager, 1024, 2);
		shadowRenderer.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
		shadowRenderer.setLambda(0.55f);
		shadowRenderer.setShadowIntensity(0.6f);
		shadowRenderer.setCompareMode(CompareMode.Hardware);
		shadowRenderer.setFilterMode(FilterMode.PCF4);
		addShadowProcessor(shadowRenderer);
	}

	protected void initBasicShadowRenderer() {
		/*
		 * These shadows work, kinda, but create nasty little edge effects.
		 */

		BasicShadowRenderer bsr = new BasicShadowRenderer(assetManager, 256);
		bsr.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
		addShadowProcessor(bsr);
	}

	/** Make a solid floor and add it to the scene. */
	public void initFloorBowlWall() {
		/** Initialize the floor geometry */
		Box floorBox = new Box(Vector3f.ZERO, 10f, 0.1f, 5f);
		floorBox.scaleTextureCoordinates(new Vector2f(3, 6));
		Geometry floor_geo = new Geometry("Floor", floorBox);
		floor_geo.setMaterial(myPondMat);
		floor_geo.setShadowMode(ShadowMode.Receive);
		floor_geo.setLocalTranslation(0, -0.1f, 0);

		/* Floor is physical with "mass 0.0f" = immovable object */
		makePhysicalObjControlAndAttachToRoot(floor_geo, 0.0f, null);
	}

	protected void attachPhysicalObjToRoot(Geometry geom, RigidBodyControl rbc, boolean attachAsControl) {
		if (attachAsControl) {
			geom.addControl(rbc);
		}
		rootNode.attachChild(geom);
		getPhysicsSpace().add(rbc);
	}

	protected void attachPhysicalObjToRoot(Geometry geom) {
		rootNode.attachChild(geom);
		getPhysicsSpace().add(geom);
	}

	protected void makePhysicalObjControlAndAttachToRoot(Geometry geom, float mass, Float friction) {
		RigidBodyControl rbc = new RigidBodyControl(mass);
		attachPhysicalObjToRoot(geom, rbc, true);
		if (friction != null) {
			rbc.setFriction(friction);
		}
	}
}
