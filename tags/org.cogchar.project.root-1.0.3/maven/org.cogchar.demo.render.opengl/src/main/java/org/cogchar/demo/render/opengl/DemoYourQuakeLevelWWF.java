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

import com.jme3.bullet.BulletAppState;
import com.jme3.asset.plugins.HttpZipLocator;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.PhysicsCharacter;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.MaterialList;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.plugins.ogre.OgreMeshKey;
import java.io.File;
import org.cogchar.render.opengl.app.DemoApp;

public class DemoYourQuakeLevelWWF extends UnfinishedDemoApp implements ActionListener {
	static String LOCAL_SCENE_PATH = "jme_asset_zips/quake3level.zip";
	static String HTTP_SCENE_PATH = "http://jmonkeyengine.googlecode.com/files/quake3level.zip";
	
    private BulletAppState bulletAppState;
    private Node gameLevel;
    private PhysicsCharacter player;
    private Vector3f walkDirection = new Vector3f();
    private static boolean useHttp = false;
    private boolean left=false,right=false,up=false,down=false;

    public static void main(String[] args) {
        File file = new File(LOCAL_SCENE_PATH);
        if (!file.exists()) {
            useHttp = true;
        }
        DemoYourQuakeLevelWWF app = new DemoYourQuakeLevelWWF();
        app.start();
    }

    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        flyCam.setMoveSpeed(100);
        setupKeys();

        this.cam.setFrustumFar(2000);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White.clone().multLocal(2));
        dl.setDirection(new Vector3f(-1, -1, -1).normalize());
        rootNode.addLight(dl);

        AmbientLight am = new AmbientLight();
        am.setColor(ColorRGBA.White.mult(2));
        rootNode.addLight(am);
		
        if (useHttp) {
            assetManager.registerLocator(HTTP_SCENE_PATH, HttpZipLocator.class.getName());
        } else {
            assetManager.registerLocator(LOCAL_SCENE_PATH, ZipLocator.class.getName());
        }		


        // create the geometry and attach it
        MaterialList matList = (MaterialList) assetManager.loadAsset("Scene.material");
        OgreMeshKey key = new OgreMeshKey("main.meshxml", matList);
        gameLevel = (Node) assetManager.loadAsset(key);
        gameLevel.setLocalScale(0.1f);

        // add a physics control, it will generate a MeshCollisionShape based on the gameLevel
        gameLevel.addControl(new RigidBodyControl(0));

        player = new PhysicsCharacter(new SphereCollisionShape(5), .01f);
        player.setJumpSpeed(20);
        player.setFallSpeed(30);
        player.setGravity(30);

        player.setPhysicsLocation(new Vector3f(60, 10, -60));

        rootNode.attachChild(gameLevel);

        getPhysicsSpace().addAll(gameLevel);
        getPhysicsSpace().add(player);
    }

    private PhysicsSpace getPhysicsSpace(){
        return bulletAppState.getPhysicsSpace();
    }

    @Override
    public void simpleUpdate(float tpf) {
        Vector3f camDir = cam.getDirection().clone().multLocal(0.6f);
        Vector3f camLeft = cam.getLeft().clone().multLocal(0.4f);
        walkDirection.set(0,0,0);
        if(left)
            walkDirection.addLocal(camLeft);
        if(right)
            walkDirection.addLocal(camLeft.negate());
        if(up)
            walkDirection.addLocal(camDir);
        if(down)
            walkDirection.addLocal(camDir.negate());
        player.setWalkDirection(walkDirection);
        cam.setLocation(player.getPhysicsLocation());
    }

    private void setupKeys() {
        inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this,"Lefts");
        inputManager.addListener(this,"Rights");
        inputManager.addListener(this,"Ups");
        inputManager.addListener(this,"Downs");
        inputManager.addListener(this,"Space");
    }

    public void onAction(String binding, boolean value, float tpf) {

        if (binding.equals("Lefts")) {
            if(value)
                left=true;
            else
                left=false;
        } else if (binding.equals("Rights")) {
            if(value)
                right=true;
            else
                right=false;
        } else if (binding.equals("Ups")) {
            if(value)
                up=true;
            else
                up=false;
        } else if (binding.equals("Downs")) {
            if(value)
                down=true;
            else
                down=false;
        } else if (binding.equals("Space")) {
            player.jump();
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
