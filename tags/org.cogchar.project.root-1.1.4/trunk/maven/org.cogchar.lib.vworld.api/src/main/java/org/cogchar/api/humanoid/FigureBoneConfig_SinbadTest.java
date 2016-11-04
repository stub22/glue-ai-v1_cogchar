/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.api.humanoid;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class FigureBoneConfig_SinbadTest extends FigureBoneConfig {
	public void addSinbadDefaultBoneDescs() {

		// Explicitly bound to KRD in Bowl-At-Sinbad demo
		addBoneDesc("Ulna.L");
		addBoneDesc("Ulna.R");
		addBoneDesc("Chest");
		addBoneDesc("Foot.L");
		addBoneDesc("Foot.R");
		addBoneDesc("Hand.R");
		addBoneDesc("Hand.L");
		addBoneDesc("Neck");
		addBoneDesc("Root");
		addBoneDesc("Stomach");
		addBoneDesc("Waist");
		addBoneDesc("Humerus.L");
		addBoneDesc("Humerus.R");
		addBoneDesc("Thigh.L");
		addBoneDesc("Thigh.R");
		addBoneDesc("Calf.L");
		addBoneDesc("Calf.R");
		addBoneDesc("Clavicle.L");
		addBoneDesc("Clavicle.R");

		// extras
		addBoneDesc("Neck");
		addBoneDesc("Head");
		addBoneDesc("Eye.L");
		addBoneDesc("Eye.R");
		addBoneDesc("Brow.C");
		addBoneDesc("Brow.L");
		addBoneDesc("Brow.R");
		addBoneDesc("Cheek.L");
		addBoneDesc("Cheek.R");
		addBoneDesc("UpperLip");
		addBoneDesc("Jaw");
		addBoneDesc("TongueBase");
		addBoneDesc("LowerLip");
	}
	/*
	 * 
	 */
	/*
	public void addZenoDefaultBoneDescs() {

		addBoneDesc("Root");
		addBoneDesc("Spine1");
		addBoneDesc("bn_neck01");
		addBoneDesc("bn_HeadPivot2");
		addBoneDesc("bn_Jaw01");
		addBoneDesc("be_Jaw2");
		addBoneDesc("be_RevJaw2");
		
		addBoneDesc("LtEye1");
		addBoneDesc("LtEye2");
		addBoneDesc("RtEeye1");  // sic
		addBoneDesc("RtEye2");
		addBoneDesc("LtBrow");
		addBoneDesc("RtBrow");
		
		addBoneDesc("LtClav");
		addBoneDesc("LtShoulder");
		addBoneDesc("LtElbow");
		addBoneDesc("LtWrist");
		addBoneDesc("LtPalm");
		addBoneDesc("LtThumb1");
		addBoneDesc("LtThumb2");
		addBoneDesc("LtThumb3");
		addBoneDesc("LtIndex1");
		addBoneDesc("LtIndex2");
		addBoneDesc("LtIndex3");
		addBoneDesc("LtMiddle1");
		addBoneDesc("LtMiddle2");
		addBoneDesc("LtMiddle3");
		addBoneDesc("LtPinky1");
		addBoneDesc("LtPinky2");
		addBoneDesc("LtPinky3");
		addBoneDesc("RtClav");
		addBoneDesc("RtShoulder");
		addBoneDesc("RtElbow");
		addBoneDesc("RtWrist");
		addBoneDesc("RtPalm");
		addBoneDesc("RtThumb1");
		addBoneDesc("RtThumb2");
		addBoneDesc("RtThumb3");
		addBoneDesc("RtIndex1");
		addBoneDesc("RtIndex2");
		addBoneDesc("RtIndex3");
		addBoneDesc("RtMiddle1");
		addBoneDesc("RtMiddle2");
		addBoneDesc("RtMiddle3");
		addBoneDesc("RtPinky1");
		addBoneDesc("RtPinky2");
		addBoneDesc("RtPinky3");
		addBoneDesc("LtHip");
		addBoneDesc("LtKnee");
		addBoneDesc("LtAnkle");
		addBoneDesc("RtHip");
		addBoneDesc("RtKnee");
		addBoneDesc("RtAnkle");
	}
	*/
}
