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
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
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
import org.cogchar.render.goody.flat.ParagraphGoody;
import org.cogchar.render.gui.bony.VirtualCharacterPanel;

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
	
	private GoodySpace myGoodySpace;
	
	
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
		myGoodySpace = new GoodySpace(gmrc);
		VirtualCharacterPanel optCharPanel = gmrc.getPanel();
		if (optCharPanel != null) {
			myGoodySpace.applyNewScreenDimension(optCharPanel.getSize(null));
		}
	}
	
	public GoodySpace getGoodySpace() { 
		return myGoodySpace;
	}
	public VWorldEntityActionConsumer getActionConsumer() {
		return myGoodySpace;
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
	
	public VWorldEntity createByAction(GoodyActionExtractor ga) {
		VWorldEntity novGoody = null;
		if (ga.getKind() == GoodyActionExtractor.Kind.CREATE) {
			// Switch on string local name would be nice
			// This is getting out of hand
			// Big problem here is that GoodyFactory needs to know about each Goody type and how to make them
			// Ripe for refactoring to avoid that, perhaps via a Chain of Responsibility pattern?
			// Or perhaps we would like to pass the GoodyActionExtractor to the goodies in their constructors
			// Still would need a way (possibly reflection?) to get goody class from type
			try {
				//theLogger.info("Trying to create a goody, type is {}", ga.getType()); // TEST ONLY
				Vector3f scaleVec = ga.getScaleVec3f();
				Float scaleUniform = ga.getScaleUniform();
				if ((scaleVec == null) && (scaleUniform != null)) {
					scaleVec = new Vector3f(scaleUniform, scaleUniform, scaleUniform);
				}
				Vector3f locVec = ga.getLocationVec3f();
				Quaternion rotQuat = ga.getRotationQuaternion();
				Ident goodyID = ga.getGoodyID();
				Ident goodyType = ga.getType();
				ColorRGBA gcolor = ga.getColor();
				String goodyText = ga.getText();
				Boolean bitBoxState = ga.getSpecialBoolean(GoodyNames.BOOLEAN_STATE);
				Boolean isAnO =  ga.getSpecialBoolean(GoodyNames.USE_O);				
				Integer rowCount = ga.getSpecialInteger(GoodyNames.ROWS);
				
				
				if (GoodyNames.TYPE_BIT_BOX.equals(goodyType)) {
					novGoody = new BitBox(myRRC, goodyID, locVec, rotQuat,	scaleVec, bitBoxState);
				} else if (GoodyNames.TYPE_BIT_CUBE.equals(goodyType)) {
					novGoody = new BitCube(myRRC, goodyID, locVec, rotQuat,	scaleVec, bitBoxState);
				} else if (GoodyNames.TYPE_FLOOR.equals(goodyType)) {
					// Assuming physical floor for now, but that may be a good thing to define in repo
					novGoody = new VirtualFloor(myRRC, ga.getGoodyID(), locVec, gcolor, true);
				} else if (GoodyNames.TYPE_TICTAC_MARK.equals(goodyType)) {
					novGoody = new TicTacMark(myRRC, goodyID, locVec, rotQuat, scaleVec, isAnO);
				} else if (GoodyNames.TYPE_TICTAC_GRID.equals(goodyType)) {
					novGoody = new TicTacGrid(myRRC, goodyID, locVec, rotQuat, gcolor, scaleVec);
				} else if (GoodyNames.TYPE_CROSSHAIR.equals(goodyType)) {
					novGoody = new CrossHairGoody(myRRC, goodyID, locVec, scaleUniform);
				} else if (GoodyNames.TYPE_SCOREBOARD.equals(goodyType)) {
					float sizeX = ga.getSizeVec3D()[0];
					float rowHeight = sizeX;
					float textSize = scaleUniform;					
					theLogger.info("Scoreboard row count=" + rowCount + ", rowHeight=" + rowHeight 
								+ ", textSize=" + textSize+ ", locVec=" + locVec);
	
					novGoody = new ScoreBoardGoody(myRRC, goodyID, locVec, rowHeight, rowCount, textSize);
					
				} else if (GoodyNames.TYPE_TEXT.equals(goodyType)) {
					// scale.getX() should return scalarScale if that is provided, or use Robosteps API scalar scale which
					// is represented as a vector scale with identical components
					novGoody = new ParagraphGoody(myRRC, goodyID, locVec, scaleVec.getX(), gcolor, goodyText); 
					
				} else if (GoodyNames.TYPE_BOX.equals(goodyType)) {
					novGoody = new GoodyBox(myRRC, goodyID, locVec, rotQuat, gcolor, scaleVec);
				} else if (GoodyNames.TYPE_CAMERA.equals(goodyType)) {
					Ident cameraUri = goodyID;
					if (myGoodySpace.getGoody(cameraUri) == null) { //Otherwise this camera wrapper is already created
						theLogger.info("Adding a VWorldCameraEntity for {}", cameraUri);						
						CameraBinding camBinding = myRRC.getOpticCameraFacade(null).getCameraBinding(cameraUri);
						if (camBinding != null) {
							Camera cam = camBinding.getCamera();
							if (cam != null) {
								novGoody = (new VWorldCameraEntity(myRRC, cameraUri, cam));
							} else {
								throw new RuntimeException("No actual camera found in binding at " + cameraUri);
							}
						}
						else {
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
		return novGoody;
	}
	
	// This way, EntitySpace doesn't need to know about the root node to attach. But this pattern can change if
	// we decide we rather it did!
	public VWorldEntity createAndAttachByAction(GoodyActionExtractor ga, VWorldEntity.QueueingStyle qStyle) {
		VWorldEntity newGoody = createByAction(ga);
		if (newGoody != null) {
			newGoody.attachToVirtualWorldNode(myRootNode, qStyle);
		}
		return newGoody;
	}
}
