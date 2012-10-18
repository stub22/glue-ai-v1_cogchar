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

import java.util.ArrayList;
import java.util.List;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.Ident;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * CommandBinding groups some set of actionBindings.
 * It is also a trigger, which ignores its argument.
 */

public class CommandBinding extends BasicDebugger implements CogcharActionTrigger {
	private		Ident							myCommandID;
	private		List<CogcharActionBinding>		myActionList = new ArrayList<CogcharActionBinding>();
	
	public CommandBinding(Ident cmdID) {
		myCommandID = cmdID;
	}
	
	public void appendAction(CogcharActionBinding action) { 
		myActionList.add(action);
	}
	public void performAllActions() {
		for (CogcharActionBinding b : myActionList) {
			b.perform();
		}
	}
	public void fire(CogcharScreenBox bt) {
		getLogger().info("Firing command group {}", myCommandID);
		performAllActions();
	}
	
}
