/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.render.sys.goody;

import org.cogchar.render.scene.goody.PathMgr;
import org.cogchar.render.scene.goody.SpatialAnimMgr;
import org.cogchar.render.sys.registry.RenderRegistryFuncs;

/**
 *
 * @author stub22
 */
public class GoodyRenderRegistryFuncs extends RenderRegistryFuncs {
	protected static RFSpec<PathMgr> THE_CC_SCENE_PATH_FACADE;
	protected static RFSpec<SpatialAnimMgr> THE_CC_SCENE_ANIM_FACADE;
	static {
		THE_CC_SCENE_PATH_FACADE = new RFSpec<PathMgr>(RFKind.CC_SCENE_PATH_FACADE, PathMgr.class, false);
		THE_CC_SCENE_ANIM_FACADE = new RFSpec<SpatialAnimMgr>(RFKind.CC_SCENE_ANIM_FACADE, SpatialAnimMgr.class, false);
		
	}
	protected static PathMgr findOrMakeScenePathFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_SCENE_PATH_FACADE, optionalName, null);
	}

	protected static SpatialAnimMgr findOrMakeSceneAnimFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_SCENE_ANIM_FACADE, optionalName, null);
	}	
	
	// protected static FacadeSpec<PhysicsStuffBuilder>	THE_CC_PHYSICS_FACADE;
	//		CC_PHYSICS_FACADE		
}
