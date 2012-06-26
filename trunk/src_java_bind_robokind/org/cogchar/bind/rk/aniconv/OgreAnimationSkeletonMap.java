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
package org.cogchar.bind.rk.aniconv;

import org.cogchar.api.skeleton.config.BoneJointConfig;
import org.cogchar.api.skeleton.config.BoneRobotConfig;
import org.cogchar.api.skeleton.config.BoneRotationAxis;
import org.cogchar.api.skeleton.config.BoneProjectionRange;

import org.robokind.api.common.position.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */
public class OgreAnimationSkeletonMap{

    public final static String ROTATE = "Ctrl_rotate";
    public final static String TRANSLATE = "Ctrl_translate";
    public final static String SCALE = "Ctrl_scale";
    public final static String X = "X";
    public final static String Y = "Y";
    public final static String Z = "Z";

    public static AnimationData mapSkeleton(
            BoneRobotConfig skeleton,
            AnimationData oldAnimData){
        Map<BoneJointConfig, ChannelData<Double>> jointTable =
                buildAnimationMap(skeleton, oldAnimData);

        AnimationData animData =
                buildAnimationData(oldAnimData.getName(), jointTable);
        
        return animData;
    }

    private static Map<BoneJointConfig, ChannelData<Double>> buildAnimationMap(
            BoneRobotConfig skeleton, AnimationData animData){
        Map<BoneJointConfig, ChannelData<Double>> jointTable =
                new HashMap(skeleton.myBJCs.size());

        for(ChannelData<Double> chan : animData.getChannels()){
            String boneName = getBoneName(chan.getName());
            if(boneName == null){
                continue;
            }

            BoneRotationAxis axis = getRotationAxis(chan.getName());
            BoneProjectionRange bpr = getProjectionRange(
                    boneName, axis, skeleton);
            BoneJointConfig joint = bpr.getJointConfig();
            
            if(jointTable.containsKey(joint)) {
                continue;
            }
            
            DoubleRange range = new DoubleRange(
                    bpr.getMinPosAngRad(), bpr.getMaxPosAngRad());

            chan.setRange(range);
            jointTable.put(joint, chan);
        }
        
        return jointTable;
    }

    private static BoneRotationAxis getRotationAxis(String chanName){
        if(chanName.endsWith(X)){
            return BoneRotationAxis.X_ROT;
        }else if(chanName.endsWith(Y)){
            return BoneRotationAxis.Y_ROT;
        }else if(chanName.endsWith(Z)){
            return BoneRotationAxis.Z_ROT;
        }

        throw new IllegalArgumentException();
    }

    private static String getBoneName(String chanName){
        if(chanName.isEmpty()){
            throw new IllegalArgumentException();
        }

        chanName = chanName.substring(0, chanName.length() - 1);

        if(chanName.endsWith(ROTATE)){
            return chanName.substring(0, chanName.length() - ROTATE.length());
        }else if(chanName.endsWith(TRANSLATE)){
            return chanName.substring(0, chanName.length() - TRANSLATE.length());
        }else if(chanName.endsWith(SCALE)){
            return chanName.substring(0, chanName.length() - SCALE.length());
        }

        return null;
    }

    private static BoneProjectionRange getProjectionRange(String boneName,
            BoneRotationAxis axis, BoneRobotConfig skeleton){
        for(BoneJointConfig joint : skeleton.myBJCs){
            if(joint.myJointName.equals(boneName)){
                for(BoneProjectionRange bpr : joint.myProjectionRanges){
                    if(bpr.getRotationAxis() == axis){
                        return bpr;
                    }
                }
            }
        }

        return null;
    }
    
    private static AnimationData buildAnimationData(
            String animName,
            Map<BoneJointConfig, ChannelData<Double>> jointTable) {
        AnimationData newAnimData = new AnimationData(animName);
        
        for(Entry<BoneJointConfig,ChannelData<Double>> e : jointTable.entrySet()){
            BoneJointConfig joint = e.getKey();
            ChannelData<Double> chanDataOrig = e.getValue();
            int id = joint.myJointNum;
            String name = joint.myJointName;
            ChannelData<Double> chanData = 
                    new ChannelData<Double>(id, name, chanDataOrig.getRange());
            newAnimData.addChannel(chanData);
        }
        return newAnimData;
    }
}
