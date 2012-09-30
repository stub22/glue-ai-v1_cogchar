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

import org.osgi.framework.BundleContext;

import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;


import org.cogchar.blob.emit.BehaviorConfigEmitter;

import org.cogchar.api.humanoid.HumanoidConfig;
import org.cogchar.api.skeleton.config.BoneCN;
import org.cogchar.api.skeleton.config.BoneRobotConfig;

import org.cogchar.bind.rk.speech.client.SpeechOutputClient;

import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.app.trigger.SceneActions;

import org.cogchar.platform.trigger.DummyBox;
import org.cogchar.platform.trigger.DummyBinder;
import org.cogchar.platform.trigger.DummyTrigger;

import org.cogchar.app.buddy.busker.TriggerItems;

import org.cogchar.impl.scene.Theater;
import org.cogchar.impl.scene.SceneBook;

import org.cogchar.impl.perform.ChannelNames;
import org.cogchar.impl.perform.FancyTextChan;

import org.cogchar.impl.trigger.FancyTriggerFacade;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaDualCharacter extends DummyBox {

	private		SpeechOutputClient		mySOC;
	private		Ident					myCharIdent;
	private		String					myNickName;
	private		PumaHumanoidMapper		myHumoidMapper;
	public		String					myUpdateBonyRdfPath;
	private		BehaviorConfigEmitter	myBehaviorCE;
	public		Theater					myTheater;
	private		ServiceRegistration		myBoneRobotConfigServiceRegistration;

	public PumaDualCharacter(HumanoidRenderContext hrc, BundleContext bundleCtx, PumaAppContext pac, Ident charIdent, String nickName) {
		myCharIdent = charIdent;
		myNickName = nickName;
		myHumoidMapper = new PumaHumanoidMapper(hrc, bundleCtx, charIdent);
		myTheater = new Theater();	
	}
	public void absorbContext(PumaContextMediator mediator) { 
		BehaviorConfigEmitter behavCE = new BehaviorConfigEmitter();
		String sysContextURI = mediator.getSysContextRootURI();
		if (sysContextURI != null) {
			behavCE.setSystemContextURI(sysContextURI);
		}
		String filesysRootPath = mediator.getOptionalFilesysRoot();
		if (filesysRootPath != null) {
			behavCE.setLocalFileRootDir(filesysRootPath);
		}
		setBehaviorConfigEmitter(behavCE);
	}
	private void setBehaviorConfigEmitter(BehaviorConfigEmitter bce) {
		myBehaviorCE = bce;
	}

	public boolean connectBonyCharToRobokindSvcs(BundleContext bundleCtx, Ident qGraph, HumanoidConfig hc, RepoClient qi, BoneCN bqn) throws Throwable {
		// bonyConfig path is going away as we move to query-based config
		// Looks like Turtle BoneRobotConfig is going away for good, in which case this block can be deleted
		/*
		String bonyConfigPathPerm = HumanoidConfigEmitter.getBonyConfigPath(myCharIdent);
		myUpdateBonyRdfPath = bonyConfigPathPerm; // Currently update and perm path are set the same for TriggerItems.UpdateBonyConfig
		
		BoneRobotConfig boneRobotConf;
		if (bonyConfigPathPerm.equals(SHEET_RESOURCE_MARKER)) {
			boneRobotConf = new BoneRobotConfig(myCharIdent); 
		} else {
			boneRobotConf = readBoneRobotConfig(bonyConfigPathPerm, myInitialBonyRdfCL);
		}
		*/ 
		BoneRobotConfig boneRobotConf = new BoneRobotConfig(qi, myCharIdent, qGraph, bqn); 
		myBoneRobotConfigServiceRegistration = bundleCtx.registerService(BoneRobotConfig.class.getName(), boneRobotConf, null);
		//logInfo("Initializing new BoneRobotConfig: " + boneRobotConf.getFieldSummary()); // TEST ONLY
		boolean boneRobotOK = myHumoidMapper.initModelRobotUsingBoneRobotConfig(qi, boneRobotConf, qGraph, hc, myBehaviorCE);
		if (boneRobotOK) {
			myHumoidMapper.connectToVirtualChar();
			// myPHM.applyInitialBoneRotations();
			connectAnimOutChans();
		} else {
			getLogger().warn("connectBonyCharToRobokindSvcs() aborting due to failed boneRobot init, for charIdent: " + myCharIdent);
		}
		return boneRobotOK;
	}
	
	public void disconnectBonyCharFromRobokindSvcs() {
		myBoneRobotConfigServiceRegistration.unregister();
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

		// RenderConfigEmitter renderCE = myHumoidMapper.getHumanoidRenderContext().getConfigEmitter();
		// String bonyConfigPathTail = bonyCE.getBonyConfigPathTailForChar(myCharURI);
		// BehaviorConfigEmitter behavCE = renderCE.getBehaviorConfigEmitter();

		String behavPath = myBehaviorCE.getBehaviorPermPath(pathTail);
		if (useTempFiles) {
			// "TempFiles" currently ignored
			//behavPath = behavCE.getBehaviorTempFilePath(pathTail);
		}
		// true = Clear caches first
		boolean clearCachesFirst = true;
		// optCLforJenaFM was originally set to null,
		// apparently depending on CL to have already been added by something else, in this case cinematic / lights / camera config
		ClassLoader optCLforJenaFM = org.cogchar.bundle.render.resources.ResourceBundleActivator.class.getClassLoader();
		myTheater.loadSceneBook(behavPath, optCLforJenaFM, clearCachesFirst);
	}

	public void startTheater() {
		SceneBook sb = myTheater.getSceneBook();
		DummyBinder trigBinder = SceneActions.getBinder();
		//myWebMapper.connectLiftSceneInterface(myBundleCtx); // Now done in PumaAppContext.initCinema
		FancyTriggerFacade.registerTriggersForAllScenes(trigBinder, myTheater, sb);
		myTheater.startThread();
	}

	public void stopTheater() {
		// Should be long enough for the 100 Msec loop to cleanly exit.
		// Was 200, but very occasionally this wasn't quite long enough, and myWorkThread was becoming null after the
		// check in Theater.killThread, causing a NPE
		int killTimeWaitMsec = 250; 
		//myWebMapper.disconnectLiftSceneInterface(myBundleCtx); // Now done in PumaAppContext.reloadAll
		myTheater.fullyStop(killTimeWaitMsec);
	}

	public void stopEverything() {
		logInfo("stopEverything - Stopping Theater.");
		stopTheater();
		logInfo("stopEverything - Stopping Anim Jobs.");
		myHumoidMapper.stopAndReset();
		logInfo("stopEverything - Stopping Speech-Output Jobs.");
		if (mySOC != null) {
			mySOC.cancelAllRunningSpeechTasks();
		}

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

	// This method is called once (for each character) when bony config update is requested
	public void updateBonyConfig(RepoClient qi, Ident graphIdent, BoneCN bqn) throws Throwable {
		myHumoidMapper.updateModelRobotUsingBoneRobotConfig(new BoneRobotConfig(qi, myCharIdent, graphIdent, bqn));
	}

	/* On the way out as we move away from Turtle config
	public BoneRobotConfig readBoneRobotConfig(String rdfConfigFlexPath, ClassLoader optResourceClassLoader) {
		return AssemblerUtils.readOneConfigObjFromPath(BoneRobotConfig.class, rdfConfigFlexPath, optResourceClassLoader);

	}
	*/
	
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
