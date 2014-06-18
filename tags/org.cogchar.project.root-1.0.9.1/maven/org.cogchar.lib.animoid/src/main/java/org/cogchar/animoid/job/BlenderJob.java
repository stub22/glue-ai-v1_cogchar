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

package org.cogchar.animoid.job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cogchar.animoid.calc.estimate.PositionEstimator;
import org.cogchar.animoid.calc.estimate.TimeKeeper;
import org.cogchar.api.animoid.config.bonus.AnimoidConfig;
import org.cogchar.api.animoid.protocol.Frame;
import org.cogchar.api.animoid.protocol.JVFrame;
import org.cogchar.api.animoid.protocol.Joint;
import org.cogchar.api.animoid.protocol.JointStateCoordinateType;
import org.cogchar.api.animoid.protocol.JointVelocityAROMPS;
import org.cogchar.zzz.platform.stub.JobSpaceStub;
import org.cogchar.platform.util.TimeUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BlenderJob extends AnimoidJob implements PositionEstimator, TimeKeeper {
	private static Logger	theLogger = LoggerFactory.getLogger(BlenderJob.class.getName());
	// We could just keep the frame of velocities from PREVIOUS eval of transformFrame.
	// But...this is incompatible with Joystick and other still external (C++) influences.
	// Also incompatible with adjustments to the current-pos-snapshot based on 
	// servo information feedback.  
	// Also incompatible with truncation of movement due to boundaries.
	// So, better to subtract last position snapshot
	// from this position snapshot, and use THAT as velocity estimate.
	private		Frame				myPrevInputPosFrame;
	private		Frame				myPrevOutputPosFrame;
	private		Frame				myPrevNominalVel;
	// prevTS = approximate time at which prevFrame was sent to servo controllers
	private		long				myPrevTimestampMsec;

	// dubious: list of jobs duplicates Thalamus contents.
	private		List<MotionJob>		myMotionJobs;
	// Doubly dubious!  And publicly so!
	// public		AttentionJob		theTestAttentionJob;
	public		BlinkJob			theTestBlinkJob;
	public		VisemeJob			theTestVisemeJob;

	private		long				myFirstTimestampMsec;
	private		long				myLastKeyframeTimestampMsec;
	private		long				myTotalFramesProcessed = 0;
	
	public BlenderJob (AnimoidConfig aconf) {
		super(aconf);
		myMotionJobs =  new ArrayList<MotionJob>();
	}
	public synchronized void registerMotionJob(MotionJob mj) {
		myMotionJobs.add(mj);
		mj.setTimeKeeper(this);
	}
	public synchronized void unregisterMotionJob(MotionJob mj) {
		myMotionJobs.remove(mj);
	}
	protected synchronized void dropDeadJobs() {
		Iterator<MotionJob> mji = myMotionJobs.iterator();
		while (mji.hasNext()) {
			MotionJob mj = mji.next();
			if (!mj.mayBeRunnableNowOrLater()) {
				// This job is ended or ending.  Give it the boot.
				theLogger.info("Dropping motion job: " + mj); //  of type " + mj.getTypeString() + " with status " + mjStatus);
				mji.remove();
			}
		}
	}

	protected Collection<MotionJob> getMotionJobs() {
		return myMotionJobs;
	}
	protected Set<Joint> compileCautionJoints() {
		Collection<MotionJob> jobs = getMotionJobs();
		Set<Joint> resultSet = new HashSet<Joint>();
		for (MotionJob mj: jobs) {
			Collection<Joint> jobCautionJoints = mj.getCautionJoints();
			if (jobCautionJoints != null) {
				resultSet.addAll(jobCautionJoints);
			}
		}
		return resultSet;
	}

	@Override public Double getNominalSecPerFrame() {
		return getAnimoidConfig().getSecondsPerFrame();
	}
	@Override public Double getFrameDurationSmoothingFactor() {
		return getAnimoidConfig().getFrameDurationSmoothingFactor();
	}
	public synchronized Frame transformFrame(Frame currPosAbsRomFrame) {
		// This method implements our java animation system, as a position
		// transform from estimated current position to desired next position.
		long xformStartStamp = TimeUtils.currentTimeMillis();
		double frameDeltaSec = (xformStartStamp - myPrevTimestampMsec) / 1000.0;
		theLogger.trace("****************** Starting xform at: " + xformStartStamp +
				", which is " + frameDeltaSec + " sec since last frame-xform-start");
		
		if (myPrevOutputPosFrame == null) {
			// Initialize state and timing vars on first iteration.
			myPrevOutputPosFrame = currPosAbsRomFrame;
			myPrevInputPosFrame = currPosAbsRomFrame;
			// These timing vars do not need to be absolutely precise.
			myPrevTimestampMsec = xformStartStamp - Math.round(getNominalSecPerFrame() * 1000.0);
			myFirstTimestampMsec = xformStartStamp;
			myLastKeyframeTimestampMsec = xformStartStamp;
		}

		// Note that my prev OUT frame is not involved in the velocity calc.
		// If all went great on prev frame, then currPosAbsRom will be same as my previous out,
		// but there are many reasons it might not be.  (Truncation, Joystick, ...)
		Frame prevVelFrame = Frame.computeDerivativeFrame(JointStateCoordinateType.FLOAT_VEL_RANGE_OF_MOTION_PER_SEC,
					myPrevInputPosFrame, currPosAbsRomFrame, getNominalSecPerFrame());
		JVFrame prevJVFrame = JVFrame.makeFrom(prevVelFrame);
		JVFrame velSumFrame = new JVFrame();
		Map<Joint, Integer> contribCounts = new HashMap<Joint, Integer>();
		Set<Joint> cautionJoints = compileCautionJoints();
		dropDeadJobs();
		Collection<MotionJob> jobs = getMotionJobs();
		Frame currentPosEstAbsROM = estimatePositionNow(true);
		for (MotionJob mj: jobs) {
			JVFrame jobVelFrame = mj.contributeVelFrame(currentPosEstAbsROM, prevJVFrame, cautionJoints);
			if (jobVelFrame != null) {
				velSumFrame = JVFrame.sumJVFrames(velSumFrame, jobVelFrame);
				// This contribution counting is unnecessary at the moment,
				// since the averaging multiplier below is disabled.
				// updateContribCounts(contribCounts, jobVelFrame);
			}
		}
		// We want strict summation rather than averaging, because
		// 1) We only care about the interaction of gaze + scripted anim
		// 2) We have an assumption that scripted anims which affect gaze
		// are gaze-direction-neutral (eyes compensate for head, etc).
		// scaleSummedVelocitiesIntoAverages(velSumFrame, contribCounts);
	

		Frame<JointVelocityAROMPS> averageV = velSumFrame; // nextVelFrame;
		// Integrate (i.e. multiply) the velocity vector for the frame length to
		// produce a delta-position vector.
		Frame deltaP = averageV.integrate(getNominalSecPerFrame());

		Frame nextP = currPosAbsRomFrame.copy();
		nextP.addDeltaFrame(deltaP);
		// Note that this truncation does not cause the nominalVel to be corrected.
		nextP.truncate();
		// So now we correct it!
		Frame betterVelEstimate = Frame.computeDerivativeFrame(
				JointStateCoordinateType.FLOAT_VEL_RANGE_OF_MOTION_PER_SEC, currPosAbsRomFrame,
				nextP, getNominalSecPerFrame());
		myPrevInputPosFrame = currPosAbsRomFrame;
		myPrevOutputPosFrame = nextP;
		// Save our estimate of output velocity for use on next frame's computation.
		myPrevNominalVel = betterVelEstimate;
		long xformEndStamp = TimeUtils.currentTimeMillis();
		double elapsedSinceLastXformEnd = (xformEndStamp - myPrevTimestampMsec) / 1000.0;
		myPrevTimestampMsec = xformEndStamp;	
		myTotalFramesProcessed++;
		// We are done with real work.  Now update diagnostic counters and timers.
		if ((myTotalFramesProcessed % 100) == 0) {
			long totalMsec = xformEndStamp - myFirstTimestampMsec;
			long avgFramePeriod = totalMsec / myTotalFramesProcessed;
			long lastIntervalMsec = xformEndStamp - myLastKeyframeTimestampMsec;
			long recentAvgFramePeriod = lastIntervalMsec / 100;
			theLogger.info("After " + myTotalFramesProcessed + " frames, average motion frame msec=" + avgFramePeriod + ", last 100 frames averaged " + recentAvgFramePeriod);
			myLastKeyframeTimestampMsec = xformEndStamp;
		}
		double xformElapsedSec = (xformEndStamp - xformStartStamp) / 1000.0;
		// theLogger.info("BlenderJob output: " + nextP);
		theLogger.trace("*************************** Finished transform at: " + xformEndStamp
					+ ", xformElapsedSec=" + xformElapsedSec
					+ ", time since last frameStamp=" + elapsedSinceLastXformEnd);
		return nextP;
	}

	public void setupTestMotionJobs( AnimoidConfig aconf, JobSpaceStub jobSpace) { // SightModel sm,
		//theTestAttentionJob = new AttentionJob(this, sm, aconf, jobSpace);
	//	jobSpace.postManualJob(theTestAttentionJob);
		theTestBlinkJob = new BlinkJob(aconf);
		registerMotionJob(theTestBlinkJob);
		jobSpace.postManualJob(theTestBlinkJob);
		theTestVisemeJob = new  VisemeJob(aconf, getNominalSecPerFrame());
		registerMotionJob(theTestVisemeJob);
		jobSpace.postManualJob(theTestVisemeJob);
		// AttentionJob is not a motion job.  It is a manager of motion jobs.
		// Currently we are avoiding the obvious potential delegation pattern.
		// registerMotionJob(theTestAttentionJob);
	}
	/**
	 * @param tstampMsec - which is either "fresh" or "old".
	 * Fresh means >= the start time of our last output  frame calculation in the
	 * transformFrame method.  Since both methods are synchronized, there is
	 * a possibility that this method is blocked during that method's exec,
	 * allowing this tstampMsec to become "old".
	 *
	 * @return Frame containing enhanced estimated positions at moment, if it
	 * is "fresh", otherwise the last output position (for "old" timestamps).
	 */
	public synchronized Frame estimatePositionAtMoment(long tstampMsec) {
		if (myPrevNominalVel == null) {
			return myPrevOutputPosFrame;
		}
		double deltaTsec = (tstampMsec - myPrevTimestampMsec) / 1000.0;
		if (deltaTsec >= 0.0) {
			double servoFrameSmoothing = getFrameDurationSmoothingFactor();
			double servoFrameSpaceTsec = getNominalSecPerFrame() * servoFrameSmoothing;
			
			double motionFractionComplete = deltaTsec / servoFrameSpaceTsec;
			if (motionFractionComplete > 1.0) {
				theLogger.trace ("Cannot estimate position after motion complete, momentTS=" + tstampMsec + ", prevFrameTS=" + myPrevTimestampMsec);
				return myPrevOutputPosFrame;
			}
			// We need to subtract the amount of motion not yet completed from the previous
			// goal frame.  (We shouldn't start from the frame previous to that, because it
			// is less accurate w.r.t the action of any C++ blend rules).
			double reverseMotionFraction = 1.0 - motionFractionComplete;
			// Limitation:  If non-java blend rules (e.g. Joystick) are in play, then this
			// estimate (and other estimates) are hosed.
			Frame deltaP = myPrevNominalVel.integrate(-1.0 * reverseMotionFraction * getNominalSecPerFrame());
			Frame posEst = myPrevOutputPosFrame.copy();
			posEst.addDeltaFrame(deltaP);
			// theLogger.info("BlenderJob returning posEstimate: " + posEst);
			return posEst;
		} else {
			theLogger.trace ("Cannot estimate position before motion started, momentTS=" + tstampMsec + ", prevFrameTS=" + myPrevTimestampMsec);
			return myPrevOutputPosFrame;
		}
	}
	public synchronized Frame estimateVelocityAtMoment(long tstampMsec) {
		return myPrevNominalVel;
	}
	public Frame estimatePositionRoughly() {
		return myPrevOutputPosFrame;
	}
	public String getContentSummaryString() {
		return "Later";
	}	
	public String  getTypeString() {
		return "BlenderJob";
	}
	public Frame estimatePositionNow(boolean enhancedAccuracy) {
		Frame jointPosSnap = null;
		if (enhancedAccuracy) {
			long nowMsec = TimeUtils.currentTimeMillis();
			jointPosSnap = estimatePositionAtMoment(nowMsec);
		} else {
			jointPosSnap = estimatePositionRoughly();
		}
		return jointPosSnap;
	}
}
