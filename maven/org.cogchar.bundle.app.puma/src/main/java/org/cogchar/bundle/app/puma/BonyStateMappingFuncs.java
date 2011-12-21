/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.bundle.app.puma;
import java.util.ArrayList;
import org.robokind.api.motion.Joint;
import org.robokind.api.motion.Robot;

import org.cogchar.bind.robokind.joint.BonyRobotUtils;
import org.cogchar.bind.robokind.joint.BonyRobotFactory;
import org.cogchar.bind.robokind.joint.BonyRobot;
import org.cogchar.bind.robokind.joint.BonyJoint;


import org.cogchar.render.opengl.bony.app.BonyVirtualCharApp;
import org.cogchar.render.opengl.bony.sys.BonyContext;
import org.cogchar.render.opengl.bony.state.FigureState;
import org.cogchar.render.opengl.bony.state.BoneState;

import java.util.List;

import java.util.Map;
import java.util.Map.Entry;
import org.cogchar.bind.robokind.joint.BlendingBonyRobotContext;
import org.cogchar.bind.robokind.joint.BlendingRobotContext;
import org.cogchar.bind.robokind.joint.BoneRotationRange;
import org.cogchar.bind.robokind.joint.BoneRotationRange.BoneRotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class BonyStateMappingFuncs {
	public static void propagateState(BonyRobot br, BonyContext bc) { 
		FigureState fs = bc.getFigureState();
        applyAllRotations(fs, BonyRobotUtils.getGoalAnglesAsRotations(br));
	}
    
    private static void applyAllRotations(FigureState fs, Map<String,List<BoneRotation>> rotMap){
        List<BoneRotation> rots = new ArrayList<BoneRotation>();
        for(Entry<String,List<BoneRotation>> e : rotMap.entrySet()){
            BoneState bs = fs.getBoneState(e.getKey());
            if(bs == null){
                continue;
            }
            applyRotations(bs, rots);
        }
    }
    
    private static void applyRotations(BoneState bs, List<BoneRotation> rots){
        for(BoneRotation rot : rots){
            float rads = (float)rot.getAngleRadians();
            switch(rot.getRotationAxis()){
                case PITCH: bs.rot_X_pitch = rads; break;
                case ROLL:  bs.rot_Y_roll = rads;  break;
                case YAW:   bs.rot_Z_yaw = rads;   break;
            }
        }
    }	
}
