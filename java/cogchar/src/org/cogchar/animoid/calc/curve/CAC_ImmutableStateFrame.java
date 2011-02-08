/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.animoid.calc.curve;
import static org.cogchar.animoid.calc.curve.ConstAccelCurveStateVarSymbol.*;
import org.jscience.mathematics.number.Number;
/**
 *
 * @author Stu Baurmann
 */
public class CAC_ImmutableStateFrame<RN extends Number<RN>>
		extends ImmutableStateFrame<ConstAccelCurveStateVarSymbol, RN> {
	public CAC_ImmutableStateFrame(double timeOff, double constAccel, double initPos, double initVel) {
		super(ConstAccelCurveStateVarSymbol.values().length);
		initValueForSymbol(TIME_OFFSET, timeOff);
		initValueForSymbol(CONST_ACCEL, constAccel);
		initValueForSymbol(INIT_POS, initPos);
		initValueForSymbol(INIT_VEL, initVel);
	}
	public double getTimeOffsetVal() {
		return getValueAtStateIndex(ConstAccelCurveStateVarSymbol.TIME_OFFSET.ordinal());
	}	
}
