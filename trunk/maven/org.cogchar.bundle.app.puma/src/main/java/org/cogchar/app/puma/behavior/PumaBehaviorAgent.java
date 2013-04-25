/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.app.puma.behavior;

import java.lang.ClassLoader;
import java.util.List;
import java.util.List;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.app.puma.config.PumaConfigManager;
import org.cogchar.app.puma.registry.PumaRegistryClient;
import org.cogchar.app.puma.registry.ResourceFileCategory;
import org.cogchar.bind.rk.robot.client.RobotAnimClient;
import org.cogchar.bind.rk.robot.svc.RobotServiceContext;
import org.cogchar.bind.rk.speech.client.SpeechOutputClient;
import org.cogchar.blob.emit.BehaviorConfigEmitter;
import org.cogchar.impl.perform.FancyTextPerfChan;
import org.cogchar.impl.scene.SceneBook;
import org.cogchar.impl.scene.Theater;
import org.cogchar.impl.scene.TheaterTest;
import org.cogchar.impl.trigger.FancyTriggerFacade;
import org.cogchar.platform.trigger.CogcharEventActionBinder;
import org.cogchar.platform.trigger.CogcharScreenBox;
import org.cogchar.render.app.trigger.SceneActions;
import org.osgi.framework.BundleContext;
import org.cogchar.platform.trigger.BoxSpace;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class PumaBehaviorAgent extends CogcharScreenBox {
	
	private		Ident						myAgentID;
	private		BehaviorConfigEmitter		myBehaviorCE;
	public		Theater						myTheater;

	private		PumaRobotMotionMapper		myRobotMotionMapper;	
	private		PumaSpeechOutputMapper		mySpeechOutputMapper;
		
	public PumaBehaviorAgent(Ident agentID, BehaviorConfigEmitter bce)  {
		myAgentID = agentID;
		myBehaviorCE = bce;
		myTheater = new Theater(myAgentID);	

	}
	public void initMappers(PumaRegistryClient prc, RobotServiceContext rsc) { 
		List<ClassLoader> clsForRKConf = prc.getResFileCLsForCat(ResourceFileCategory.RESFILE_RK_CONF);
		myRobotMotionMapper = new PumaRobotMotionMapper (myAgentID, myBehaviorCE, clsForRKConf, rsc);
		mySpeechOutputMapper = new PumaSpeechOutputMapper(myAgentID);		
	}
	public void setupAndStart(BundleContext bunCtx, PumaRegistryClient prc, String chanGraphQN,  String behavGraphQN)  { 
		try {
			PumaConfigManager  pcm = prc.getConfigMgr(null);
			setupAndStartBehaviorTheater(pcm, chanGraphQN,  behavGraphQN);
			// We connect animation output channels for triggering (regardless of whether we are doing virtual-world animation or not).
			connectAnimOutChans();
			connectSpeechOutputSvcs(bunCtx);			
			BoxSpace bs = prc.getTargetBoxSpace(null);
			bs.addBox(getCharIdent(), this);
		} catch (Throwable t) {
			getLogger().error("Cannot setup+start behavior theater", t);
		}
	}
	
	public void connectAnimOutChans() {
		FancyTextPerfChan bestAnimOutChan = myRobotMotionMapper.getBestAnimOutChan();
		myTheater.registerChannel(bestAnimOutChan);
	}
	public void connectSpeechOutputSvcs(BundleContext bundleCtx) {
		try {
			mySpeechOutputMapper.connectSpeechOutputSvcs(bundleCtx, myTheater);
		} catch (Throwable t) {
			getLogger().error("Cannot connect speech output", t);
		}
	}
	public void setupAndStartBehaviorTheater(PumaConfigManager pcm, String chanGraphQN, String behavGraphQN) throws Throwable {
		boolean clearCachesFirst = true;
		// Old way, may still be useful durnig behavior development by advanced users.
		// loadBehaviorConfigFromTestFile(clearCachesFirst);
		RepoClient rc = pcm.getMainConfigRepoClient();
		Ident	chanGraphID = rc.makeIdentForQName(chanGraphQN);
		
		Ident	behavGraphID = rc.makeIdentForQName(behavGraphQN);
		loadBehaviorConfigFromRepo(rc, chanGraphID, behavGraphID, clearCachesFirst);
		
		startTheater();
	}	
	public void loadBehaviorConfigFromTestFile(boolean clearCachesFirst) throws Throwable {
		String pathTail = "bhv_nugget_02.ttl";
		String behavPath = myBehaviorCE.getBehaviorPermPath(pathTail);
		// if (useTempFiles) {	//behavPath = behavCE.getBehaviorTempFilePath(pathTail);
		ClassLoader optCLforJenaFM = org.cogchar.bundle.render.resources.ResourceBundleActivator.class.getClassLoader();
		TheaterTest.loadSceneBookFromFile(myTheater, behavPath, optCLforJenaFM, clearCachesFirst);
	}
	public void loadBehaviorConfigFromRepo(RepoClient repoClient, Ident chanGraphID, Ident behavGraphID, 
					boolean clearCachesFirst) throws Throwable {
		TheaterTest.loadSceneBookFromRepo(myTheater, repoClient, chanGraphID, behavGraphID, clearCachesFirst);
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
	
	public Ident getCharIdent() { 
		return myAgentID;
	}
	public void stopEverything() {
		Ident charID = getCharIdent();
		getLogger().info("stopEverything for {} - Stopping Theater.", charID);
		stopTheater();
		getLogger().info("stopEverything for {} - Stopping Anim Jobs.", charID);
		myRobotMotionMapper.stopAndReset();
		getLogger().info("stopEverything for {} - Stopping Speech-Output Jobs.", charID);
		mySpeechOutputMapper.stopAllSpeechOutput();
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
		myRobotMotionMapper.playBuiltinAnimNow(RobotAnimClient.BuiltinAnimKind.BAK_GOTO_DEFAULTS);
		getLogger().info("stopResetAndRecenter - Restarting behavior theater.");
		startTheater();
		getLogger().info("stopResetAndRecenter - Complete.");
	}
	public void playBuiltinAnimNow(RobotAnimClient.BuiltinAnimKind baKind) {
		myRobotMotionMapper.playBuiltinAnimNow(baKind);
	}
	public void sayTextNow(String txt) {
		mySpeechOutputMapper._directlyStartSpeakingText(txt);
	}
	public void usePermAnims() {
		getLogger().warn("usePermAnims() not implemented yet");
	}

	public void useTempAnims() {
		getLogger().warn("useTempAnims() not implemented yet");
	}
}
