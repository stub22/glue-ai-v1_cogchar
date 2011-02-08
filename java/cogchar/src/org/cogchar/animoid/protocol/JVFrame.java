/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.animoid.protocol;

import java.util.Set;

/**
 *
 * @author Stu Baurmann
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
