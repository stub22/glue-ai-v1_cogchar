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
package org.cogchar.render.opengl.bony.demo;

import com.jme3.input.controls.Trigger;
import org.cogchar.platform.trigger.DummyBinding;
import org.cogchar.platform.trigger.DummyBox;
import org.cogchar.platform.trigger.DummyTrigger;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BoundAction implements DummyBinding {
	DummyBox		myActionBox;
	// Our Action Trigger types are entirely separate from the JME3 "Trigger" for inputs. 
	DummyTrigger	myActionTrigger;   
	
	@Override public void setTargetBox(DummyBox box) {
		myActionBox = box;
	}

	@Override public void setTargetTrigger(DummyTrigger trig) {
		myActionTrigger = trig;
	}
	public boolean includedInMinSim() { 
		return false;
	}	
	public void perform() {
		if (myActionTrigger != null) {
			myActionTrigger.fire(myActionBox);
		}			
	}
}
