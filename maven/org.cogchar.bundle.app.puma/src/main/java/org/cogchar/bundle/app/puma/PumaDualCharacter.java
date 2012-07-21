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
package org.cogchar.bundle.app.puma;

import java.util.Set;

import org.osgi.framework.BundleContext;

import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.core.item.Ident;
import org.appdapter.core.log.BasicDebugger;


import org.cogchar.blob.emit.BonyConfigEmitter;
import org.cogchar.blob.emit.BehaviorConfigEmitter;
import org.cogchar.blob.emit.HumanoidConfigEmitter;

import org.cogchar.api.skeleton.config.BoneRobotConfig;

import org.cogchar.bind.rk.speech.client.SpeechOutputClient;

import org.cogchar.render.app.bony.BonyRenderContext;
import org.cogchar.render.app.core.CogcharRenderContext;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.app.humanoid.SceneActions;

import org.cogchar.platform.trigger.DummyBox;
import org.cogchar.platform.trigger.DummyBinder;
import org.cogchar.platform.trigger.DummyTrigger;

import org.cogchar.app.buddy.busker.TriggerItems;

import org.cogchar.impl.scene.Theater;
import org.cogchar.impl.scene.SceneBook;

import org.cogchar.impl.perform.ChannelNames;
import org.cogchar.impl.perform.FancyTextChan;

import org.cogchar.impl.trigger.FancyTriggerFacade;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaDualCharacter extends BasicDebugger implements DummyBox {

	private SpeechOutputClient mySOC;
	private Ident myCharIdent;
	private String myNickName;
	private PumaHumanoidMapper myHumoidMapper;
	private PumaWebMapper myWebMapper;
	private PumaRenderMapper myRenderMapper;
	private ClassLoader myInitialBonyRdfCL = org.cogchar.bundle.render.resources.ResourceBundleActivator.class.getClassLoader();
	public String myUpdateBonyRdfPath;
	public Theater myTheater;

	public PumaDualCharacter(HumanoidRenderContext hrc, BundleContext bundleCtx, Ident charIdent, String nickName) {
		myCharIdent = charIdent;
		myNickName = nickName;
		myHumoidMapper = new PumaHumanoidMapper(hrc, bundleCtx, charIdent);
		myTheater = new Theater();
		myRenderMapper = new PumaRenderMapper();
		myWebMapper = new PumaWebMapper();
	}

	public void connectBonyCharToRobokindSvcs(BundleContext bundleCtx) throws Throwable {
		BonyRenderContext bonyRendCtx = myHumoidMapper.getHumanoidRenderContext();
		String bonyConfigPathPerm = HumanoidConfigEmitter.getBonyConfigPath(myCharIdent);
		myUpdateBonyRdfPath = bonyConfigPathPerm; // Currently update and perm path are set the same for TriggerItems.UpdateBonyConfig
		BoneRobotConfig boneRobotConf = readBoneRobotConfig(bonyConfigPathPerm, myInitialBonyRdfCL);
		bundleCtx.registerService(BoneRobotConfig.class.getName(), boneRobotConf, null);
		myHumoidMapper.initModelRobotUsingBoneRobotConfig(boneRobotConf);

		CogcharRenderContext cogRendCtx = bonyRendCtx;

		ClassLoader optCL = myInitialBonyRdfCL;
		
		myRenderMapper.initCameraMgrHumanoidRenderContext(myHumoidMapper.getHumanoidRenderContext()); // Needed so CameraMgr/CoreFeatureAdapter have access to HumanoidRenderContext.getHumanoidFigure 
		myRenderMapper.initLightsAndCamera(cogRendCtx, optCL);
		myRenderMapper.initCinematics(cogRendCtx, optCL);
		

		// myPHM.initModelRobotUsingAvroJointConfig();
		myHumoidMapper.connectToVirtualChar();
		// myPHM.applyInitialBoneRotations();
		connectAnimOutChans();

		myWebMapper.connectCogCharResources(myInitialBonyRdfCL, myHumoidMapper.getHumanoidRenderContext());
	}

	private void connectAnimOutChans() {
		FancyTextChan bestAnimOutChan = myHumoidMapper.getBestAnimOutChan();
		myTheater.registerChannel(bestAnimOutChan);
	}

	public void connectSpeechOutputSvcs(BundleContext bundleCtx) {
		Ident speechChanIdent = ChannelNames.getOutChanIdent_SpeechMain();
		mySOC = new SpeechOutputClient(bundleCtx, speechChanIdent);
		myTheater.registerChannel(mySOC);
	}

	public void loadBehaviorConfig(boolean useTempFiles) throws Throwable {
		// Currently we can only process
		String pathTail = "bhv_nugget_02.ttl";

		BonyConfigEmitter bonyCE = myHumoidMapper.getHumanoidRenderContext().getBonyConfigEmitter();
		// String bonyConfigPathTail = bonyCE.getBonyConfigPathTailForChar(myCharURI);
		BehaviorConfigEmitter behavCE = bonyCE.getBehaviorConfigEmitter();

		String behavPath = behavCE.getBehaviorPermPath(pathTail);
		if (useTempFiles) {
			// "TempFiles" currently ignored
			//behavPath = behavCE.getBehaviorTempFilePath(pathTail);
		}
		// true = Clear caches first
		boolean clearCachesFirst = true;
		ClassLoader optCLforJenaFM = null;
		myTheater.loadSceneBook(behavPath, optCLforJenaFM, clearCachesFirst);
	}

	public void startTheater() {
		SceneBook sb = myTheater.getSceneBook();
		DummyBinder trigBinder = SceneActions.getBinder();
		myWebMapper.connectMoreWebStuff();
		FancyTriggerFacade.registerAllTriggers(trigBinder, myTheater, sb);
		myTheater.startThread();
	}

	public void stopTheater() {
		// Should be long enough for the 100 Msec loop to cleanly exit.
		int killTimeWaitMsec = 200;
		myTheater.fullyStop(killTimeWaitMsec);
	}

	private void stopEverything() {
		logInfo("stopEverything - Stopping Theater.");
		stopTheater();
		logInfo("stopEverything - Stopping Anim Jobs.");
		myHumoidMapper.stopAndReset();
		logInfo("stopEverything - Stopping Speech-Output Jobs.");
		mySOC.cancelAllRunningSpeechTasks();

	}

	public void stopAndReset() {
		stopEverything();
		// TODO:  Send character to default positions.
		logInfo("stopAndReset - Restarting behavior theater.");
		startTheater();
		logInfo("stopAndReset - Complete.");
	}

	public void stopResetAndRecenter() {
		stopEverything();
		logWarning("stopResetAndRecenter - Recenter is not implemented yet!");
		logInfo("stopResetAndRecenter - Restarting behavior theater.");
		startTheater();
		logInfo("stopResetAndRecenter - Complete.");
	}

	public void registerDefaultSceneTriggers() {
		for (int i = 0; i < SceneActions.getSceneTrigKeyCount(); i++) {
			TriggerItems.SceneMsg smti = new TriggerItems.SceneMsg();
			smti.sceneInfo = "yowza " + i;
			registerTheaterBinding(i, smti);
		}
	}

	private void registerTheaterBinding(int sceneTrigIdx, DummyTrigger trig) {
		SceneActions.setTriggerBinding(sceneTrigIdx, myTheater, trig);
	}

//	private InputStream openAssetStream(String assetName) { 
//		return myBRC.openAssetStream(assetName);
//	}	
	public String getNickName() {
		return myNickName;
	}

	public Ident getCharIdent() {
		return myCharIdent;
	}

	public PumaHumanoidMapper getHumanoidMapper() {
		return myHumoidMapper;
	}

	public void playDangerYogaTestAnim() {
		myHumoidMapper.playDangerYogaTestAnim();
	}

	public void sayText(String txt) {
		// TODO:  Guard against concurrent activity through the channel/behavior systerm
		try {
			mySOC.speakText(txt);
		} catch (Throwable t) {
			logError("problem speaking", t);
		}
	}

	public void updateBonyConfig(String rdfConfigFlexPath, ClassLoader optRdfResourceCL) {
		try {
			BoneRobotConfig.Builder.clearCache();
			BoneRobotConfig brc = readBoneRobotConfig(rdfConfigFlexPath, optRdfResourceCL);
			myHumoidMapper.updateModelRobotUsingBoneRobotConfig(brc);
		} catch (Throwable t) {
			logError("problem updating bony config from flex-path[" + rdfConfigFlexPath + "]", t);
		}
	}

	public BoneRobotConfig readBoneRobotConfig(String rdfConfigFlexPath, ClassLoader optResourceClassLoader) {
		return AssemblerUtils.readOneConfigObjFromPath(BoneRobotConfig.class, rdfConfigFlexPath, optResourceClassLoader);

	}

	@Override
	public String toString() {
		return "PumaDualChar[uri=" + myCharIdent + ", nickName=" + myNickName + "]";
	}

	public void usePermAnims() {
		logWarning("usePermAnims() not implemented yet");
	}

	public void useTempAnims() {
		logWarning("useTempAnims() not implemented yet");
	}
}
