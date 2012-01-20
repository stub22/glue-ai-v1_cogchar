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
import org.cogchar.bind.rk.robot.model.ModelRobotFactory;
import org.cogchar.bind.rk.robot.model.ModelRobot;
import org.cogchar.bind.rk.robot.model.ModelJoint;


import org.cogchar.render.opengl.bony.app.BonyVirtualCharApp;
import org.cogchar.render.opengl.bony.sys.BonyRenderContext;
import org.cogchar.render.opengl.bony.state.FigureState;
import org.cogchar.render.opengl.bony.state.BoneState;

import java.util.List;

import java.util.Map;
import java.util.Map.Entry;
import org.cogchar.bind.rk.robot.svc.BlendingRobotServiceContext;
import org.cogchar.bind.rk.robot.model.ModelBoneRotRange;
import org.cogchar.bind.rk.robot.model.ModelBoneRotation;
import org.cogchar.avrogen.bind.robokind.RotationAxis;
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
	public static void propagateState(ModelRobot br, BonyRenderContext bc) { 
		FigureState fs = bc.getFigureState();
		Map<String,List<ModelBoneRotation>> rotMap = ModelRobotUtils.getGoalAnglesAsRotations(br);
		// theLogger.info("Sending " + fs + " to " + rotMap);
		applyAllSillyEulerRotations(fs, rotMap);
	}
    
    public static void applyAllSillyEulerRotations(FigureState fs, Map<String,List<ModelBoneRotation>> rotMap){
       //  List<ModelBoneRotation> rots = new ArrayList<ModelBoneRotation>();
        for(Entry<String,List<ModelBoneRotation>> e : rotMap.entrySet()){
			String boneName = e.getKey();
            BoneState bs = fs.getBoneState(boneName);
            if(bs == null){
				theLogger.warn("Can't find boneState for " + boneName);
                continue;
            }
			List<ModelBoneRotation> rots = e.getValue();
            applySillyEulerRotations(bs, rots);
        }
    }
    // This is not a viable technique - rotations are not commutative!
    private static void applySillyEulerRotations(BoneState bs, List<ModelBoneRotation> rots){
        for(ModelBoneRotation rot : rots){
			RotationAxis rotAxis = rot.getRotationAxis();
            float rads = (float)rot.getAngleRadians();
			// theLogger.info("Rotating " + bs.getBoneName() + " angle " + rotAxis + " to " + rads + " radians.");
            switch(rotAxis) {
                case PITCH: bs.rot_X_pitch = rads; break;
                case ROLL:  bs.rot_Y_roll = rads;  break;
                case YAW:   bs.rot_Z_yaw = rads;   break;
            }
        }
    }	
}
