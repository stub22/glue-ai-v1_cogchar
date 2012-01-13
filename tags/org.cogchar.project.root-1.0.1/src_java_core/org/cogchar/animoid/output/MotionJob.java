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

import java.util.Collection;
import java.util.Set;
import org.cogchar.animoid.calc.estimate.TimeKeeper;
import org.cogchar.animoid.config.AnimoidConfig;
import org.cogchar.animoid.protocol.Frame;
import org.cogchar.animoid.protocol.JVFrame;
import org.cogchar.animoid.protocol.Joint;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public abstract class MotionJob extends AnimoidJob {
	private		transient		TimeKeeper		myTimeKeeper;

	public MotionJob(AnimoidConfig aconf) {
		super(aconf);
	}
	public Collection<Joint> getCautionJoints() {
		return null;
	}
	public abstract JVFrame contributeVelFrame(Frame prevPosAbsRomFrame, JVFrame prevVelRomFrame,
				Set<Joint> cautionJoints);
	
	public void setTimeKeeper(TimeKeeper tk) {
		myTimeKeeper = tk;
	}
	public TimeKeeper getTimeKeeper() {
		return myTimeKeeper;
	}
}
