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

package org.cogchar.animoid.calc.curve;

public enum ConstAccelCurveStateVarSymbol 
			implements StateVariableSymbol<ConstAccelCurveStateVarSymbol> {
	
	TIME_OFFSET() {
	  public String getSymbolString() {
		  return "_t.";
	  }
	},
	CONST_ACCEL() {
	  public String getSymbolString() {
		  return "_a.";
	  }
	},
	INIT_POS() {
	  public String getSymbolString() {
		  return "_x0.";
	  }
	},
	INIT_VEL() {
	  public String getSymbolString() {
		  return "_v0.";
	  }
	};

	public int getSymbolIndex() {
		return this.ordinal();
	}

	public int getSymbolBlockSize() {
		return this.values().length;
	}

	public ConstAccelCurveStateVarSymbol getSymbolAtIndex(int idx) {
		return this.values()[idx];
	}
}
