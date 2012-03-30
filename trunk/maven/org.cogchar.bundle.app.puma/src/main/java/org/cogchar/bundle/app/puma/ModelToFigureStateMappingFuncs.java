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


import org.cogchar.render.app.bony.BonyVirtualCharApp;
import org.cogchar.render.app.bony.BonyRenderContext;
import org.cogchar.render.model.bony.FigureState;
import org.cogchar.render.model.bony.BoneState;
import org.cogchar.render.model.humanoid.HumanoidFigure;

import java.util.List;

import java.util.Map;
import java.util.Map.Entry;
import org.cogchar.bind.rk.robot.svc.BlendingRobotServiceContext;
import org.cogchar.api.skeleton.config.BoneProjectionRange;
import org.cogchar.api.skeleton.config.BoneProjectionPosition;
import org.cogchar.api.skeleton.config.BoneRotationAxis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Stu B. <www.texpedient.com>
 * 
 * 
 * In this class, all our huge design flaws are revealed!
 * EVERYTHING about this class is wrong, including it's very existence.
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
    // This is not yet a viable technique, as rotations are not commutative!
	// Also, JME3 has some confusing direction labeling things going on - appears
	// that PITCH, ROLL, YAW are not defined in the traditional manner rel. to X, Y, Z.
	// Needs review!
    private static void applySillyEulerRotations(BoneState bs, List<BoneProjectionPosition> rots){
        for(BoneProjectionPosition rot : rots){
			BoneRotationAxis rotAxis = rot.getRotationAxis();
            float rads = (float)rot.getAngleRadians();
			// theLogger.info("Rotating " + bs.getBoneName() + " around " + rotAxis + " by  " + rads + " radians.");
            switch(rotAxis) {

                case X_ROT:		bs.rot_X_A3rd = rads;  break;
                case Y_ROT:		bs.rot_Y_A1st = rads;   break;
                case Z_ROT:		bs.rot_Z_A2nd = rads; break;
					
            }
        }
    }	
}
