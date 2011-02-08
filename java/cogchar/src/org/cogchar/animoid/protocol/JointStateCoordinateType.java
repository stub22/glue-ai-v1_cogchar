package org.cogchar.animoid.protocol;

public enum JointStateCoordinateType {

	FLOAT_ABS_RANGE_OF_MOTION, // [0.0 to 1.0]
	FLOAT_VEL_RANGE_OF_MOTION_PER_SEC, // [-inf to +inf], 0.0 = "stay"
	FLOAT_ACC_RANGE_OF_MOTION_PSPS, // per-sec-per-sec [-inf to +inf], 0.0 = "apply Newton's 1st law"
	FLOAT_ABS_LOPSIDED_PIECEWISE_LINEAR, // [-1.0 to 1.0], 0.0 = "default"
	FLOAT_REL_RANGE_OF_MOTION, // [-1.0 to 1.0] - offset, 0.0 = "stay".
	INT_DEVICE, // [0 to 250] for SSC-32 - Brookshire VSA compatible
	INT_PULSE_WIDTH
}
