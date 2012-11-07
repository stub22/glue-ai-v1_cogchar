/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.render.model.humanoid;

import org.cogchar.api.humanoid.HumanoidBoneDesc;
import org.cogchar.api.humanoid.HumanoidBoneConfig;

import com.jme3.bullet.joints.SixDofJoint;
import com.jme3.bullet.joints.motors.RotationalLimitMotor;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.FastMath;

import java.util.List;
import org.cogchar.api.humanoid.HumanoidFigureConfig;


/**
 * @author Stu B. <www.texpedient.com>
 */
public class PhysicallyWigglingHumanoid extends HumanoidFigure {
	private float myPhysicsWigglePhase = 0.0f;		
	public 	PhysicallyWigglingHumanoid(HumanoidFigureConfig hfc)   {
		super(hfc);
	}
	public void wiggleUsingPhysics(float tpf) { 
		wiggleUsingPhysicsMotors(getHBConfig(), tpf);
	}
	public void wiggleUsingPhysicsMotors(HumanoidBoneConfig hbc, float tpf) {
		myPhysicsWigglePhase += tpf / 10.0f;
		if (myPhysicsWigglePhase > 1.0f) {
			System.out.println("************ Wiggle phase reset ------ hmmmm, OK");
			myPhysicsWigglePhase = 0.0f;
		}
		float amplitude = 5.0f;
		float wigglePhaseRad = FastMath.TWO_PI  * myPhysicsWigglePhase;
		float wiggleVel = amplitude * FastMath.sin2(wigglePhaseRad);

		if (myPhysicsWigglePhase < 0.5) {
			wiggleVel = amplitude;
		} else {
			wiggleVel = -1.0f * amplitude;
		}
		wiggleAllBonesUsingRotMotors(hbc, wiggleVel);
	}
	private void wiggleAllBonesUsingRotMotors(HumanoidBoneConfig hbc, float wiggleVel) {
		List<HumanoidBoneDesc> descs = hbc.getBoneDescs();
		for(HumanoidBoneDesc hbd : descs) {
			String boneName = hbd.getSpatialName();
			// Uncomment to wigle just the "Head" bone.
			//if (!boneName.equals("Head")) {
			//	continue;
			//}
			// Don't have a direct need for the PRB yet, but we're sure to later!
			PhysicsRigidBody prb = myHumanoidKRC.getBoneRigidBody(boneName);
			SixDofJoint boneJoint = myHumanoidKRC.getJoint(boneName);
			RotationalLimitMotor xRotMotor =  boneJoint.getRotationalLimitMotor(0);
			RotationalLimitMotor yRotMotor =  boneJoint.getRotationalLimitMotor(1);
			RotationalLimitMotor zRotMotor =  boneJoint.getRotationalLimitMotor(2);
			
			xRotMotor.setTargetVelocity(wiggleVel);
			yRotMotor.setTargetVelocity(wiggleVel);
			zRotMotor.setTargetVelocity(wiggleVel);
		}
	
	}	
}
