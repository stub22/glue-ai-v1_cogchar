/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.animoid.calc.curvematrix;

import org.cogchar.calc.number.NumberFactory;
import org.cogchar.animoid.calc.curve.*;
import org.cogchar.animoid.calc.optimize.ParameterVector;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jscience.mathematics.function.Variable;
import org.jscience.mathematics.number.Real;
import org.jscience.mathematics.number.Number;
import org.jscience.mathematics.structure.Field;

/**
 *
 * @param <RN> 
 * @author Stu Baurmann
 */
public class SharedDurationCACM<RN extends Number<RN> & Field<RN>> extends ConstAccelCurveMatrix<RN> {
	protected	List<SDCACM_Interval>		myIntervals = new ArrayList<SDCACM_Interval>();

	public SharedDurationCACM(NumberFactory<RN> numFact) {
		super(numFact);
	}
	public SDCACM_Interval appendInterval() {
		int		idx = myIntervals.size();
		String nameSuffix = "" + idx + 1;
		SDCACM_Interval interval = new SDCACM_Interval(idx, nameSuffix);
		Variable<Real> toffVar =  interval.getTimeOffsetVariable();
		Set<ConstAccelCurve> curves = addAndCollectOneCurvePerSequence(toffVar, nameSuffix);
		interval.setCurveSet(curves);
		myIntervals.add(interval);
		return interval;
	}
	private Set<ConstAccelCurve> addAndCollectOneCurvePerSequence(Variable<Real> timeOffsetVar, String nameSuffix) {
		Set<ConstAccelCurve> curves = new HashSet<ConstAccelCurve>();
		for (ConstAccelCurveSequence seq : getSequences()) {
			String seqName = seq.getName();
			String curveSymbolSuffix = seqName + "_" + nameSuffix;
			ConstAccelCurve curve = new ConstAccelCurve(curveSymbolSuffix, timeOffsetVar, myNumberFactory);
			seq.addStepCurve(curve);
			curves.add(curve);
		}
		return curves;
	}
	public void setDurations(ParameterVector durPV) {
		for (ConstAccelCurveSequence seq :getSequences()) {
			seq.setDurationParams(durPV);
		}
	}
	public List<SDCACM_Interval>	getIntervals() {
		return myIntervals;
	}

}
