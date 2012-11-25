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
import java.util.Map;
import org.appdapter.core.name.Ident;
import org.cogchar.api.thing.ThingActionSpec;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class GoodySpace {
	private	Map<Ident, BasicVirtualThing>		myVThingsByID;
	
	public GoodySpace() { 
		myVThingsByID = new HashMap<Ident, BasicVirtualThing>();
	}
	
	/**
	 * The targetThing is presumed to be a "goody", either existing or new.
	 */
	public void processAction(ThingActionSpec actionSpec) {
		GoodyAction ga = new GoodyAction(actionSpec);
		Ident gid = ga.getGoodyID();
		// If it's a CREATE action, we will do some different stuff
		// For the moment, let's focus on "update"
		BasicVirtualThing goodyBVT = myVThingsByID.get(gid);
		// Now - "apply" the "action" to the "virtual-thing"/"goody".
		// 
	}
	
}
