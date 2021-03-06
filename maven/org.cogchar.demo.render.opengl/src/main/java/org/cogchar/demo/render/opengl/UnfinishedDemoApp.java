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
package org.cogchar.demo.render.opengl;

import org.cogchar.render.app.core.CogcharPresumedApp;
import org.cogchar.render.sys.context.CogcharRenderContext;
import org.cogchar.render.sys.context.ConfiguredPhysicalModularRenderContext;
import org.cogchar.render.sys.context.CoreFeatureAdapter;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class UnfinishedDemoApp  extends CogcharPresumedApp  {
	@Override protected CogcharRenderContext makeCogcharRenderContext() {
		return new DYPS_RenderContext();
	}

	public void setupLight() { 
		ConfiguredPhysicalModularRenderContext drc = (ConfiguredPhysicalModularRenderContext) getRenderContext();
		CoreFeatureAdapter.setupLight(drc);
	}
	public static class DYPS_RenderContext extends ConfiguredPhysicalModularRenderContext {
		
	}
}
