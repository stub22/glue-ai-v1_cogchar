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
package org.cogchar.impl.perform.basic;

import org.cogchar.api.channel.BasicChannel;
import org.cogchar.api.event.Listener;
import org.cogchar.api.event.Event;
import java.util.List;
import java.util.ArrayList;

import org.cogchar.api.event.BasicNotifier;
import org.cogchar.api.event.Notifier;
import org.cogchar.api.perform.Media;
import org.cogchar.api.perform.PerfChannel;
import org.cogchar.api.perform.Performance;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * BasicPerformance adds Notifier functionality to the essentials of Performance.
 * EPT stands for the EventPerformanceType - some extension of the present type that is incorporated into Event contract.
 */
public abstract class BasicPerformance<Cursor, M extends Media<Cursor>, WorldTime>
		extends BasicNotifier<BasicPerformanceEvent<Cursor, M, WorldTime>>
		implements Performance<Cursor, M, WorldTime> {
	
	
	private	M								myMedia;
	private	PerfChannel							myChannel;
	private State							myState;
	// This cursor *may* be updatable in place.
	private	Cursor							myMediaCursor;

	public BasicPerformance(M media, PerfChannel chan, Cursor initialCursor) {
	// public BasicPerformance(M media, Channel<Cursor, M, WorldTime> chan, Cursor initialCursor) {
		myMedia = media;
		myChannel = chan;
		myState = State.INITING;
		// TODO:  Be smarter about copying an immutable cursor, and whatnot.
		myMediaCursor = initialCursor;
	}
	@Override public PerfChannel getChannel() { 
	// @Override public Channel<Cursor, M, WorldTime> getChannel() { 
		return myChannel;
	}
	@Override public M getMedia() { 
		return myMedia;
	}
	@Override public State getState() {
		return myState;
	}
	@Override public Cursor getCursor() { 
		return myMediaCursor;
	}
	// Maybe markState and markCursor should be exposed by an UpdatablePerformance interface.
	public synchronized void markState(State s) {
		State prevState = myState;
		myState = s;
		WorldTime eventTime = getCurrentWorldTime();
		BasicPerformanceEvent<Cursor, M, WorldTime> stateChangeEvent = makeStateChangeEvent(eventTime, prevState, s, myMediaCursor);
		notifyListeners(stateChangeEvent);
	}
	public synchronized  void markCursor(Cursor c, boolean notify) {
		myMediaCursor = c;
		if (notify) {
			// Lazy version.  Do a trivial self-state update.  Acceptable?
			markState(myState);
		}
	}
	
	@Override public String toString() { 
		return getClass().getSimpleName() + "[chan=" + myChannel + ", state=" + myState + "media=" + myMedia  + "]";
	}
	
	protected abstract BasicPerformanceEvent<Cursor, M, WorldTime> 
			makeStateChangeEvent(WorldTime worldTime, State prevState, State nextState, Cursor cursor);
	
	
	protected abstract WorldTime getCurrentWorldTime();
	
	@Override public boolean attemptToScheduleInstruction(WorldTime worldTime, Instruction instruct) {
		// So far, BasicChannel only responds to the START-PERFORMANCE action, others are ignored.
		return myChannel.schedulePerfInstruction(this, worldTime, instruct);
	}	
	
	// This is used only from BasicChannel, and is not part of our public API!
	protected void impl_attemptStart() throws Throwable { 
		
		if (myChannel instanceof BasicPerfChan) { 
			((BasicPerfChan) myChannel).startPerfFromBegin(this);
		}
		
	}


}
