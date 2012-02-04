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
package org.cogchar.render.opengl.scene;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import org.cogchar.render.opengl.optic.OpticFacade;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class SceneFacade {
	private		FlatOverlayMgr			myFlatOverlayMgr;
	private		DeepSceneMgr			myDeepSceneMgr;
	// TODO:   Replace instance variable with registry lookup.
	private		OpticFacade				myOpticFacade;
	private		GeomFactory				myGeomFactory;
	private		ModelSpatialFactory		myModelML;	
	private		TextMgr					myTextMgr;
	
	public SceneFacade (AssetManager assetMgr, OpticFacade opticFacade, Node mainParentNode, Node flatOverlayParentNode) {
		myOpticFacade = opticFacade;
		myDeepSceneMgr = new DeepSceneMgr(mainParentNode);
		myFlatOverlayMgr = new FlatOverlayMgr(flatOverlayParentNode);
		myGeomFactory = new GeomFactory(myOpticFacade.getMatMgr());
		myModelML = new ModelSpatialFactory(assetMgr);
		myTextMgr = new TextMgr(assetMgr);
	}
	// TODO:   This should go through Registry instead of instance variable.
	protected OpticFacade getOpticFacade() {
		return myOpticFacade;
	}
	public DeepSceneMgr getDeepSceneMgr() { 
		return myDeepSceneMgr;
	}
	public FlatOverlayMgr getFlatOverlayMgr() { 
		return myFlatOverlayMgr;
	}
	public GeomFactory getGeomFactory() { 
		return myGeomFactory;
	}
	public ModelSpatialFactory getModelML() {
		return myModelML;
	}
	public TextMgr getTextMgr() { 
		return myTextMgr;
	}
}
