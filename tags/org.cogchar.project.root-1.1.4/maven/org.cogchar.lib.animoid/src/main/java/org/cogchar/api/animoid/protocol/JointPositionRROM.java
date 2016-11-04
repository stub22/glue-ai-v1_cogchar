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
import static org.cogchar.api.animoid.protocol.JointStateCoordinateType.*;
/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class JointPositionRROM  extends JointPosition {
	public JointPositionRROM(Joint j) {
		super(j);
	}
	public JointPositionRROM(Joint j, double relRomPos) {
		this (j);
		setCoordinateFloat(FLOAT_REL_RANGE_OF_MOTION, relRomPos);
	}
	public JointPositionRROM(JointPosition jp) {
		this(jp.getJoint(), jp.getCoordinateFloat(FLOAT_REL_RANGE_OF_MOTION));
	}
	public double getPosRelROM() {
		return getCoordinateFloat(FLOAT_REL_RANGE_OF_MOTION);
	}
}
