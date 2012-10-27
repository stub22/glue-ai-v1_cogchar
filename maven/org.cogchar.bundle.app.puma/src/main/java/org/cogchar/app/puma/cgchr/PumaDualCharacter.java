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

import java.io.File;
import org.osgi.framework.BundleContext;

import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;


import org.cogchar.blob.emit.BehaviorConfigEmitter;

import org.cogchar.api.humanoid.HumanoidConfig;
import org.cogchar.api.skeleton.config.BoneCN;
import org.cogchar.api.skeleton.config.BoneRobotConfig;
import org.cogchar.bind.rk.robot.client.RobotAnimClient.BuiltinAnimKind;
import org.cogchar.bind.rk.speech.client.SpeechOutputClient;

import org.cogchar.render.app.trigger.SceneActions;

import org.cogchar.platform.trigger.CogcharScreenBox;
import org.cogchar.platform.trigger.CogcharEventActionBinder;
import org.cogchar.platform.trigger.CogcharActionTrigger;

import org.cogchar.app.buddy.busker.TriggerItems;
import org.cogchar.app.puma.config.PumaConfigManager;

import org.cogchar.app.puma.config.PumaContextMediator;
import org.cogchar.bind.rk.robot.svc.RobotServiceContext;
import org.cogchar.bind.rk.robot.svc.RobotServiceFuncs;

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
		myTheater = new Theater(charIdent);	
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
	public void setupAndStartBehaviorTheater(PumaConfigManager pcm, PumaVirtualWorldMapper vWorldMapper) throws Throwable {
		boolean clearCachesFirst = true;
		// Old way, may still be useful durnig behavior development by advanced users.
		// loadBehaviorConfigFromTestFile(clearCachesFirst);
		RepoClient rc = pcm.getMainConfigRepoClient();
		Ident	chanGraphID = rc.makeIdentForQName("ccrt:chan_sheet_AZR50");
		
		Ident	behavGraphID = rc.makeIdentForQName("hrk:behav_file_44");
		loadBehaviorConfigFromRepo(rc, chanGraphID, behavGraphID, clearCachesFirst);
		
		if (vWorldMapper != null) {
			// ToDo:  Remove this stuff in favor of BoxSpace/CommandSpace setup.
			vWorldMapper.registerSpecialInputTriggers(this);
		}
		startTheater();
	}
	public void loadBehaviorConfigFromTestFile(boolean clearCachesFirst) throws Throwable {
		String pathTail = "bhv_nugget_02.ttl";
		String behavPath = myBehaviorCE.getBehaviorPermPath(pathTail);
		// if (useTempFiles) {	//behavPath = behavCE.getBehaviorTempFilePath(pathTail);
		ClassLoader optCLforJenaFM = org.cogchar.bundle.render.resources.ResourceBundleActivator.class.getClassLoader();
		myTheater.loadSceneBookFromFile(behavPath, optCLforJenaFM, clearCachesFirst);
	}
	public void loadBehaviorConfigFromRepo(RepoClient repoClient, Ident chanGraphID, Ident behavGraphID, 
					boolean clearCachesFirst) throws Throwable {
		myTheater.loadSceneBookFromRepo(repoClient, chanGraphID, behavGraphID, clearCachesFirst);
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
		getLogger().info("stopResetAndRecenter - Starting GOTO_DEFAULTS anim");
		myHumoidMapper.playBuiltinAnimNow(BuiltinAnimKind.BAK_GOTO_DEFAULTS);
		getLogger().info("stopResetAndRecenter - Restarting behavior theater.");
		startTheater();
		getLogger().info("stopResetAndRecenter - Complete.");
	}

	
	public String getNickName() {
		return myNickName;
	}

	public Ident getCharIdent() {
		return myCharIdent;
	}

	public PumaHumanoidMapper getHumanoidMapper() {
		return myHumoidMapper;
	}

	public void playBuiltinAnimNow(BuiltinAnimKind baKind) {
		myHumoidMapper.playBuiltinAnimNow(baKind);
	}
	

	public void sayText(String txt) {
		// TODO:  Prevent/blend concurrent activity through the channel/behavior systerm
		try {
			if (mySOC != null) {
				mySOC.speakText(txt);
			} else {
				getLogger().warn("Character {} ignoring request to sayText, because SpeechOutputClient is null", myCharIdent);
			}
		} catch (Throwable t) {
			getLogger().error("problem speaking", t);
		}
	}

	// This method is called once (for each character) when bony config update is requested
	public void updateBonyConfig(RepoClient qi, Ident graphIdent, BoneCN bqn) throws Throwable {
		myHumoidMapper.updateModelRobotUsingBoneRobotConfig(new BoneRobotConfig(qi, myCharIdent, graphIdent, bqn));
	}

	/* Old way to load, direct from Turtle config
	public BoneRobotConfig readBoneRobotConfig(String rdfConfigFlexPath, ClassLoader optResourceClassLoader) {
		return AssemblerUtils.readOneConfigObjFromPath(BoneRobotConfig.class, rdfConfigFlexPath, optResourceClassLoader);
	}
	*/
	
	@Override public String toString() {
		return "PumaDualChar[uri=" + myCharIdent + ", nickName=" + myNickName + "]";
	}

	public void usePermAnims() {
		getLogger().warn("usePermAnims() not implemented yet");
	}

	public void useTempAnims() {
		getLogger().warn("useTempAnims() not implemented yet");
	}
	

	public boolean setupCharacterBindingToRobokind(BundleContext bunCtx, RepoClient rc, Ident graphIdentForBony, 
					HumanoidConfig hc, ClassLoader clForRKJG) {
		Ident charIdent = getCharIdent();
		getLogger().debug("Setup for {} using graph {} and humanoidConf {}", new Object[]{charIdent, graphIdentForBony, hc});
		try {
			BoneCN bqn = new BoneCN();
			boolean connectedOK = connectBonyCharToRobokindSvcs(bunCtx, graphIdentForBony, rc, bqn);
			if (connectedOK) {
				if (clForRKJG != null) {
					setupRobokindJointGroup(hc.myJointConfigPath, clForRKJG);				
				} else {
					getLogger().warn("No RK classLoader, cannot setup JointGroup for {}", charIdent);
				}
				connectSpeechOutputSvcs(bunCtx);
				return true;
			} else {
				getLogger().warn("aborting RK binding for character: {}", charIdent);
				return false;
			}
		} catch (Throwable t) {
			getLogger().error("Exception during setupCharacterBindingToRobokind for character: {}", charIdent, t);
			return false;
		}
	}
	public boolean initVWorldHumanoidFigure(RepoClient qi, Ident qGraph, HumanoidConfig hc) throws Throwable { 
		boolean vwHumOK = myHumoidMapper.initVWorldHumanoid(qi, qGraph, hc);
		return vwHumOK;
	}
	private boolean connectBonyCharToRobokindSvcs(BundleContext bundleCtx, Ident qGraph, RepoClient qi, BoneCN bqn) throws Throwable {
		// We useta read from a TTL file with: 	boneRobotConf = readBoneRobotConfig(bonyConfigPathPerm, myInitialBonyRdfCL);
		BoneRobotConfig boneRobotConf = new BoneRobotConfig(qi, myCharIdent, qGraph, bqn); 	
		myBoneRobotConfigServiceRegistration = bundleCtx.registerService(BoneRobotConfig.class.getName(), boneRobotConf, null);
		//logInfo("Initializing new BoneRobotConfig: " + boneRobotConf.getFieldSummary()); // TEST ONLY
		boolean boneRobotOK = myHumoidMapper.initModelRobotUsingBoneRobotConfig(boneRobotConf, myBehaviorCE);
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
	private void setupRobokindJointGroup(String jgFullPath, ClassLoader clForRK) throws Throwable {
		//Ident chrIdent = pdc.getCharIdent();
		String tgtFilePath = getNickName() + "temporaryJointGroupResource.xml";
		File jgConfigFile = RobotServiceFuncs.copyJointGroupFile(tgtFilePath, jgFullPath, clForRK);

		if (jgConfigFile != null) {
			PumaHumanoidMapper phm = getHumanoidMapper();
			RobotServiceContext rsc = phm.getRobotServiceContext();
			rsc.startJointGroup(jgConfigFile);
		} else {
			getLogger().warn("jointGroup file not found: {}", jgFullPath);
		}
	}	
}
