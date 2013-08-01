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

// import org.cogchar.render.model.humanoid.VWorldHumanoidFigureEntity;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import org.appdapter.core.name.Ident;
import org.cogchar.impl.thing.basic.BasicThingActionConsumer;
import org.cogchar.api.thing.ThingActionSpec;
// import org.cogchar.render.app.humanoid.HumanoidRenderContext;
// import org.cogchar.render.model.humanoid.HumanoidFigure;
import org.cogchar.render.sys.context.CogcharRenderContext;
import org.cogchar.render.sys.goody.GoodyModularRenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Formerly GoodySpace, this class manages actions for Entities. Mostly this still focuses on Goodies, but now this is
 * being generalized for web actions as well. Much refinement, additions, and refactoring TBD.
 *
 * @author Stu B. <www.texpedient.com>, Ryan Biggs <rbiggs@hansonrobokind.com>
 *
 * Web stuff is currently glomming on here, and EntitySpace needs refactoring, probably into multiple classes, to
 * separate the web, goody, and other future entity functions. - Ryan 18 April 2013
 *
 * Render-side
 */
public class VWorldEntityActionConsumer extends BasicThingActionConsumer { //  extends BasicEntitySpace {

	private static Logger theLogger = LoggerFactory.getLogger(VWorldEntityActionConsumer.class);
	private Map<Ident, VWorldEntity> myGoodiesByID;
	private Dimension myScreenDimension;

	// We only need hrc here for the temporary way to get camera and character lists -- eventually will come directly
	// from RDF
	public VWorldEntityActionConsumer(GoodyModularRenderContext gmrc) {
		myGoodiesByID = new HashMap<Ident, VWorldEntity>();
		// Now camera goody wrappers are added as required by GoodyFactory
		// addCameraGoodies(gmrc); // Cameras aren't really goodies, but we can pretend for the moment!
	}

	public void addGoody(VWorldEntity newGoody) {
		if (newGoody != null) {
			Ident goodyUri = newGoody.getUri();
			theLogger.info("Adding Goody with URI: {}", goodyUri);
			myGoodiesByID.put(goodyUri, newGoody);
		} else {
			theLogger.warn("Something is attempting to add a null goody to the GoodySpace, ignoring");
		}

	}

	public void removeGoody(VWorldEntity departingGoody) {
		departingGoody.detachFromVirtualWorldNode(); // Safe to perform even if it's not currently attached
		myGoodiesByID.remove(departingGoody.getUri());
	}

	// Providing this so that CinematicMgr can access goodies to use in Cinematics
	public VWorldEntity getGoody(Ident goodyUri) {
		return myGoodiesByID.get(goodyUri);
	}

	public void applyNewScreenDimension(Dimension newDimension) {
		myScreenDimension = newDimension;
		// Notify goodies of new dimensions
		for (VWorldEntity aGoody : myGoodiesByID.values()) {
			aGoody.applyScreenDimension(myScreenDimension);
		}
	}

	public Dimension getScreenDimension() {
		return myScreenDimension;
	}
	@Override public ConsumpStatus consumeAction(ThingActionSpec actionSpec, Ident srcGraphID) {
		theLogger.info("The targetThingType is {}", actionSpec.getTargetThingTypeID()); // TEST ONLY

		// How do we decide whether it's really a VWorld / Goody action?
		// Below, the targetThing is presumed to be a "goody", either existing or new.
		GoodyAction ga = new GoodyAction(actionSpec);
		Ident gid = ga.getGoodyID();
		VWorldEntity goodyOne = myGoodiesByID.get(gid);
		//theLogger.info("The kind for Goody is {}", ga.getKind()); // TEST ONLY
		switch (ga.getKind()) {
			case CREATE: { // If it's a CREATE action, we will do some different stuff
				if (myGoodiesByID.containsKey(gid)) {
					theLogger.warn("Goody already created! Ignoring additional creation request for goody: {}", gid);
				} else {
					goodyOne = GoodyFactory.getTheFactory().createAndAttachByAction(ga);
					if (goodyOne != null) {
						addGoody(goodyOne);
						return ConsumpStatus.USED;
					}
				}
				break;
			}
			case DELETE: {
				if (!myGoodiesByID.containsKey(gid)) {
					theLogger.warn("Could not delete goody because it does not exist: {}", gid);
				} else {
					removeGoody(goodyOne);
					return ConsumpStatus.USED;
				}
				break;
			}
			default: {
				// For the moment, let's focus on "update"
				try {
					// Now - apply the action to goodyOne
					goodyOne.applyAction(ga);
					return ConsumpStatus.USED;
				} catch (Exception e) {
					theLogger.warn("Problem attempting to update goody with URI: {}", gid, e);
				}
			}
		}
		return ConsumpStatus.IGNORED;
	}

	/* Moved to HumaonoidRenderWorldMapper
	// A temporary way to make it possible to interact with figures... ultimately Humanoids aren't goodies!
	private void addHumanoidGoodies(GoodyModularRenderContext hrc) {
		Map<Ident, HumanoidFigure> humanoidFigures = hrc.getHumanoidFigureManager().getHumanoidFigures();
		for (Ident figureUri : humanoidFigures.keySet()) {
			theLogger.info("Adding a HumanoidFigureGoodyWrapper for {}", figureUri);
			addGoody(new VWorldHumanoidFigureEntity(hrc.getRenderRegistryClient(), figureUri, humanoidFigures.get(figureUri)));
		}
	}
	*/
	// Now camera goody wrappers are added as required by GoodyFactory
	/*
	private void addCameraGoodies(GoodyModularRenderContext hrc) {
		Map<String, Camera> cameras = hrc.getRenderRegistryClient().getOpticCameraFacade(null).getCameraMap();
		for (String cameraName : cameras.keySet()) {
			theLogger.info("Adding a Camera for {}", cameraName);
			// This evidences the fact that the CameraMgr needs to switch to URIs to identify cameras, not strings:
			addGoody(new VWorldCameraEntity(hrc.getRenderRegistryClient(), new FreeIdent(NamespaceDir.NS_CCRT_RT + cameraName),
					cameras.get(cameraName)));
		}
	}
	*/
	
}
