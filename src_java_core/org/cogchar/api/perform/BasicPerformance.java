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

import org.cogchar.api.event.Listener;
import org.cogchar.api.event.Event;
import java.util.List;
import java.util.ArrayList;

import org.appdapter.api.module.Module.State;
import org.cogchar.api.event.BasicNotifier;
import org.cogchar.api.event.Notifier;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BasicPerformance<M extends Media, Time, EPT extends Performance<M, Time>, E extends Event<EPT, Time>>
		extends BasicNotifier<EPT, Time, E>
		implements Performance<M, Time>, Notifier<EPT, Time, E> {
	
	
	private	M						myMedia;
	private	Channel<M, Time>		myChannel;
	private State					myState;

	public BasicPerformance(M media, Channel<M, Time> chan) {
		myMedia = media;
		myChannel = chan;
		myState = State.IN_INIT;
	}

	@Override public Channel<M, Time> getChannel() { 
		return myChannel;
	}
	@Override public M getMedia() { 
		return myMedia;
	}
	public State getState() {
		return myState;
	}	
	protected void markState(State s) {
		myState = s;
		notifyListeners();
	}
	protected void notifyListeners() { 
		//for (Listener<M, C> l : myListeners) {
		//	l.notifyChange(this);
		//}
	}

	public boolean attemptToScheduleAction(Action action, Time t) {
		return myChannel.schedulePerfAction(this, action, t);
	}
	
	@Override public String toString() { 
		return getClass().getSimpleName() + "[chan=" + myChannel + ", state=" + myState + "media=" + myMedia  + "]";
	}


}
