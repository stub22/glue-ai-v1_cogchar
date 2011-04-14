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

package org.cogchar.animoid.output;

import java.util.Set;
import org.cogchar.animoid.config.AnimoidConfig;
import org.cogchar.animoid.protocol.Frame;
import org.cogchar.animoid.protocol.JVFrame;
import org.cogchar.animoid.protocol.Joint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Stu Baurmann
 */
public class BlinkJob extends MotionJob {
	private static Logger	theLogger = LoggerFactory.getLogger(BlinkJob.class.getName());

		// Maintain some useful params, e.g. blink rate

	// Maintain some private state about the last time we opened/closed, etc.


	public BlinkJob(AnimoidConfig aconf) {
		super(aconf);
		// pull useful stuff out of animoid config, e.g. default blink rate.
	}
	@Override public JVFrame contributeVelFrame(Frame prevPosAbsRomFrame, JVFrame prevVelRomFrame, Set<Joint> cautionJoints) {
		theLogger.debug("The BlinkJob does nothing...again!");
		return null;
	}

	// Add some setter methods so that Drools/JMX can change our blink rate and other params.

}
