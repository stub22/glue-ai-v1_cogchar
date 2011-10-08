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
 */

package org.cogchar.render.opengl.bony.app;
import com.jme3.bullet.BulletAppState;
import org.cogchar.render.opengl.bony.model.DemoBonyWireframeRagdoll;

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
	public DemoBonyWireframeRagdoll getRagdoll() {
		return myRagdoll;
	}
}
