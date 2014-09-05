/*
 *  Copyright 2014 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.render.goody.dynamic;

import org.appdapter.core.name.Ident;
import org.cogchar.render.opengl.scene.DeepSceneMgr;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import com.jme3.scene.Node;
import com.jme3.scene.Geometry;

/**
 *
 * @author Stu B22 <stub22@appstract.com>
 * 
 * Gives a group of vizShapes into a single Node parent, thus putting them in a coordinate system.
 * (These shapes would otherwise be able to have different Node parents, and no *spatial* relation to each other).
 */
public class VizShapeSiblingGroup extends VizShapeGroup {
	private		Node		mySubsysNode;

	public VizShapeSiblingGroup(Ident groupID) {
		super(groupID);
		mySubsysNode = new Node(groupID.getLocalName());
	}
	
	public void enable_onRendThrd(RenderRegistryClient	rrc) {
		if (mySubsysNode != null) {
			DeepSceneMgr dsm = rrc.getSceneDeepFacade(null);
			dsm.attachTopSpatial(mySubsysNode);
		}
	}
	public void disable_onRendThrd(RenderRegistryClient	rrc) {
		if (mySubsysNode != null) {
			DeepSceneMgr dsm = rrc.getSceneDeepFacade(null);
			dsm.detachTopSpatial(mySubsysNode);
		}		
	}
	@Override public void configureMemberGeom_onRendThrd(RenderRegistryClient	rrc, VizShape child) {
		super.configureMemberGeom_onRendThrd(rrc, child);
		Geometry childGeom = child.getGeom();		
		mySubsysNode.attachChild(childGeom);
	}

}
