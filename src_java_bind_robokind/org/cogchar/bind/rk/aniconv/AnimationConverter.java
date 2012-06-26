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
import org.cogchar.api.skeleton.config.BoneRobotConfig;
import org.robokind.api.animation.Animation;

/**
 *
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */


public class AnimationConverter {
    public static Animation convertAnimation(
            String animName, BoneRobotConfig skeleton, StreamTokenizer st) {
        AnimationData ogreAnimData =
                OgreAnimationParser.parseAnimation(animName, st);
        AnimationData animData =
                OgreAnimationSkeletonMap.mapSkeleton(skeleton, ogreAnimData);
        NormalizingAnimationFactory converter =
                new NormalizingAnimationFactory(animData);
        
        Animation anim = converter.getAnimation();
        return anim;
    }
}
