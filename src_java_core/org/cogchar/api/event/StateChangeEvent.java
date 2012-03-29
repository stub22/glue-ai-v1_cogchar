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

package org.cogchar.api.event;

import org.appdapter.api.module.Module;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class StateChangeEvent<Source, Time> extends BasicEvent<Source, Time> {
	private		 Module.State		myPrevState, myNextState;
	public StateChangeEvent(Source src, Time t,  Module.State prevState,  Module.State nextState) {
		super(src, t);
	}
	public Module.State getPrevState() {
		return myPrevState;
	}
	public Module.State getNextState() {
		return myNextState;
	}
}
