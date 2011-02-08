/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.animoid.calc.curvematrix;

import org.cogchar.animoid.calc.curve.*;

/**
 * @param <StateVarSymbol> 
 * @author Stu Baurmann
 * Virtual vector+matrix structure.
 * Implementer gets to decide how to map the implied matrix structure into a single vector.
 * In matrix we have rows for time segments and columns for state variables.
 *    (The transpose could also be the case, this interface does not care).
 * The state variables are a list of "blocks" = regular lists of columns (or rows!)
 * all keyed by the same enum class.
 *
 * For example:  block = joint
 *  each block contains:  TimeOffset, ConstAccel, InitPos, InitVel
 */
public interface StateFrameMatrixVectorizer<StateVarSymbol extends StateVariableSymbol> {
	public int getStateFrameIndex(int stateBlockIndex, int timeSegIndex);
	public int getStateValueIndex(int stateBlockIndex, StateVarSymbol sve, int timeSegIndex);
	public int getStateValueCount();
}
