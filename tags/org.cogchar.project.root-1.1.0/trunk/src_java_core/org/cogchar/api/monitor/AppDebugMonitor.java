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

package org.cogchar.api.monitor;

import org.cogchar.api.thing.BasicEntityAction;
import org.cogchar.api.thing.ThingActionSpec;

/**
 * @author Stu B. <www.texpedient.com>
 */

public interface AppDebugMonitor {

	public enum TransitKind {
		INPUT_FROM_GUI,
		INPUT_FROM_CLIENT,
		FOUND_IN_REPO,
		TASTED_BY_ROUTER
		
	}
	public void notifyThingActionTransit(TransitKind kind, ThingActionSpec taSpec);
	
	public void notifyEntityActionTransit(TransitKind kind, BasicEntityAction action);

}
