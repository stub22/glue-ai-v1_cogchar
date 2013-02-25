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
package org.cogchar.render.sys.module;

import org.cogchar.render.sys.context.CogcharRenderContext;
import org.appdapter.api.module.Module;
import org.cogchar.render.goody.basic.DataballGoodyBuilder;
import org.cogchar.render.sys.context.CogcharRenderContext;

/**
 * @author Stu B. <www.texpedient.com>
 * @author rbiggs
 * Stu 2013-02-24: The ball-builder stuff probably doesn't belong here.
 */
public class ModularRenderContext extends CogcharRenderContext {
	private		CogcharRenderModulator			myRenderModulator;
	private		DataballGoodyBuilder						myBallBuilder;
	private		boolean							thisBallBuilderSet;
	
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
		if (thisBallBuilderSet) {myBallBuilder.applyUpdates(tpf);} // tpf is passed to BallBuilder only for debugging now; it may get used more broadly eventally or may be removed from the method
		myRenderModulator.runOneCycle(tpf);
	}
	
	// Adding a method to manage locally stored BallBuilder instance. We could get it using BallBuilder.getBallBuilder
	// each time we need it, but that's once per update cycle. 
	public void setTheBallBuilder(DataballGoodyBuilder theBallBuilder) {
		myBallBuilder = theBallBuilder;
		if (myBallBuilder != null) {
			thisBallBuilderSet = true; // In theory, this variable allows a fast boolean check in doUpdate instead of having to check for null each update
		}
	}
}
