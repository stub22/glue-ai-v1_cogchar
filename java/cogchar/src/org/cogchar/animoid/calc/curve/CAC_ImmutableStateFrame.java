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
