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
	
	@Deprecated public void consumeAllActions(RepoClient rc, Ident srcGraphID) {
		BasicThingActionUpdater updater = new BasicThingActionUpdater();
		// takeThingActions is our obsolete, deprecated legacy prototype implementation of message reaping
		List<ThingActionSpec> actionSpecList = updater.takeThingActions(rc, srcGraphID); 
		// It is due to be replaced with the following, although in fact the whole consumer pattern
		// should instead be dissolved, as noted in the class-comment header for this class.
		// Actions will no longer be "consumed", they will only be noticed, and eventually forgotten.
		// updater.viewActions(rc, srcGraphID);
		for (ThingActionSpec actionSpec : actionSpecList) {
			getLogger().info("Consuming from graph {} : {} ", srcGraphID, actionSpec);
			consumeAction(actionSpec, srcGraphID);
		}
	}	
    
	@Deprecated public void viewAndConsumeAllActions(RepoClient rc, Ident srcGraphID) {
		BasicThingActionUpdater updater = new BasicThingActionUpdater();
		// takeThingActions is our obsolete, deprecated legacy prototype implementation of message reaping
		List<ThingActionSpec> actionSpecList = updater.viewActions(rc, srcGraphID); 
		// It is due to be replaced with the following, although in fact the whole consumer pattern
		// should instead be dissolved, as noted in the class-comment header for this class.
		// Actions will no longer be "consumed", they will only be noticed, and eventually forgotten.
		// updater.viewActions(rc, srcGraphID);
		for (ThingActionSpec actionSpec : actionSpecList) {
			getLogger().info("Consuming from graph {} : {} ", srcGraphID, actionSpec);
			consumeAction(actionSpec, srcGraphID);
		}
	}	
    
	@Deprecated public void viewAndMarkAllActions(RepoClient rc, Ident srcGraphID, Long cutoffTime, Ident viewingAgent) {
		BasicThingActionUpdater updater = new BasicThingActionUpdater();
		// takeThingActions is our obsolete, deprecated legacy prototype implementation of message reaping
		List<ThingActionSpec> actionSpecList = updater.viewActionsAndMark(rc, srcGraphID, cutoffTime, viewingAgent);
		// It is due to be replaced with the following, although in fact the whole consumer pattern
		// should instead be dissolved, as noted in the class-comment header for this class.
		// Actions will no longer be "consumed", they will only be noticed, and eventually forgotten.
		// updater.viewActions(rc, srcGraphID);
		for (ThingActionSpec actionSpec : actionSpecList) {
			getLogger().info("Consuming from graph {} : {} ", srcGraphID, actionSpec);
			consumeAction(actionSpec, srcGraphID);
		}
	}	
}
