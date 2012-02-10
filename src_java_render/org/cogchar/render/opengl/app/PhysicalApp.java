/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.render.opengl.app;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import org.cogchar.blob.emit.DemoConfigEmitter;
import org.cogchar.render.opengl.bony.world.PhysicsStuffBuilder;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * 		// Comment in PhysicsSpace says to setGravity "before creating physics objects".
		// ps.setGravity(Vector3f.ZERO);
		// Turn on the blue wireframe collision bounds.
		// ps.enableDebug(asstMgr);
 */
public class PhysicalApp extends DemoApp {
	
	private		BulletAppState			myBulletAS;
	private		PhysicsStuffBuilder		myPSB;
	
	public PhysicalApp(DemoConfigEmitter bce) {
		super(bce);
	}
	@Override public void simpleInitApp() {
		super.simpleInitApp();
		myBulletAS = new BulletAppState();
        myBulletAS.setEnabled(true);
        stateManager.attach(myBulletAS);		
		PhysicsSpace ps = myBulletAS.getPhysicsSpace();
		// TODO: Check config for initial debug setting
		ps.enableDebug(assetManager);

		myPSB =  new PhysicsStuffBuilder(getRenderContext(), ps, rootNode);
	}
	protected BulletAppState getBulletAppState() { 
		return myBulletAS;
	}
    protected PhysicsSpace getPhysicsSpace() {
        return myBulletAS.getPhysicsSpace();
    }	
	protected PhysicsStuffBuilder getPhysicsStuffBuilder() { 
		return myPSB;
	}
	protected void initBasicTestPhysics() {
		myPSB.createPhysicsTestWorld();
	}	
	protected void initSoccerTestPhysics() {
		myPSB.createPhysicsTestWorldSoccer();
	}	
}
