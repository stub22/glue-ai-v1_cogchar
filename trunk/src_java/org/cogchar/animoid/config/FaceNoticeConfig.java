/*
 *  Copyright 2008 Hanson Robotics Inc.
 *  All Rights Reserved.
 */

package org.cogchar.animoid.config;

/**
 *
 * @author Stu Baurmann
 */
public class FaceNoticeConfig {
	public Double		initialStrength;
	
	// 2 exponential decay constants.  HIDDEN = face is known to be outside FOV, EXPOSED = in FOV
	public Double		hiddenDecayConstant;
	public Double		exposedDecayConstant;
	
	public Double		postingThreshold;
	public Double		clearingThreshold;
	public Double		survivalThreshold;

	public Integer		obsRetainedInHypo;
	public Integer		obsRetainedInFreckleFace;
	
	
	public Double		cogDistCoeffAzDiamSquared;
	public Double		cogDistCoeffElDiamSquared;
	public Double		cogDistCoeffSeconds;
	public Double		cogDistCoeffProduct;

	public Double		cogDistCoeffTimestampOverlap;
	public Double		cogDistCoeffFreckleMatch;
	public Double		cogDistCoeffFreckleMismatch;

	public Double		mergeThresholdCogDist;
/*
 *		Correction factor for using vision timestamp to estimate position.
 *		How much slower is the camera+openCV stack than the serial-to-servo stack?
 *		Use a negative number if we think serial-to-servo is slower.
 */
	public Double		visionToMotionOffsetSec;

	public Double		ageUncertaintyWeight;
	public Double		positionUncertaintyWeight;


	
	public String toString() {
		return "FaceNoticeConfig[" +
				"\ninitialStrength=" + initialStrength +
				"\nhiddenDecayConstant=" + hiddenDecayConstant +
				"\nexposedDecayConstant=" + exposedDecayConstant +
				"\npostingThreshold=" + postingThreshold +
				"\nclearingThreshold=" + clearingThreshold +
				"\nsurvivalThreshold=" + survivalThreshold +
				"\nobsRetainedInHypo=" + obsRetainedInHypo +
				"\nobsRetainedInFreckleFace=" + obsRetainedInFreckleFace +
				"\ncogDistCoeffAzDiamSquared=" + cogDistCoeffAzDiamSquared +
				"\ncogDistCoeffElDiamSquared=" + cogDistCoeffElDiamSquared +
				"\ncogDistCoeffSeconds=" + cogDistCoeffSeconds +
				"\ncogDistCoeffTimestampOverlap=" + cogDistCoeffTimestampOverlap +
				"\ncogDistCoeffFreckleMatch=" + cogDistCoeffFreckleMatch +
				"\ncogDistCoeffFreckleMismatch=" + cogDistCoeffFreckleMismatch +	
				// "\ncogDistCoeffProduct=" + cogDistCoeffProduct +
				"\nmergeThresholdCogDist=" + mergeThresholdCogDist +
				"\nvisionToMotionOffsetSec=" + visionToMotionOffsetSec +
				"\nageUncertaintyWeight=" + ageUncertaintyWeight +
				"\npositionUncertaintyWeight=" + positionUncertaintyWeight +
				"]";
	}
}
