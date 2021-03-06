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

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.SkyFactory;
import com.jme3.water.SimpleWaterProcessor;

/**
 *
 * @author normenhansen
 */
public class DemoSimpleWater extends SimpleApplication implements ActionListener {

    Material mat;
    Spatial waterPlane;
    Geometry lightSphere;
    SimpleWaterProcessor waterProcessor;
    Node sceneNode;
    boolean useWater = true;
    private Vector3f lightPos =  new Vector3f(33,12,-29);


    public static void main(String[] args) {
        DemoSimpleWater app = new DemoSimpleWater();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        initInput();
        initScene();

        //create processor
        waterProcessor = new SimpleWaterProcessor(assetManager);
        waterProcessor.setReflectionScene(sceneNode);
        waterProcessor.setDebug(true);
        viewPort.addProcessor(waterProcessor);

        waterProcessor.setLightPosition(lightPos);

        //create water quad
        //waterPlane = waterProcessor.createWaterGeometry(100, 100);
        waterPlane=(Spatial)  assetManager.loadAsset("Models/WaterTest/WaterTest.mesh.xml");
        waterPlane.setMaterial(waterProcessor.getMaterial());
        waterPlane.setLocalScale(40);
        waterPlane.setLocalTranslation(-5, 0, 5);

        rootNode.attachChild(waterPlane);
    }

    private void initScene() {
        //init cam location
        cam.setLocation(new Vector3f(0, 10, 10));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        //init scene
        sceneNode = new Node("Scene");
        mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", assetManager.loadTexture("Interface/Logo/Monkey.jpg"));
        Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("Box", b);
        geom.setMaterial(mat);
        sceneNode.attachChild(geom);

        // load sky
        sceneNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false));
        rootNode.attachChild(sceneNode);

        //add lightPos Geometry
        Sphere lite=new Sphere(8, 8, 3.0f);
        lightSphere=new Geometry("lightsphere", lite);
        lightSphere.setMaterial(mat);
        lightSphere.setLocalTranslation(lightPos);
        rootNode.attachChild(lightSphere);
    }

    protected void initInput() {
        flyCam.setMoveSpeed(3);
        //init input
        inputManager.addMapping("use_water", new KeyTrigger(KeyInput.KEY_O));
        inputManager.addListener(this, "use_water");
        inputManager.addMapping("lightup", new KeyTrigger(KeyInput.KEY_T));
        inputManager.addListener(this, "lightup");
        inputManager.addMapping("lightdown", new KeyTrigger(KeyInput.KEY_G));
        inputManager.addListener(this, "lightdown");
        inputManager.addMapping("lightleft", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addListener(this, "lightleft");
        inputManager.addMapping("lightright", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addListener(this, "lightright");
        inputManager.addMapping("lightforward", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addListener(this, "lightforward");
        inputManager.addMapping("lightback", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addListener(this, "lightback");
    }

    @Override
    public void simpleUpdate(float tpf) {
        fpsText.setText("Light Position: "+lightPos.toString()+" Change Light position with [U], [H], [J], [K] and [T], [G] Turn off water with [O]");
        lightSphere.setLocalTranslation(lightPos);
        waterProcessor.setLightPosition(lightPos);
    }

    public void onAction(String name, boolean value, float tpf) {
        if (name.equals("use_water") && value) {
            if (!useWater) {
                useWater = true;
                waterPlane.setMaterial(waterProcessor.getMaterial());
            } else {
                useWater = false;
                waterPlane.setMaterial(mat);
            }
        } else if (name.equals("lightup") && value) {
            lightPos.y++;
        } else if (name.equals("lightdown") && value) {
            lightPos.y--;
        } else if (name.equals("lightleft") && value) {
            lightPos.x--;
        } else if (name.equals("lightright") && value) {
            lightPos.x++;
        } else if (name.equals("lightforward") && value) {
            lightPos.z--;
        } else if (name.equals("lightback") && value) {
            lightPos.z++;
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
