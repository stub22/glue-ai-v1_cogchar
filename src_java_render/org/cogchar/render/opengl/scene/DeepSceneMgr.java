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
package org.cogchar.render.opengl.scene;

import com.jme3.light.Light;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class DeepSceneMgr {
	private	Node	myParentNode;
	public DeepSceneMgr (Node parentNode) {
		myParentNode = parentNode;
	}
	public void attachTopSpatial(Spatial s) {
		myParentNode.attachChild(s);
	}
	public void detachTopSpatial(Spatial s) {
		myParentNode.detachChild(s);
	}
	public void addLight(Light l) {
		myParentNode.addLight(l);
	}
}
