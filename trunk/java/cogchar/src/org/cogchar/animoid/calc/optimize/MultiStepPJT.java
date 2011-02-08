/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.animoid.calc.optimize;

import org.cogchar.animoid.calc.number.NumberFactory;

/**
 *
 * @author humankind
 */
public class MultiStepPJT extends ParametricJointTrajectory {

	public enum Dimension {
		LEVEL,
		DURATION
	}
	public	int					myStepCount;
	private	ParameterVector		myLevelPV, myDurationPV;

	public MultiStepPJT(NumberFactory nf) {
		myLevelPV = new ParameterVector(nf);
		myDurationPV = new ParameterVector(nf);
	}
	public void setStepCount(int numSteps) {
		myStepCount = numSteps;
		myLevelPV.setLength(numSteps);
		myDurationPV.setLength(numSteps);
	}
	public ParameterVector	getDurationPV() {
		return myDurationPV;
	}
	public void setDurationPV(ParameterVector durPV) {
		myDurationPV = durPV;
	}
	public ParameterVector	getLevelPV() {
		return myLevelPV;
	}
/*
	public void setValue(Dimension d, int idx, double level) {
		switch (d) {
			case LEVEL:
				myLevelPV.setValue(idx, level);
			break;
			case DURATION:

			break;
		}
	
		myStepLevels[idx] = level;
	}
	public void setStepDuration(int idx, double dur) {
		myStepDurations[idx] = dur;
	}
	public double getStepLevel(int idx) {
		return myStepLevels[idx];
	}
	public double getStepDuration(int idx) {
		return myStepDurations[idx];
	}
	public void resetPenalties() {
		for (int i=0; i < myStepCount; i++) {
			myStepLevelPenalties[i] = 0.0;
			myStepDurationPenalties[i] = 0.0;
		}
	}
 */
	public void incrementBoundaryPenalty(Dimension d, int idx, double penalty) {
		
	}
	// TODO - pass in a penalizing polynomial
	public void fixDurationsAndAssessPenalties(double minDur, double maxDur) {

	}

	/** @return number of params written.
	 * We always write levels, and then optionally n-2 durations.
	 * The abbreviated durations does not set the first or last step duration.
	 **/
	public int writeToArray(double array[], int arrayStartIdx,
			boolean writeAbbrevDurations) {
		myLevelPV.writeValuesToArray(array, arrayStartIdx, 0, myStepCount);
		if (writeAbbrevDurations) {
			myDurationPV.writeValuesToArray(array, arrayStartIdx + myStepCount,
					1, myStepCount - 2);
			return 2 * myStepCount - 2;
		} else {
			return myStepCount;
		}
	}
	/**
	 * We always read levels, then optionally read n-2 durations.
	 *
	 * The abbreviated durations does not set the first or last step duration.
	 * @return number of params read. **/
	public int readFromArray(double array[], int arrayStartIdx,
			boolean readAbbrevDurations) {
		myLevelPV.readValuesFromArray(array, arrayStartIdx, 0, myStepCount);
		if (readAbbrevDurations) {
			myDurationPV.readValuesFromArray(array, arrayStartIdx + myStepCount,
					1, myStepCount - 2);
			return 2 * myStepCount - 2;
		} else {
			return myStepCount;
		}
	}
	public String toString() {
		return "MultiStepPJT[stepCount=" + this.myStepCount
			+ ", durationPV=" + this.myDurationPV
			+ ", levelPV=" + this.myLevelPV + "]";
	}
}
