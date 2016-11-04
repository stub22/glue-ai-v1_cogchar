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

package org.cogchar.platform.trigger;

import java.util.HashMap;
import java.util.Map;
import org.appdapter.core.name.Ident;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class CommandSpace {
	private		Map<Ident, CommandBinding>	myCommandBindingsByID = new HashMap<Ident, CommandBinding>();
	
	public CommandBinding findBinding(Ident cmdID) {
		return myCommandBindingsByID.get(cmdID);
	}
	public CommandBinding findOrMakeBinding(Ident cmdID) {
		CommandBinding cb = findBinding(cmdID);
		if (cb == null) {
			cb = new CommandBinding(cmdID);
			myCommandBindingsByID.put(cmdID, cb);
		}
		return cb;
	}
	public boolean invokeCommand(Ident cmdID) {
		CommandBinding cb = findBinding(cmdID);
		if (cb != null) {
			cb.performAllActions();
			return true;
		} 
		return false;
	}
}
