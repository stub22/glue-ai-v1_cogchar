/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.render.sys.goody;

import org.cogchar.render.app.bony.BonyGameFeatureAdapter;
import org.cogchar.render.app.bony.BonyRenderContext;
import org.cogchar.render.goody.physical.ProjectileLauncher;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 *
 * @author Owner
 */
public class GoodyGameFeatureAdapter extends BonyGameFeatureAdapter {
	private		ProjectileLauncher			myPrjctlMgr;
	public GoodyGameFeatureAdapter(GoodyModularRenderContext gmrc) {
		super(gmrc);
	}
	public void cmdShoot() {
		RenderRegistryClient rrc = getRenderRegistyClient();
		myPrjctlMgr.cmdShoot(rrc);
	}
	public void cmdBiggerProjectile() { 
		myPrjctlMgr.cmdBiggerProjectile();
	}
	public void cmdSmallerProjectile() { 
		myPrjctlMgr.cmdSmallerProjectile();
	}
	
	@Override 	public void initFeatures() { 
		super.initFeatures();
		RenderRegistryClient rrc = getRenderRegistyClient();
		myPrjctlMgr = ProjectileLauncher.makeProjectileLauncher(rrc);
		myPrjctlMgr.initStuff();  // Can be done at any time in this startup seq						
	}
}
