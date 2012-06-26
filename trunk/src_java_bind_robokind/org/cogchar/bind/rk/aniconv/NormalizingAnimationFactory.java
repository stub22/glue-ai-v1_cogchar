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

import org.robokind.api.animation.Animation;
import org.robokind.api.animation.Channel;
import org.robokind.api.animation.ControlPoint;
import org.robokind.api.animation.MotionPath;
import org.robokind.api.common.position.*;


/**
 *
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */


public class NormalizingAnimationFactory {
    private AnimationData animData;
    private Animation anim;
    
    public NormalizingAnimationFactory(AnimationData animData) {
        if(animData == null) {
            throw new NullPointerException();
        }
        
        this.animData = animData;
        
        buildAnimation();
    }
    
    public Animation getAnimation() {
        return anim;
    }
    
    private void buildAnimation() {
        anim = new Animation();
        
        for(ChannelData<Double> chanData: animData.getChannels()) {
            Channel chan = buildChannel(chanData);
            anim.addChannel(chan);
        }
    }
    
    private Channel buildChannel(ChannelData<?> chanData) {
        MotionPath mp = new MotionPath();
        Channel chan = new Channel(chanData.getID(), chanData.getName());

        for(ControlPoint<NormalizedDouble> point: chanData.normalizePoints()) {
            mp.addPoint(point.getTime(), point.getPosition().getValue());
        }

        chan.addPath(mp);
        
        return chan;
    }
}
