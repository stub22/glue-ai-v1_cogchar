/*

 */

package org.cogchar.demo.render.opengl.jme3;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.Bone;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.debug.SkeletonDebugger;

/**
 *
 * 
 */

public class DemoEarth extends SimpleApplication {

    private AnimControl control;
    private float angle = 0;
    private float scale = 1;
    private float rate = 1;

    public static void main(String[] args) {
        DemoEarth app = new DemoEarth();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setMoveSpeed(10f);
        cam.setLocation(new Vector3f(6.4013605f, 7.488437f, 12.843031f));
        cam.setRotation(new Quaternion(-0.060740203f, 0.93925786f, -0.2398315f, -0.2378785f));

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.1f, -0.7f, -1).normalizeLocal());
        dl.setColor(new ColorRGBA(1f, 1f, 1f, 1.0f));
        rootNode.addLight(dl);
		
		
        PointLight pl = new PointLight();
        pl.setColor(new ColorRGBA(1, 0.9f, 0.9f, 0));
        pl.setPosition(new Vector3f(0f, 0f, 20f));
        rootNode.addLight(pl);
		
		Node earthModel = (Node) assetManager.loadModel("ogre_earth_mapping/Earth.mesh.xml");
		System.out.println("Got earthModel: " + earthModel);
		// earthModel.setMaterial(assetManager.loadMaterial("Common/Materials/RedColor.j3m"));
		
		// Status 2012-11-15:  The model loads and displays, sorta, but the material + texture is not working right.
		rootNode.attachChild(earthModel);
    }

  

}
