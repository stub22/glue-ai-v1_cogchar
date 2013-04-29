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

import org.appdapter.scafun.FullTrigger;
import org.appdapter.scafun.FullBox;
/**
 * @author Stu B. <www.texpedient.com>
 * Just a trigger with a fire method, that is typed to work on CogcharScreenBoxes.
 */
public interface CogcharActionTrigger extends FullTrigger<CogcharScreenBox> {
	// Inherits:  public abstract void fire(DummyBox targetBox);
}
