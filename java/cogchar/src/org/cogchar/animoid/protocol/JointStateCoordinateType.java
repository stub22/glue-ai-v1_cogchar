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
