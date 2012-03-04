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

import org.cogchar.render.opengl.bony.gui.VirtCharPanel;
import org.cogchar.render.opengl.bony.sys.BonyRenderContext;
import org.cogchar.render.opengl.bony.world.ScoreBoard;
import com.jme3.animation.AnimControl;
import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import java.util.List;
import org.cogchar.render.opengl.bony.app.BodyController;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class StickFigureTwister extends RenderModule {
	private	BonyRenderContext		myContext;
	
	private float			myWaistTwistAngle = 0;
	private float			myWaistTwistRate = 1;
	private	boolean			myTwistScoringFlag = false;
	
	private BodyController	myBodyController;
	
	public StickFigureTwister(BonyRenderContext bc) {
		myContext = bc;
		setDebugRateModulus(100);
	}
	public void setBodyController(BodyController tc) {
		myBodyController = tc;
	}
	public void twist(float tpf) { 
				//	System.out.println("simpleUpdate, tpf=" + tpf);
		if (myBodyController != null) {
			List<AnimControl> animControls = myContext.getAnimControls();
			// VirtCharPanel vcp = myContext.getPanel();
			int twistChannelNum = myBodyController.getTwistChannelNum();
			String direction = myBodyController.getTwistDirection();
		// System.out.println("Direction=" + direction);
			AnimControl ac = animControls.get(twistChannelNum);
			Skeleton csk = ac.getSkeleton();

			Bone roots[] = csk.getRoots();
			// System.out.println("Found " + roots.length + " root bones: " + roots);
			Bone rootBone = roots[0];
			dumpBonePositionsToVCP(rootBone, "rootBone");
			Bone tgtBone = rootBone;
			String mod = myBodyController.getTwistChannelModifier();
			if (mod.equals("first child")) {
				List<Bone> kids = rootBone.getChildren();
				Bone firstKid = kids.get(0);
				tgtBone = firstKid;
			}
			twistBone(tpf, tgtBone, direction);
		}
	}
	public void dumpBonePositionsToVCP(Bone b, String prefix) { 
		//VirtCharPanel vcp = myContext.getPanel();
		//Vector3f localPos = b.getLocalPosition();
		//Vector3f modelPos = b.getModelSpacePosition();
		// System.out.println("================================================================");
		//vcp.setDumpText(prefix + "qryBone=" + b + ", localPos=" + localPos + ", modelPos=" + modelPos + ", localRot=" + b.getLocalRotation());		
	}
	public void twistBone(float tpf, Bone tgtBone, String direction) {

		myWaistTwistAngle += tpf * myWaistTwistRate;
		float posQuarterPi = FastMath.HALF_PI / 2f;
		float negQuarterPi = -1 * posQuarterPi;
		if (myWaistTwistAngle > posQuarterPi) {
			myWaistTwistAngle = posQuarterPi;
			myWaistTwistRate = -1;
		} else if (myWaistTwistAngle < negQuarterPi) {
			myWaistTwistAngle = negQuarterPi;
			myWaistTwistRate = 1;
		}

		Quaternion q = makeRotQuatForSingleAxis(myWaistTwistAngle, direction);
		applyBoneRotQuat(tgtBone, q);
		ScoreBoard sb = myContext.getScoreBoard();
		if ((myTwistScoringFlag) && (sb != null)) {
			sb.displayScore(0, "tgtBone=" + tgtBone);
			sb.displayScore(1, "xformRot=" + q);
			sb.displayScore(2, "tpf=" + tpf);
		}
	}
	public Quaternion makeRotQuatForSingleAxis(float angleMag, String direction) {
		Quaternion q = new Quaternion();
		float pitchAngle = 0.0f;
		float rollAngle = 0.0f;
		float yawAngle = 0.0f;
		if (direction.equals("pitch")) {
			pitchAngle = angleMag;
		} else if (direction.equals("roll")) {
			rollAngle = angleMag;
		} else if (direction.equals("yaw")) {
			yawAngle = angleMag;
		}
		q.fromAngles(pitchAngle, rollAngle, yawAngle);
		return q;
	}
	public static void applyBoneRotQuat(Bone tgtBone, Quaternion q) {
	
		tgtBone.setUserControl(true);
		
		// This applies rotation q to the "initial"/"bind" orientation, putting result in "local" rot.
		tgtBone.setUserTransforms(Vector3f.ZERO, q, Vector3f.UNIT_XYZ);
	}
	
	public void setScoringFlag(boolean f) {
		myTwistScoringFlag = f;
	}

	@Override protected void doRenderCycle(long runSeqNum, float tpf) {
		twist(tpf);
	}
}
