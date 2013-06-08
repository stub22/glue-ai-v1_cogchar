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

import org.appdapter.core.name.Ident;

/**
 * @author Stu B. <www.texpedient.com>
 */

public interface WantsThingAction {
	public enum ConsumpStatus {
		IGNORED,	// The action was definitely not useful to this Consumer.
		QUEUED,		// The action was asynchronously queued, we won't know until later if it was used/consumed.
		USED,		// The action was used or understood, but others still may want to process it.
		CONSUMED	// The action has been consumed in some final way, upstream processors should consider it "done".
	}	
	public ConsumpStatus consumeAction(ThingActionSpec actionSpec, Ident srcGraphID);

}
