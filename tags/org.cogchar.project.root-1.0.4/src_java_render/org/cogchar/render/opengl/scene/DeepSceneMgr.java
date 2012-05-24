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

import com.jme3.renderer.Camera;
import com.jme3.light.Light;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.renderer.ViewPort;
import org.cogchar.render.sys.core.RenderRegistryAware;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class DeepSceneMgr extends RenderRegistryAware {
	private	Node	myParentNode;

	public void setParentNode(Node n) {
		myParentNode = n;
	}
	protected Node getParentNode() {
		if (myParentNode == null) {
			myParentNode = findJme3RootDeepNode(null);
		}
		return myParentNode;
	}
	public void attachTopSpatial(Spatial s) {
		getParentNode().attachChild(s);
	}
	public void detachTopSpatial(Spatial s) {
		getParentNode().detachChild(s);
	}
	public void addLight(Light l) {
		getParentNode().addLight(l);
	}
        
	// Added so that new cameras loaded from RDF can create new viewports - could move this to ViewPortFacade if we want
	public void addViewPort(String label, Camera c) {
		ViewPort vp = findOrMakeOpticViewportFacade(null).getRenderManager().createPostView(label, c); // PostView or MainView?
		vp.setClearFlags(true, true, true);
		vp.setBackgroundColor(ColorRGBA.LightGray); // This is set for main window right now in WorkaroundFuncsMustDie.setupCameraLightAndViewport - yuck. May want a more consistent way to do this in long run.
		vp.attachScene(getParentNode());
	}
}
