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
package org.cogchar.api.animoid.protocol;

import java.util.Set;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class JVFrame extends Frame<JointVelocityAROMPS> {
	public static JVFrame makeFrom(Frame f) {
		if (f == null) {
			return null;
		}
		JVFrame jvf = new JVFrame();
		jvf.shallowCopyPositionsFrom(f);
		return jvf;
	}
	public static JVFrame sumJVFrames(JVFrame f1, JVFrame f2) {
		Frame rawSum = Frame.sumCompatibleFrames(f1, f2);
		return makeFrom(rawSum);
	}
	public static JVFrame weightedSumJVFrameCommonJoints(JVFrame f1, double w1,
				JVFrame f2, double w2) {
		Frame rawSum = Frame.weightedSumCommonJoints(f1, w1, f2, w2);
		return makeFrom(rawSum);
	}
	public JVFrame subJVFrame(Set<Joint> joints, boolean ignoreMissingPositions) {
		return makeFrom(getSubframe(joints, ignoreMissingPositions));
	}
}
