/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.convoid.output.exec;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.cogchar.convoid.output.config.Step;
import java.util.Map;

/**
 * @author Stu B. <www.texpedient.com>
 *
 */
public class DummyStepExecution extends StepExecution {
	private static Logger	theLogger = Logger.getLogger(DummyStepExecution.class.getName());
	static {
		theLogger.setLevel(Level.ALL);
	}
	
	public DummyStepExecution(Step s, Map<String, String> configMap) {
		super(s, configMap);
	}
	
	/* (non-Javadoc)
	 * @see com.hansonrobotics.convoid.mouth.StepExecution#connect()
	 */
	@Override
	public void connect() {
		theLogger.finer("DummyStepExecution - connect(), step=" + getStep());
	}

	/* (non-Javadoc)
	 * @see com.hansonrobotics.convoid.mouth.StepExecution#disconnect()
	 */
	@Override
	public void disconnect() {
		theLogger.finer("DummyStepExecution - disconnect(), step=" + getStep());
	}

	/* (non-Javadoc)
	 * @see com.hansonrobotics.convoid.mouth.StepExecution#start()
	 */
	@Override
	public void start() {
		theLogger.finer("DummyStepExecution - start(), step=" + getStep());
	}

	/* (non-Javadoc)
	 * @see com.hansonrobotics.convoid.mouth.StepExecution#stop()
	 */
	@Override
	public void stop() {
		theLogger.finer("DummyStepExecution - stop(), step=" + getStep());
	}
	
	public static class Factory implements StepExecutionFactory {
		public StepExecution makeStepExecution(Step s, Map<String, String> configMap) {
			return new DummyStepExecution(s, configMap);
		}
		public static void registerAsDefault() {
			Factory f = new Factory();
			StepExecution.registerFactory(f.getClass().getName(), f, true);
		}
	}
}
