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

import java.util.ArrayList;
import java.util.List;
import org.robokind.api.animation.ControlPoint;
import org.robokind.api.common.position.DoubleRange;
import org.robokind.api.common.position.NormalizableRange;
import org.robokind.api.common.position.NormalizedDouble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class OgreAnimationMayaMapper {
	
	private final static Logger theLogger = LoggerFactory.getLogger(OgreAnimationMayaMapper.class);
	
	AnimationData mapMayaModel(AnimationData ogreAnimData, MayaModelMap conversionMap) {
		List<String> processedChannels = new ArrayList<String>();
		AnimationData convertedAnimData = new AnimationData(ogreAnimData.getName());
		for (ChannelData<Double> channel : ogreAnimData.getChannels()) {
			String thisChannelName = channel.getName();
			if (conversionMap.containsKey(thisChannelName)) {
				if (processedChannels.contains(thisChannelName)) {
					theLogger.warn("Found duplicate section in input file for channel with name {}, ignoring...", thisChannelName);
					continue;
				}
				processedChannels.add(thisChannelName);
				MayaChannelMapping channelMapping = conversionMap.get(channel.getName());
				ChannelData<Double> newChanData = new ChannelData<Double>(channelMapping.jointNum, 
						channelMapping.jointName, new DoubleRange(channelMapping.min, channelMapping.max));
				for (ControlPoint<Double> point : channel.getPoints()) {
					ControlPoint millisecondPoint = new ControlPoint(point.getTime()*1000, point.getPosition());
					newChanData.addPoint(millisecondPoint);
				}
				convertedAnimData.addChannel(newChanData);
			}
		}
		return convertedAnimData;
	}
}
