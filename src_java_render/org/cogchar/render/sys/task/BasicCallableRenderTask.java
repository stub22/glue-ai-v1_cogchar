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

package org.cogchar.render.sys.task;

import org.cogchar.platform.trigger.BasicCallableTask;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.cogchar.render.sys.registry.RenderRegistryClientFinder;

/**
 * @author Stu B. <www.texpedient.com>
 */

public abstract class BasicCallableRenderTask extends BasicCallableTask implements CallableRenderTask {
	RenderRegistryClientFinder		myFinder;
	
	public BasicCallableRenderTask(RenderRegistryClientFinder rrcf) {
		myFinder = rrcf;
	}
	@Override public RenderRegistryClient getRenderRegistryClient() {
		return myFinder.getRenderRegistryClient();
	}

	@Override public void perform() throws Throwable {
		RenderRegistryClient rrc = getRenderRegistryClient();
		performWithClient(rrc);
	}

}
