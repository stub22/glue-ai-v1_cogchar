/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.calc.number;
import java.util.List;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.jscience.mathematics.function.Polynomial;
import org.jscience.mathematics.function.Variable;
import org.jscience.mathematics.number.Number;
import org.jscience.mathematics.number.Real;
/**
 *
 * @author Stu Baurmann
 */
public class NumberFuncs {
	public static <NT extends Number<NT>> NT evalPoly(Polynomial<NT> poly) {
		return evalPrintReturnPoly(poly, null, null, null, false);
	}
	public static <NT extends Number<NT>> NT evalPrintReturnPoly(Polynomial<NT> poly, String logLabel,
				Logger logger, Level logLev, boolean logFlag) {
		NT nval = poly.evaluate();
// 		if (logFlag && logger.isLoggable(logLev)) {
		if (logFlag) {
			StringBuffer msg = new StringBuffer(logLabel).append(" = {");
			msg.append(poly.toString()).append("} (").append(dumpPolyVars(poly)).append(") = ").append(nval);
			logger.info(msg.toString());
//			logger.log(logLev, msg.toString());
		}
		return nval;
	}
	public static <NT extends Number<NT>> String dumpPolyVars(Polynomial<NT> poly) {
		StringBuffer buf = new StringBuffer("[");
		List<Variable<NT>> polyVars = poly.getVariables();
		boolean firstVar = true;
		for (Variable<NT> v : polyVars) {
			String sym = v.getSymbol();
			NT nval = v.get();
			if (!firstVar) {
				buf.append(",");
			}
			buf.append(sym).append("=").append(nval.toString());
			firstVar = false;
		}
		buf.append("]");
		return buf.toString();
	}
}
