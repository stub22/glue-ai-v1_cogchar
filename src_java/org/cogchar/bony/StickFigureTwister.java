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

package org.cogchar.bony;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import java.util.List;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class StickFigureTwister {
	private float			myWaistTwistAngle = 0;
	private float			myWaistTwistRate = 1;
	private	boolean			myTwistScoringFlag = false;
	
	private	VirtCharPanel	myVCP;
	private	ScoreBoard		myScoreBoard;
	public StickFigureTwister(VirtCharPanel vcp, ScoreBoard sb) {
		myVCP = vcp;
		myScoreBoard = sb;
	}
	public void twist(float tpf,  List<AnimControl> animControls) { 
				//	System.out.println("simpleUpdate, tpf=" + tpf);
		int testChannelNum = myVCP.getTestChannelNum();

		String direction = myVCP.getTestDirection();
		// System.out.println("Direction=" + direction);
		AnimControl ac = animControls.get(testChannelNum);
		Skeleton csk = ac.getSkeleton();

		Bone roots[] = csk.getRoots();
		// System.out.println("Found " + roots.length + " root bones: " + roots);
		Bone rootBone = roots[0];
		Bone tgtBone = rootBone;
		String mod = myVCP.getTestChannelModifier();
		if (mod.equals("first child")) {
			List<Bone> kids = rootBone.getChildren();
			Bone firstKid = kids.get(0);
			tgtBone = firstKid;
		}

		Vector3f localPos = rootBone.getLocalPosition();
		Vector3f modelPos = rootBone.getModelSpacePosition();
		// System.out.println("================================================================");
		myVCP.setDumpText("tgtBone=" + tgtBone + ", localPos=" + localPos + ", modelPos=" + modelPos + ", localRot=" + rootBone.getLocalRotation());


		myWaistTwistAngle += tpf * myWaistTwistRate;
		if (myWaistTwistAngle > FastMath.HALF_PI / 2f) {
			myWaistTwistAngle = FastMath.HALF_PI / 2f;
			myWaistTwistRate = -1;
		} else if (myWaistTwistAngle < -FastMath.HALF_PI / 2f) {
			myWaistTwistAngle = -FastMath.HALF_PI / 2f;
			myWaistTwistRate = 1;
		}

		Quaternion q = new Quaternion();
		// yaw, roll, pitch
		//	System.out.println("Setting roll for bone: " + b + " to " + myWaistTwistAngle);

		float pitchAngle = 0.0f;
		float rollAngle = 0.0f;
		float yawAngle = 0.0f;
		if (direction.equals("pitch")) {
			pitchAngle = myWaistTwistAngle;
		} else if (direction.equals("roll")) {
			rollAngle = myWaistTwistAngle;
		} else if (direction.equals("yaw")) {
			yawAngle = myWaistTwistAngle;
		}
		q.fromAngles(pitchAngle, rollAngle, yawAngle);
		tgtBone.setUserControl(true);
		
		// This applies rotation q to the "initial"/"bind" orientation, putting result in "local" rot.
		tgtBone.setUserTransforms(Vector3f.ZERO, q, Vector3f.UNIT_XYZ);
		if ((myTwistScoringFlag) && (myScoreBoard != null)) {
			myScoreBoard.displayScore(0, "tgtBone=" + tgtBone);
			myScoreBoard.displayScore(1, "xformRot=" + q);
			myScoreBoard.displayScore(2, "tpf=" + tpf);
		}
	}
	public void setScoringFlag(boolean f) {
		myTwistScoringFlag = f;
	}
	public ScoreBoard getScoreBoard() {
		return myScoreBoard;
	}
	public VirtCharPanel getVirtCharPanel() { 
		return myVCP;
	}
}
