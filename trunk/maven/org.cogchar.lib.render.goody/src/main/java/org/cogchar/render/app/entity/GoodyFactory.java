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

package org.cogchar.render.app.entity;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import java.util.concurrent.Callable;
import org.appdapter.core.name.Ident;
import org.cogchar.name.goody.GoodyNames;
// import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.sys.context.CogcharRenderContext;
import org.cogchar.render.goody.basic.GoodyBox;
import org.cogchar.render.goody.basic.VirtualFloor;
import org.cogchar.render.goody.bit.BitBox;
import org.cogchar.render.goody.bit.BitCube;
import org.cogchar.render.goody.bit.TicTacGrid;
import org.cogchar.render.goody.bit.TicTacMark;
import org.cogchar.render.goody.flat.CrossHairGoody;
import org.cogchar.render.goody.flat.ScoreBoardGoody;
import org.cogchar.render.goody.flat.TextGoody;

import org.cogchar.render.optic.goody.VWorldCameraEntity;
import org.cogchar.render.opengl.scene.DeepSceneMgr;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.cogchar.render.sys.goody.GoodyRenderRegistryClient;
import org.cogchar.render.sys.goody.GoodyModularRenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class GoodyFactory {
	
	private static Logger theLogger = LoggerFactory.getLogger(GoodyFactory.class);
	
	// Just for now, making this a pseudo singleton. Later will figure out where the "main" instance will be 
	// held more permanently -- the render registry?
	private static GoodyFactory theGoodyFactory;
	
	private VWorldEntityActionConsumer myActionConsumer;
	
	public static GoodyFactory getTheFactory() {
		return theGoodyFactory;
	}
	public static GoodyFactory createTheFactory(GoodyRenderRegistryClient rrc, GoodyModularRenderContext gmrc) {
		theLogger.info("Creating new GoodyFactory");
		theGoodyFactory = new GoodyFactory(rrc, gmrc);
		// This is used in ModularRenderContext.doUpdate to do 2-D screen resizing stuff.  
		gmrc.setTheEntitySpace(theGoodyFactory.getActionConsumer()); // Notify hrc (ModularRenderContext) of the EntitySpace so it can apply screen dimension updates
		return theGoodyFactory;
	}
	
	private GoodyRenderRegistryClient myRRC;
	private Node myRootNode = new Node("GoodyNode"); // A node for test, though we may want to have "finer grained" nodes to attach to
	
	private GoodyFactory(GoodyRenderRegistryClient rrc, GoodyModularRenderContext gmrc) {
		myRRC = rrc;
		attachGoodyNode();
		myActionConsumer = new VWorldEntityActionConsumer(gmrc);
		myActionConsumer.applyNewScreenDimension(gmrc.getPanel().getSize(null));
	}
	
	public VWorldEntityActionConsumer getActionConsumer() {
		return myActionConsumer;
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
	
	public VWorldEntity createByAction(GoodyAction ga) {
		VWorldEntity newGoody = null;
		if (ga.getKind() == GoodyAction.Kind.CREATE) {
			// Switch on string local name would be nice
			// This is getting out of hand
			// Big problem here is that GoodyFactory needs to know about each Goody type and how to make them
			// Ripe for refactoring to avoid that, perhaps via a Chain of Responsibility pattern?
			// Or perhaps we would like to pass the GoodyAction to the goodies in their constructors
			// Still would need a way (possibly reflection?) to get goody class from type
			try {
				//theLogger.info("Trying to create a goody, type is {}", ga.getType()); // TEST ONLY
				Vector3f scale = ga.getVectorScale();
				Float scalarScale = ga.getScale();
				if ((scale == null) && (scalarScale != null)) {
					scale = new Vector3f(scalarScale, scalarScale, scalarScale);
				}
				if (GoodyNames.TYPE_BIT_BOX.equals(ga.getType())) {
					boolean bitBoxState = Boolean.valueOf(ga.getSpecialString(GoodyNames.BOOLEAN_STATE));
					newGoody = new BitBox(myRRC, ga.getGoodyID(), ga.getLocationVector(), ga.getRotationQuaternion(),
						scale, bitBoxState);
				} else if (GoodyNames.TYPE_BIT_CUBE.equals(ga.getType())) {
					boolean bitBoxState = Boolean.valueOf(ga.getSpecialString(GoodyNames.BOOLEAN_STATE));
					newGoody = new BitCube(myRRC, ga.getGoodyID(), ga.getLocationVector(), ga.getRotationQuaternion(),
						scale, bitBoxState);
				} else if (GoodyNames.TYPE_FLOOR.equals(ga.getType())) {
					// Assuming physical floor for now, but that may be a good thing to define in repo
					newGoody = new VirtualFloor(myRRC, ga.getGoodyID(), ga.getLocationVector(), ga.getColor(), true);
				} else if (GoodyNames.TYPE_TICTAC_MARK.equals(ga.getType())) {
					boolean isAnO = Boolean.valueOf(ga.getSpecialString(GoodyNames.USE_O));
					newGoody = new TicTacMark(myRRC, ga.getGoodyID(), ga.getLocationVector(), ga.getRotationQuaternion(),
							scale, isAnO);
				} else if (GoodyNames.TYPE_TICTAC_GRID.equals(ga.getType())) {
					newGoody = new TicTacGrid(myRRC, ga.getGoodyID(), ga.getLocationVector(), ga.getRotationQuaternion(),
							ga.getColor(), scale);
				} else if (GoodyNames.TYPE_CROSSHAIR.equals(ga.getType())) {
					newGoody = new CrossHairGoody(myRRC, ga.getGoodyID(), ga.getLocationVector(), scalarScale);
				} else if (GoodyNames.TYPE_SCOREBOARD.equals(ga.getType())) {
					int rows = Integer.valueOf(ga.getSpecialString(GoodyNames.ROWS));
					newGoody = new ScoreBoardGoody(myRRC, ga.getGoodyID(), ga.getLocationVector(),
							ga.getSize()[0], rows, scalarScale);
				} else if (GoodyNames.TYPE_TEXT.equals(ga.getType())) {
					// scale.getX() should return scalarScale if that is provided, or use Robosteps API scalar scale which
					// is represented as a vector scale with identical components
					newGoody = new TextGoody(myRRC, ga.getGoodyID(), ga.getLocationVector(),
							scale.getX(), ga.getColor(), ga.getText()); 
				} else if (GoodyNames.TYPE_BOX.equals(ga.getType())) {
					newGoody = new GoodyBox(myRRC, ga.getGoodyID(), ga.getLocationVector(), ga.getRotationQuaternion(),
							ga.getColor(), scale);
				} else if (GoodyNames.TYPE_CAMERA.equals(ga.getType())) {
					Ident cameraUri = ga.getGoodyID();
					if (myActionConsumer.getGoody(cameraUri) == null) { //Otherwise this camera wrapper is already created
						theLogger.info("Adding a VWorldCameraEntity for {}", cameraUri);
						// This evidences the fact that the CameraMgr needs to switch to URIs to identify cameras, not strings:
						Camera cam = myRRC.getOpticCameraFacade(null).getNamedCamera(cameraUri.getLocalName());
						if (cam != null) {
							newGoody = (new VWorldCameraEntity(myRRC, cameraUri, cam));
						} else {
							theLogger.warn("Couldn't find camera with URI {} for goody", cameraUri);
						}
					}
				} else {
					theLogger.warn("Did not recognize requested goody type for creation: {}", ga.getType());
				}
			} catch (Exception e) {
				theLogger.error("Error attempting to create goody {}", ga.getGoodyID(), e);
			}
		} else {
			theLogger.warn("GoodyFactory received request to add a goody, but the GoodyAction kind was not CREATE! Goody URI: {}",
					ga.getGoodyID());
		}
		return newGoody;
	}
	
	// This way, EntitySpace doesn't need to know about the root node to attach. But this pattern can change if
	// we decide we rather it did!
	public VWorldEntity createAndAttachByAction(GoodyAction ga) {
		VWorldEntity newGoody = createByAction(ga);
		if (newGoody != null) {
			newGoody.attachToVirtualWorldNode(myRootNode);
		}
		return newGoody;
	}
}
