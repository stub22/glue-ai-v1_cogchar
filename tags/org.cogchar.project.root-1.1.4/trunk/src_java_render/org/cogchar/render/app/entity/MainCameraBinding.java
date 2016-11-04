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

package org.cogchar.render.app.entity;

import org.appdapter.core.name.Ident;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.cogchar.render.sys.task.Queuer;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class MainCameraBinding extends CameraBinding {
	public MainCameraBinding(Queuer queuer, Ident requiredID) {		
		super(queuer, requiredID);
	}	
	@Override public void attachViewPort(RenderRegistryClient rrc) {
		getLogger().info("Ignoring attachViewPort() command for MainCameraBinding id={}", getIdent());
	}
	@Override public void detachViewPort(RenderRegistryClient rrc) {
		getLogger().info("Ignore attachViewPort() command for MainCameraBinding");
	}
}
