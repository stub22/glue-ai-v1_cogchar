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
 * 
 * ------------------------------------------------------------------------------
 *
 *		This file contains code copied from the JMonkeyEngine project.
 *		You may not use this file except in compliance with the
 *		JMonkeyEngine license.  See full notice at bottom of this file. 
 */

package org.cogchar.demo.render.opengl;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import org.cogchar.render.opengl.app.DemoApp;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.PQTorus;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import org.cogchar.render.opengl.bony.world.CollisionMgr;

/** Sample 8 - how to let the user pick (select) objects in the scene 
 * using the mouse or key presses. Can be used for shooting, opening doors, etc. */
public class DemoYouPickStuff extends DemoApp {

	Node myShootablesRootNode;
	Geometry myMark, myArrowMark;
	
	private static String		MARK_ACTION = "markSpot";
	private static String		ARROW_NORMAL = "arrowNormal";
	
	public static void main(String[] args) {
		DemoYouPickStuff app = new DemoYouPickStuff();
		app.start();
	}


	@Override public void simpleInitApp() {
		super.simpleInitApp();
		flyCam.setMoveSpeed(20);

		initCrossHairs(); // a "+" in the middle of the screen to help aiming
		initKeys();       // load custom key mappings
		initMark();       // a red sphere to myMark the hit
		initArrowMark();
		setupLight();
		/** create four colored boxes and a floor to shoot at: */
		myShootablesRootNode = new Node("Shootables");
		rootNode.attachChild(myShootablesRootNode);
		myShootablesRootNode.attachChild(makeCube("a Dragon", -2f, 0f, 1f, 3.0f));
		myShootablesRootNode.attachChild(makeCube("a tin can", 1f, -2f, 0f, 1.5f));
		myShootablesRootNode.attachChild(makeCube("the Sheriff", 0f, 1f, -2f, 1.0f));
		myShootablesRootNode.attachChild(makeCube("the Deputy", 1f, 0f, -4f, 0.5f));

		//myShootablesRootNode.attachChild(makePQT("spiral", 5,3, 2f, 1f, 32, 32)); // Spiral torus
		//myShootablesRootNode.attachChild(makePQT("flower", 3,8, 2f, 1f, 32, 32)); // Flower torus

		myShootablesRootNode.attachChild(makeFloor());
		Spatial otoSpatial = getSceneFacade().getModelML().makeOtoSpatialFromDefaultPath();
		otoSpatial.scale(0.5f);
		otoSpatial.setLocalTranslation(-1.0f, -1.5f, -0.6f);

		myShootablesRootNode.attachChild(otoSpatial);
		
		Geometry rr = makeBlueQuadGeom();
		myShootablesRootNode.attachChild(rr);
		BitmapText btSpatial = makeTextSpatial();
		myShootablesRootNode.attachChild(btSpatial);
	}
	private Geometry makeBlueQuadGeom() { 
        Quad q = new Quad(6, 3);
        Geometry g = getGeomFactory().makeColoredUnshadedGeom("blueRect", q, ColorRGBA.Blue, null);
        g.setLocalTranslation(0, -3, -0.0001f);
        return g;
	}
	private BitmapText makeTextSpatial() {
        String txtB =   "ABCDEFGHIKLMNOPQRSTUVWXYZ1234567890`~!@#$%^&*()-=_+[]\\;',./{}|:<>?";
		BitmapText txtSpatial = getTextMgr().getScaledBitmapText(txtB, 1.0f);
        txtSpatial.setBox(new Rectangle(0, 0, 6, 3));
        txtSpatial.setQueueBucket(Bucket.Transparent);
        txtSpatial.setSize( 0.5f );
        txtSpatial.setText(txtB);
		return txtSpatial;
    }
	/** Declaring the "Shoot" action and mapping to its triggers. */
	private void initKeys() {
		inputManager.addMapping(MARK_ACTION,
				new KeyTrigger(KeyInput.KEY_SPACE), // trigger 1: spacebar
				new MouseButtonTrigger(MouseInput.BUTTON_LEFT)); // trigger 2: left-button click
		inputManager.addListener(actionListener, MARK_ACTION);
	}
	/** Defining the "Shoot" action: Determine what was hit and how to respond. */
	private ActionListener actionListener = new ActionListener() {

		public void onAction(String name, boolean keyPressed, float tpf) {
			if (name.equals(MARK_ACTION) && !keyPressed) {
				
				CollisionResults coRes = CollisionMgr.getCameraCollisions(cam, myShootablesRootNode);
				CollisionMgr.printCollisionDebug(getLogger(), coRes);

				// Mark the hit object
				if (coRes.size() > 0) {
					// The closest collision point is what was truly hit:
					CollisionResult closest = coRes.getClosestCollision();
					// Let's interact - we myMark the hit with a red dot.
					myMark.setLocalTranslation(closest.getContactPoint());
					getSceneFacade().getDeepSceneMgr().attachTopSpatial(myMark);
				} else {
					// No hits? Then remove the red myMark.
					getSceneFacade().getDeepSceneMgr().detachTopSpatial(myMark);
				}
			}
		}
	};

	/** A cube object for target practice */
	protected Geometry makeCube(String geomName, float x, float y, float z, float sideLen) {
		Box cubeMesh = getMeshFF().getShapeMF().makeBoxMesh(new Vector3f(x, y, z), sideLen, sideLen, sideLen);
		Geometry cubeGeom = getGeomFactory().makeRandomlyColoredUnshadedGeom(geomName, cubeMesh, null);
		return cubeGeom;
	}

	/** A floor to show that the "shot" can go through several objects. */
	protected Geometry makeFloor() {
		Box floorMesh = getMeshFF().getShapeMF().makeBoxMesh(new Vector3f(0, -4, -5), 15, .2f, 15);
		Geometry floorGeom = getGeomFactory().makeColoredUnshadedGeom("theFloor", floorMesh, ColorRGBA.Gray, null);
		return floorGeom;
	}
	protected Geometry makePQT(String geomName, float p, float q, float radius, float width, int steps, int radialSamples) {
		PQTorus pqtMesh = getMeshFF().getShapeMF().makePQTorusMesh(p, q, radius, width, steps, radialSamples);
		Geometry pqtGeom = getGeomFactory().makeRandomlyColoredUnshadedGeom(geomName, pqtMesh, null);
		return pqtGeom;
	}	

	/** A red ball that marks the last spot that was "hit" by the "shot". */
	protected void initMark() {
		Sphere markMesh = getMeshFF().getShapeMF().makeSphereMesh(30, 30, 0.2f);
		myMark = getGeomFactory().makeColoredUnshadedGeom("theMark", markMesh, ColorRGBA.Red, null); 
	}

	protected void initArrowMark() {
		// From TestMousePick:
		Arrow arrow = getMeshFF().getFancyMF().makeArrowMesh(Vector3f.UNIT_Z.mult(2f), 3f);
		myArrowMark = getSceneFacade().getGeomFactory().makeColoredUnshadedGeom(ARROW_NORMAL, arrow, ColorRGBA.Red, null);
	}

	@Override public void simpleUpdate(float tpf) {
		// From TestMousePick
		 /*
		Vector3f origin    = cam.getWorldCoordinates(inputManager.getCursorPosition(), 0.0f);
		Vector3f direction = cam.getWorldCoordinates(inputManager.getCursorPosition(), 0.3f);
		direction.subtractLocal(origin).normalizeLocal();
		
		Ray ray = new Ray(origin, direction);
		 */
		Ray ray = new Ray(cam.getLocation(), cam.getDirection());

		CollisionResults results = new CollisionResults();
		myShootablesRootNode.collideWith(ray, results);

		if (results.size() > 0) {
			CollisionResult closest = results.getClosestCollision();
			myArrowMark.setLocalTranslation(closest.getContactPoint());

			Quaternion q = new Quaternion();
			q.lookAt(closest.getContactNormal(), Vector3f.UNIT_Y);
			myArrowMark.setLocalRotation(q);

			rootNode.attachChild(myArrowMark);
		} else {
			rootNode.detachChild(myArrowMark);
		}
	}
}

/*
 * 
 * Contains code copied and modified from the JMonkeyEngine.com project,
 * under the following terms:
 * 
 * -----------------------------------------------------------------------
 * 
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
