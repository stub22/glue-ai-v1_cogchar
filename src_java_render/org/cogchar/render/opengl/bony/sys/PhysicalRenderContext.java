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
package org.cogchar.render.opengl.bony.sys;

import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.scene.Node;
import org.cogchar.render.opengl.bony.world.PhysicsStuffBuilder;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PhysicalRenderContext extends ModularRenderContext {
	/**
	 * 
	 * TODO:  Register the PhysicsSpace and PhysicsStuff-builder in RenderRegistry,
	 * instead of holding these instance variables.
	 */
	
	private		BulletAppState			myBulletAS;
	private		PhysicsStuffBuilder		myPSB;
	
	@Override public void completeInit() {
		super.completeInit();
		
		myBulletAS = new BulletAppState();
        myBulletAS.setEnabled(true);
		AppStateManager appStateMgr = findJme3AppStateManager(null);
        appStateMgr.attach(myBulletAS);		
		PhysicsSpace ps = myBulletAS.getPhysicsSpace();
		// TODO: Check config for initial debug setting
		AssetManager assetMgr = findJme3AssetManager(null);
		ps.enableDebug(assetMgr);
		
		Node rootNode = findJme3RootDeepNode(null);

		myPSB =  new PhysicsStuffBuilder(this, ps, rootNode);		
		
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
	public void initBasicTestPhysics() {
		myPSB.createPhysicsTestWorld();
	}	
	protected void initSoccerTestPhysics() {
		myPSB.createPhysicsTestWorldSoccer();
	}		
}
