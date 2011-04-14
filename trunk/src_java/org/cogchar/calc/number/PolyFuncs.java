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

package org.cogchar.calc.number;

import java.util.List;
import java.util.logging.Level;
import org.jscience.mathematics.function.Polynomial;
import org.jscience.mathematics.function.Variable;
import org.slf4j.Logger;
import org.jscience.mathematics.number.Number;
import org.jscience.mathematics.number.Real;

/**
 *
 * @author winston
 */
public class PolyFuncs {
	public static <RN extends Number<RN>> double evalPoly(Polynomial<RN> poly) {
		return evalPrintReturnPoly(poly, null, null, null, false);
	}
	public static <RN extends Number<RN>> double evalPrintReturnPoly(Polynomial<RN> poly, String logLabel,
				Logger logger, Level logLev, boolean logFlag) {
		RN val = poly.evaluate();
		if (logFlag) { // (logFlag && logger.isLoggable(logLev)) {
			StringBuffer msg = new StringBuffer(logLabel).append(" = {");
			msg.append(poly.toString()).append("} (").append(dumpPolyVars(poly)).append(") = ").append(val);
			// logger.log(logLev, msg.toString());
			logger.info(msg.toString());
		}
		return val.doubleValue();
	}
	public static <RN extends Number<RN>> String dumpPolyVars(Polynomial<RN> poly) {
		StringBuffer buf = new StringBuffer("[");
		List<Variable<RN>> polyVars = poly.getVariables();
		boolean firstVar = true;
		for (Variable<RN> v : polyVars) {
			String sym = v.getSymbol();
			RN rval = v.get();
			if (!firstVar) {
				buf.append(",");
			}
			buf.append(sym).append("=").append(rval.toString());
			firstVar = false;
		}
		buf.append("]");
		return buf.toString();
	}
}
