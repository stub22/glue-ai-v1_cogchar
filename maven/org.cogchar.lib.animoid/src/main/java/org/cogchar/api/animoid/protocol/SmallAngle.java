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

package org.cogchar.api.animoid.protocol;

import java.io.Serializable;

/**
 * @author Stu B. <www.texpedient.com>
 * A "small angle" is always between -pi=-180 and +pi=+180.
 * Attempts to set outside this range result in silent wrapping of the angle,
 * so +197 deg becomes -163deg.
 */
public class SmallAngle implements Serializable {
	private		double		myValueRad;
	public static SmallAngle makeFromRad(double rval) {
		SmallAngle a = new SmallAngle();
		a.myValueRad = normalizedAngle(rval);
		return a;
	}
	public static SmallAngle makeFromDeg(double dval) {
		double rad = Math.toRadians(dval);
		return makeFromRad(rad);
	}
	public double getRadians() {
		return myValueRad;
	}
	public double getDegrees() {
		return Math.toDegrees(myValueRad);
	}
	public SmallAngle add(SmallAngle other) {
		double totalRad = getRadians() + other.getRadians();
		return makeFromRad(totalRad);
	}
	public SmallAngle subtract(SmallAngle other) {
		double totalRad = getRadians() - other.getRadians();
		return makeFromRad(totalRad);
	}
	// Scale should be less than 1.0.
	public SmallAngle multiply(double scale) {
		double totalRad = getRadians() * scale;
		return makeFromRad(totalRad);
	}
	
	// Compute version of angle that is closest to zero (between -pi and +pi)
	public static double normalizedAngle(double ang) {
		double circleAngle = 2.0 * Math.PI;
		double circlesMag = Math.abs(ang / circleAngle);
		double fullCirclesMag = Math.round(circlesMag);
		double fullCircles = fullCirclesMag * Math.signum(ang);
		double normal = ang - fullCircles * circleAngle;
		return normal;
	}	
	public String toString() {
		return "SmallAngle[rad=" + getRadians() + ", deg=" + getDegrees() + "]";
	}
	public String getDegreesText() {
		double deg = getDegrees();
		return String.format("%+8.4f", deg);
	}
}
