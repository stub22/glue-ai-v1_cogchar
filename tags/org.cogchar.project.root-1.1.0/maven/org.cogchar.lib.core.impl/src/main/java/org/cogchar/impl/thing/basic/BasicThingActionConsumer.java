/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.impl.thing.basic;

import java.util.List;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.api.thing.ThingActionSpec;
import org.cogchar.api.thing.WantsThingAction;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * We expect to dissolve most of this functionality in favor of our MarkingAgent pattern.
 */

public abstract class BasicThingActionConsumer extends BasicDebugger implements WantsThingAction {
	
	// public abstract ConsumpStatus consumeAction(ThingActionSpec actionSpec, Ident srcGraphID);

	/**
	 * This form is called only from PumaVirtualWorldMapper.setupActionConsumer() - to "clear()" 
 * old/init actions, before we start listening for new ones. 
 * The graph it wants to clear is identified by:
 * Ident actionGraphID = gce.ergMap().get(worldConfigID).get(EntityRoleCN.THING_ACTIONS_BINDINGS_ROLE);
 * 
 * 
	 * But note also the distinct 1-arg form of this method defined in BasicThingActionRouter, which is
	 * the one called from ordinary runtime callbacks, after each time that the repo is updated. 
	 * 
	 * @param rc
	 * @param srcGraphID
	 * @deprecated
	 */
	
	
	@Deprecated synchronized public void consumeAllActions(RepoClient rc, Ident srcGraphID) {
		BasicThingActionUpdater updater = new BasicThingActionUpdater();
		// takeThingActions_TX is our obsolete, deprecated legacy prototype implementation of message reaping
		List<ThingActionSpec> actionSpecList = updater.takeThingActions_TX(rc, srcGraphID); 
		// It is due to be replaced with the following, although in fact the whole consumer pattern
		// should instead be dissolved, as noted in the class-comment header for this class.
		// Actions will no longer be "consumed", they will only be noticed, and eventually forgotten.
		// updater.viewActions(rc, srcGraphID);
		for (ThingActionSpec actionSpec : actionSpecList) {
			getLogger().info("Consuming from graph {} actionSpec with spec-ID {}", srcGraphID, actionSpec.getActionSpecID());
			getLogger().debug("Debug - full spec dump: {} ", actionSpec);
			ConsumpStatus status = consumeAction(actionSpec, srcGraphID);
			getLogger().info("Returned Consump-Status = {}", status);
		}
	}
	

	/**
	 * 
	 * @param rc
	 * @param srcGraphID
	 * @param cutoffTime
	 * @param viewingAgent
	 * @deprecated
	 * 
	 * Still called from 
	 * BasicThingActionRouter.consumeAllActions(), 
	 * which is still called from
	 * Puma.GruesomeTAProcessingFuncs.processPendingThingActions()
	 */
	@Deprecated public void viewAndMarkAllActions(RepoClient rc, Ident srcGraphID, Long cutoffTime, Ident viewingAgent) {
		BasicThingActionUpdater updater = new BasicThingActionUpdater();
		// takeThingActions_TX is our obsolete, deprecated legacy prototype implementation of message reaping
		List<ThingActionSpec> actionSpecList = updater.viewActionsAndMark_TX(rc, srcGraphID, cutoffTime, viewingAgent);
		// It is due to be replaced with the following, although in fact the whole consumer pattern
		// should instead be dissolved, as noted in the class-comment header for this class.
		// Actions will no longer be "consumed", they will only be noticed, and eventually forgotten.
		// updater.viewActions(rc, srcGraphID);
		for (ThingActionSpec actionSpec : actionSpecList) {
			getLogger().info("Consuming from graph {} actionSpec with spec-ID {}, viewingAgentID is {}", 
						new Object[] {srcGraphID, actionSpec.getActionSpecID(), viewingAgent});
			
			getLogger().debug("Debug - full spec dump: {} ", actionSpec);
			consumeAction(actionSpec, srcGraphID);
		}
	}

}
