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

import com.jme3.asset.AssetManager;
import java.util.ArrayList;
import java.util.List;
import org.cogchar.render.opengl.mesh.DebugMeshLoader;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class AssetContext {
	private		AssetManager					myAssetManager;
	private		List<JmonkeyAssetLocation>		myAssetSources = new ArrayList<JmonkeyAssetLocation>();
	
	public AssetContext(AssetManager assetMgr){ 
		JmonkeyAssetLocation frameJAL = new JmonkeyAssetLocation(AssetManager.class);
		addAssetSource(frameJAL);
		myAssetManager = assetMgr;
	}
	public void addAssetSource(JmonkeyAssetLocation jmal) {
		myAssetSources.add(jmal);
	}
	public void resolveAndRegisterAllAssetSources() { 
		// Optionally add a bonyAssetLocator here for debugging.
		for (JmonkeyAssetLocation jmal : myAssetSources) {
			jmal.resolve();
			jmal.registerLocators(myAssetManager);
		}
		// DebugMeshLoader helps with debugging.		
		myAssetManager.registerLoader(DebugMeshLoader.class, "meshxml", "mesh.xml");
		
	}
}
