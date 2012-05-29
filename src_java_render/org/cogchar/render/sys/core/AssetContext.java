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
package org.cogchar.render.sys.core;

import com.jme3.asset.AssetManager;
import java.util.ArrayList;
import java.util.List;
import org.cogchar.render.opengl.mesh.DebugMeshLoader;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class AssetContext extends RenderRegistryAware {
	private		AssetManager					myAssetManager;
	private		List<JmonkeyAssetLocation>		myAssetSources = new ArrayList<JmonkeyAssetLocation>();
	
	public AssetContext(){ 
		JmonkeyAssetLocation frameJAL = new JmonkeyAssetLocation(AssetManager.class);
		addAssetSource(frameJAL);
	}
	public void setAssetManager(AssetManager assetMgr) { 
		myAssetManager = assetMgr;
	}
	public AssetManager getAssetManager() { 
		if (myAssetManager == null) {
			myAssetManager = findJme3AssetManager(null);
		}
		return myAssetManager;
	}
	public void addAssetSource(JmonkeyAssetLocation jmal) {
		myAssetSources.add(jmal);
/*		// We cannot do:		
 * 		AssetManager am = getAssetManager();
		if (am != null) {
			jmal.resolve();
			jmal.ensurerLocatorsReged(am);
		}
 * Because:		
 * OSGi bundle permissions problem
     [java] 	at org.appdapter.osgi.registry.RegistryServiceFuncs.getTheWellKnownRegistry(RegistryServiceFuncs.java:150)
     [java] 	at org.cogchar.blob.emit.RegistryClient.getVerySimpleRegistry(RegistryClient.scala:44)
     [java] 	at org.cogchar.blob.emit.RegistryClient.getRequiredOverRegistry(RegistryClient.scala:48)
     [java] 	at org.cogchar.blob.emit.RegistryClient.findOrMakeSubsystemFacadeRegistry(RegistryClient.scala:62)
     [java] 	at org.cogchar.blob.emit.RegistryClient.findExternalFacade(RegistryClient.scala:90)
     [java] 	at org.cogchar.render.sys.core.RenderRegistryFuncs.findExternalFacadeOrNull(RenderRegistryFuncs.java:183)
     [java] 	at org.cogchar.render.sys.core.RenderRegistryFuncs.findJme3AssetManager(RenderRegistryFuncs.java:198)
     [java] 	at org.cogchar.render.sys.core.AssetContext.getAssetManager(AssetContext.java:52)
     [java] 	at org.cogchar.render.sys.core.AssetContext.addAssetSource(AssetContext.java:58)* 
 */		
	}
	
	public void ensureAllSourcesReged() { 
		getLogger().info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX  resolveAndRegisterAllAssetSources()");
		AssetManager assetMgr = getAssetManager();
		// Optionally add a bonyAssetLocator here for debugging.
		for (JmonkeyAssetLocation jmal : myAssetSources) {
			jmal.resolve();
			jmal.ensurerLocatorsReged(assetMgr);
		}
		// DebugMeshLoader helps with debugging.		
		assetMgr.registerLoader(DebugMeshLoader.class, "meshxml", "mesh.xml");
		
	}
}
