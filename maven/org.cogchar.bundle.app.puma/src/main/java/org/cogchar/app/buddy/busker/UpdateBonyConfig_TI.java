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
public class UpdateBonyConfig_TI extends TriggerItem {
	static Logger theLogger = LoggerFactory.getLogger(UpdateBonyConfig_TI.class);
	
	public ClassLoader		myOptResourceClassLoader;
	
	@Override public void fire(DummyBox targetBox) {
		PumaDualCharacter pdc = (PumaDualCharacter) targetBox;
		String bonyRdfConfigPath = pdc.myUpdateBonyRdfPath;
		theLogger.info("Updating bony config using path[" + bonyRdfConfigPath + "] for char [" + pdc + "]");
		if ((pdc != null) && (bonyRdfConfigPath != null)) {
			pdc.updateBonyConfig(bonyRdfConfigPath, myOptResourceClassLoader);
		}
	}
}
