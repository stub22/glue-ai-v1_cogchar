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

import java.util.HashMap;
import java.util.List;
import java.util.Map;



import org.cogchar.animoid.output.AnimationExecJob;
import org.cogchar.animoid.output.AttentionJob;
import org.cogchar.animoid.output.BlenderJob;
import org.cogchar.animoid.output.VisemeJob;
import org.cogchar.animoid.calc.estimate.PositionEstimator;
import org.cogchar.animoid.config.AnimoidConfig;
import org.cogchar.animoid.config.AnimoidConfigLoader;
import org.cogchar.animoid.config.GazeJoint;
import org.cogchar.animoid.config.ServoChannelConfig;
import org.cogchar.animoid.config.VisemeConfig;
import org.cogchar.animoid.gaze.GazeStrategyCue;
import org.cogchar.animoid.gaze.IGazeTarget;
import org.cogchar.animoid.protocol.Animation;
import org.cogchar.animoid.protocol.Device;
import org.cogchar.animoid.protocol.EgocentricDirection;
import org.cogchar.animoid.protocol.Frame;
import org.cogchar.animoid.protocol.Joint;
import org.cogchar.animoid.protocol.JointPosition;
import org.cogchar.animoid.protocol.JointPositionAROM;
import org.cogchar.animoid.protocol.JointStateCoordinateType;
import org.cogchar.animoid.protocol.Library;
import org.cogchar.animoid.protocol.Robot;
import org.cogchar.platform.stub.JobSpaceStub;
import org.cogchar.sight.hypo.SightHypothesis;
import org.cogchar.sight.hypo.SightModel;
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
	private		SightModel				mySightModel;
	private		Robot					myMainRobot;
	private		Device					myMainDevice;
	private		Library					myAnimationLibrary;

	private		VisemeConfig			myVisemeConfig;	
	private		ServoChannelConfig[]	myServoConfigSparseArray;
	private		JobSpaceStub			myJobSpace;
	private		AnimoidCueSpaceStub			myCueSpace;

	private		boolean					myAnimationsEnabledFlag = false;
	
	public AnimoidFacade(String servoConfigPath, String animoidConfigPath, 
				String visemeConfigPath, Integer msecPerFrame,
				double frameDurationSmoothingFactor) throws Throwable {

		loadServoChannelConfigs(servoConfigPath);
		theLogger.info("Reading animoid config file from: " + animoidConfigPath);
		myAnimoidConfig = AnimoidConfigLoader.loadAnimoidConfig(animoidConfigPath,
					myMainRobot, msecPerFrame, frameDurationSmoothingFactor);
		SightHypothesis.loadConfig(myAnimoidConfig.getFaceNoticeConfig());
		loadVisemeConfig(visemeConfigPath);
	}
	public void setSightModel(SightModel sm) {
		mySightModel = sm;
	}
	public SightModel getSightModel() {
		return mySightModel;
	}
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
		myBlenderJob.setupTestMotionJobs(mySightModel, myAnimoidConfig, myJobSpace);
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
	public void suggestGazeStrategyName(String gazeStrategyName) {
		GazeStrategyCue gazeStrategy = myAnimoidConfig.getNamedGazeStrategy(gazeStrategyName);
		myBlenderJob.theTestAttentionJob.suggestGazeStrategy(gazeStrategy);
	}
	public void suggestHoldStrategyName(String holdStrategyName) {
		GazeStrategyCue holdAndRecenterStrategy = myAnimoidConfig.getNamedGazeStrategy(holdStrategyName);
		myBlenderJob.theTestAttentionJob.suggestHoldAndRecenterStrategy(holdAndRecenterStrategy);
	}
	public void suggestAttentionTarget(IGazeTarget target) {
		// Please do not call this method.  It's not really public!!!
		// As of 2010-3-27, it should only be called from PersonResolver.
		myBlenderJob.theTestAttentionJob.suggestAttentionTarget(target);
	}
	public AttentionJob getAttentionJob() { 
		return myBlenderJob.theTestAttentionJob;
	}
	public VisemeJob getVisemeJob() {
		return myBlenderJob.theTestVisemeJob;
	}
	public IGazeTarget getAttentionTarget() {
		return getAttentionJob().getAttentionTarget();
	}
	public void suggestAttentionState(boolean state) {
		theLogger.warn("Requested gaze attention state: " + state);
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
	/*
	public JointPositionSnapshot getCurrentGazeSnapshot(){
		Frame thisFrame = myBlenderJob.estimatePositionNow(true);
		// Note - this uses hardcoded muscle joint IDs!
		return JointPositionSnapshot.getGazeSnapshot(thisFrame);
	}
	 */
	public EgocentricDirection getCurrentEgocentricDirection(){
		Frame f = myBlenderJob.estimatePositionNow(true);
		if (f != null) {
			return mySightModel.getGazeDirectionComputer().computeGazeCenterDirection(f);
		} else {
			return null;
		}
	}

	public List<GazeJoint> getAllGazeJoints() {
		return getAnimoidConfig().getGazeJoints();
	}

	public VisemeConfig getVisemeConfig(){
		return myVisemeConfig;
	}
	public void rebalanceGazeJobs() {
		AttentionJob aj = myBlenderJob.theTestAttentionJob;
		if (aj != null) {
			aj.rebalanceGazeJobs();
		}
	}

	public String getAttentionDebugText() {
		String attentionDebugText = "No Attention Job";
		AttentionJob aj = getAttentionJob();
		if (aj != null) {
			attentionDebugText = aj.getAttentionDebugText();
		}
		EgocentricDirection currDir = getCurrentEgocentricDirection();
		String debugText ="currDir=" + currDir + "\n\n" + attentionDebugText;
		return debugText;
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
	public void enableAttentionGaze() {
		theLogger.info("Enabling attention gaze");
		AttentionJob aj = getAttentionJob();
		if (aj != null) {
			aj.enableMotion();
		}
		myCueSpace.clearMatchingNamedCues(T_ATTENTION_DISABLED);
		myCueSpace.addThoughtCueForName(T_ATTENTION_ENABLED, 1.0);
	}
	public void disableAttentionGaze() {
		theLogger.info("Disabling attention gaze");
		AttentionJob aj = getAttentionJob();
		if (aj != null) {
			aj.disableMotion();
		}
		myCueSpace.clearMatchingNamedCues(T_ATTENTION_ENABLED);
		myCueSpace.addThoughtCueForName(T_ATTENTION_DISABLED, 1.0);
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

	/*
	public Boolean getGazeHoldStatus() {
		AttentionJob aj = myBlenderJob.theTestAttentionJob;
		if (aj != null) {
			return aj.getHoldingStatusFlag();
		} else {
			return null;
		}
	}
	 */
	/*
	public double getEgocentricXAbsRoM(){
		ensureEgoExtremes();
		Double range = myMaxEgoX - myMinEgoX;
		Double curAz = getCurrentEgocentricDirection().getAzimuth().getDegrees();
		curAz -= myMinEgoX;
		return curAz/range;
	}
	public double getEgocentricYAbsRoM(){
		ensureEgoExtremes();
		Double range = myMaxEgoY - myMinEgoY;
		Double curEl = getCurrentEgocentricDirection().getElevation().getDegrees();
		curEl -= myMinEgoY;
		return curEl/range;
	}
	private void ensureEgoExtremes(){
		if(myMinEgoX != null && myMaxEgoX != null &&
				myMinEgoY != null && myMaxEgoY != null){
			return;
		}
		Frame center = AnimationBuilder.makeGazeCenteringFrame(getAllGazeJoints());
		Frame minF = getJointPositionFrame(center, 0.0);
		Frame maxF = getJointPositionFrame(center, 1.0);

		GazeDirectionComputer gdc = mySightModel.getGazeDirectionComputer();
		if(gdc == null){
			theLogger.fine("Gaze Direction Computer is null, cannot continue.");
			return;
		}
		EgocentricDirection edMax = gdc.computeGazeCenterDirection(maxF);
		EgocentricDirection edMin = gdc.computeGazeCenterDirection(minF);
		if(edMax == null || edMin == null){
			theLogger.fine("Egocentric Direction is null, cannot continue.");
			return;
		}
		myMinEgoX = edMin.getAzimuth().getDegrees();
		myMaxEgoX = edMax.getAzimuth().getDegrees();
		myMinEgoY = edMin.getElevation().getDegrees();
		myMaxEgoY = edMax.getElevation().getDegrees();
	}
*/
		/*
	public Animation convertDenseAbsROMtoDenseRelROM(Animation absAnim) {
		Animation relAnim = new Animation(absAnim.getName(), absAnim.getFramePeriodSeconds());
		int frameCount = absAnim.getFrameCount();
		// First rel frame will always be all 0.0's
		Frame prevAbsFrame = absAnim.getFrameAt(0);
		for(int frameIDX=0; frameIDX < frameCount; frameIDX++) {
			Frame nextAbsFrame = absAnim.getFrameAt(frameIDX);
			Frame nextRelFrame = new Frame();
			List<JointPosition> nextAbsJPs = nextAbsFrame.getAllPositions();
			for (JointPosition nextAbsJP : nextAbsJPs) {
				Joint j = nextAbsJP.getJoint();
				JointPosition prevAbsJP = prevAbsFrame.getJointPositionForJoint(j);
				double nextAbsPos = nextAbsJP.getCoordinateFloat(JointPosition.CoordinateType.FLOAT_ABS_RANGE_OF_MOTION);
				double prevAbsPos = prevAbsJP.getCoordinateFloat(JointPosition.CoordinateType.FLOAT_ABS_RANGE_OF_MOTION);
				double moveDelta = nextAbsPos - prevAbsPos;
				JointPosition nextRelJP = new JointPosition(j);
				nextRelJP.setCoordinateFloat(JointPosition.CoordinateType.FLOAT_REL_RANGE_OF_MOTION, moveDelta);
				nextRelFrame.addPosition(nextRelJP);
			}
			relAnim.appendFrame(nextRelFrame);
			prevAbsFrame = nextAbsFrame;
		}
		return relAnim;
	}
	*/
	/*
	public void initGazeReturnAnim(){
		//using "INIT" as a temporary placeholder.  Works fine now, but we will
		//need to make many animations on the fly.  This is a temporary method
		initGazeReturnAnim("INIT");
	}
	public void initGazeReturnAnim(String init){
		Animation a = new Animation(init, 1.0);
		Frame snapshot = getCurrentGazeSnapshot();
		a.appendFrame(snapshot);
		myAnimationLibrary.registerAnimation(a);
	}
	public void completeGazeReturnAnim(String name, int length){
		//same as initGazeReturnAnim
		completeGazeReturnAnim("INIT", name, length);
	}
	public void completeGazeReturnAnim(String init, String name, int length){
		//same as initGazeReturnAnim
		Animation base = myAnimationLibrary.getAnimationForName(init);
		Frame start = getCurrentGazeSnapshot();
		Frame end = base.getFrameAt(0);
		Animation a = AnimationBuilder.makeLinearAnimation(name, start, end, length);
		myAnimationLibrary.registerAnimation(a);
	}
	public void makeCenteringAnimation(){
		Frame start = getCurrentGazeSnapshot();
		Frame end = AnimationBuilder.makeGazeCenteringFrame(getAllGazeJoints());
		Animation complete = AnimationBuilder.makeLinearAnimation("Centering", start, end, 10);
		myAnimationLibrary.registerAnimation(complete);
	}
    public void makeNeckHoldAnimation(int frameCount){
        List<JointPosition> pos = new ArrayList();
        Frame<JointPosition> f = myBlenderJob.estimatePositionNow(true);
        for(GazeJoint j : getAllGazeJoints()){
            pos.add(f.getJointPositionForJoint(j.getJoint()));
        }
        Animation a = AnimationBuilder.makeConstantAnimation("HOLD_NECK", frameCount, pos, 0.05);
        myAnimationLibrary.registerAnimation(a);
    }
	 */
	/*
	private Frame getJointPositionFrame(Frame center, double val) {
		Frame f = new Frame();
		List<JointPosition> jps = center.getAllPositions();
		for (JointPosition jp : jps) {
			JointPosition newJp = jp.convertToCooordinateType(JointStateCoordinateType.FLOAT_ABS_RANGE_OF_MOTION);
			newJp.setCoordinateFloat(JointStateCoordinateType.FLOAT_ABS_RANGE_OF_MOTION, val);
			f.addPosition(newJp);
		}
		return f;
	}
	*/

}
