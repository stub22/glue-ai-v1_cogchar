/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.animoid.calc.number;

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
