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

import java.util.HashSet;
import java.util.Set;

import org.cogchar.animoid.calc.blend.SlopeBlendFuncs;
import org.cogchar.animoid.config.AnimoidConfig;
import org.cogchar.animoid.protocol.Animation;
import org.cogchar.animoid.protocol.Frame;
import org.cogchar.animoid.protocol.JPARFrame;
import org.cogchar.animoid.protocol.JPRRFrame;
import org.cogchar.animoid.protocol.JVFrame;
import org.cogchar.animoid.protocol.Joint;
import org.cogchar.animoid.protocol.JointStateCoordinateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 *
 * End of animation is indicated by the status of the Job changing to COMPLETED.
 */
public class AnimationExecJob extends MotionJob {
	private static Logger	theLogger = LoggerFactory.getLogger(AnimationExecJob.class.getName());

	private	transient	Animation		myAnimation;
	private int							myCurrentFrameIndex;
	
	private	transient	Set<Joint>		myCachedUsedJointSet;
	

	private				String			myGestureName;
	private				boolean			myOverrideCautionFlag = false;

	/* These two coefficients determine how much "error" our absolute ("rash")
	// channels are allowed to try to "make up for" on each frame.
	// The "multiplier" is applied to the rate of change on the channel.
	// the "fixedBonus" is an additional constant.
	// These allowances serve to LIMIT rather than specify the amount of
	// Normally the "multiplier" is greater than 1.0, which allows the animation
	// to gradually catch up to it's planned trajectory.  The faster the animation
	// channel is moving, the more "allowance" we get via this multiplier, so the
	// faster we will catch up to the absolute trajectory.  Even when the animation
	// is sitting still, we are still allowed to catch up at a rate determined
	// by the fixed bonus allowance.
	*/
	private double			myRashAllowanceMultiplier;
	private	double			myRashBonusAllowance;
	
	public AnimationExecJob(AnimoidConfig aconf, Animation a, String gestureName,
				double rashAllowanceMultiplier, double rashBonusAllowance) {
		super(aconf);
		myAnimation = a;
		myCurrentFrameIndex = 0;
		myGestureName = gestureName;
		myRashAllowanceMultiplier = rashAllowanceMultiplier;
		myRashBonusAllowance = rashBonusAllowance;
	}
	public void setOverrideCautionFlag(boolean flagVal) {
		myOverrideCautionFlag = flagVal;
	}

	public Animation getAnimation() {
		return myAnimation;
	}
	public String getGestureName() {
		return myGestureName;
	}
	public String getScriptName() {
		return myAnimation.getName();
	}
	public boolean hasMoreFrames() {
		return myCurrentFrameIndex < myAnimation.getFrameCount();
	}
	public double getFractionComplete() {
		return ((double) myCurrentFrameIndex) / ((double) myAnimation.getFrameCount());
	}
	@Override public String  getTypeString() {
		return "AnimationExecJob";
	}
	@Override public String getContentSummaryString() {
		return "scriptName=" + myAnimation.getName()
				+ ", gestureName=" + myGestureName
				+ ", frameCount=" + myAnimation.getFrameCount()
				+ ", fractionComplete=" + getFractionComplete()
				+ ", rashAllowanceMult=" + myRashAllowanceMultiplier
				+ ", rashBonusAllowance=" + myRashBonusAllowance
				+ "]";
	}

	public Set<Joint> getUsedJoints() {
		if (myCachedUsedJointSet == null) {
			myCachedUsedJointSet = this.myAnimation.getUsedJointSet();
		}
		return myCachedUsedJointSet;
	}
	@Override public JVFrame contributeVelFrame(Frame prevPosAbsRomFrame, JVFrame prevVelRomFrame, Set<Joint> cautionJoints) {
		if (!hasMoreFrames()) {
			// All done!  (And why are you still calling me?  It's O-VER!)
			return new JVFrame();
		}
		if (myOverrideCautionFlag) {
			cautionJoints = new HashSet<Joint>();
		}
		JVFrame rashVelSubframe = getVelSubframeForRashJoints(prevPosAbsRomFrame, cautionJoints);
		JVFrame cautVelSubframe = getVelSubframeForCautionJoints(cautionJoints);
		
		JVFrame completeVelFrame = JVFrame.sumJVFrames(rashVelSubframe, cautVelSubframe);
		myCurrentFrameIndex++;
		if (!hasMoreFrames()) {
			theLogger.info("AnimationExecJob completed!");
			setStatus(Status.COMPLETED);
		}
		// theLogger.info("AnimationExecJob output: " + targetVelFrame.toString());
		return JVFrame.makeFrom(completeVelFrame);

	}
	protected JVFrame getVelSubframeForRashJoints(Frame prevPosAbsRomFrame,
				Set<Joint> cautionJoints) {
		Frame relVelSubframe = null;
		HashSet<Joint> absoluteJoints = getAbsoluteJointsForCautionContext(cautionJoints);

		if (myCurrentFrameIndex != 0) {
			JPRRFrame goalDeltaFrame =
						getCurrentGoalDeltaSubframe(absoluteJoints);
			JPARFrame goalAbsFrame =
						getUnrampedPositionTargetsOnAbsoluteJoints(absoluteJoints);

			Frame ppAbsSubframe = prevPosAbsRomFrame.getSubframe(absoluteJoints, false);
			JPARFrame prevPosJPARF = JPARFrame.makeFrom(ppAbsSubframe);
			JPRRFrame naiveDeltaFrame = prevPosJPARF.computeRelFrame(goalAbsFrame);

			double goalAllowanceCoeff = myRashAllowanceMultiplier;
			double fixedAllowance = myRashBonusAllowance;
			
			JPRRFrame actualDeltaFrame = SlopeBlendFuncs.computeActualDeltaFrame(
					naiveDeltaFrame, goalDeltaFrame, goalAllowanceCoeff, fixedAllowance);
			double secPerFrame = getAnimoidConfig().getSecondsPerFrame();
			relVelSubframe = actualDeltaFrame.makeVelFrame(secPerFrame);
		}
		return JVFrame.makeFrom(relVelSubframe);
	}
	
	protected JVFrame getVelSubframeForCautionJoints(Set<Joint> cautionJoints) {
		Frame relVelSubframe = null;
		
		Set<Joint> usedCautiousJoints = new HashSet<Joint>(cautionJoints);
		usedCautiousJoints.retainAll(getUsedJoints());
		if (myCurrentFrameIndex != 0) {
			JPRRFrame goalDeltaSubframe = getCurrentGoalDeltaSubframe(usedCautiousJoints);
			double secPerFrame = getAnimoidConfig().getSecondsPerFrame();
			relVelSubframe = goalDeltaSubframe.makeVelFrame(secPerFrame);
		}
		return JVFrame.makeFrom(relVelSubframe);
	}
	protected JPRRFrame getCurrentGoalDeltaSubframe(Set<Joint> joints) {
		Frame goalDeltaSubframe = null;
		if (myCurrentFrameIndex != 0) {
			goalDeltaSubframe = myAnimation.getPartialFrameInCoordinateSystem(myCurrentFrameIndex,
					joints, JointStateCoordinateType.FLOAT_REL_RANGE_OF_MOTION);
		}
		return JPRRFrame.make(goalDeltaSubframe);
	}
	protected HashSet<Joint> getAbsoluteJointsForCautionContext(Set<Joint> cautionJoints) {
		HashSet<Joint> absoluteJoints = new HashSet<Joint>(getUsedJoints());
		absoluteJoints.removeAll(cautionJoints);
		return absoluteJoints;
	}

	protected JPARFrame getUnrampedPositionTargetsOnAbsoluteJoints(Set<Joint> absoluteJoints) {
		if (!hasMoreFrames()) {
			// All done!  (And WHY are you STILL calling me?  It's O-VER!)
			return new JPARFrame();
		}

		Frame targetAbsSubframe = myAnimation.getPartialFrameInCoordinateSystem(myCurrentFrameIndex, absoluteJoints,
					JointStateCoordinateType.FLOAT_ABS_RANGE_OF_MOTION);
		return JPARFrame.makeFrom(targetAbsSubframe);
	}

	public Frame takeFrameAndAdvance() {
		// Unused in Distro 17b+
		Frame currentFrame = null;
		if (hasMoreFrames()) {
			currentFrame = myAnimation.getFrameAt(myCurrentFrameIndex++);	
		}	
		if (!hasMoreFrames()) {
			theLogger.info("AnimationExecJob completed!");
			setStatus(Status.COMPLETED);
		}
		return currentFrame;
	}
	
}
