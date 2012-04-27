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

import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import org.cogchar.render.sys.core.RenderRegistryAware;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class FlatOverlayMgr extends RenderRegistryAware {
	private Node	myAppGuiNode;

	public void setParentNode(Node n) {
		myAppGuiNode = n;
	}
	protected Node getParentNode() {  
		if (myAppGuiNode == null) {
			myAppGuiNode = findJme3RootOverlayNode(null);
		}
		return myAppGuiNode;
	}
	
	public void detachAllOverlays() { 
		getParentNode().detachAllChildren();
	}
	public void attachOverlaySpatial(Spatial s) {
		getParentNode().attachChild(s);
	}
        // Added by Ryan Biggs for help screen toggling 25 April 2012
        public void detachOverlaySpatial(Spatial s) {
		getParentNode().detachChild(s);
	}
}
