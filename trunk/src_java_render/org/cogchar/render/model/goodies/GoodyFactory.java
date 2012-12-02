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

package org.cogchar.render.model.goodies;

import com.jme3.scene.Node;
import java.util.concurrent.Callable;
import org.cogchar.render.opengl.scene.DeepSceneMgr;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class GoodyFactory {
	
	private static Logger theLogger = LoggerFactory.getLogger(GoodyFactory.class);
	
	// Just for now, making this a pseudo singleton. Later will figure out where the "main" instance will be 
	// held more permanently
	private static GoodyFactory theFactory;
	public static GoodyFactory getTheFactory() {
		return theFactory;
	}
	public static GoodyFactory createTheFactory(RenderRegistryClient rrc) {
		theFactory = new GoodyFactory(rrc);
		return theFactory;
	}
	
	private RenderRegistryClient myRRC;
	private Node myRootNode = new Node("GoodyNode"); // A node for test, though we may want to have "finer grained" nodes to attach to
	
	GoodyFactory(RenderRegistryClient rrc) {
		myRRC = rrc;
		attachGoodyNode();
	}
	
	// Is this a good place for the relevant instance of GoodySpace to live, or should it be moved elsewhere?
	private GoodySpace theGoodySpace = new GoodySpace();
	public GoodySpace getTheGoodySpace() {
		return theGoodySpace;
	}
	
	public final void attachGoodyNode() {
		final DeepSceneMgr dsm = myRRC.getSceneDeepFacade(null);
		myRRC.getWorkaroundAppStub().enqueue(new Callable<Void>() { // Must manually do this on main render thread, ah jMonkey...

			@Override
			public Void call() throws Exception {
				dsm.attachTopSpatial(myRootNode);
				return null;
			}
		});
	}
	
	public void createByAction(GoodyAction ga) {
		if (ga.getKind() == GoodyAction.Kind.CREATE) {
			if (GoodyNames.TYPE_BIT_BOX.equals(ga.getType())) {
				BitBox newBitBox = new BitBox(myRRC, ga.getGoodyID(), ga.getLocationVector(), ga.getSize()[0]);
				theGoodySpace.addGoody(newBitBox);
				boolean bitBoxState = Boolean.valueOf(ga.getSpecialString(GoodyNames.BOOLEAN_STATE));
				newBitBox.attachToVirtualWorldNode(myRootNode, bitBoxState);
			} else {
				theLogger.warn("Did not recognize requested goody type for creation {}", ga.getType());
			}
		} else {
			theLogger.warn("GoodyFactory was requested to add a goody, but the GoodyAction kind was not CREATE! Goody URI: {}", ga.getGoodyID());
		}
	}
}
