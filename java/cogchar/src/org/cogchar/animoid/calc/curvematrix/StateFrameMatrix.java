/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.animoid.calc.curvematrix;
import org.cogchar.animoid.calc.curve.*;
import org.jscience.mathematics.number.Number;
/**
 *
 * @param <StateVarSymbol>
 * @param <RN>
 * @author Stu Baurmann
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
