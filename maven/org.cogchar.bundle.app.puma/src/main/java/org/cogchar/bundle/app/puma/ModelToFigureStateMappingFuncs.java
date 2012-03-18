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

package org.cogchar.bundle.app.puma;
import java.util.ArrayList;
import org.robokind.api.motion.Joint;
import org.robokind.api.motion.Robot;

import org.cogchar.bind.rk.robot.model.ModelRobotUtils;
import org.cogchar.bind.rk.robot.model.ModelRobot;
import org.cogchar.bind.rk.robot.model.ModelJoint;


import org.cogchar.render.opengl.bony.app.BonyVirtualCharApp;
import org.cogchar.render.opengl.bony.sys.BonyRenderContext;
import org.cogchar.render.opengl.bony.state.FigureState;
import org.cogchar.render.opengl.bony.state.BoneState;
import org.cogchar.render.model.humanoid.HumanoidFigure;

import java.util.List;

import java.util.Map;
import java.util.Map.Entry;
import org.cogchar.bind.rk.robot.svc.BlendingRobotServiceContext;
import org.cogchar.bind.rk.robot.config.BoneProjectionRange;
import org.cogchar.bind.rk.robot.config.BoneProjectionPosition;
import org.cogchar.bind.rk.robot.config.BoneRotationAxis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Stu B. <www.texpedient.com>
 * 
 * 
 * In this class, all our huge design flaws are revealed!
 * EVERYTHING about this class is wrong!
 * It's a perfect 0!  Except...it works for the moment
 * as a bit of glue for testing less-fun parts.  This junk
 * shall soon be absorbed into a proper state representation.
 * Stu: 2011-12-21
 * 
 */
public class ModelToFigureStateMappingFuncs {
	static Logger theLogger = LoggerFactory.getLogger(ModelToFigureStateMappingFuncs.class);	
	
	public static void propagateState(ModelRobot br, HumanoidFigure hf) { 
		FigureState fs = hf.getFigureState();
		Map<String,List<BoneProjectionPosition>> rotMap = ModelRobotUtils.getGoalAnglesAsRotations(br);
		// theLogger.info("Sending " + fs + " to " + rotMap);
		applyAllSillyEulerRotations(fs, rotMap);
	}
    
    public static void applyAllSillyEulerRotations(FigureState fs, Map<String,List<BoneProjectionPosition>> rotMap){
       //  List<ModelBoneRotation> rots = new ArrayList<ModelBoneRotation>();
        for(Entry<String,List<BoneProjectionPosition>> e : rotMap.entrySet()){
			String boneName = e.getKey();
            BoneState bs = fs.getBoneState(boneName);
            if(bs == null){
				theLogger.warn("Can't find boneState for " + boneName);
                continue;
            }
			List<BoneProjectionPosition> rots = e.getValue();
            applySillyEulerRotations(bs, rots);
        }
    }
    // This is not really a viable technique - rotations are not commutative!
	// Also, JME3 has some confusing direction labeling things going on - appears
	// that PITCH, ROLL, YAW are not defined in the traditional manner rel. to X, Y, Z.
	// Needs review!
    private static void applySillyEulerRotations(BoneState bs, List<BoneProjectionPosition> rots){
        for(BoneProjectionPosition rot : rots){
			BoneRotationAxis rotAxis = rot.getRotationAxis();
            float rads = (float)rot.getAngleRadians();
			// theLogger.info("Rotating " + bs.getBoneName() + " angle " + rotAxis + " to " + rads + " radians.");
            switch(rotAxis) {
				/*
				 * We are currently using the EuclideanSpace.com conventions, in which 
				 * the character is treated like a vehicle pointing down the X axis,
				 * which conflicts with the JMonkey convention (and also often with the
				 * orientation of different bones of a loaded skeleton).
				 * 
				 * http://jmonkeyengine.org/groups/general-2/forum/topic/definition-of-pitch-yaw-roll-in-jmonkeyengine/?topic_page=2&num=15
				 * 
				 * 
				 */
                case PITCH: bs.rot_Z_attitude_A2nd = rads; break;
                case ROLL:  bs.rot_X_bank_A3rd = rads;  break;
                case YAW:   bs.rot_Y_heading_A1st = rads;   break;
            }
        }
    }	
}
