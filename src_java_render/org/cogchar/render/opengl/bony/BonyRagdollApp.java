/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.render.opengl.bony;
import com.jme3.bullet.BulletAppState;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BonyRagdollApp extends BonyStickFigureApp {
	private	DemoBonyWireframeRagdoll	myRagdoll;
	
	public BonyRagdollApp(String sceneFilePath, float sceneScale) {
		super (sceneFilePath, sceneScale);
		myRagdoll = new DemoBonyWireframeRagdoll();
	}
	@Override public void simpleInitApp() {
		super.simpleInitApp();
		BulletAppState bulletAppState = DemoBonyWireframeRagdoll.makePhysicsAppState(stateManager, assetManager, rootNode);
		myRagdoll.realizeDollAndAttach(rootNode, bulletAppState);
		myRagdoll.registerTraditionalInputHandlers(inputManager);
	}
	@Override public void simpleUpdate(float tpf) {
		super.simpleUpdate(tpf);
		myRagdoll.doSimpleUpdate(tpf);
	}
}
