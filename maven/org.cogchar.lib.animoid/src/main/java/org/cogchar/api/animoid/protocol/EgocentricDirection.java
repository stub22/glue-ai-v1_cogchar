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
 */
public class EgocentricDirection implements Serializable {
	// 0.0 is ahead, -pi/2 = -90.0 is left, +pi/2 = +90.0 is right
	private		SmallAngle	myAzimuth;
	
	// 0.0 is ahead, -pi/2 = -90.0 is down, +pi/2 = +90.0 is up
	private		SmallAngle	myElevation;
	
	private		static		EgocentricDirection		theForwardDirection = null;
	
	public EgocentricDirection(SmallAngle az, SmallAngle el) {
		myAzimuth = az;
		myElevation = el;
	}
	public static EgocentricDirection makeFromRad(double azRad, double elRad) {
		SmallAngle az = SmallAngle.makeFromRad(azRad);
		SmallAngle el = SmallAngle.makeFromRad(elRad);
		return new EgocentricDirection(az, el);
	}
	public static EgocentricDirection makeFromDeg(double azDeg, double elDeg) {
		SmallAngle az = SmallAngle.makeFromDeg(azDeg);
		SmallAngle el = SmallAngle.makeFromDeg(elDeg);
		return new EgocentricDirection(az, el);
	}
	public static EgocentricDirection getForwardDirection() {
		if (theForwardDirection == null) {
			theForwardDirection = makeFromRad(0.0, 0.0);
		}
		return theForwardDirection;
	}
	public SmallAngle getAzimuth() {
		return myAzimuth;
	}
	public SmallAngle getElevation() {
		return myElevation;
	}
	public EgocentricDirection add(EgocentricDirection other) {
		SmallAngle azSum = getAzimuth().add(other.getAzimuth());
		SmallAngle elSum = getElevation().add(other.getElevation());
		return new EgocentricDirection(azSum, elSum);
	}
	/**
	 * @return EgocentricDirection-delta, which should be distinguished by type.
	 */
	public EgocentricDirection subtract(EgocentricDirection other) {
		SmallAngle azDiff = getAzimuth().subtract(other.getAzimuth());
		SmallAngle elDiff = getElevation().subtract(other.getElevation());
		return new EgocentricDirection(azDiff, elDiff);
	}
	/**
	 *  @return "distance angle" estimate, computed as a magnitude of the vector of component
	 *  angle distances, which is probably not exactly the spherical angle we want,
	 *  but it's close enough for the observation-cognitive-distance algorithm,
	 *  which is the only place it's used at present.
	 *
	 * TODO:  compute the real spherical angle using an inverse cosecant or whatever.
	 */
	public SmallAngle computeDistanceAngle(EgocentricDirection other) {
		double azRadDelta = other.getAzimuth().getRadians() - getAzimuth().getRadians();
		double elRadDelta = other.getElevation().getRadians() - getElevation().getRadians();
		double distanceRad = Math.sqrt(azRadDelta * azRadDelta + elRadDelta * elRadDelta);
		return SmallAngle.makeFromRad(distanceRad);
	}

	@Override public String toString() {
		return "[EgoDir azDeg=" + myAzimuth.getDegreesText()
					+ ", elDeg=" + myElevation.getDegreesText() + "]";
	}
}
