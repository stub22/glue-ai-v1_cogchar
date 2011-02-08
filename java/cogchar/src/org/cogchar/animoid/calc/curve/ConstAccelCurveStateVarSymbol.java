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
