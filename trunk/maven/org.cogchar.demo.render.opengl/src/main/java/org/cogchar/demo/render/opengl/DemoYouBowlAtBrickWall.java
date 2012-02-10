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

import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
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
import com.jme3.shadow.BasicShadowRenderer;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import org.cogchar.render.opengl.app.DemoApp;
import org.cogchar.render.opengl.optic.MatFactory;

/**
 * From:   jme3test.helloworld.HelloPhysics
 * Example 12 - how to give objects physical properties so they bounce and fall.
 */
public class DemoYouBowlAtBrickWall extends DemoApp {

  public static void main(String args[]) {
    DemoYouBowlAtBrickWall app = new DemoYouBowlAtBrickWall();
    app.start();
  }

  /** Prepare the Physics Application State (jBullet) */
  private BulletAppState bulletAppState;

  /** Activate custom rendering of shadows */
  BasicShadowRenderer bsr;

  /** Prepare Materials */
  Material wall_mat;
  Material stone_mat;
  Material floor_mat;

  /** Prepare geometries and physical nodes for bricks and cannon balls. */
  private RigidBodyControl    brick_phy;
  private static final Box    box;
  private RigidBodyControl    ball_phy;
  private static final Sphere sphere;
  private RigidBodyControl    floor_phy;
  private static final Box    floor;
  
  /** dimensions used for bricks and wall */
  private static final float brickLength = 0.48f;
  private static final float brickWidth  = 0.24f;
  private static final float brickHeight = 0.12f;

  static {
    /** Initialize the cannon ball geometry */
    sphere = new Sphere(32, 32, 0.4f, true, false);
    sphere.setTextureMode(TextureMode.Projected);
    /** Initialize the brick geometry */
    box = new Box(Vector3f.ZERO, brickLength, brickHeight, brickWidth);
    box.scaleTextureCoordinates(new Vector2f(1f, .5f));
    /** Initialize the floor geometry */
    floor = new Box(Vector3f.ZERO, 10f, 0.1f, 5f);
    floor.scaleTextureCoordinates(new Vector2f(3, 6));
  }

  @Override
  public void simpleInitApp() {

	  super.simpleInitApp();
    /** Set up Physics Game */
    bulletAppState = new BulletAppState();
    stateManager.attach(bulletAppState);
    /** Configure cam to look at scene */
    cam.setLocation(new Vector3f(0, 6f, 6f));
    cam.lookAt(Vector3f.ZERO, new Vector3f(0, 1, 0));
    cam.setFrustumFar(15);
    /** Add InputManager action: Left click triggers shooting. */
    inputManager.addMapping("shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
    inputManager.addListener(actionListener, "shoot");
    /** Initialize the scene, materials, and physics space */
    initMaterials();
    initWall();
    initFloor();
    initCrossHairs();
  }

  /**
   * Every time the shoot action is triggered, a new cannon ball is produced.
   * The ball is set up to fly from the camera position in the camera direction.
   */
  private ActionListener actionListener = new ActionListener() {
    public void onAction(String name, boolean keyPressed, float tpf) {
      if (name.equals("shoot") && !keyPressed) {
        makeCannonBall();
      }
    }
  };

  /** Initialize the materials used in this scene. */
  public void initMaterials() {
	MatFactory mf = getMatMgr();
	
    wall_mat = mf.getBrickWallMat();
    stone_mat = mf.makeRockMat();
    floor_mat = mf.getPondMat();
	
  }

  /** Make a solid floor and add it to the scene. */
  public void initFloor() {
    Geometry floor_geo = new Geometry("Floor", floor);
    floor_geo.setMaterial(floor_mat);
    floor_geo.setShadowMode(ShadowMode.Receive);
    floor_geo.setLocalTranslation(0, -0.1f, 0);
    this.rootNode.attachChild(floor_geo);
    /* Make the floor physical with mass 0.0f! */
    floor_phy = new RigidBodyControl(0.0f);
    floor_geo.addControl(floor_phy);
    bulletAppState.getPhysicsSpace().add(floor_phy);
  }

  /** This loop builds a wall out of individual bricks. */
  public void initWall() {
    float startpt = brickLength / 4;
    float height = 0;
    for (int j = 0; j < 15; j++) {
      for (int i = 0; i < 4; i++) {
        Vector3f vt =
         new Vector3f(i * brickLength * 2 + startpt, brickHeight + height, 0);
        makeBrick(vt);
      }
      startpt = -startpt;
      height += 2 * brickHeight;
    }
  }


  /** This method creates one individual physical brick. */
  public void makeBrick(Vector3f loc) {
    /** Create a brick geometry and attach to scene graph. */
    Geometry brick_geo = new Geometry("brick", box);
    brick_geo.setMaterial(wall_mat);
    rootNode.attachChild(brick_geo);
    /** Position the brick geometry and activate shadows */
    brick_geo.setLocalTranslation(loc);
    brick_geo.setShadowMode(ShadowMode.CastAndReceive);
    /** Make brick physical with a mass > 0.0f. */
    brick_phy = new RigidBodyControl(2f);
    /** Add physical brick to physics space. */
    brick_geo.addControl(brick_phy);
    bulletAppState.getPhysicsSpace().add(brick_phy);
  }

  /** This method creates one individual physical cannon ball.
   * By defaul, the ball is accelerated and flies
   * from the camera position in the camera direction.*/
   public void makeCannonBall() {
    /** Create a cannon ball geometry and attach to scene graph. */
    Geometry ball_geo = new Geometry("cannon ball", sphere);
    ball_geo.setMaterial(stone_mat);
    rootNode.attachChild(ball_geo);
    /** Position the cannon ball and activate shadows */
    ball_geo.setLocalTranslation(cam.getLocation());
    ball_geo.setShadowMode(ShadowMode.CastAndReceive);
    /** Make the ball physcial with a mass > 0.0f */
    ball_phy = new RigidBodyControl(1f);
    /** Add physical ball to physics space. */
    ball_geo.addControl(ball_phy);
    bulletAppState.getPhysicsSpace().add(ball_phy);
    /** Accelerate the physcial ball to shoot it. */
    ball_phy.setLinearVelocity(cam.getDirection().mult(25));
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
