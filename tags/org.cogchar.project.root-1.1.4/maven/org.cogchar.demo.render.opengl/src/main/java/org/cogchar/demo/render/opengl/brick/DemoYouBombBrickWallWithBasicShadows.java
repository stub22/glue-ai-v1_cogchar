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

package org.cogchar.demo.render.opengl.brick;

import org.cogchar.render.goody.physical.LaunchableCollidingRigidBodyControl;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import org.cogchar.render.app.bony.BonyGameFeatureAdapter;

/**
 * From: jme3test.bullet.TestBrickWall
 *
 */
public class DemoYouBombBrickWallWithBasicShadows extends BrickApp {

    static float bLength = 0.48f;
    static float bWidth = 0.24f;
    static float bHeight = 0.12f;

	private static Sphere				myRockSphere;
    private static Box					myBrickBox;
    private static SphereCollisionShape myRockColShape;


    public static void main(String args[]) {
        DemoYouBombBrickWallWithBasicShadows f = new DemoYouBombBrickWallWithBasicShadows();
        f.start();
    }

    @Override public void simpleInitApp() {
        super.simpleInitApp();
		

        myPhysAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(myPhysAppState);

        myRockSphere = new Sphere(32, 32, 0.4f, true, false);
        myRockSphere.setTextureMode(TextureMode.Projected);
        myRockColShape = new SphereCollisionShape(0.4f);
        myBrickBox = new Box(Vector3f.ZERO, bLength, bHeight, bWidth);
        myBrickBox.scaleTextureCoordinates(new Vector2f(1f, .5f));

        initWall();
        initFloorBombWallBasic();
        BonyGameFeatureAdapter.initCrossHairs(settings, getRenderContext().getRenderRegistryClient());
		
        this.cam.setLocation(new Vector3f(0, 6f, 6f));
        cam.lookAt(Vector3f.ZERO, new Vector3f(0, 1, 0));
        cam.setFrustumFar(15);
        inputManager.addMapping("shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener, "shoot");
        inputManager.addMapping("gc", new KeyTrigger(KeyInput.KEY_X));
        inputManager.addListener(actionListener, "gc");

		initBasicShadowRenderer();
    }


    private ActionListener actionListener = new ActionListener() {

        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("shoot") && !keyPressed) {
                Geometry bulletg = new Geometry("bullet", myRockSphere);
                bulletg.setMaterial(myRockMat);
                bulletg.setShadowMode(ShadowMode.CastAndReceive);
                bulletg.setLocalTranslation(cam.getLocation());
                
                SphereCollisionShape bulletCollisionShape = new SphereCollisionShape(0.4f);
                RigidBodyControl bulletNode = new LaunchableCollidingRigidBodyControl(assetManager, bulletCollisionShape, 1);
//                RigidBodyControl bulletNode = new RigidBodyControl(bulletCollisionShape, 1);
                bulletNode.setLinearVelocity(cam.getDirection().mult(25));
                bulletg.addControl(bulletNode);
                rootNode.attachChild(bulletg);
                getPhysicsSpace().add(bulletNode);
            }
            if (name.equals("gc") && !keyPressed) {
                System.gc();
            }
        }
    };

    public void initWall() {
        float startpt = bLength / 4;
        float height = 0;
        for (int j = 0; j < 15; j++) {
            for (int i = 0; i < 4; i++) {
                Vector3f vt = new Vector3f(i * bLength * 2 + startpt, bHeight + height, 0);
                addBrickToWall(vt);
            }
            startpt = -startpt;
            height += 2 * bHeight;
        }
    }

    public void initFloorBombWallBasic() {
        Box floorBox = new Box(Vector3f.ZERO, 10f, 0.1f, 5f);
        floorBox.scaleTextureCoordinates(new Vector2f(3, 6));

        Geometry floor = new Geometry("floor", floorBox);
        floor.setMaterial(myPondMat);
        floor.setShadowMode(ShadowMode.Receive);
        floor.setLocalTranslation(0, -0.1f, 0);
		RigidBodyControl rbc = new RigidBodyControl(new BoxCollisionShape(new Vector3f(10f, 0.1f, 5f)), 0);
		attachPhysicalObjToRoot(floor, rbc, true);
    }

    public void addBrickToWall(Vector3f ori) {

        Geometry reBoxg = new Geometry("brick", myBrickBox);
        reBoxg.setMaterial(myBrickMat);
        reBoxg.setShadowMode(ShadowMode.CastAndReceive);
		
        reBoxg.setLocalTranslation(ori);
		makePhysicalObjControlAndAttachToRoot(reBoxg, 1.5f, 0.6f);
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
