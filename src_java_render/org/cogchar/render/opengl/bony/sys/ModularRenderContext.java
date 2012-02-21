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

import org.appdapter.api.module.Module;
import org.cogchar.render.opengl.bony.model.CogcharRenderModulator;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class ModularRenderContext extends CogcharRenderContext {
	private		CogcharRenderModulator			myRenderModulator;
	
	@Override public void completeInit() {
		super.completeInit();
		logInfo("init CogcharRenderModulator");		
		myRenderModulator = new CogcharRenderModulator();
	}
	
	public void attachModule(Module<CogcharRenderModulator> m) { 
		myRenderModulator.attachModule(m);
	}
	public void detachModule(Module<CogcharRenderModulator> m) { 
		myRenderModulator.detachModule(m);
	}
	protected CogcharRenderModulator getModulator() { 
		return myRenderModulator;
	}
	@Override public void doUpdate(float tpf) {
		myRenderModulator.runOneCycle(tpf);
	}
}
