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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.api.thing.ThingActionSpec;
import org.cogchar.api.thing.ThingActionUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class GoodySpace {
	
	private static Logger theLogger = LoggerFactory.getLogger(GoodySpace.class);
	
	private	Map<Ident, BasicGoody>		myGoodiesByID;
	
	public GoodySpace() { 
		myGoodiesByID = new HashMap<Ident, BasicGoody>();
	}
	
	public void addGoody(BasicGoody newGoody) {
		Ident goodyUri = newGoody.getUri();
		theLogger.info("Adding Goody with URI: {}", goodyUri);
		myGoodiesByID.put(goodyUri, newGoody);
	}
	
	public void removeGoody(BasicGoody departingGoody) {
		departingGoody.detachFromVirtualWorldNode(); // Safe to perform even if it's not currently attached
		myGoodiesByID.remove(departingGoody.getUri());
	}
	
	// Providing this so that CinematicMgr can access goodies to use in Cinematics
	public BasicGoody getGoody(Ident goodyUri) {
		return myGoodiesByID.get(goodyUri);
	}
	
	/**
	 * The targetThing is presumed to be a "goody", either existing or new.
	 */
	public void processAction(ThingActionSpec actionSpec) {
		GoodyAction ga = new GoodyAction(actionSpec);
		Ident gid = ga.getGoodyID();
		BasicGoody goodyOne = myGoodiesByID.get(gid);
		switch (ga.getKind()) {
			case CREATE: { // If it's a CREATE action, we will do some different stuff
				if (myGoodiesByID.containsKey(gid)) {
					theLogger.warn("Goody already created! Ignoring additional creation request for goody: {}", gid);
				} else {
					goodyOne = GoodyFactory.getTheFactory().createAndAttachByAction(ga);
					addGoody(goodyOne);
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
	
	// Not immediately clear if this should be here or elsewhere
	public void readAndApplyGoodyActions(RepoClient rc, Ident graphIdent) {
		ThingActionUpdater updater = new ThingActionUpdater();
		List<ThingActionSpec> actionSpecList = updater.getThingActions(rc, graphIdent);
		for (ThingActionSpec actionSpec : actionSpecList) {
			processAction(actionSpec);
		}
	}
	
}
