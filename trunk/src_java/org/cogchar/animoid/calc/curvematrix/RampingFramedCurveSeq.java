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

package org.cogchar.animoid.calc.curvematrix;

import org.cogchar.animoid.calc.curve.ConstAccelCurve;
import org.cogchar.animoid.calc.optimize.ParameterVector;
import org.cogchar.calc.number.NumberFactory;

import org.jscience.mathematics.number.Number;
import org.jscience.mathematics.function.Variable;
import org.jscience.mathematics.structure.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RampingFramedCurveSeq<RN extends Number<RN> & Field<RN>> extends ConstAccelCurveSequence<RN> {
	private static Logger		theLogger = LoggerFactory.getLogger(RampingFramedCurveSeq.class.getName());

	public enum Phase {
		BRAKE_INIT, // Get our velocity into a normal range of 0 to maxVel in progress direction.
					// This may involve either stopping a regressive motion, or slowing a
					// progressive motion.
		ACCEL_HARD,
		ACCEL_SOFT, // At most 1 frame,
		COAST,
		BRAKE_SOFT, // Always exactly 1 frame
		BRAKE_HARD
	}
	public double minWorldPosDeg;
	// <= 0, always a position below "default"
	public double maxWorldPosDeg;
	// >= 0, always a position above "default"
	public double maxVelMagDPS;
	// >= 0
	public double maxAccelMagPerFrame;
	// >= 0  DPS-per-frame
	public double maxDecelMagPerFrame;
	// >= 0  DPS-per-frame
	public double initWorldVelDPS;
	public double initWorldPosDeg;
	// These are both signed
	public double postIBWorldVelDPS;
	// What's our speed after initial braking phase?
	public double goalDirectionSign;
	// +1.0 for positive, -1.0 for negative
	public double frameLenSec;

	public RampingFramedCurveSeq(String name, NumberFactory<RN> numberFactory) {
		super(name, numberFactory);
		setupCurves();
	}

	private void setupCurves() {
		String seqName = getName();
		for (Phase p : Phase.values()) {
			String nameSuffix = p.name();
			String curveSymbolSuffix = seqName + "_" + nameSuffix;
			Variable<RN> timeOffsetVar = new Variable.Local<RN>("_t_" + nameSuffix);
			ConstAccelCurve curve = new ConstAccelCurve(curveSymbolSuffix, timeOffsetVar,  getNumberFactory());
			addStepCurve(curve);
		}
	}

	public double getSignedInitProgressRateDPS() {
		// Positive if initWorldVelDPS is in same direction as goal, else neg.
		return initWorldVelDPS * goalDirectionSign;
	}

	private RN getEndPositionForCurrentParams() {
		propagateEndpointConditions();
		RN totalDuration = getTotalDuration();
		RN endPos = getPosAtTime(totalDuration);
		return endPos;
	}

	private double getYieldForPosition(double endPosVal) {
		double deltaPos = endPosVal - initWorldPosDeg;
		double yield = goalDirectionSign * deltaPos;
		return yield;
	}

	private double getYieldForCurrentParamsIgnoringPosConstraints() {
		RN endPos = getEndPositionForCurrentParams();
		return getYieldForPosition(endPos.doubleValue());
	}

	public RN getYieldForCurrentParamsWithPosConstraints(String dbgHeader, double truncationWarningThresh,  double negativeYieldWarningThresh) {
		RN endPos = getEndPositionForCurrentParams();
		double endPosVal = endPos.doubleValue();
		if (endPosVal > maxWorldPosDeg) {
			if (endPosVal - maxWorldPosDeg > truncationWarningThresh) {
				theLogger.trace(dbgHeader + "[seqName=" + getName() + "] truncating endPosVal from " + endPosVal + " to max=" + maxWorldPosDeg);
			}
			endPosVal = maxWorldPosDeg;
		}
		if (endPosVal < minWorldPosDeg) {
			if (minWorldPosDeg - endPosVal > truncationWarningThresh) {
				theLogger.trace(dbgHeader + "[seqName=" + getName() + "] truncating endPosVal from " + endPosVal + " to min=" + minWorldPosDeg);
			}
			endPosVal = minWorldPosDeg;
		}
		double yield = getYieldForPosition(endPosVal);
		if (yield < negativeYieldWarningThresh) {
			// This should happen only if initWorldVelDPS is in wrong direction and
			// we don't have time to accelerate into positive territory
			// (Because of insufficient braking power to use during initialBraking)
			theLogger.warn(dbgHeader + "[seqName=" + getName() + "] got negative yield: " + yield + " on curveSeq=" + toString() + ",  endPos=" + endPosVal + ", totalDur=" + getTotalDuration() + ", initProgessRateDPS=" + getSignedInitProgressRateDPS() + ", minFramesToStopFromInit=" + minFramesToStopFromInitVel());
		}
		return getNumberFactory().makeNumberFromDouble(yield);
	}

	public void syncInitialConditions() {
		RN initPosRN = getNumberFactory().makeNumberFromDouble(initWorldPosDeg);
		RN initVelRN = getNumberFactory().makeNumberFromDouble(initWorldVelDPS);
		setInitialConditions(initPosRN, initVelRN);
	}

	/*
	public double maxInitialBrakingMagPerFrame() {
	return Math.max(maxAccelMagPerFrame, maxDecelMagPerFrame);
	}
	 */
	public int minFramesToStopFromInitVel() {
		// Note - initVelMag may be larger than maxVelMagDPS!
		// Idea:  we should allow using the larger of either accel or deccel when coming
		// out of a negative or too-large initial velocity.  (Currently we are required
		// to use accel, which may not be able to stop as fast as this minFrames value
		// indicates).
		double initVelMag = Math.abs(initWorldVelDPS);
		return RampingFramedCACM.minFramesToChangeSpeed(initVelMag, maxDecelMagPerFrame);
	}

	private double initBrakingTargetProgressRate() {
		double initProgressRateDPS = getSignedInitProgressRateDPS();
		double initialTargetRate = initProgressRateDPS;
		if (initProgressRateDPS < 0.0) {
			initialTargetRate = 0.0;
		} else if (initProgressRateDPS > maxVelMagDPS) {
			initialTargetRate = maxVelMagDPS;
		}
		return initialTargetRate;
	}

	public void establishParamsForMaxYieldIgnoringPosConstraints(int frameCount) {
		int brakeFramesBareMin = minFramesToStopFromInitVel();
		if (frameCount < brakeFramesBareMin) {
			// This shouldn't happen, b/c we already checked before calling this method.
			throw new RuntimeException("Can\'t even stop from initVel in allowed frames");
		}
		double initProgressRateDPS = getSignedInitProgressRateDPS();
		double progRatePostIB = initBrakingTargetProgressRate();
		postIBWorldVelDPS = goalDirectionSign * progRatePostIB;
		double ibSignedDeltaVel = postIBWorldVelDPS - initWorldVelDPS;
		double ibdvMag = Math.abs(ibSignedDeltaVel);
		int ibFrameCount = 0;
		if (ibdvMag > 0.001) {
			ibFrameCount = (int) RampingFramedCACM.minFramesToChangeSpeed(ibdvMag, maxDecelMagPerFrame);
		}
		if (ibFrameCount > frameCount) {
			throw new RuntimeException("We don\'t even have time for initial braking manuever!");
		}
		if (ibFrameCount >= frameCount - 1) {
			// Time is so short that we should just use the initial braking manuever as our
			// entire motion.
			progRatePostIB = 0.0;
			postIBWorldVelDPS = 0.0;
			ibSignedDeltaVel = -1.0 * initWorldVelDPS;
			ibdvMag = Math.abs(ibSignedDeltaVel);
			ibFrameCount = frameCount;
		}
		double ibAccelDPSPS = (ibFrameCount > 0) ? ibSignedDeltaVel / (ibFrameCount * frameLenSec) : 0.0;
		int postIBframeCount = frameCount - ibFrameCount;
		int framesHardUp = maxPossibleHardUpFrames(postIBframeCount);
		int framesPostHU = postIBframeCount - framesHardUp;
		double progRatePostHardUp = progRatePostIB + framesHardUp * maxAccelMagPerFrame;
		double absPRPHU = Math.abs(progRatePostHardUp);
		int minBrakeFrames = 0;
		if (absPRPHU > 0.001) {
			minBrakeFrames =  RampingFramedCACM.minFramesToChangeSpeed(absPRPHU, maxDecelMagPerFrame);
		}
		double peakLegitVelMag = progRatePostHardUp;
		// If needed, the softDown phase can be at maxDecel rate.
		int framesSoftUp = 0;
		int framesCoast = 0;
		int framesSoftDn = 1;
		int framesHardDn = 0;
		framesHardDn = minBrakeFrames - framesSoftDn;
		if (minBrakeFrames > framesPostHU) {
			throw new RuntimeException("CalcError[" + getName() + "]: Can\'t stop after hardUp phase "
						+ ", totalFrameCount=" + frameCount
						+ ", progRatePostHU=" + progRatePostHardUp
						+ ", minBrakeFrames=" + minBrakeFrames
						+ ", framesPostHU=" + framesPostHU
						+ ", ibFrameCount=" + ibFrameCount
						+ ", framesHardUp=" + framesHardUp);
		} else if (minBrakeFrames < framesPostHU) {
			// Soft Accel could be anywhere from 0 to maxAccel
			framesSoftUp = 1;
		}
		framesCoast = framesPostHU - framesSoftUp - framesSoftDn - framesHardDn;
		ParameterVector durPV = new ParameterVector(getNumberFactory());
		durPV.setLength(Phase.values().length);
		durPV.setValue(Phase.BRAKE_INIT.ordinal(), ibFrameCount * frameLenSec);
		durPV.setValue(Phase.ACCEL_HARD.ordinal(), framesHardUp * frameLenSec);
		durPV.setValue(Phase.ACCEL_SOFT.ordinal(), framesSoftUp * frameLenSec);
		durPV.setValue(Phase.COAST.ordinal(), framesCoast * frameLenSec);
		durPV.setValue(Phase.BRAKE_SOFT.ordinal(), framesSoftDn * frameLenSec);
		durPV.setValue(Phase.BRAKE_HARD.ordinal(), framesHardDn * frameLenSec);
		setDurationParams(durPV);
		ParameterVector accPV = new ParameterVector(getNumberFactory());
		accPV.setLength(Phase.values().length);
		accPV.setValue(Phase.BRAKE_INIT.ordinal(), ibAccelDPSPS);
		accPV.setValue(Phase.ACCEL_HARD.ordinal(), goalDirectionSign * maxAccelMagPerFrame / frameLenSec);
		accPV.setValue(Phase.COAST.ordinal(), 0.0);
		// But in the weird case where decel is actually to stop our huge negative
		// init vel, this sign should be different!
		accPV.setValue(Phase.BRAKE_HARD.ordinal(), -1.0 * goalDirectionSign * maxDecelMagPerFrame / frameLenSec);
		// The accelSoft value is between 0.0 and the hardAccel value.
		// It cannot cause us to exceed maxVel, or to go faster than
		// we can stop using BRAKE_HARD + BRAKE_SOFT.
		double softAccelFrameMag = 0.0;
		if (framesSoftUp == 1) {
			double maxTotalDecel = maxDecelMagPerFrame * (framesHardDn + framesSoftDn);
			double excessDecel = maxTotalDecel - peakLegitVelMag;
			if (excessDecel < 0.0) {
				throw new RuntimeException("CalcError[" + getName() + "]:  can\'t stop from peakVel even before softAccel added");
			}
			double peakVelHeadroom = maxVelMagDPS - peakLegitVelMag;
			if (peakVelHeadroom < 0.0) {
				peakVelHeadroom = 0.0;
			}
			softAccelFrameMag = Math.min(excessDecel, peakVelHeadroom);
		}
		double actualPeakVel = peakLegitVelMag + softAccelFrameMag;
		double softDecelFrameVal = actualPeakVel - framesHardDn * maxDecelMagPerFrame;
		// Matt added the 0.1 tolerance parameter, right?
		if (softDecelFrameVal > maxDecelMagPerFrame + 000.1) {
			throw new RuntimeException("CalcError[" + getName() + "]:  softDecelFrameVal " + softDecelFrameVal + " exceeds max!");
		}
		accPV.setValue(Phase.ACCEL_SOFT.ordinal(), goalDirectionSign * softAccelFrameMag / frameLenSec);
		accPV.setValue(Phase.BRAKE_SOFT.ordinal(), -1.0 * goalDirectionSign * softDecelFrameVal / frameLenSec);
		setAccelParams(accPV);
	}

	/*
	 * Assumptions:
	 * 1) It's possible to stop from nomSignedInitVel using maxDecel within frameCount.
	 * 2) The HardUp accel will not cause us to hit a position boundary.
	 */
	public int maxPossibleHardUpFrames(int frameCount) {
		if (frameCount == 0) {
			return 0;
		}
		double initProgressRateDPS = postIBWorldVelDPS * goalDirectionSign;
		// getSignedInitProgressRateDPS();
		int validHardUpFrames = 0;
		while (true) {
			int postHardUpFrames = frameCount - validHardUpFrames;
			double validTotalHardAccelMag = validHardUpFrames * maxAccelMagPerFrame;
			double validVelMagAfterHA = Math.abs(initProgressRateDPS + validTotalHardAccelMag);
			int framesToStopAfterHA = RampingFramedCACM.minFramesToChangeSpeed(validVelMagAfterHA, maxDecelMagPerFrame);
			if (framesToStopAfterHA > postHardUpFrames) {
				theLogger.error("Troubled seq dump: " + this.toString());
				throw new RuntimeException("CalcError[" + getName() + "]- can\'t stop in time:  frameCount=" + frameCount
							+ ", postHardUpFrames=" + postHardUpFrames + ", framesToStopAfterHA=" + framesToStopAfterHA + ", ");
			} else if (framesToStopAfterHA == postHardUpFrames) {
				break;
			} else {
				// We've got a little time-breathing room, but:
				// 1) Would another hard-up push us over maxVelMagDPS?
				// 2) Is there enough TIME for another hard-up?
				int candHardUpFrames = validHardUpFrames + 1;
				double candTotalHardAccel = candHardUpFrames * maxAccelMagPerFrame;
				double candPeakVelMag = initProgressRateDPS + candTotalHardAccel;
				if (candPeakVelMag > maxVelMagDPS) {
					// This would cause us to exceed maxVel.
					break;
				}
				// If nomSignedInitVel is large and negative, the meaning of
				// these "braking" calcs gets distored.
				int framesToStopAfterCHA = RampingFramedCACM.minFramesToChangeSpeed(Math.abs(candPeakVelMag), maxDecelMagPerFrame);
				if (framesToStopAfterCHA > (frameCount - candHardUpFrames)) {
					// Not enough room to stop.
					break;
				}
				validHardUpFrames = candHardUpFrames;
			}
		}
		if ((initProgressRateDPS > maxVelMagDPS) && (validHardUpFrames != 0)) {
			throw new RuntimeException("initProgressRateDPS=" + initProgressRateDPS + ", maxVelMagDPS=" + maxVelMagDPS + ", validHardUpFrames=" + validHardUpFrames);
		}
		return validHardUpFrames;
	}

	public void lowerYieldToTargetValueByReducingAccel(double targetYield) {
		// Again, the position constraints are not our problem.
		// targetYield should not cause us to exceed them!
		double currentUnconstrainedYield = getYieldForCurrentParamsIgnoringPosConstraints();
		if (targetYield > currentUnconstrainedYield) {
			throw new RuntimeException("CalcError[" + getName() + "]: requested targetYield: " + targetYield + " is higher than current unconstrained yield " + currentUnconstrainedYield);
		}
		double yieldRatio = targetYield / currentUnconstrainedYield;
	}

	@Override
	public String toString() {
		return "CurveSeq[name=" + getName()
			+ ", minPos=" + minWorldPosDeg
			+ ", maxPos=" + maxWorldPosDeg
			+ ", maxVelMag=" + maxVelMagDPS
			+ ", maxAccelMagPerFrame=" + maxAccelMagPerFrame
			+ ", maxDecelMagPerFrame=" + maxDecelMagPerFrame
			+ ", initVel=" + initWorldVelDPS
			+ ", initPos=" + initWorldPosDeg
			+ ", postIBWorldVelDPS=" + postIBWorldVelDPS
			+ ", goalDirectionSign=" + goalDirectionSign
			+ ", frameLenSec=" + frameLenSec
			+ ", super=" + super.toString() + "]";
	}
}
