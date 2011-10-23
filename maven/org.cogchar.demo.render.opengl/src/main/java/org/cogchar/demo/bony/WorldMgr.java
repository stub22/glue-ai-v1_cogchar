/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.demo.bony;

import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import org.cogchar.demo.render.opengl.PhysicsStuffBuilder;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class WorldMgr {
	private BulletAppState myPhysicsAppState;
	protected void initPhysAppStuff(AssetManager asstMgr, AppStateManager stateMgr,
					Node parentNode) { 
		myPhysicsAppState = new BulletAppState();
		myPhysicsAppState.setEnabled(true);
		stateMgr.attach(myPhysicsAppState);	
		PhysicsSpace ps = getPhysicsSpace();
		// Turn on the blue wireframe collision bounds.
		ps.enableDebug(asstMgr);
		PhysicsStuffBuilder.createPhysicsTestWorld(parentNode, asstMgr, ps);
	
	}
	protected PhysicsSpace getPhysicsSpace() {
		return myPhysicsAppState.getPhysicsSpace();
	}	
	public static DirectionalLight makeDirectionalLight() {

        //   rootNode.addLight(al);

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.1f, -0.7f, -1).normalizeLocal());
        dl.setColor(new ColorRGBA(1f, 1f, 1f, 1.0f));
        return dl;
    }
	public static AmbientLight makeAmbientLight() {
		AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1));
        return al;
	}
	protected static void makeCrossHairs(AssetManager asstMgr, Node parentNode,
				BitmapFont font, 	AppSettings settings) {
		
		BitmapText ch = new BitmapText(font, false);
		ch.setSize(font.getCharSet().getRenderedSize() * 2);
		ch.setText("+"); // crosshairs
		ch.setLocalTranslation( // center
				settings.getWidth() / 2 - font.getCharSet().getRenderedSize() / 3 * 2,
				settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
		parentNode.attachChild(ch);
	}
	
}
