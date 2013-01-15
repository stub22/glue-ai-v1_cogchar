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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.cogchar.api.skeleton.config.BoneJointConfig;
import org.cogchar.api.skeleton.config.BoneProjectionRange;
import org.cogchar.api.skeleton.config.BoneRobotConfig;
import org.cogchar.api.skeleton.config.BoneRotationAxis;
import org.robokind.api.animation.ControlPoint;
import org.robokind.api.common.position.DoubleRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */
public class OgreAnimationSkeletonMap{

	public final static String ROTATE = "_rotate";
	public final static String TRANSLATE = "_translate";
	public final static String SCALE = "_scale";
    public final static String X = "rotateX";
    public final static String Y = "rotateY";
    public final static String Z = "rotateZ";
	
	//private final static JunkyConversionTable JUNKY = new JunkyConversionTable(); // A temporary and exceedingly ugly crutch
	
	private static Logger theLogger = LoggerFactory.getLogger(OgreAnimationSkeletonMap.class); 

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
			
            BoneRotationAxis axis = getRotationAxis(chan.getName());

			//theLogger.info("Processing bone {} with axis {} from chanName " + chan.getName(), boneName, axis); // TEST ONLY
			if ((axis == null) || (boneName == null)) {continue;} // will be true unless chan.getName() suffix is rotateX/Y/Z
			//theLogger.info("Looking for boneName " + boneName + " and axis " + axis + " from name " + chan.getName()); // TEST ONLY
            BoneProjectionRange bpr = getProjectionRange(
                    boneName, axis, skeleton);
			if (bpr == null) {
				theLogger.warn("Could not find BoneProjectionRange for bone: {} -- ignoring", boneName);
				continue;
			}
			theLogger.info("Adding joint for bone: {}", boneName);
            BoneJointConfig joint = bpr.getJointConfig();
            
			// Ignores duplicated channel sections -- potentially a dangerous assumption:
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
		return null;
    }

    private static String getBoneName(String chanName){
        if(chanName.isEmpty()){
            throw new IllegalArgumentException();
        }
		
		if (chanName.contains("Global")) {return "Root";} // Handles "root" rotations; we may or may not want this as such
		String boneName = null;

		// Remove last character from chanName, which is typically the axis designation (X, Y, or Z)
        chanName = chanName.substring(0, chanName.length() - 1);

        if(chanName.endsWith(ROTATE)){
            boneName = chanName.substring(0, chanName.length() - ROTATE.length());
        }/*else if(chanName.endsWith(TRANSLATE)){
            boneName = chanName.substring(0, chanName.length() - TRANSLATE.length());
        }else if(chanName.endsWith(SCALE)){
            boneName = chanName.substring(0, chanName.length() - SCALE.length());
        }
		*/

		return boneName; 
    }

    private static BoneProjectionRange getProjectionRange(String boneName,
            BoneRotationAxis axis, BoneRobotConfig skeleton){
		//theLogger.info("Getting projection ranges from BRC: {}", skeleton); // TEST ONLY
        for(BoneJointConfig joint : skeleton.myBJCs){
                for(BoneProjectionRange bpr : joint.myProjectionRanges){
					if ((bpr.myBoneName.equals(boneName)) && (bpr.getRotationAxis() == axis)){
						theLogger.info("Found matching bone for {}", boneName); // TEST ONLY
                        return bpr;
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
			// It appears that the channel pairs (control points) are never added! The loop below will try to do that, at least in a quick-and-dirty way:
			for (ControlPoint<Double> point : chanDataOrig.getPoints()) {
				ControlPoint millisecondPoint = new ControlPoint(point.getTime()*1000, point.getPosition());
				chanData.addPoint(millisecondPoint); // We just copy them with no conversion except time -- this seems to be the right thing to do...
			}
			if (AnimationTrimmer.positionsChange(chanData)) {
				newAnimData.addChannel(chanData);
			}
        }
        return newAnimData;
    }
}
