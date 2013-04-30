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

import org.cogchar.render.sys.physics.LaunchableCollidingRigidBodyControl;
import com.jme3.bullet.BulletAppState;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.shadow.PssmShadowRenderer.CompareMode;
import com.jme3.shadow.PssmShadowRenderer.FilterMode;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import org.cogchar.render.app.bony.BonyGameFeatureAdapter;
import org.cogchar.render.app.core.CogcharPresumedApp;

/**
 * From:    jme3test.bullet.TestBrickTower - author double1984 (tower mod by atom).
 * Shadows seem to work a little better than in DemoYouBombBrickWallWithShadows.
 * This one uses PssmShadowRenderer instead of BasicShadowRenderer.
 */
public class DemoYouBombBrickTowerWithPssmShadows extends BrickApp {

    int bricksPerLayer = 8;
    int brickLayers = 30;

    static float brickWidth = .75f, brickHeight = .25f, brickDepth = .25f;
    float radius = 3f;


    private Sphere					myRockSphere;
    private Box						myBrickBox;
    private SphereCollisionShape	myRockSphereColShape;

    public static void main(String args[]) {
        DemoYouBombBrickTowerWithPssmShadows f = new DemoYouBombBrickTowerWithPssmShadows();
        f.start();
    }

    @Override public void simpleInitApp() {
		
		super.simpleInitApp();

        myPhysAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
     //   bulletAppState.setEnabled(false);
        stateManager.attach(myPhysAppState);
        myRockSphere = new Sphere(32, 32, 0.4f, true, false);
        myRockSphere.setTextureMode(TextureMode.Projected);
        myRockSphereColShape = new SphereCollisionShape(0.4f);

        myBrickBox = new Box(Vector3f.ZERO, brickWidth, brickHeight, brickDepth);
        myBrickBox.scaleTextureCoordinates(new Vector2f(1f, .5f));
        //bulletAppState.getPhysicsSpace().enableDebug(assetManager);

        initTower();
        initFloorBombTowerPssm();
		BonyGameFeatureAdapter.initCrossHairs(settings, getRenderContext().getRenderRegistryClient());
        this.cam.setLocation(new Vector3f(0, 25f, 8f));
        cam.lookAt(Vector3f.ZERO, new Vector3f(0, 1, 0));
        cam.setFrustumFar(80);
        inputManager.addMapping("shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener, "shoot");
		
        initPssmShadowRenderer();
    }

    private ActionListener actionListener = new ActionListener() {

        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("shoot") && !keyPressed) {
                Geometry bulletg = new Geometry("bullet", myRockSphere);
                bulletg.setMaterial(myRockMat);
                bulletg.setShadowMode(ShadowMode.CastAndReceive);
                bulletg.setLocalTranslation(cam.getLocation());
                RigidBodyControl bulletNode = new LaunchableCollidingRigidBodyControl(assetManager, myRockSphereColShape, 1);
//                RigidBodyControl bulletNode = new RigidBodyControl(bulletCollisionShape, 1);
                bulletNode.setLinearVelocity(cam.getDirection().mult(25));
                bulletg.addControl(bulletNode);
                rootNode.attachChild(bulletg);
                getPhysicsSpace().add(bulletNode);
            }
        }
    };

    public void initTower() {
		float brickMass = 1.5f;
		float brickFrict = 1.6f;
		
        double tempX = 0;
        double tempY = 0;
        double tempZ = 0;
        float brickAngle = 0f;
        for (int i = 0; i < brickLayers; i++){
            // Increment rows
            if(i!=0)
                tempY+=brickHeight*2;
            else
                tempY=brickHeight;
            // Alternate brick seams
            brickAngle = 360.0f / bricksPerLayer * i/2f;
            for (int j = 0; j < bricksPerLayer; j++){
              tempZ = Math.cos(Math.toRadians(brickAngle))*radius;
              tempX = Math.sin(Math.toRadians(brickAngle))*radius;
              System.out.println("x="+((float)(tempX))+" y="+((float)(tempY))+" z="+(float)(tempZ));
              Vector3f vt = new Vector3f((float)(tempX), (float)(tempY), (float)(tempZ));
              // Add crenelation
              if (i==brickLayers-1){
                if (j%2 == 0){
                    addBrickToTower(vt, brickAngle, brickMass, brickFrict);
                }
              }
              // Create main tower
              else {
                addBrickToTower(vt, brickAngle, brickMass, brickFrict);
              }
              brickAngle += 360.0/bricksPerLayer;
            }
          }

    }
    protected void initFloorBombTowerPssm() {
        Box floorBox = new Box(Vector3f.ZERO, 10f, 0.1f, 5f);
        floorBox.scaleTextureCoordinates(new Vector2f(3, 6));
        Geometry floor = new Geometry("floor", floorBox);
        floor.setMaterial(myPondMat);
        floor.setShadowMode(ShadowMode.Receive);
        floor.setLocalTranslation(0, 0, 0);
		makePhysicalObjControlAndAttachToRoot(floor, 0f, null);
    }


    protected void addBrickToTower(Vector3f localTrans, float brickAngle, float mass, float friction) {
        Geometry reBoxg = new Geometry("brick", myBrickBox);
        reBoxg.setMaterial(myBrickMat);
        reBoxg.setShadowMode(ShadowMode.CastAndReceive);
		
		reBoxg.setLocalTranslation(localTrans);
        reBoxg.rotate(0f, (float)Math.toRadians(brickAngle) , 0f );
        reBoxg.addControl(new RigidBodyControl(mass));

        reBoxg.getControl(RigidBodyControl.class).setFriction(friction);
		attachPhysicalObjToRoot(reBoxg);
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
