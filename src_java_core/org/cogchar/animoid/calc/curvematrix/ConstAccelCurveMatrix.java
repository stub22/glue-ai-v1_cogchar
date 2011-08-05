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
 * @author Stu B. <www.texpedient.com>
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
