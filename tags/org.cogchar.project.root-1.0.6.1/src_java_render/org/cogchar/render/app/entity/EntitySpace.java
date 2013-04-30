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

import com.jme3.renderer.Camera;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.api.thing.ThingActionSpec;
import org.cogchar.api.thing.ThingActionUpdater;
import org.cogchar.bind.lift.ControlConfig;
import org.cogchar.bind.lift.LiftAmbassador;
import org.cogchar.name.dir.NamespaceDir;
import org.cogchar.name.web.WebActionNames;
import org.cogchar.render.app.goody.GoodyAction;
import org.cogchar.render.app.goody.GoodyFactory;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.app.web.WebAction;
import org.cogchar.render.goody.basic.BasicGoody;
import org.cogchar.render.goody.basic.CameraGoodyWrapper;
import org.cogchar.render.goody.basic.HumanoidFigureGoodyWrapper;
import org.cogchar.render.model.humanoid.HumanoidFigure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Formerly GoodySpace, this class manages actions for Entities. Mostly this still focuses on Goodies, but now this
 * is being generalized for web actions as well. Much refinement, additions, and refactoring TBD.
 * 
 * @author Stu B. <www.texpedient.com>, Ryan Biggs <rbiggs@hansonrobokind.com>
 * 
 * Web stuff is currently glomming on here, and EntitySpace needs refactoring, probably into multiple classes, to 
 * separate the web, goody, and other future entity functions. - Ryan 18 April 2013
 * 
 * Render-side 
 */

public class EntitySpace {
	
	private static Logger theLogger = LoggerFactory.getLogger(EntitySpace.class);
	
	private	Map<Ident, BasicGoody>		myGoodiesByID;
	
	private Dimension myScreenDimension;
	
	// We only need hrc here for the temporary way to get camera and character lists -- eventually will come directly
	// from RDF
	public EntitySpace(HumanoidRenderContext hrc) { 
		myGoodiesByID = new HashMap<Ident, BasicGoody>();
		addHumanoidGoodies(hrc); // Humanoids aren't really goodies, but we can pretend for the moment!
		addCameraGoodies(hrc); // Cameras aren't really goodies, but we can pretend for the moment!
	}
	
	public void addGoody(BasicGoody newGoody) {
		if (newGoody != null) {
			Ident goodyUri = newGoody.getUri();
			theLogger.info("Adding Goody with URI: {}", goodyUri);
			myGoodiesByID.put(goodyUri, newGoody);
		} else {
			theLogger.warn("Something is attempting to add a null goody to the GoodySpace, ignoring");
		}
		
	}
	
	public void removeGoody(BasicGoody departingGoody) {
		departingGoody.detachFromVirtualWorldNode(); // Safe to perform even if it's not currently attached
		myGoodiesByID.remove(departingGoody.getUri());
	}
	
	// Providing this so that CinematicMgr can access goodies to use in Cinematics
	public BasicGoody getGoody(Ident goodyUri) {
		return myGoodiesByID.get(goodyUri);
	}
	
	public void applyNewScreenDimension(Dimension newDimension) {
		myScreenDimension = newDimension;
		// Notify goodies of new dimensions
		for (BasicGoody aGoody : myGoodiesByID.values()) {
			aGoody.applyScreenDimension(myScreenDimension);
		}
	}
	
	public Dimension getScreenDimension() {
		return myScreenDimension;
	}

	public void processAction(ThingActionSpec actionSpec) {
		// The horrors of this method abound!
		// Temporary (and ugly) way to tie in web actions
		theLogger.info("The targetThing is {}", actionSpec.getTargetThingTypeID()); // TEST ONLY
		if ((actionSpec.getTargetThingTypeID().equals(WebActionNames.WEBCONTROL)) || 
				(actionSpec.getTargetThingTypeID().equals(WebActionNames.WEBCONFIG))) {
			performWebActions(actionSpec);	
		} else { //  else the targetThing is presumed to be a "goody", either existing or new.
			GoodyAction ga = new GoodyAction(actionSpec);
			Ident gid = ga.getGoodyID();
			BasicGoody goodyOne = myGoodiesByID.get(gid);
			//theLogger.info("The kind for Goody is {}", ga.getKind()); // TEST ONLY
			switch (ga.getKind()) {
				case CREATE: { // If it's a CREATE action, we will do some different stuff
					if (myGoodiesByID.containsKey(gid)) {
						theLogger.warn("Goody already created! Ignoring additional creation request for goody: {}", gid);
					} else {
						goodyOne = GoodyFactory.getTheFactory().createAndAttachByAction(ga);
						if (goodyOne != null) {
							addGoody(goodyOne);
						}
					}
					break;
				}
				case DELETE: {
					if (!myGoodiesByID.containsKey(gid)) {
						theLogger.warn("Could not delete goody because it does not exist: {}", gid);
					} else {
						removeGoody(goodyOne);
					}
					break;
				}
				default: {
					// For the moment, let's focus on "update"
					try {
						// Now - apply the action to goodyOne
						goodyOne.applyAction(ga);
					} catch (Exception e) {
						theLogger.warn("Problem attempting to update goody with URI: {}", gid, e);
					}
				}
			}
		}
	}
	
	private void performWebActions(ThingActionSpec actionSpec) {
		LiftAmbassador la = LiftAmbassador.getLiftAmbassador();
		WebAction wa = new WebAction(actionSpec);
		String webUser = wa.getUserName();
		String webUserClass = wa.getUserClass();
		if (actionSpec.getTargetThingTypeID().equals(WebActionNames.WEBCONTROL)) { // Big ugly if-else-if chain must go -- really need switch on Ident! (or Scala...)
			// Assuming for now it's CREATE only
			Ident controlAction = wa.getControlActionUri();
			if (controlAction != null) {
				la.activateControlAction(controlAction);
			} else {
				ControlConfig newCC = generateControlConfig(wa);
				Integer slotNum = wa.getSlotID();
				if (slotNum != null) {
					if (webUser != null) {
						// Activate for user
						la.activateControlFromConfigForUser(webUser, slotNum, newCC);
					} else if (webUserClass != null) {
						la.activateControlFromConfigForUserClass(webUserClass, slotNum, newCC);
					} else {
						la.activateControlFromConfig(slotNum, newCC);
					}
				} else {
					theLogger.warn("Could not display control by action spec -- desired control slot is null");
				}
			}
		} else if (actionSpec.getTargetThingTypeID().equals(WebActionNames.WEBCONFIG)) {
			// Assuming for now it's CREATE only
			Ident configIdent = wa.getConfigIdent();
			if (configIdent != null) {
				if (webUser == null) {
					la.activateControlsFromUri(configIdent);
				} else {
					la.activateControlsFromUriForUser(webUser, configIdent);
				}
			} else {
				theLogger.warn("Could not set web config by action spec -- desired config URI is null");
			}
		}
	}
	
	// A method to generate a new ControlConfig for display from a WebAction
	private ControlConfig generateControlConfig(WebAction wa) {
		ControlConfig cc = new ControlConfig();
		if (wa.getControlType() == null) {
			cc.controlType = "NULLTYPE";
		} else {
			cc.controlType = wa.getControlType().getLocalName().toUpperCase(); // Ensures lc:type property is case insensitive to local name
		}
		cc.action = wa.getControlAction();
		cc.text = wa.getControlText();
		cc.style = wa.getControlStyle();
		cc.resource = wa.getControlResource();
		return cc;
	}
	
	// A temporary way to make it possible to interact with figures... ultimately Humanoids aren't goodies!
	private void addHumanoidGoodies(HumanoidRenderContext hrc) {
		Map<Ident, HumanoidFigure> humanoidFigures = hrc.getHumanoidFigureManager().getHumanoidFigures();
		for (Ident figureUri : humanoidFigures.keySet()) {
			theLogger.info("Adding a HumanoidFigureGoodyWrapper for {}", figureUri);
			addGoody(new HumanoidFigureGoodyWrapper(hrc.getRenderRegistryClient(), figureUri, humanoidFigures.get(figureUri)));
		}
	}
	
	private void addCameraGoodies(HumanoidRenderContext hrc) {
		Map<String, Camera> cameras = hrc.getRenderRegistryClient().getOpticCameraFacade(null).getCameraMap();
		for (String cameraName : cameras.keySet()) {
			theLogger.info("Adding a Camera for {}", cameraName);
			// This evidences the fact that the CameraMgr needs to switch to URIs to identify cameras, not strings:
			addGoody(new CameraGoodyWrapper(hrc.getRenderRegistryClient(), new FreeIdent(NamespaceDir.NS_CCRT_RT + cameraName), 
					cameras.get(cameraName)));
		}
	}
	
	// Not immediately clear if this should be here or elsewhere
	public void readAndApplyGoodyActions(RepoClient rc, Ident graphIdent) {
		ThingActionUpdater updater = new ThingActionUpdater();
		List<ThingActionSpec> actionSpecList = updater.takeThingActions(rc, graphIdent);
		for (ThingActionSpec actionSpec : actionSpecList) {
			processAction(actionSpec);
		}
	}
	
}
