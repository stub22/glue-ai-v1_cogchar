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

package org.cogchar.animoid.config;

import java.util.EnumSet;

/**
 * Copied from MjCanonicalNames.h in the "ControlSystems" C++ project on 2008-07-04.
 * That's right - independence day (from C++), baby!
 * @author Stu Baurmann
 */
public enum MuscleJoint {
	NO_MJ_ID(-1),
	Jaw_Down(0),                        // HR
	LowerNod_Forward(1),                // H-
	LeftCornerMouth_ApertureClose(2),   // H- Purse Lips L or "OO" L
	LeftCornerMouth_Retract(3),         // H- "EE" L
	LeftZigomaticus_Up(4),              // H- Smile L
	UpperLipCenter_Up(5),               // H-
	LeftOuterBrow_Up(6),                // H-
	LeftInnerBrow_DownIn(7),            // H- Scowl L
	LeftUpperCheek_UpOut(8),            // H- Squint L
	RightSideOfNose_Up(9),              // H- Sneer R
	MidLeftUpperLip_Up(10),             // H-
	RightEye_TurnRight(11),             // H-
	MidLeftLowerLip_Down(12),           // H-
	BothEyes_Up(13),                    // HR
	UpperNod_Forward(14),               // HR
	Head_TurnRight(15),                 // HR
	LowerLipCenter_Down(16),            // HR
	RightCornerMouth_ApertureClose(17), // H- Purse Lips R or "OO" R
	RightCornerMouth_Retract(18),       // H- "EE" R
	BothCornerMouth_Down(19),           // H- Frown Both
	RightZigomaticus_Up(20),            // H- Smile R
	BothLowerLids_Down(21),             // H-
	BothUpperLids_Up(22),               // H-
	RightOuterBrow_Up(23),              // H-
	RightInnerBrow_DownIn(24),          // H- Scowl R
	CenterBrow_Up(25),                  // H-
	MidRightLowerLip_Down(26),          // H-
	MidRightUpperLip_Up(27),            // H-
	LeftSideOfNose_Up(28),              // H- Sneer L
	RightUpperCheek_UpOut(29),          // H- Squint R
	LeftEye_TurnRight(30),              // H- 
	Head_TiltLeft(31),                  // HR

	ZenoSmile_Up(32),                   // -R BothZigomaticusBothCornerMouth_Up (with Inverted LowerLipCenter)
	BothBrow_Up(33),                    // -R
	BothEyes_TurnRight(34),             // -R
	BothBlink_Open(35),                 // -R
	RightUpperLid_Up(36),               // H-
	LeftUpperLid_Up(37),                // H-
	RightCornerMouth_Down(38),          // H- Frown Right
	LeftCornerMouth_Down(39),           // H- Frown Left

	UpperTorso_TurnLeft(1000),          // -R Rotate Y Clockwise
	LeftShoulder_RotateUp(1001),        // -R Rotate Z Clockwise
	LeftShoulder_RotateForward(1002),   // -R Rotate Y Counterclockwise
	LeftElbow_RotateDownIn(1003),       // -R Rotate Y Counterclockwise Z Counterclockwise
	LeftUpperLeg_RotateYIn(1004),       // -- Rotate Y Counterclockwise
	LeftUpperLeg_RotateZOut(1005),      // -R Rotate Z Clockwise
	LeftUpperLeg_RotateXBack(1006),     // -R Rotate X Clockwise
	LeftKnee_RotateXBack(1007),         // -R Rotate X Clockwise
	LeftAnkle_RotateXBack(1008),        // -R Rotate X Clockwise
	LeftAnkle_RotateZIn(1009),          // -R Rotate Z Counterclockwise
	RightShoulder_RotateUp(1010),       // -R Rotate Z Counterclockwise
	RightShoulder_RotateForward(1011),  // -R Rotate Y Clockwise
	RightElbow_RotateDownIn(1012),      // -R Rotate Y Clockwise Z Clockwise
	RightUpperLeg_RotateYIn(1013),      // -- Rotate Y Clockwise
	RightUpperLeg_RotateZOut(1014),     // -R Rotate Z Counterclockwise
	RightUpperLeg_RotateXBack(1015),    // -R Rotate X Clockwise
	RightKnee_RotateXBack(1016),        // -R Rotate X Clockwise
	RightAnkle_RotateXBack(1017),       // -R Rotate X Clockwise
	RightAnkle_RotateZIn(1018),         // -R Rotate Z Clockwise
	BothUpperLeg_RotateYIn(1019);       // -R Rotate Left Y Counterclockwise and Right Y Clockwise

	private	final int	myJointID;
	MuscleJoint(int jid) {
		myJointID = jid;
	}
	public int getJointID() {
		return myJointID;
	}
	private static EnumSet<MuscleJoint> allJoints = EnumSet.allOf(MuscleJoint.class);
	public static MuscleJoint findJointForID(int id) {
		for (MuscleJoint mj : allJoints) {
			if (mj.getJointID() == id) {
				return mj;
			}
		}
		return null;
	}
}
