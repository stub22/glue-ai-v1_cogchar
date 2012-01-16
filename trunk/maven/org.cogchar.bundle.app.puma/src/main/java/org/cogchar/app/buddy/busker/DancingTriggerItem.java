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
package org.cogchar.app.buddy.busker;

import org.cogchar.bundle.app.puma.PumaDualCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cogchar.platform.trigger.DummyTrigger;
import org.cogchar.platform.trigger.DummyBox;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class DancingTriggerItem extends TriggerItem {
	static Logger theLogger = LoggerFactory.getLogger(DancingTriggerItem.class);
	
	@Override public void fire(DummyBox targetBox) {
		theLogger.info("trigger[" + toString() + "] firing on " + targetBox.toString());
		PumaDualCharacter pdc = (PumaDualCharacter) targetBox;
		pdc.triggerTestAnim();
	}
}
