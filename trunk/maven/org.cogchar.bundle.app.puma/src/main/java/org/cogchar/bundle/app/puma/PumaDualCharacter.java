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

import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.core.item.Ident;
import org.appdapter.core.log.BasicDebugger;


import org.cogchar.blob.emit.BonyConfigEmitter;
import org.cogchar.blob.emit.BehaviorConfigEmitter;
import org.cogchar.blob.emit.HumanoidConfigEmitter;
import org.cogchar.blob.emit.GlobalConfigEmitter;

import org.cogchar.api.skeleton.config.BoneRobotConfig;

import org.cogchar.bind.rk.speech.client.SpeechOutputClient;

import org.cogchar.render.app.bony.BonyRenderContext;
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
	private PumaAppContext myPumaAppContext; // Needed to get graph information from PAC for config updates
	private PumaHumanoidMapper myHumoidMapper;
	private PumaWebMapper myWebMapper;
	private ClassLoader myInitialBonyRdfCL = org.cogchar.bundle.render.resources.ResourceBundleActivator.class.getClassLoader();
	public String myUpdateBonyRdfPath;
	public Theater myTheater;
	private BundleContext myBundleCtx; // Set at connectBonyCharToRobokindSvcs so it can be passed around to start dependencies needed for managed services
	
	final static String SHEET_RESOURCE_MARKER = "//SHEET"; // As an RdfPath, indicates that config should be loaded from spreadsheet instead

	public PumaDualCharacter(HumanoidRenderContext hrc, BundleContext bundleCtx, PumaAppContext pac, Ident charIdent, String nickName) {
		myCharIdent = charIdent;
		myNickName = nickName;
		myPumaAppContext = pac;
		myHumoidMapper = new PumaHumanoidMapper(hrc, bundleCtx, charIdent);
		myTheater = new Theater();
		myWebMapper = new PumaWebMapper();
	}

	public void connectBonyCharToRobokindSvcs(BundleContext bundleCtx, Ident qGraph) throws Throwable {
		myBundleCtx = bundleCtx;
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
		BoneRobotConfig boneRobotConf = new BoneRobotConfig(myCharIdent, qGraph); 
		bundleCtx.registerService(BoneRobotConfig.class.getName(), boneRobotConf, null);
		//logInfo("Initializing new BoneRobotConfig: " + boneRobotConf.getFieldSummary()); // TEST ONLY
		myHumoidMapper.initModelRobotUsingBoneRobotConfig(boneRobotConf, qGraph);

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
		// optCLforJenaFM was originally set to null,
		// apparently depending on CL to have already been added by something else, in this case cinematic / lights / camera config
		// ClassLoaders won't be required at all soon when this config becomes query-based
		ClassLoader optCLforJenaFM = org.cogchar.bundle.render.resources.ResourceBundleActivator.class.getClassLoader();
		myTheater.loadSceneBook(behavPath, optCLforJenaFM, clearCachesFirst);
	}

	public void startTheater() {
		SceneBook sb = myTheater.getSceneBook();
		DummyBinder trigBinder = SceneActions.getBinder();
		myWebMapper.connectLiftSceneInterface(myBundleCtx);
		FancyTriggerFacade.registerAllTriggers(trigBinder, myTheater, sb);
		myTheater.startThread();
	}

	public void stopTheater() {
		// Should be long enough for the 100 Msec loop to cleanly exit.
		int killTimeWaitMsec = 200;
		myWebMapper.disconnectLiftSceneInterface(myBundleCtx);
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

	public void updateBonyConfig() {
		BoneRobotConfig.reloadResource(); // Oops, this now called once for each robot, inefficient!!!
		myPumaAppContext.updateGlobalConfig(); // Oops, this now called once for each robot, inefficient!!!
		Ident graphIdent;
		try {
			graphIdent = myPumaAppContext.getGlobalConfig().ergMap().get(myCharIdent).get(PumaModeConstants.BONY_CONFIG_ROLE);
			try {
				myHumoidMapper.updateModelRobotUsingBoneRobotConfig(new BoneRobotConfig(myCharIdent, graphIdent));
			} catch (Throwable t) {
				logError("problem updating bony config from queries for " + myCharIdent, t);
			}
		} catch (Exception e) {
			logWarning("Could not get a valid graph on which to query for config update of " + myCharIdent.getLocalName());
		}
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
