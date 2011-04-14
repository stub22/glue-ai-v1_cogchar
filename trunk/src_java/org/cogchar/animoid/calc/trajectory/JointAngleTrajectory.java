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


package org.cogchar.animoid.calc.trajectory;

import org.jscience.mathematics.number.Number;
import org.jscience.mathematics.structure.Field;



import org.cogchar.animoid.protocol.JointPosition;
import org.cogchar.animoid.protocol.JointVelocityAROMPS;
import org.cogchar.animoid.world.WorldJoint;
import org.cogchar.calc.function.BumpUF;
import org.cogchar.calc.number.NumberFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  This class is an adapter between the abstract world of function curves and
 *  the concrete world of JointPositions.
 * @author Stu Baurmann
 */
public abstract class JointAngleTrajectory<TimeType extends Number<TimeType> & Field<TimeType>,
			AngleType extends Number<AngleType> & Field<AngleType>> {
	private static Logger	theLogger = LoggerFactory.getLogger(JointAngleTrajectory.class.getName());

	protected	WorldJoint				myWorldJoint;

	protected	double					myTimeUnitsPerSecond;

	// It's always good to have a time factory!
	private		NumberFactory<TimeType>			myTimeFactory;
	protected	BoundaryStyle					myBoundaryStyle;

	protected abstract BumpUF<TimeType, AngleType> getBumpFunction();
		
	public JointAngleTrajectory(WorldJoint wj, double timeUnitsPerSecond,
				NumberFactory<TimeType> timeFactory,
				BoundaryStyle boundaryStyle ) {

		myWorldJoint = wj;
		myTimeUnitsPerSecond = timeUnitsPerSecond;
		myTimeFactory = timeFactory;
		myBoundaryStyle = boundaryStyle;
	}
	/**
	 *  "Raw" means inversion flag not applied
	 * @param time
	 * @return
	 */
	protected double getWorldAnglePosAtTime(double time) {
		BumpUF<TimeType, AngleType> bumpUF = getBumpFunction();
		TimeType timeVal = myTimeFactory.makeNumberFromDouble(time);
		AngleType angleVal = bumpUF.getOutputForInput(timeVal);
		return angleVal.doubleValue();
	}
	protected double getWorldAngleSpeedAtTime(double time) {
		BumpUF<TimeType, AngleType> bumpUF = getBumpFunction();
		TimeType timeVal = myTimeFactory.makeNumberFromDouble(time);
		AngleType speedVal = bumpUF.getDerivativeAtInput(timeVal, 1);
		return speedVal.doubleValue();
	}

	protected double getRomPosAtTime(double time) {
		double angleVal = getWorldAnglePosAtTime(time);
		double romPos = myWorldJoint.getROM_posForWorldAngleDeg(angleVal);
		// TODO: Check boundaries and apply boundaryEnforceStyle
		return romPos;
	}
	protected double getRomVelAtTime(double time) {
		double speedVal = getWorldAngleSpeedAtTime(time);
		double romSpeed = myWorldJoint.getROM_velForWorldAngleSpeed(speedVal);
		return romSpeed;
	}

	public JointPosition getJointPosAtTime(double time) {
		double romPos = getRomPosAtTime(time);
		return myWorldJoint.getJoint().makeJointPosForROM_value(romPos);
	}

	public JointVelocityAROMPS getJointVelAtTime(double time) {
		double velRomPS = getRomVelAtTime(time);
		return myWorldJoint.getJoint().makeJointVelForROM_speedValue(velRomPS);
	}

}
