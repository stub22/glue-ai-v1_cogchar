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

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.List;
import org.robokind.api.animation.ControlPoint;
import org.robokind.api.common.position.DoubleRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class OgreAnimationMayaMapper {
	
	private final static Logger theLogger = LoggerFactory.getLogger(OgreAnimationMayaMapper.class);
	
	
	////////////////This section contains variables necessary for "special conversions"///////////////////
	///////////////////////which are needed for converting legacy A07 anim files./////////////////////////
	///////////////It could live elsewhere, but this is probably a short-term construct///////////////////

	private List<ChannelData<Double>> elbowRotChannelsL = new ArrayList<ChannelData<Double>>();
	private List<ChannelData<Double>> elbowRotChannelsR = new ArrayList<ChannelData<Double>>();
	
	private final static String elbowRotChannelString = "_elbowRotYCtrl_01_rotate";
	private final static String leftPrefix = "cc_l";
	private final static String rightPrefix = "cc_r";
	
	private final static String elbowRotChannelStringL = leftPrefix + elbowRotChannelString;
	private final static String elbowRotChannelStringR = rightPrefix + elbowRotChannelString;
	
	private final static String[] axisSuffixes = {"X", "Y", "Z"};
	
	private final static String[] elbowRotChannelNamesL = {
		elbowRotChannelStringL + axisSuffixes[0],
		elbowRotChannelStringL + axisSuffixes[1],
		elbowRotChannelStringL + axisSuffixes[2]	
	};
	private final static String[] elbowRotChannelNamesR = {
		elbowRotChannelStringR + axisSuffixes[0],
		elbowRotChannelStringR + axisSuffixes[1],
		elbowRotChannelStringR + axisSuffixes[2]	
	};
	
	private final static String A07_ELBOW_PITCH_L = "special:A07ElbowPitch_L";
	private final static String A07_ELBOW_PITCH_R = "special:A07ElbowPitch_R";
	private final static String A07_ELBOW_YAW_L = "special:A07ElbowYaw_L";
	private final static String A07_ELBOW_YAW_R = "special:A07ElbowYaw_R";
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////

	
	AnimationData mapMayaModel(AnimationData ogreAnimData, MayaModelMap conversionMap) {
		List<String> processedChannels = new ArrayList<String>();
		AnimationData convertedAnimData = new AnimationData(ogreAnimData.getName());
		for (ChannelData<Double> channel : ogreAnimData.getChannels()) {
			String thisChannelName = channel.getName();
			if ((conversionMap.containsKey(thisChannelName)) && (AnimationTrimmer.positionsChange(channel))) {
				if (processedChannels.contains(thisChannelName)) {
					theLogger.warn("Found duplicate section in input file for channel with name {}, ignoring...", thisChannelName);
					continue;
				}
				processedChannels.add(thisChannelName);
				MayaChannelMapping channelMapping = conversionMap.get(channel.getName());
				ChannelData<Double> newChanData = makeNewChannelForMapping(channelMapping);
				for (ControlPoint<Double> point : channel.getPoints()) {
					ControlPoint millisecondPoint = new ControlPoint(point.getTime()*1000, point.getPosition());
					newChanData.addPoint(millisecondPoint);
				}
				convertedAnimData.addChannel(newChanData);
			}
			checkForSpecialMappingChannel(channel);
		}
		convertedAnimData = performSpecialMappings(convertedAnimData, conversionMap);
		clearSpecialMappingChannels(); // Yeah this is a horrible ugly way to do this, should be passing a list of these things
		return convertedAnimData;
	}
	
	private void checkForSpecialMappingChannel(ChannelData<Double> channel) {
		String channelName = channel.getName();
		for (int i=0; i<3; i++) {
			if (channelName.equals(elbowRotChannelNamesL[i])) {
				elbowRotChannelsL.add(i, channel);
			}
			if (channelName.equals(elbowRotChannelNamesR[i])) {
				elbowRotChannelsR.add(i, channel);
			}
		}
	}
	
	private void clearSpecialMappingChannels() {
		elbowRotChannelsL.clear();
		elbowRotChannelsR.clear();
	}
	
	// Performs "special" mappings requiring data reduction from multiple channels
	// Hopefully a one-off need for A07(A04) conversions; if not, this should become something less quick-and-dirty
	AnimationData performSpecialMappings(AnimationData existingData, MayaModelMap conversionMap) {
		if ((conversionMap.containsKey(A07_ELBOW_PITCH_L)) || (conversionMap.containsKey(A07_ELBOW_YAW_L))) {
			existingData = addA07ElbowRotations(elbowRotChannelsL, existingData, conversionMap, A07_ELBOW_PITCH_L, A07_ELBOW_YAW_L);
		}
		if ((conversionMap.containsKey(A07_ELBOW_PITCH_R)) || (conversionMap.containsKey(A07_ELBOW_YAW_R))) {
			existingData = addA07ElbowRotations(elbowRotChannelsR, existingData, conversionMap, A07_ELBOW_PITCH_R, A07_ELBOW_YAW_R);
		}
		return existingData;
	}
	
	AnimationData addA07ElbowRotations(List<ChannelData<Double>> elbowChannels, AnimationData existingData, 
			MayaModelMap conversionMap, String pitchKey, String yawKey) {
		theLogger.info("Adding a set of A07 Elbow rotations (pitch key {})", pitchKey);
		for (int i=0; i<3; i++) {
			try {
				if (elbowChannels.get(i) == null) {
					throw new IndexOutOfBoundsException();
				}	
			} catch (IndexOutOfBoundsException e) {
				theLogger.warn("Could not find all elbow channels for special A07 rotation mappings: {}", elbowChannels);
				return existingData;
			}
		}
		int keyframeCount = elbowChannels.get(0).getPoints().size();
		if ((keyframeCount != (elbowChannels.get(1).getPoints().size())) || (keyframeCount != (elbowChannels.get(2).getPoints().size()))) {
			theLogger.error("Attempting to process A07 elbow rotations, but not all elbow channels have the same number of keyframes!");
			return existingData;
		}
		ChannelData<Double> pitchChannel = null, yawChannel = null;
		if (conversionMap.containsKey(pitchKey)) {
			pitchChannel = makeNewChannelForMapping(conversionMap.get(pitchKey));
		}
		if (conversionMap.containsKey(yawKey)) {
			yawChannel = makeNewChannelForMapping(conversionMap.get(yawKey));
		}
		//System.out.println("Time,Pitch,Yaw,X,Y,Z"); // TEST ONLY
		boolean rightFlag = yawKey.contains("_R");
		float xStart = rightFlag? -1f : 1f;
		double xMultiplier = rightFlag? 1 : -1;
		double yMultiplier = -xMultiplier;
		double yawMultiplier = yMultiplier;
		Vector3f zeroPosition = new Vector3f(xStart, 0f, 0f);
		for (int i=0; i<keyframeCount; i++) {
			float[] rotations = {elbowChannels.get(0).getPoints().get(i).getPosition().floatValue(), 
				elbowChannels.get(1).getPoints().get(i).getPosition().floatValue(),
				elbowChannels.get(2).getPoints().get(i).getPosition().floatValue()};
			Quaternion rotQuats[] = {new Quaternion(), new Quaternion(), new Quaternion()};
			Quaternion totalRotQuat;
			rotQuats[2].fromAngleAxis(rotations[2], new Vector3f(0f,0f,1f));
			rotQuats[1].fromAngleAxis(rotations[1], rotQuats[2].mult(new Vector3f(0f,1f,0f)));
			totalRotQuat = rotQuats[1].mult(rotQuats[2]);
			rotQuats[0].fromAngleAxis(rotations[0], totalRotQuat.mult(new Vector3f(1f,0f,0f)));
			totalRotQuat = rotQuats[0].mult(totalRotQuat);
			Vector3f endPosition = totalRotQuat.mult(zeroPosition);
			
			double pitch = Math.PI - Math.acos(xMultiplier*endPosition.getX());
			double yaw = yawMultiplier*Math.atan2(yMultiplier*endPosition.getY(),endPosition.getZ());
			if (pitch == 0) {yaw = 0;}
			double time = elbowChannels.get(0).getPoints().get(i).getTime()*1000;
			if (pitchChannel != null) {
				ControlPoint pitchPoint = new ControlPoint(time, pitch);
				pitchChannel.addPoint(pitchPoint);
			}
			if (yawChannel != null) {
				ControlPoint yawPoint = new ControlPoint(time, yaw);
				yawChannel.addPoint(yawPoint);
			}
			//System.out.println(time+","+pitch+","+yaw+","+endPosition.getX()+","+endPosition.getY()+","+endPosition.getZ()); // TEST ONLY
		}
		existingData = addChannelIfChangingAndNonNull(existingData, pitchChannel);
		existingData = addChannelIfChangingAndNonNull(existingData, yawChannel);
		return existingData;
	}

	private ChannelData<Double> makeNewChannelForMapping(MayaChannelMapping mapping) {
		return new ChannelData<Double>(mapping.jointNum, 
					mapping.jointName, new DoubleRange(mapping.min, mapping.max));
	}
	
	private AnimationData addChannelIfChangingAndNonNull(AnimationData data, ChannelData<Double> channel) {
		if (channel != null) {
			if (AnimationTrimmer.positionsChange(channel)) {
				data.addChannel(channel);
			}
		}
		return data;
	}
	
}