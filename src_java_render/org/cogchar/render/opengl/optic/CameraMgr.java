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

import com.jme3.renderer.Camera;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class CameraMgr {
	public enum CommonCameras {
		DEFAULT,
		TOP_VIEW,
		WIDE_VIEW
	}
	private Map<String, Camera> myCamerasByName = new HashMap<String, Camera>();
	public Camera cloneCamera(Camera orig) {
		return orig.clone();
	}
	public void registerNamedCamera(String name, Camera cam) {
		myCamerasByName.put(name, cam);
	}
	public Camera getNamedCamera(String name) {
		return myCamerasByName.get(name);
	}
	
	public void registerCommonCamera(CommonCameras id,  Camera cam) {
		registerNamedCamera(id.name(), cam);
	}
	public Camera getCommonCamera(CommonCameras id) {
		return getNamedCamera(id.name());
	}
	
}
