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

import java.awt.Dimension;
import org.appdapter.api.module.Module;
// import org.cogchar.render.app.entity.VWorldEntityActionConsumer;
// import org.cogchar.render.goody.basic.DataballGoodyBuilder;
// import org.cogchar.render.gui.bony.VirtualCharacterPanel;
import org.cogchar.render.sys.context.CogcharRenderContext;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 * @author Stu B. <www.texpedient.com>
 * @author rbiggs
 * Stu 2013-02-24: The ball-builder stuff probably doesn't belong here.
 * Ryan 2013-04-17: As long as we're adding stuff that doesn't belong, how about some Goody update stuff too?!
 * We'll find a cleaner way to plug this stuff in soon.
 */
public class ModularRenderContext extends CogcharRenderContext {
	private CogcharRenderModulator myRenderModulator;

	public ModularRenderContext(RenderRegistryClient rrc) {
		super(rrc);
	}

	//	public VirtualCharacterPanel myVCP;

	@Override public void completeInit() {
		synchronized (completedInitLock) {
			if (myRenderModulator != null)
				return;
			super.completeInit();
			logInfo("init CogcharRenderModulator");
			myRenderModulator = new CogcharRenderModulator();
		}
	}

	public void attachModule(Module<CogcharRenderModulator> m) {
		synchronized (completedInitLock) {
			ensureInitCompleted();
			myRenderModulator.attachModule(m);
		}
	}

	protected void ensureInitCompleted() {
		synchronized (completedInitLock) {
			if (myRenderModulator == null) {
				completeInit();
			}
		}
	}

	public void detachModule(Module<CogcharRenderModulator> m) {
		synchronized (completedInitLock) {
			myRenderModulator.detachModule(m);
		}
	}

	protected CogcharRenderModulator getModulator() {
		synchronized (completedInitLock) {
			return myRenderModulator;
		}
	}

	@Override public void doUpdate(float tpf) {
		if (myRenderModulator != null) {
			myRenderModulator.runOneCycle(tpf);
		}
	}
	

}
