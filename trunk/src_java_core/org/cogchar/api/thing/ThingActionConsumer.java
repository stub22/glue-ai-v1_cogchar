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

package org.cogchar.api.thing;

import java.util.List;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * We expect to dissolve most of this functionality in favor of our MarkingAgent pattern.
 */

public abstract class ThingActionConsumer extends BasicDebugger implements WantsThingAction {
	
	// public abstract ConsumpStatus consumeAction(ThingActionSpec actionSpec, Ident srcGraphID);
	
	@Deprecated public void consumeAllActions(RepoClient rc, Ident srcGraphID) {
		ThingActionUpdater updater = new ThingActionUpdater();
		List<ThingActionSpec> actionSpecList = updater.takeThingActions(rc, srcGraphID);
		for (ThingActionSpec actionSpec : actionSpecList) {
			getLogger().info("Consuming from graph {} : {} ", srcGraphID, actionSpec);
			consumeAction(actionSpec, srcGraphID);
		}
	}	
}
