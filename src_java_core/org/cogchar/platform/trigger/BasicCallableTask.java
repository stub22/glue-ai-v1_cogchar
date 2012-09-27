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

import org.appdapter.core.log.BasicDebugger;

/**
 * @author Stu B. <www.texpedient.com>
 */

public abstract class BasicCallableTask extends BasicDebugger implements CallableTask {
	@Override public Throwable call() throws Exception {
		try {
			logInfo("%%%%%%%%%%%%%%%%%%% BasicCallableTask on JME3 thread is calling perform()");
			perform();
			logInfo("%%%%%%%%%%%%%%%%%%% BasicCallableTask.perform() completed, returning");
			return null;
		} catch (Throwable t) {
			return t;
		}
	}
	
	public static BasicCallableTask makeCallableTaskFromAny(final Task nestedTask) throws Throwable {
		return new BasicCallableTask() {
			@Override public void perform() throws Throwable {
				logInfo("%%%%%%%%%%%%%%%%%%% BasicCallableTask is calling task.perform()");
				nestedTask.perform();
				logInfo("%%%%%%%%%%%%%%%%%%% BasicCallableTask.perform() completed, call)_ method is returning");
			}
		};
	}
}
