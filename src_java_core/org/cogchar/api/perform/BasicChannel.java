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
import org.appdapter.core.item.Ident;
import org.appdapter.core.item.FreeIdent;
import org.cogchar.api.perform.Performance.Action;

import org.appdapter.api.module.Module.State;

/**
 * @author Stu B. <www.texpedient.com>
 */
public abstract class BasicChannel<M extends Media, Time> extends BasicDebugger implements Channel<M, Time> {

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

	public synchronized  boolean schedulePerfAction(Performance<M, Time> perf, Action action, Time actionTime) {
		boolean resultFlag = true;
		String debugDesc = "Action[" + action + "] for Time[" + actionTime + "] for Performance [" + perf + "]";
		try {
			switch (action) {
				case START:
					if (myStatus == Channel.Status.IDLE) {
						markStatus(Channel.Status.PERFORMING);
					}

					// hack to raw type
					if (perf instanceof BasicPerformance) {
						((BasicPerformance) perf).markState(State.IN_START);
					}
					attemptPerformanceStart(perf);
					if (perf instanceof BasicPerformance) {
						((BasicPerformance) perf).markState(State.IN_RUN);
					}
					resultFlag = true;
					break;
			}
		} catch (Throwable t) {
			logError("Problem scehduling " + debugDesc, t);
		}
		return resultFlag;
	}

	public void attemptPerformanceStart(Performance<M, Time> perf) throws Throwable {
		M	media = perf.getMedia();
		attemptMediaStartNow(media);
	}
	protected abstract void attemptMediaStartNow(M m) throws Throwable;
}
