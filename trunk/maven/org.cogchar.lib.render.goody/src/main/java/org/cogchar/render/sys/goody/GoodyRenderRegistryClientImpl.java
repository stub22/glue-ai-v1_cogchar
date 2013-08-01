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
import org.cogchar.render.sys.registry.BasicRenderRegistryClientImpl;
import org.cogchar.render.sys.registry.RenderRegistryFuncs;

/**
 *
 * @author Owner
 */
public class GoodyRenderRegistryClientImpl extends BasicRenderRegistryClientImpl implements GoodyRenderRegistryClient {
	@Override
	public PathMgr getScenePathFacade(String optionalName) {
		return GoodyRenderRegistryFuncs.findOrMakeScenePathFacade(optionalName);
	}
	
	@Override
	public SpatialAnimMgr getSceneAnimFacade(String optionalName) {
		return GoodyRenderRegistryFuncs.findOrMakeSceneAnimFacade(optionalName);
	}

}
