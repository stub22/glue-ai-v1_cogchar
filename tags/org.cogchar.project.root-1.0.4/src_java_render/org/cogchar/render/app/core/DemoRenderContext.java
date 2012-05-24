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
package org.cogchar.render.app.core;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import org.cogchar.blob.emit.DemoConfigEmitter;
import org.cogchar.render.app.core.AppStub;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class DemoRenderContext extends PhysicalRenderContext {
	DemoConfigEmitter		myDCE;
	public DemoRenderContext(DemoConfigEmitter dce) {
		myDCE = dce;
	}
	public DemoRenderContext() { 
		this(new DemoConfigEmitter());
	}
	public DemoConfigEmitter getConfigEmiiter() { 
		return myDCE;
	}
	@Override public void completeInit() {
		super.completeInit();
		initDefaultGuiFonts();
	}
	protected void initDefaultGuiFonts() { 
		AssetManager amgr = findJme3AssetManager(null);
		BitmapFont defGuiFont = amgr.loadFont(myDCE.getFontPath());
		AppStub stub = getAppStub();
		stub.setGuiFont(defGuiFont);
	}	
	public void setupLight() { 
		addDemoDirLightToRootNode();
	}
	
}
