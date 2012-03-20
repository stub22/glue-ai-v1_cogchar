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
package org.cogchar.api.perform;

import java.util.List;
import java.util.ArrayList;
import org.cogchar.api.perform.Performance.Result;
import org.appdapter.api.module.Module.State;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BasicPerformance<Chan extends Channel> implements Performance<Chan> {
	
	protected static class BasicResult<C extends Channel> implements Performance.Result<C> {
		private State	myState;
		public State getState() {
			return myState;
		}
	}
	
	
	private	Chan					myChannel;
	private	BasicResult<Chan>		myCurrentResult;
	private List<Listener<Chan>>	myListeners = new ArrayList<Listener<Chan>>();
	public BasicPerformance(Chan chan) {
		myChannel = chan;
		myCurrentResult = new BasicResult<Chan>();
		myCurrentResult.myState = State.IN_INIT;
	}
	@Override public Result<Chan> getCurrentResult() {
		return myCurrentResult;
	}
	@Override public void addListener(Listener<Chan> l) {
		myListeners.add(l);
	}
	@Override public void removeListener(Listener<Chan> l) {
		myListeners.remove(l);
	}		
	protected Chan getChannel() { 
		return myChannel;
	}
	public void updateResultState(State s) {
		myCurrentResult.myState = s;
		notifyListeners();
	}
	protected void notifyListeners() { 
		for (Listener<Chan> l : myListeners) {
			l.notifyChange(this);
		}
	}
	

}
