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

import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.Ident;
import org.appdapter.core.name.FreeIdent;
import org.cogchar.api.perform.Performance.Instruction;


/**
 * @author Stu B. <www.texpedient.com>
 */
public abstract class BasicChannel extends BasicDebugger implements Channel {
// <Cursor, M extends Media<Cursor>, WorldTime> extends BasicDebugger implements Channel<Cursor, M, WorldTime> {

	private Ident myIdent;
	private Channel.Status myStatus = Channel.Status.INIT;

	public BasicChannel(Ident ident) {
		myIdent = ident;
	}

	public Ident getIdent() {
		return myIdent;
	}

	public Status getStatus() {
		return myStatus;
	}

	protected void markStatus(Channel.Status updatedStatus) {
		myStatus = updatedStatus;
		// Notify listeners
	}

	public String getName() {
		return myIdent.getLocalName();
	}
	/**
	 * This implementation currently handles only the "START" action, and only for BasicPerformance.
	 * (We delegate to BasicPerformance.impl_attemptStart(), which delegates right back to us).
	 * Other actions and performance types are ignored.
	 * This is the area ripe for deeper implementation in Scala, keeping in mind error handling,
	 * interruption, and notification requirements.
	 * 
	 * @param perf
	 * @param action
	 * @param actionTime
	 * @return 
	 */
//	public synchronized  boolean schedulePerfAction(Performance<Cursor, M, WorldTime> perf, Instruction action, WorldTime actionTime) {
    public <Cursor, M extends Media<Cursor>, Time> boolean schedulePerfInstruction(Performance<Cursor, M, Time> perf, Time worldTime, 
					Performance.Instruction<Cursor> instruct) {
		boolean resultFlag = true;
		String debugDesc = "Scheduling instruct[" + instruct + "] for Time[" + worldTime + "] for Performance [" + perf + "]";
		try {
			switch (instruct.myKind) {
				// So far we can only handle START, see comments above.
				case PLAY:
					if (myStatus == Channel.Status.IDLE) {
						markStatus(Channel.Status.PERFORMING);
					}
					// So far we can only handle BasicPerformance, see comments above.
					if (perf instanceof BasicPerformance) {
						((BasicPerformance) perf).impl_attemptStart();
					}
					resultFlag = true;
					break;
			}
		} catch (Throwable t) {
			getLogger().error("Problem scehduling " + debugDesc, t);
		}
		return resultFlag;
	}
	// TODO:  Generalize to handle media cursor position (e.g. to "resume" after "pause" or "cue").
	public <Cursor, M extends Media<Cursor>, Time> void attemptPerformanceStart(Performance<Cursor, M, Time> perf) throws Throwable {
		M	media = perf.getMedia();
		attemptMediaPlayNow(media);
	}
	// protected abstract <Cursor, M extends Media<Cursor>>  void attemptMediaStartNow(M m) throws Throwable;
	protected abstract void attemptMediaPlayNow(Media<?> m) throws Throwable;
	
	@Override public String toString() { 
		return getClass().getSimpleName() + " ident=" + myIdent + ", stat=" + myStatus;
	}
}
