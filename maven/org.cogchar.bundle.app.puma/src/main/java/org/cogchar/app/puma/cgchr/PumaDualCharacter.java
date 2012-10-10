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
package org.cogchar.app.puma.cgchr;

import org.osgi.framework.BundleContext;

import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;


import org.cogchar.blob.emit.BehaviorConfigEmitter;

import org.cogchar.api.humanoid.HumanoidConfig;
import org.cogchar.api.skeleton.config.BoneCN;
import org.cogchar.api.skeleton.config.BoneRobotConfig;

import org.cogchar.bind.rk.speech.client.SpeechOutputClient;

import org.cogchar.render.app.trigger.SceneActions;

import org.cogchar.platform.trigger.CogcharScreenBox;
import org.cogchar.platform.trigger.CogcharEventActionBinder;
import org.cogchar.platform.trigger.CogcharActionTrigger;

import org.cogchar.app.buddy.busker.TriggerItems;

import org.cogchar.bundle.app.puma.PumaContextMediator;
import org.cogchar.bundle.app.puma.PumaVirtualWorldMapper;

import org.cogchar.impl.scene.Theater;
import org.cogchar.impl.scene.SceneBook;

import org.cogchar.impl.perform.ChannelNames;
import org.cogchar.impl.perform.FancyTextChan;

import org.cogchar.impl.trigger.FancyTriggerFacade;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaDualCharacter extends CogcharScreenBox {

	private		SpeechOutputClient		mySOC;
	private		Ident					myCharIdent;
	private		String					myNickName;
	private		PumaHumanoidMapper		myHumoidMapper;
	public		String					myUpdateBonyRdfPath;
	private		BehaviorConfigEmitter	myBehaviorCE;
	public		Theater					myTheater;
	private		ServiceRegistration		myBoneRobotConfigServiceRegistration;

	public PumaDualCharacter(PumaVirtualWorldMapper vWorldMapper, BundleContext bundleCtx, Ident charIdent, String nickName) {
		myCharIdent = charIdent;
		myNickName = nickName;
		myHumoidMapper = new PumaHumanoidMapper(vWorldMapper, bundleCtx, charIdent);
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
			// This does nothing if there is no vWorld, or no human figure for this char in the vWorld.
			myHumoidMapper.connectToVirtualChar();
			// This was an antiquated way of controlling initial char position, left here as reminder of the issue.
			// myPHM.applyInitialBoneRotations();
			// We connect animation output channels for triggering (regardless of whether we are doing virtual-world animation or not).
			connectAnimOutChans();
		} else {
			getLogger().warn("connectBonyCharToRobokindSvcs() aborting due to failed boneRobot init, for charIdent: {}", myCharIdent);
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
		CogcharEventActionBinder trigBinder = SceneActions.getBinder();
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
		Ident charID = getCharIdent();
		getLogger().info("stopEverything for {} - Stopping Theater.", charID);
		stopTheater();
		getLogger().info("stopEverything for {} - Stopping Anim Jobs.", charID);
		myHumoidMapper.stopAndReset();
		getLogger().info("stopEverything for {} - Stopping Speech-Output Jobs.", charID);
		if (mySOC != null) {
			mySOC.cancelAllRunningSpeechTasks();
		}

	}

	public void stopAndReset() {
		Ident charID = getCharIdent();
		stopEverything();
		// TODO:  Send character to default positions.
		getLogger().info("stopAndReset for {} - Restarting behavior theater.", charID);
		startTheater();
		getLogger().info("stopAndReset - Complete.");
	}

	public void stopResetAndRecenter() {
		Ident charID = getCharIdent();
		stopEverything();
		getLogger().warn("stopResetAndRecenter for {} - Recenter is not implemented yet!", charID);
		getLogger().info("stopResetAndRecenter - Restarting behavior theater.");
		startTheater();
		getLogger().info("stopResetAndRecenter - Complete.");
	}

	public void registerDefaultSceneTriggers() {
		for (int i = 0; i < SceneActions.getSceneTrigKeyCount(); i++) {
			TriggerItems.SceneMsg smti = new TriggerItems.SceneMsg();
			smti.sceneInfo = "yowza " + i;
			registerTheaterBinding(i, smti);
		}
	}

	private void registerTheaterBinding(int sceneTrigIdx, CogcharActionTrigger trig) {
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
		// TODO:  Prevent/blend concurrent activity through the channel/behavior systerm
		try {
			mySOC.speakText(txt);
		} catch (Throwable t) {
			getLogger().error("problem speaking", t);
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
		getLogger().warn("usePermAnims() not implemented yet");
	}

	public void useTempAnims() {
		getLogger().warn("useTempAnims() not implemented yet");
	}
}
