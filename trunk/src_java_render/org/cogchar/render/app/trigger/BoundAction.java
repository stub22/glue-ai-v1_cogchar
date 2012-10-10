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
package org.cogchar.render.app.trigger;

import java.util.ArrayList;
import java.util.List;
import org.cogchar.platform.trigger.CogcharActionBinding;
import org.cogchar.platform.trigger.CogcharScreenBox;
import org.cogchar.platform.trigger.CogcharActionTrigger;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BoundAction implements CogcharActionBinding {
	// Changed to list of DummyBox instead of a single one to support multiple characters - 27 July 2012 Ryan Biggs

	private List<CogcharScreenBox> myActionBoxes = new ArrayList<CogcharScreenBox>();
	// Our Action Trigger types are entirely separate from the JME3 "Trigger" for inputs. 
	private CogcharActionTrigger myActionTrigger;

	public boolean includedInMinSim() {
		return false;
	}

	@Override public void addTargetBox(CogcharScreenBox box) {
		myActionBoxes.add(box);
	}

	@Override public void setTargetTrigger(CogcharActionTrigger trig) {
		myActionTrigger = trig;
	}

	@Override public void perform() {
		if (myActionTrigger != null) {
			for (CogcharScreenBox myActionBox : myActionBoxes) {
				myActionTrigger.fire(myActionBox);
			}
		}
	}
	
	@Override public void clearTargetBoxes() {
		myActionBoxes.clear();
	}
}
