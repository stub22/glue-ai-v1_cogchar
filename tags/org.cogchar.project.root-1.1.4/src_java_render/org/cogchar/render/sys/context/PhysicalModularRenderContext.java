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
package org.cogchar.render.sys.context;

import org.cogchar.render.sys.module.ModularRenderContext;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.scene.Node;
import org.cogchar.render.sys.physics.PhysicsStuffBuilder;
import org.cogchar.render.sys.registry.RenderRegistryClient;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class PhysicalModularRenderContext extends ModularRenderContext {
	/**
	 * 
	 * TODO:  Register the PhysicsStuff-builder in RenderRegistry,
	 * instead of holding these instance variables.
	 */
	
	// private		BulletAppState			myBulletAS;
	protected		PhysicsStuffBuilder		myPSB;

	public PhysicalModularRenderContext(RenderRegistryClient rrc) {
		super(rrc);
	}
	@Override public void completeInit() {
		super.completeInit();
		CoreFeatureAdapter.unrolledInitPRC(this);
	}
	protected void initBulletAppState() { 
		RenderRegistryClient rrc = getRenderRegistryClient();
		BulletAppState bas = new BulletAppState();
        bas.setEnabled(true);
		AppStateManager appStateMgr = rrc.getJme3AppStateManager(null);
        appStateMgr.attach(bas);
		
		rrc.putJme3BulletAppState(bas, null);
	}
	public void enablePhysicsDebug() {
		PhysicsSpace ps = getPhysicsSpace();
		AssetManager assetMgr = getRenderRegistryClient().getJme3AssetManager(null);
		ps.enableDebug(assetMgr);
	}
	public void disablePhysicsDebug() {
		PhysicsSpace ps = getPhysicsSpace();
		ps.disableDebug();
	}
	
	// public void buildPhysicsStuff() { }
	protected BulletAppState getBulletAppState() { 
		return getRenderRegistryClient().getJme3BulletAppState(null);
	}
    public PhysicsSpace getPhysicsSpace() {
        return getBulletAppState().getPhysicsSpace();
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
