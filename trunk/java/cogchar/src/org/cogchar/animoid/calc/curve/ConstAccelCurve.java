/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.animoid.calc.curve;

import org.cogchar.calc.number.NumberFactory;
import static org.cogchar.animoid.calc.curve.ConstAccelCurveStateVarSymbol.*;
import org.jscience.mathematics.function.Polynomial;
import org.jscience.mathematics.function.Term;
import org.jscience.mathematics.function.Variable;
import org.jscience.mathematics.number.Number;

/**
 * This second-order (parabolic + linear) curve is parametrized by and differentiable in 4 variables:
 * TimeOffset
 * ConstAccel
 * InitPos
 * InitVel
 * 
 * "Init" values are at timeOffset=0.
 *

 * @param <RN>
 * @author Stu Baurmann
 */
public class ConstAccelCurve<RN extends Number<RN>>
	extends PolynomialMotionCurve<ConstAccelCurveStateVarSymbol, RN> {

	private NumberFactory<RN>		myNumberFactory;
	private	Polynomial<RN>			myAccelCurve;
	// private	Variable<RN>					myTimeOffsetVar;

	/**
	 * In this form, the timeOffset variable is supplied externally so that it may
	 * easily be shared among other curves.   This sharing is important when
	 * polynomials over this curve are combined with polynomials over curves on
	 * the same time segment, as it allows like terms to be combined symbolically.
	 *
	 *
	 * @param symbolSuffix
	 * @param offsetTimeVar
	 */

	public ConstAccelCurve(String symbolSuffix, Variable<RN> offsetTimeVar,
				NumberFactory<RN> numFact) {
		super(symbolSuffix);
		// myTimeOffsetVar = offsetTimeVar;
		mySymbolSuffix = symbolSuffix;
		if (offsetTimeVar != null) {
			useExistingPolyVar(TIME_OFFSET, offsetTimeVar);
		}
		myNumberFactory = numFact;
		Polynomial<RN> curvePoly = makeCurvePoly();
		setCurvePoly(curvePoly);
	}
	public ConstAccelCurve(String symbolSuffix, NumberFactory<RN> numFact) {
		this(symbolSuffix, null, numFact);
	}
	private Polynomial<RN> makeCurvePoly() {
		RN one = myNumberFactory.getOne();
		RN oneHalf = myNumberFactory.getOneHalf();

		Variable<RN>	timeVar = getPolyVar(TIME_OFFSET);
		Variable<RN>	accVar = getPolyVar(CONST_ACCEL);
		Variable<RN>	initPosVar = getPolyVar(INIT_POS);
		Variable<RN>	initVelVar = getPolyVar(INIT_VEL);

		Term  timeSquaredTerm = Term.valueOf(timeVar, 2);
		Term  accelTerm = Term.valueOf(accVar, 1);
		Term  accelTimeSqTerm = timeSquaredTerm.times(accelTerm);

		Polynomial<RN> parabPoly = Polynomial.valueOf(oneHalf, accelTimeSqTerm);

		Term timeTerm = Term.valueOf(timeVar, 1);
		Term velTerm = Term.valueOf(initVelVar, 1);
		Term velTimeTerm = timeTerm.times(velTerm);
		Polynomial<RN> linearPoly = Polynomial.valueOf(one, velTimeTerm);

		Polynomial constPoly = Polynomial.valueOf(one, initPosVar);

		Polynomial constPlusLinear = constPoly.plus(linearPoly);

		Polynomial fullPoly = constPlusLinear.plus(parabPoly);

		return fullPoly;		
	}
	
	public Polynomial<RN> getVelocityCurve() {
		return getDerivPolyForStateVarSymbol(TIME_OFFSET);
	}
	public Polynomial<RN> getAccelCurve() {
		if (myAccelCurve == null) {
			Polynomial<RN> velCurve = getVelocityCurve();
			Variable<RN>	timeVar = getPolyVar(TIME_OFFSET);
			myAccelCurve = velCurve.differentiate(timeVar);
		}
		return myAccelCurve;
	}
	public RN getVelocityAtCurrentState() {
		return evalDerivPolyAtCurrentState(TIME_OFFSET);
	}
	public RN getPositionAtCurrentState() {
		return evalCurvePolyAtCurrentState();
	}
	public RN getAccelAtCurrentState() {
		return getStateVarVal(CONST_ACCEL);
	}
	public RN getTimeOffsetAtCurrentState() {
		return getStateVarVal(TIME_OFFSET);
	}

	public void setInitPosition(RN initPos) {
		setStateVarVal(INIT_POS, initPos);
	}
	public void setInitVelocity(RN initVel) {
		setStateVarVal(INIT_VEL, initVel);
	}
	public void setConstAccel(RN constAccel) {
		setStateVarVal(CONST_ACCEL, constAccel);
	}
	public void setTimeOffset(RN timeOffset) {
		setStateVarVal(TIME_OFFSET, timeOffset);
	}
	public RN makeNumber(double dval) {
		return myNumberFactory.makeNumberFromDouble(dval);
	}
	public void configureWithDoubles(double timeOffset, double constAccel, double initPos, 
				double initVel) {
		setTimeOffset(makeNumber(timeOffset));
		setConstAccel(makeNumber(constAccel));
		setInitPosition(makeNumber(initPos));
		setInitVelocity(makeNumber(initVel));
	}


/*
	public String getInitPosSymbol() {
		return getFullSymbolStringForStateVar(INIT_POS);
	}
	public String getInitVelSymbol() {
		return getFullSymbolStringForStateVar(INIT_VEL);
	}
	public String getConstAccSymbol() {
		return getFullSymbolStringForStateVar(CONST_ACCEL);
	}
	public String getTimeOffsetSymbol() {
		return getFullSymbolStringForStateVar(TIME_OFFSET);
	}
	public Polynomial<RN> getDerivPolyWRT_TimeOffset() {
		return getDerivPolyForStateVarSymbol(TIME_OFFSET);
	}
	public Polynomial<RN> getDerivPolyWRT_ConstAccel() {
		return getDerivPolyForStateVarSymbol(CONST_ACCEL);
	}
	public Polynomial<RN> getDerivPolyWRT_InitPos()  {
		return getDerivPolyForStateVarSymbol(INIT_POS);
	}
	public Polynomial<RN> getDerivPolyWRT_InitVel()  {
		return getDerivPolyForStateVarSymbol(INIT_VEL);
	}

	public double getTimeOffsetValue() {
		return RealFuncs.getVariableValue(myTimeOffsetVar);
	}
	public double getInitPosValue() {
		return RealFuncs.getInputVarValue(getPositionPolynomial(), getInitPosSymbol());
	}
	public double getInitVelValue() {
		return RealFuncs.getInputVarValue(getPositionPolynomial(), getInitVelSymbol());
	}
	public double getConstAccValue() {
		return RealFuncs.getInputVarValue(getPositionPolynomial(), getConstAccSymbol());
	}

	public void setTimeOffsetValue(double timeOffset) {
		RealFuncs.setVariableValue(myTimeOffsetVar, timeOffset);
	}
	void setInitPosValue(double x0val) {
		RealFuncs.setInputVarValue(getPositionPolynomial(), getInitPosSymbol(), x0val);
	}
	void setInitVelValue(double v0val) {
		RealFuncs.setInputVarValue(getPositionPolynomial(), getInitVelSymbol(), v0val);
	}
	void setConstAccValue(double aval) {
		RealFuncs.setInputVarValue(getPositionPolynomial(), getConstAccSymbol(), aval);
	}
	double getVelValueAtCurrentTimeOffset() {
		double timeOffset = getTimeOffsetValue();
		return getInitVelValue() + timeOffset * getConstAccValue();
	}
	double getPosValueAtCurrentTimeOffset() {
		// TimeOffset, AccelValue, InitVel, InitPos must all be set for this to work!
		return evalPositionPolynomial();
	}
	*/

}
