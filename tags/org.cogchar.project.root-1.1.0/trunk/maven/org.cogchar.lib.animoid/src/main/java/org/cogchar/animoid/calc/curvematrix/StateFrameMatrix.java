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
import org.cogchar.animoid.calc.curve.*;
import org.jscience.mathematics.number.Number;
/**
 *
 * @param <StateVarSymbol>
 * @param <RN>
 * @author Stu B. <www.texpedient.com>
 *
 * A "StateFrame" is a single "block" of state variable values at a particular "time-segment".
 */
public class StateFrameMatrix<StateVarSymbol extends StateVariableSymbol,
				RN extends Number<RN>>
				implements StateFrameMatrixVectorizer<StateVarSymbol> {

	private	int		myStateBlockCount, myTimeSegCount;
	private StateVariableSymbol myExampleSymbol;

	public StateFrameMatrix(int stateBlockCount, StateVariableSymbol exampleSymbol,
				int timeSegCount) {

	}
	public int getStateFrameIndex(int blockIndex, int timeSegIndex) {
		int stateFrameIndex = timeSegIndex * myStateBlockCount + blockIndex;
		return stateFrameIndex;
	}
	public int getStateValueIndex(int jointIndex, StateVarSymbol svs, int timeSegIndex) {
		int stateFrameIndex = getStateFrameIndex(jointIndex, timeSegIndex);
		int statesPerFrame = svs.getSymbolBlockSize();
		int selectedFrameBaseIndex = stateFrameIndex * statesPerFrame;
		int selectedDerivativeOffset = svs.getSymbolIndex();
		return selectedFrameBaseIndex + selectedDerivativeOffset;
	}
	public int getStateValueCount() {
		int statesPerBlock = myExampleSymbol.getSymbolBlockSize();
		return statesPerBlock * myStateBlockCount * myTimeSegCount;
	}
}
