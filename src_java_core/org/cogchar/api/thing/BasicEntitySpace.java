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

import java.util.ArrayList;
import java.util.List;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class BasicEntitySpace {
	/*
	private List<Handler>	myHandlers = new ArrayList<Handler>();
	
	// Not immediately clear if this should be here or elsewhere
	public void readAndApplyActions(RepoClient rc, Ident graphIdent) {
		ThingActionUpdater updater = new ThingActionUpdater();
		List<ThingActionSpec> actionSpecList = updater.takeThingActions(rc, graphIdent);
		for (ThingActionSpec actionSpec : actionSpecList) {
			for (Handler h : myHandlers) {
				boolean consumed = h.consumeAction(actionSpec);
				if (consumed) {
					break;
				}
			}
		}
	}

	
	public void addHandler(Handler h) { 
		myHandlers.add(h);
	}
	
	public static abstract class Handler {
		public abstract boolean consumeAction(ThingActionSpec actionSpec);
	}
	* 
	*/ 
}
