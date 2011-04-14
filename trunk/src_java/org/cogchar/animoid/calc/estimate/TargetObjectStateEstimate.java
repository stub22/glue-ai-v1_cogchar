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

package org.cogchar.animoid.calc.estimate;


import java.awt.Rectangle;
import java.io.Serializable;
import org.cogchar.animoid.config.FaceNoticeConfig;
import org.cogchar.animoid.protocol.EgocentricDirection;
import org.cogchar.animoid.protocol.Frame;

/**
 * @author Stu Baurmann
 */
public class TargetObjectStateEstimate implements Serializable {
	public Long						timeAtObs;
	public EgocentricDirection		targetDirectionAtObs;
	public EgocentricDirection		gazeSpeedAtObs;
	public Frame					jointPosAtObs;
	public Frame					jointVelAtObs;

	// This is here so we can display on GUI
	// public SightObservation			temporarilyHackedSightObservation;

	public TargetObjectStateEstimate(PositionEstimator pe, GazeDirectionComputer gdc,
				Rectangle targetRect, Long timestampMsec) {
		timeAtObs = timestampMsec;
		jointPosAtObs  = pe.estimatePositionAtMoment(timestampMsec);
		jointVelAtObs = pe.estimateVelocityAtMoment(timestampMsec);

		targetDirectionAtObs = gdc.computeGazeDirection(jointPosAtObs, targetRect);
		gazeSpeedAtObs = gdc.computeGazeVelocity(jointVelAtObs);
	}

	public double getPositionUncertainty() {
		double gazeVelAz = gazeSpeedAtObs.getAzimuth().getDegrees();
		double gazeVelEl = gazeSpeedAtObs.getElevation().getDegrees();
		double euclidNormSq = gazeVelAz * gazeVelAz + gazeVelEl * gazeVelEl;
		double euclidNorm = Math.sqrt(euclidNormSq);
		return euclidNorm;
	}
	public boolean isBetterThan(TargetObjectStateEstimate other, FaceNoticeConfig sightModelConfig) {
		if(sightModelConfig.ageUncertaintyWeight == null) {
			return false;
		}
		// Higher signed numbers mean "I am more certain than other"

		// Positive means I am newer than other
		double	ageDiffSec = (timeAtObs - other.timeAtObs) / 1000.0;
		// Positive means I am more pos-certain than other
		double  puDiff = other.getPositionUncertainty() - getPositionUncertainty();
		double  scoreDiff = sightModelConfig.ageUncertaintyWeight * ageDiffSec +
				sightModelConfig.positionUncertaintyWeight * puDiff;
		return (scoreDiff > 0.0);
	}
	public String toString() {
		return "TOSE[timestamp=" + timeAtObs + ", posUncertainty=" + getPositionUncertainty() + "]";
	}
}
