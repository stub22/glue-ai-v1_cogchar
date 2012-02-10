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
import com.jme3.scene.Node;
import org.cogchar.render.opengl.mesh.MeshFactoryFacade;
import org.cogchar.render.opengl.optic.OpticFacade;
import org.cogchar.render.opengl.scene.SceneFacade;

/**  Named to differentiate it from JMonkey "RenderContext".  
 * @author Stu B. <www.texpedient.com>
 */
public class CogcharRenderContext {
	private		AssetContext					myAssetContext;
	private		MeshFactoryFacade				myMeshFactoryFacade;
	private		OpticFacade						myOpticFacade;
	private		SceneFacade						mySceneFacade;
	
	
	public CogcharRenderContext() {
	}
	/**
	 * Typically called from simpleInitApp, after JME3.SimpleApp ingredients are fully available to use as args here.
	 * @param assetMgr
	 * @param rootNode
	 * @param guiNode 
	 */
	public void initJMonkeyStuff(AssetManager assetMgr, Node rootNode, Node guiNode) { 
		
		myAssetContext = new AssetContext(assetMgr);
		myMeshFactoryFacade = new MeshFactoryFacade();
		myOpticFacade = new OpticFacade(assetMgr);
		mySceneFacade = new SceneFacade(assetMgr, myOpticFacade, rootNode, guiNode);
		
		myAssetContext.resolveAndRegisterAllAssetSources();
	}
	
	
	public MeshFactoryFacade  getMeshFF() {
		return myMeshFactoryFacade;
	}
	public OpticFacade getOpticFacade() { 
		return myOpticFacade;
	}
	public SceneFacade getSceneFacade() { 
		return mySceneFacade;
	}
}
