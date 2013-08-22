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
package org.cogchar.animoid.broker;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import org.cogchar.animoid.job.AnimationExecJob;

import org.cogchar.animoid.job.BlenderJob;
import org.cogchar.animoid.job.VisemeJob;
import org.cogchar.animoid.calc.estimate.PositionEstimator;
import org.cogchar.api.animoid.config.bonus.AnimoidConfig;
import org.cogchar.xml.animoid.AnimoidConfigLoader;

import org.cogchar.api.animoid.config.bonus.ServoChannelConfig;
import org.cogchar.api.animoid.config.bonus.VisemeConfig;

import org.cogchar.api.animoid.protocol.Animation;
import org.cogchar.api.animoid.protocol.Device;

import org.cogchar.api.animoid.protocol.Frame;
import org.cogchar.api.animoid.protocol.Joint;
import org.cogchar.api.animoid.protocol.JointPosition;
import org.cogchar.api.animoid.protocol.JointPositionAROM;
import org.cogchar.api.animoid.protocol.JointStateCoordinateType;
import org.cogchar.api.animoid.protocol.Library;
import org.cogchar.api.animoid.protocol.Robot;
import org.cogchar.zzz.platform.stub.JobSpaceStub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stu B. <www.texpedient.com>
 * 
 * Lower level components should NOT know about the AnimoidFacade.
 */
public class AnimoidFacade implements Animator {
	private static Logger	theLogger = LoggerFactory.getLogger(AnimoidFacade.class.getName());

	private		AnimoidConfig			myAnimoidConfig;	
	// private		Animator				myAnimator;
	private		MotionController		myMotionController;
	private		BlenderJob				myBlenderJob;
	/*
	private		SightModel				mySightModel;
	*/
	private		Robot					myMainRobot;
	private		Device					myMainDevice;
	private		Library					myAnimationLibrary;

	private		VisemeConfig			myVisemeConfig;	
	private		ServoChannelConfig[]	myServoConfigSparseArray;
	private		JobSpaceStub			myJobSpace;
	private		AnimoidCueSpaceStub			myCueSpace;

	private		boolean					myAnimationsEnabledFlag = false;
	
	public AnimoidFacade(String servoConfigPath, URL animoidConfigURL, 
				String visemeConfigPath, Integer msecPerFrame,
				double frameDurationSmoothingFactor) throws Throwable {

		loadServoChannelConfigs(servoConfigPath);
		theLogger.info("Reading animoid config from URL: " + animoidConfigURL);
		myAnimoidConfig = AnimoidConfigLoader.loadAnimoidConfig(animoidConfigURL,
					myMainRobot, msecPerFrame, frameDurationSmoothingFactor);
		// SightHypothesis.loadConfig(myAnimoidConfig.getFaceNoticeConfig());
		loadVisemeConfig(visemeConfigPath);
	}
	/*
	public void setSightModel(SightModel sm) {
		mySightModel = sm;
	}
	public SightModel getSightModel() {
		return mySightModel;
	}
	*/
	public void setJobSpace(JobSpaceStub jobSpace) {
		myJobSpace = jobSpace;
	}
	public void setCueSpace(AnimoidCueSpaceStub cueSpace) {
		myCueSpace = cueSpace;
	}
	public void setAnimationLibrary(Library lib) {
		myAnimationLibrary = lib;
	}
	private void loadServoChannelConfigs(String servoConfigPath) throws Throwable {
		theLogger.info("Reading servo config file from: " + servoConfigPath);
		myServoConfigSparseArray = ServoChannelConfig.readServoConfigFile(servoConfigPath, 32);
		initUsingServoConfigSparseArray(myServoConfigSparseArray);

	}

	private void loadVisemeConfig(String visemeConfigFilename) throws Throwable {
		if (visemeConfigFilename != null) {
			myVisemeConfig = VisemeConfig.buildVisemeConfig(visemeConfigFilename, myServoConfigSparseArray, myMainRobot);
		} else {
			theLogger.warn("VisemeConfigFilename is null - skipping viseme config");
		}
	}	
	public void setupBlenderJob() {
		myBlenderJob = new BlenderJob(myAnimoidConfig);
		myJobSpace.postManualJob(myBlenderJob);
	}
	public void setTestMotionJobs() {
		myBlenderJob.setupTestMotionJobs(myAnimoidConfig, myJobSpace); // mySightModel, 
		myBlenderJob.theTestVisemeJob.setCurrentTargetPosFrame(getVisemeFrameInAbsROM(0));
		myBlenderJob.theTestVisemeJob.setNextTargetPosFrame(getVisemeFrameInAbsROM(0));
	}
	@Override public AnimoidConfig getAnimoidConfig() {
		return myAnimoidConfig;
	}		
	public Double getSecondsPerFrame() {
		return getAnimoidConfig().getSecondsPerFrame();
	}
	public PositionEstimator getPositionEstimator() {
		return myBlenderJob;
	}
	public void setMotionController(MotionController mc) {
		myMotionController = mc;
	}
	public MotionController getMotionController() {
		return myMotionController;
	}
	private AnimationExecJob startAnimationJob(Animation a, String gestureName, double rashAllowMult, double rashBonusAllow) {
		AnimationExecJob aej = new AnimationExecJob(myAnimoidConfig, a, gestureName, rashAllowMult, rashBonusAllow);
		aej.scheduleToStartNow();
		aej.click();
		myBlenderJob.registerMotionJob(aej);
		myJobSpace.postManualJob(aej);
		return aej;
	}
	public void playAnimation(String animName, String gestureName, double rashAllowMult, double rashBonusAllow) {
		if(!myAnimationsEnabledFlag) {
			theLogger.info("Ignoring request to play anim/gesture[" + animName + "/" + gestureName + "] because animations are disabled");
			return;
		}
		// We don't have an animation-start or animation-end broadcaster in this version, yet
		Animation a = getAnimationLibrary().getAnimationForName(animName);	
		if (a != null) {
			startAnimationJob(a, gestureName, rashAllowMult, rashBonusAllow);
		} else {
			theLogger.warn("Can't find animation named: " + animName + ", ignoring play request");
		}
		// SignalStation ss = SignalStation.getSignalStation();
		// ss.animationStarted(aej);			
	}
	public void initUsingServoConfigSparseArray(ServoChannelConfig[] servoConfigSparseArray) {
		myServoConfigSparseArray = servoConfigSparseArray;
		myMainRobot = new Robot("MAIN_ROBOT");
		myMainDevice = new Device("MAIN_DEVICE", Device.Type.SSC32_V20);
		for (int channelIDX = 0; channelIDX < servoConfigSparseArray.length;  channelIDX++) {
			ServoChannelConfig scc = servoConfigSparseArray[channelIDX];
			if (scc != null) {
				String deviceChannelID = "" + channelIDX;
				int logicalChannelNum = scc.logicalChannel;
				String jointName = scc.getMuscleJoint().name();
				Joint j = new Joint(myMainDevice, myMainRobot, deviceChannelID, jointName);
				j.oldLogicalJointNumber = logicalChannelNum;
				j.oldMinServoPos = scc.minPos;
				j.oldDefServoPos = scc.defaultPos;
				j.oldMaxServoPos = scc.maxPos;
				j.oldInvertedFlag = scc.inverted;
				myMainRobot.registerJoint(j);
				myMainDevice.registerJoint(j);
			}
		}
	}
	public Robot getMainRobot() {
		return myMainRobot;
	}
	public Device getMainDevice() {
		return myMainDevice;
	}	
	public Library getAnimationLibrary() {
		return myAnimationLibrary;
	}
	public Frame<JointPositionAROM> getVisemeFrameInAbsROM (int visemeNumber) {
		Frame result = null;
		if (myVisemeConfig != null) {
			result = myVisemeConfig.getFrameForVisemeNumber(visemeNumber);
		}
		return result;
	}

	public ServoChannelConfig[] getServoChannelConfigSparseArray() {
		return myServoConfigSparseArray;
	}	
	public VisemeJob getVisemeJob() {
		return myBlenderJob.theTestVisemeJob;
	}
	public VisemeConfig getVisemeConfig(){
		return myVisemeConfig;
	}

	public void suggestViseme(int curViseme, int duration, byte flags, int nextViseme) {
		theLogger.trace("Suggesting a viseme: curViseme-" + curViseme +
				", nextViseme-" + nextViseme + ", durration-" + duration);
		VisemeJob vj = myBlenderJob.theTestVisemeJob;
		vj.setCurrentVisemeNumber(curViseme);
		vj.setNextVisemeNumber(nextViseme);
		vj.setDurationMillisec(duration);
		
		Frame currentFrame = getVisemeFrameInAbsROM(curViseme);
		Frame nextFrame = getVisemeFrameInAbsROM(nextViseme);
		
		vj.setCurrentTargetPosFrame(currentFrame);
		vj.setNextTargetPosFrame(nextFrame);
		// Note that this "update" does not trigger a JMX broadcast.
		// In the past, we tried posting a new VisemeJob on every change,
		// but this resulted in a rather high volume of JMX messages.
		vj.markUpdated();		
	}
	public void suggestAnimationScriptName(String scriptName, String gestureName,
				double rashAllowMult, double rashBonusAllow) {
		playAnimation(scriptName, gestureName, rashAllowMult, rashBonusAllow);
	}
	public Frame transformFrame(Frame currPosAbsRomFrame) {	
		if (myBlenderJob != null) {
			return myBlenderJob.transformFrame(currPosAbsRomFrame);
		} else {
			return currPosAbsRomFrame;
		}
	}
	public JointPosition getJointPositionAbsROM(Integer oldLogicalJointNumber, boolean now){
		Frame posEstimate = myBlenderJob.estimatePositionNow(now);
		JointPosition jp = posEstimate.getJointPositionForOldLogicalJointNumber(oldLogicalJointNumber);
		return jp.convertToCooordinateType(JointStateCoordinateType.FLOAT_ABS_RANGE_OF_MOTION);
	}

	public static final String	T_VISEMES_ENABLED = "T_VISEMES_ENABLED";
	public static final String	T_VISEMES_DISABLED = "T_VISEMES_DISABLED";
	public static final String	T_SCRIPTED_ENABLED = "T_ALL_ANIMS_ENABLED";
	public static final String	T_SCRIPTED_DISABLED = "T_ALL_ANIMS_DISABLED";
	public static final String	T_ATTENTION_ENABLED = "T_ATTENTION_ENABLED";
	public static final String	T_ATTENTION_DISABLED = "T_ATTENTION_DISABLED";

	public void enableVisemes() {
		theLogger.info("Enabling visemes");
		VisemeJob vj = getVisemeJob();
		if (vj != null) {
			vj.enableMotion();
		}
		// myCueSpace.clearMatchingNamedCues(T_VISEMES_DISABLED);
		myCueSpace.addThoughtCueForName(T_VISEMES_ENABLED, 1.0);
	}
	public void disableVisemes() {
		theLogger.info("Disabling visemes");
		VisemeJob vj = getVisemeJob();
		if (vj != null) {
			vj.disableMotion();
		}
		myCueSpace.clearMatchingNamedCues(T_VISEMES_ENABLED);
		myCueSpace.addThoughtCueForName(T_VISEMES_DISABLED, 1.0);
	}
	public void enableScriptedAnimations() {
		theLogger.info("Enabling scripted animations");
		myAnimationsEnabledFlag = true;
		myCueSpace.clearMatchingNamedCues(T_SCRIPTED_DISABLED);
		myCueSpace.addThoughtCueForName(T_SCRIPTED_ENABLED, 1.0);
	}
	public void disableScriptedAnimations() {
		theLogger.info("Disabling scripted animations");
		myAnimationsEnabledFlag = false;
		myCueSpace.clearMatchingNamedCues(T_SCRIPTED_ENABLED);
		myCueSpace.addThoughtCueForName(T_SCRIPTED_DISABLED, 1.0);
	}
	public void killAllAnimations() {
		theLogger.info("Killing all animations via thalamus (not Blender)");
		myJobSpace.terminateAndClearJobsInClass(AnimationExecJob.class);
	}
	public void forceServosToCenter() {
		theLogger.info("Forcibly playing recentering animation");
		AnimoidConfig ac = getAnimoidConfig();
		double secPerFrame = ac.getSecondsPerFrame();
		Animation anim = AnimationBuilder.makeRobotCenteringAnimation(myMainRobot,
				"RECENTER", 40, secPerFrame);
		AnimationExecJob aej = startAnimationJob(anim, "FORCE_RECENTER", 1.2, 0.025);
		aej.setOverrideCautionFlag(true);
	}

    public Map<Integer, Integer> getRoboardServoMap(){
        Map<Integer, Integer> map = new HashMap();
        for(ServoChannelConfig scc : myServoConfigSparseArray){
            if(scc == null || scc.roboardChannel == -1){
                continue;
            }
            map.put(scc.logicalChannel, scc.roboardChannel);
        }
        return map;
    }
}
