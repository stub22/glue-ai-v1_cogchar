/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.animoid.calc.curvematrix;

import org.cogchar.calc.number.NumberFactory;
import org.cogchar.animoid.calc.curve.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jscience.mathematics.number.Number;
import org.jscience.mathematics.structure.Field;

/**
 * @param <RN>
 * @author Stu Baurmann
 *
 * The elements of this matrix are all ConstAccelCurves.
 * They may be sharing underlying polynomial variables, or not.
 */
public class ConstAccelCurveMatrix<RN extends Number<RN> & Field<RN>> {

	protected		Map<String,ConstAccelCurveSequence<RN>>		mySequencesByName;
	protected		List<ConstAccelCurveSequence<RN>>			mySequences;
	protected			NumberFactory<RN>						myNumberFactory;
	public ConstAccelCurveMatrix(NumberFactory<RN> numFact) {
		mySequencesByName = new HashMap<String, ConstAccelCurveSequence<RN>>();
		mySequences = new ArrayList<ConstAccelCurveSequence<RN>>();
		myNumberFactory = numFact;
	}
	protected void addSequence(ConstAccelCurveSequence<RN> seq) {
		String seqName = seq.getName();
		mySequencesByName.put(seqName, seq);
		mySequences.add(seq);
	}
	public ConstAccelCurveSequence<RN> addEmptySequenceForName(String name) {
		ConstAccelCurveSequence<RN> seq = new ConstAccelCurveSequence<RN>(name, myNumberFactory);
		addSequence(seq);
		return seq;
	}
	public void propagateEndpointConditions() {
		for (ConstAccelCurveSequence seq: getSequences()) {
			seq.propagateEndpointConditions();
		}
	}
	public List<ConstAccelCurveSequence<RN>> getSequences() {
		return mySequences;
	}
	public int getSequenceCount() {
		return mySequences.size();
	}
	public int getCurveCountPerSequence() {
		ConstAccelCurveSequence firstSeq = mySequences.get(0);
		return firstSeq.getStepCount();
	}
	public StateFrameMatrix<ConstAccelCurveStateVarSymbol, RN> getStateFrameMatrix() {
		return null;
	}
	public void absorbStateFrameMatrix(StateFrameMatrix<ConstAccelCurveStateVarSymbol, RN> matrix) {
	}
	public String dumpSamples(int sampleCount, double lastSampleTime) {
		StringBuffer dumpBuf = new StringBuffer();
		for (ConstAccelCurveSequence seq: getSequences()) {
			dumpBuf.append(seq.dumpMotionPlan(sampleCount, lastSampleTime));
		}
		return dumpBuf.toString();
	}
	@Override public String toString() {
		return "ConstAccelCurveMatrix[seqByName=" + mySequencesByName + "]";
	}
}
