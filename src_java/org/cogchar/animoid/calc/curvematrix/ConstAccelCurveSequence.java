/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.animoid.calc.curvematrix;

import org.cogchar.calc.number.NumberFactory;
import org.cogchar.animoid.calc.curve.*;
import org.cogchar.animoid.calc.optimize.ParameterVector;

import org.cogchar.calc.function.BumpUF;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jscience.mathematics.function.Variable;
import org.jscience.mathematics.number.Number;

import org.jscience.mathematics.structure.Field;
import org.jscience.mathematics.vector.Vector;
import static org.cogchar.animoid.calc.curve.ConstAccelCurveStateVarSymbol.*;

/**
 * @param <RN> 
 * @author Stu B. <www.texpedient.com>

 * Each of our added curves will have a different timeOffset variable,
 * shared with stepCurves *in other sequences*, and other polynomials.
 *
 * Usage pattern:
 *   1) Add step curves.
 *   2) In any order, set accels, durations, and initial conditions
 *   3) Propagate endpoint conditions.
 *   4) Evaluate other functions dependent on the step curves (outside this class).
 * Then redo 2,3,4 as needed.
 *
 * Note that if the contents of accel or duration
 */
public class ConstAccelCurveSequence<RN extends Number<RN> & Field<RN>>  {
	private String						myName;
	private List<ConstAccelCurve<RN>>	myStepCurves = new ArrayList<ConstAccelCurve<RN>>();
	private ParameterVector<RN>			myDurationPV, myAccelPV;
	private NumberFactory<RN>			myNumberFactory;
	private	boolean						myDirtyFlag = true;

	public class StateVarParamPartials {
		public		Vector<RN>	partialsForDurations;
		public		Vector<RN>	partialsForAccels;
	}

	public Map<Variable<RN>, StateVarParamPartials>		myJacobianMap;

	public ConstAccelCurveSequence(String name, NumberFactory<RN> numberFactory) {
		myName = name;
		myNumberFactory = numberFactory;
	}
	public String getName() {
		return myName;
	}
	protected NumberFactory<RN> getNumberFactory() {
		return myNumberFactory;
	}
	public void addStepCurve(ConstAccelCurve<RN> nextCurve) {
		myStepCurves.add(nextCurve);
	}
	public ConstAccelCurve<RN> getStepCurve(int idx) {
		return myStepCurves.get(idx);
	}
	public ConstAccelCurve<RN> getFirstStepCurve() {
		return getStepCurve(0);
	}
	public int getStepCount() {
		return myStepCurves.size();
	}
	public void setAccelParams(ParameterVector<RN> accVec) {
		myDirtyFlag = true;
		int stepCount = getStepCount();
		if (accVec.getLength() != myStepCurves.size()) {
			throw new  RuntimeException("Mismatched sizes for curveSeq="
					+ this + " and accel-vec=" + accVec);
		}
		myAccelPV = accVec;
	}
	private void syncAccelParams(ParameterVector accVec) {
		int stepCount = getStepCount();
		for (int idx=0; idx < stepCount; idx++) {
			double accelVal = accVec.getValue(idx);
			setAccelParam(idx, accelVal);
		}
	}
	public void setAccelParam(int curveIndex, double accelValue) {
		RN accelValueNum = myNumberFactory.makeNumberFromDouble(accelValue);
		setAccelParam(curveIndex, accelValueNum);
	}
	public void setAccelParam(int curveIndex, RN accelValueNum) {
		myDirtyFlag = true;
		ConstAccelCurve<RN> c = getStepCurve(curveIndex);
		c.setStateVarVal(CONST_ACCEL, accelValueNum);
	}
	public void setDurationParams(ParameterVector<RN> durVec) {
		myDirtyFlag = true;
		int stepCount = getStepCount();
		if (durVec.getLength() != stepCount) {
			throw new  RuntimeException("Mismatched sizes for curveSeq="
					+ this + " and duration-vec=" + durVec);
		}
		myDurationPV = durVec;
	}
	public void setInitialConditions(RN initPos, RN initVel) {
		myDirtyFlag = true;
		ConstAccelCurve firstCurve = getFirstStepCurve();
		firstCurve.setStateVarVal(INIT_POS, initPos);   // .setInitPosValue(initPos);
		firstCurve.setStateVarVal(INIT_VEL, initVel);  // setInitVelValue(initVel);
	}
	public RN getStepDuration(int idx) {
		return myDurationPV.getNumber(idx);
	}
	public RN getTotalDuration() {
		double durSum = myDurationPV.sumValues();
		return myNumberFactory.makeNumberFromDouble(durSum);
	}

	public void propagateEndpointConditions() {
		if (myDirtyFlag) {
			// TODO: Verify that accels, durations, and initial conditions are already set.
			syncAccelParams(myAccelPV);
			int stepCount = getStepCount();
			for (int idx=1; idx < stepCount; idx++) {
				ConstAccelCurve<RN> curve = getStepCurve(idx);
				ConstAccelCurve<RN> prevCurve = getStepCurve(idx - 1);
				RN prevDurationNum = getStepDuration(idx - 1);
				prevCurve.setStateVarVal(TIME_OFFSET, prevDurationNum);
				RN prevEndPos = prevCurve.getPositionAtCurrentState(); //getPosValueAtCurrentTimeOffset();
				RN prevEndVel = prevCurve.getVelocityAtCurrentState(); //  getVelValueAtCurrentTimeOffset();
				curve.setStateVarVal(INIT_POS, prevEndPos);
				curve.setStateVarVal(INIT_VEL, prevEndVel);
			}
			myDirtyFlag = false;
		}
	}
	protected Integer findStepIndexForTimeOffset(RN offset) {
		double sum = 0.0;
		if (myDurationPV == null) {
			return null;
		}
		Integer idx = myDurationPV.findIndexWithinImpliedSum(offset.doubleValue());
		return idx;
	}
	protected RN findOffsetWithinStep(int stepIdx, RN offsetFromSeqStart) {
		double prevSum = myDurationPV.sumLeadingValues(stepIdx);
		double localOffset = offsetFromSeqStart.doubleValue() - prevSum;
		return myNumberFactory.makeNumberFromDouble(localOffset);
	}

	public RN getAccelAtTime(RN timeOffset) {
		Integer stepIdx = findStepIndexForTimeOffset(timeOffset);
		if (stepIdx != null) {
			ConstAccelCurve<RN> curve = getStepCurve(stepIdx);
			// Accel is constant (hence the name "Const Accel Curve", so no need to update the offset.
			return curve.getAccelAtCurrentState();
		}
		return null;
	}
	// Caveat:  This changes the timeOffset state of one of our curves!
	public RN getVelAtTime(RN timeOffset) {
		Integer stepIdx = findStepIndexForTimeOffset(timeOffset);
		if (stepIdx != null) {
			ConstAccelCurve<RN> curve = getStepCurve(stepIdx);
			RN stepOffset = findOffsetWithinStep(stepIdx, timeOffset);
			curve.setTimeOffset(stepOffset);
			return curve.getVelocityAtCurrentState();
		}
		return null;
	}
	// Caveat:  This changes the timeOffset state of one of our curves!
	public RN getPosAtTime(RN timeOffset) {
		Integer stepIdx = findStepIndexForTimeOffset(timeOffset);
		if (stepIdx != null) {
			ConstAccelCurve<RN> curve = getStepCurve(stepIdx);
			RN stepOffset = findOffsetWithinStep(stepIdx, timeOffset);
			curve.setTimeOffset(stepOffset);
			return curve.getPositionAtCurrentState();
		}
		return null;
	}

	public void appendMotionFrameDump(StringBuffer buf, RN timeOffset) {
		RN pos = getPosAtTime(timeOffset);
		RN vel = getVelAtTime(timeOffset);
		RN acc = getAccelAtTime(timeOffset);
		buf.append("t=").append(timeOffset).append(" x=").append(pos);
		buf.append(" v=").append(vel).append(" a=").append(acc);
	}
	public String dumpMotionPlan(int sampleCount, double lastSampleTime) {
		StringBuffer	motionPlanBuf = new StringBuffer("CACS_plan[name=");
		motionPlanBuf.append(myName).append("\n");
		motionPlanBuf.append("durations=" + myDurationPV.toString() + "\n");
		motionPlanBuf.append("accelerts=" + myAccelPV.toString() + "\n");
		double sampleWidth = lastSampleTime / (sampleCount -1);
		for (int idx = 0; idx < sampleCount; idx++) {
			double sampleTime = idx * sampleWidth;
			RN sampleTimeRN = myNumberFactory.makeNumberFromDouble(sampleTime);
			appendMotionFrameDump(motionPlanBuf, sampleTimeRN);
			motionPlanBuf.append("\n");
		}
		motionPlanBuf.append("]");
		return motionPlanBuf.toString();
	}

	public Map<Variable<RN>, StateVarParamPartials>	getJacobianValueMatrixMap(
				List<Variable<RN>> stateVars) {
		Map<Variable<RN>, StateVarParamPartials> jacobMap =
					new HashMap<Variable<RN>, StateVarParamPartials>();
		for (Variable<RN> sv : stateVars) {
			StateVarParamPartials svpp = new StateVarParamPartials();
			jacobMap.put(sv, svpp);
		}
		return jacobMap;
	}
	public BumpUF<RN, RN> getBumpFunction() {
		// The issue is that FTC calls for random-access evaluation in time,
		// while this sequence of curves is tied to a set of state variables,
		// which may change after this BumpUF is built.

		// For now, we pretend this is not a problem!

		return new CACS_BumpUF();
	}
	@Override public String toString() {
		double totalDur = getTotalDuration().doubleValue();
		return "ConstAccelCurveSeq[accelPV=" + myAccelPV + ", durPV=" + myDurationPV
					+ ", motionPlan=" + dumpMotionPlan(10, totalDur) + "]";
	}
	class CACS_BumpUF implements BumpUF<RN, RN> {

		public RN getDerivativeAtInput(RN inputValue, int derivativeOrder) {
			if (derivativeOrder != 1)	 {
				throw new UnsupportedOperationException("Can't yet use derivatives other than 1st");
			}
			return getVelAtTime(inputValue);
		}
		public RN getOutputForInput(RN inputValue) {
			return getPosAtTime(inputValue);
		}

		public RN getSupportLowerBound() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public RN getSupportUpperBound() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

	}
}
