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

/**
 * @param <StateVarSymbol> 
 * @author Stu B. <www.texpedient.com>
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
