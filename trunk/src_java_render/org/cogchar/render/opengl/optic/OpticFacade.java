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
package org.cogchar.render.opengl.optic;

import com.jme3.asset.AssetManager;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class OpticFacade {
	private		MatFactory			myMatMgr;
	private		LightFactory		myLightFactory;
	private		CameraMgr			myCameraMgr;
	private		ViewFactory			myViewFactory;
	
	public OpticFacade (AssetManager assetMgr) {
		myMatMgr = new MatFactory(assetMgr);
		myLightFactory = new LightFactory();
		myCameraMgr = new CameraMgr();
	}
	public MatFactory getMatMgr() { 
		return myMatMgr;
	}
	public LightFactory getLightFactory() { 
		return myLightFactory;
	}
	public CameraMgr getCameraMgr() {
		return myCameraMgr;
	}
	
}
