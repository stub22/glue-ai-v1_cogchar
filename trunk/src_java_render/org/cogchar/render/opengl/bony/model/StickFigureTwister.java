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

package org.cogchar.render.opengl.bony.model;

import org.cogchar.render.opengl.bony.sys.VirtCharPanel;
import org.cogchar.render.opengl.bony.sys.BonyContext;
import org.cogchar.render.opengl.bony.world.ScoreBoard;
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
	private	BonyContext		myContext;
	
	private float			myWaistTwistAngle = 0;
	private float			myWaistTwistRate = 1;
	private	boolean			myTwistScoringFlag = false;
	
	public StickFigureTwister(BonyContext bc) {
		myContext = bc;
	}
	public void twist(float tpf) { 
				//	System.out.println("simpleUpdate, tpf=" + tpf);
		List<AnimControl> animControls = myContext.getAnimControls();
		VirtCharPanel vcp = myContext.getPanel();
		int testChannelNum = vcp.getTestChannelNum();

		String direction = vcp.getTestDirection();
		// System.out.println("Direction=" + direction);
		AnimControl ac = animControls.get(testChannelNum);
		Skeleton csk = ac.getSkeleton();

		Bone roots[] = csk.getRoots();
		// System.out.println("Found " + roots.length + " root bones: " + roots);
		Bone rootBone = roots[0];
		Bone tgtBone = rootBone;
		String mod = vcp.getTestChannelModifier();
		if (mod.equals("first child")) {
			List<Bone> kids = rootBone.getChildren();
			Bone firstKid = kids.get(0);
			tgtBone = firstKid;
		}
		twistBone(tpf, rootBone, tgtBone, direction);
	}
	public void twistBone(float tpf, Bone rootBone, Bone tgtBone, String direction) {
		VirtCharPanel vcp = myContext.getPanel();
		Vector3f localPos = rootBone.getLocalPosition();
		Vector3f modelPos = rootBone.getModelSpacePosition();
		// System.out.println("================================================================");
		vcp.setDumpText("tgtBone=" + tgtBone + ", localPos=" + localPos + ", modelPos=" + modelPos + ", localRot=" + rootBone.getLocalRotation());


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
		ScoreBoard sb = myContext.getScoreBoard();
		if ((myTwistScoringFlag) && (sb != null)) {
			sb.displayScore(0, "tgtBone=" + tgtBone);
			sb.displayScore(1, "xformRot=" + q);
			sb.displayScore(2, "tpf=" + tpf);
		}
	}
	public void setScoringFlag(boolean f) {
		myTwistScoringFlag = f;
	}
}
