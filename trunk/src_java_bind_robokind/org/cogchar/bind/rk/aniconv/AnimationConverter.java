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

import java.io.*;
import java.util.List;
import org.cogchar.api.skeleton.config.BoneRobotConfig;
import org.robokind.api.animation.Animation;
import org.robokind.api.animation.ControlPoint;

/**
 *
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */


public class AnimationConverter {
    public static Animation convertAnimation(
			String animName, BoneRobotConfig skeleton, MayaModelMap conversionMap, boolean useMayaMap,
			boolean useControlCurves, StreamTokenizer st) {
		//System.out.println("In AnimationConverter"); // TEST ONLY
        AnimationData ogreAnimData =
                OgreAnimationParser.parseAnimation(animName, st);
		
		//examineChannels(ogreAnimData);  // TEST ONLY ALL THIS
		
		AnimationData animData;
		if (useMayaMap) {
			OgreAnimationMayaMapper mayaMapper = new OgreAnimationMayaMapper();
			animData = mayaMapper.mapMayaModel(ogreAnimData, conversionMap);
		} else {
			animData = OgreAnimationSkeletonMap.mapSkeleton(skeleton, ogreAnimData, useControlCurves);
		}
		
		
		//examineChannels(animData);  // TEST ONLY ALL THIS

		
        NormalizingAnimationFactory converter =
                new NormalizingAnimationFactory(animData);
        
        Animation anim = converter.getAnimation();
        return anim;
    }
	
	// TEST ONLY
	private static void examineChannels(AnimationData demDatas) {
		for (ChannelData channelData : demDatas.getChannels()) { // TEST ONLY ALL THIS
			System.out.println("Found " + demDatas.getName() + " channel: " + channelData.getName() + " with " + channelData.getPoints().size() + " points"); // TEST ONLY
			List<ControlPoint> pointsList = channelData.getPoints();
			for (ControlPoint<Double> point : pointsList) {
				try {
					System.out.println("A point with time " + point.getTime() + " and position " + point.getPosition());
				} catch (Exception e) {
					System.out.println("A point had null time or position");
				}
			}
		}
	}
}
