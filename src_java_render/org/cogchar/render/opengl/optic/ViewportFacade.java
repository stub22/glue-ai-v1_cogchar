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

import com.jme3.renderer.RenderManager;
import org.cogchar.render.sys.registry.RenderRegistryAware;

/**
 * @author Stu B. <www.texpedient.com>
 * A facade of perhaps debatable necessity
 */
public class ViewportFacade extends RenderRegistryAware {
	private	 RenderManager		myRenderManager;
	/*
	public ViewportFacade(RenderManager renderMgr) {
		myRenderManager = renderMgr;
	}
	*/
	public void setRenderManager(RenderManager rm) {
		myRenderManager = rm;
	}
	public RenderManager getRenderManager() {
		if (myRenderManager == null) {
			myRenderManager = findJme3RenderManager(null);
		}
		return myRenderManager;
	
	/*	
        public RenderManager getRenderManager() {
		return myRenderManager; 
	*/
	}
	
}
