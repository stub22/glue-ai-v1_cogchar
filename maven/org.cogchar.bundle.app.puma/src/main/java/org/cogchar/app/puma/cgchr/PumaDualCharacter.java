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
import java.util.List;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.app.puma.registry.PumaRegistryClient;
import org.cogchar.app.puma.registry.ResourceFileCategory;
import org.cogchar.platform.trigger.BoxSpace;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaDualCharacter extends BasicDebugger { // CogcharScreenBox {

	private		Ident						myCharID;
	private		String						myNickName;
	private		PumaModelHumanoidMapper		myModelHumoidMapper;

	public		String					myUpdateBonyRdfPath;

	private		ServiceRegistration		myBoneRobotConfigServiceRegistration;
	
	private		PumaBehaviorAgent		myBehaviorAgent;

	public PumaDualCharacter(Ident charID, String nickName) {
		myCharID = charID;
		myNickName = nickName;
			
	}
	public void absorbContext(PumaRegistryClient prc, BundleContext bundleCtx, RepoClient rc, HumanoidConfig humCfg,  
				Ident graphIdentForBony) throws Throwable {
		PumaContextMediator mediator = prc.getCtxMediator(null);
		BehaviorConfigEmitter behavCE = new BehaviorConfigEmitter();
		String sysContextURI = mediator.getSysContextRootURI();
		if (sysContextURI != null) {
			behavCE.setSystemContextURI(sysContextURI);
		}
		String filesysRootPath = mediator.getOptionalFilesysRoot();
		if (filesysRootPath != null) {
			behavCE.setLocalFileRootDir(filesysRootPath);
		}
		myBehaviorAgent = new PumaBehaviorAgent(myCharID, behavCE, prc);
		
		PumaVirtualWorldMapper vWorldMapper = prc.getVWorldMapper(null);
		// It's OK if vWorldMapper == null.
		myModelHumoidMapper = new PumaModelHumanoidMapper(vWorldMapper, bundleCtx, myCharID);
		List<ClassLoader> rkConfCLs = prc.getResFileCLsForCat(ResourceFileCategory.RESFILE_RK_CONF);
		boolean vwHumOK = myModelHumoidMapper.initVWorldHumanoid(rc, graphIdentForBony, humCfg);
		setupBonyModelBindingToRobokind(bundleCtx, rc, graphIdentForBony, humCfg, rkConfCLs);		
		
		setupBehaviorAgent(bundleCtx, prc);
	}

	public void disconnectBonyCharFromRobokindSvcs() {
		myBoneRobotConfigServiceRegistration.unregister();
	}
	public String getNickName() {
		return myNickName;
	}
	public Ident getCharIdent() {
		return myCharID;
	}
	public PumaModelHumanoidMapper getModelHumanoidMapper() {
		return myModelHumoidMapper;
	}


	// This method is called once (for each character) when bony config update is requested
	public void updateBonyConfig(RepoClient qi, Ident graphID, BoneCN bqn) throws Throwable {
		myModelHumoidMapper.updateModelRobotUsingBoneRobotConfig(new BoneRobotConfig(qi, myCharID, graphID, bqn));
	}

	/* Old way to load, direct from Turtle config
	public BoneRobotConfig readBoneRobotConfig(String rdfConfigFlexPath, ClassLoader optResourceClassLoader) {
		return AssemblerUtils.readOneConfigObjFromPath(BoneRobotConfig.class, rdfConfigFlexPath, optResourceClassLoader);
	}
	*/
	
	@Override public String toString() {
		return "PumaDualChar[uri=" + myCharID + ", nickName=" + myNickName + "]";
	}

	public void setupBehaviorAgent(BundleContext bunCtx, PumaRegistryClient prc)  { 
		String chanGraphQN =  "ccrt:chan_sheet_AZR50",  behavGraphQN  =  "hrk:behav_file_44";
		try {
			PumaConfigManager  pcm = prc.getConfigMgr(null);
			myBehaviorAgent.setupAndStartBehaviorTheater(pcm, chanGraphQN,  behavGraphQN);
			// We connect animation output channels for triggering (regardless of whether we are doing virtual-world animation or not).
			myBehaviorAgent.connectAnimOutChans();
			myBehaviorAgent.connectSpeechOutputSvcs(bunCtx);			
			BoxSpace bs = prc.getTargetBoxSpace(null);
			bs.addBox(getCharIdent(), myBehaviorAgent);
		} catch (Throwable t) {
			getLogger().error("Cannot setup+start behavior theater", t);
		}
	}
	public void stopAllBehavior() { 
		myBehaviorAgent.stopEverything();
	}
	private boolean setupBonyModelBindingToRobokind(BundleContext bunCtx, RepoClient rc, Ident graphIdentForBony, 
					HumanoidConfig hc, List<ClassLoader> clsForRKConf) {
		Ident charIdent = getCharIdent();
		getLogger().debug("Setup for {} using graph {} and humanoidConf {}", new Object[]{charIdent, graphIdentForBony, hc});
		try {
			BoneCN bqn = new BoneCN();
			boolean connectedOK = myModelHumoidMapper.connectBonyCharToRobokindSvcs(bunCtx, hc, graphIdentForBony, rc, bqn, clsForRKConf);
			if (connectedOK) {
				myBehaviorAgent.connectRobotServiceContext(myModelHumoidMapper.getRobotServiceContext());
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
/*
	private boolean connectBonyCharToRobokindSvcs(BundleContext bundleCtx, Ident qGraph, RepoClient qi, BoneCN bqn, List<ClassLoader> clsForRKConf) throws Throwable {
		// We useta read from a TTL file with: 	boneRobotConf = readBoneRobotConfig(bonyConfigPathPerm, myInitialBonyRdfCL);
		BoneRobotConfig boneRobotConf = new BoneRobotConfig(qi, myCharID, qGraph, bqn); 	
		myBoneRobotConfigServiceRegistration = bundleCtx.registerService(BoneRobotConfig.class.getName(), boneRobotConf, null);
		//logInfo("Initializing new BoneRobotConfig: " + boneRobotConf.getFieldSummary()); // TEST ONLY
		boolean boneRobotOK = myModelHumoidMapper.initModelRobotUsingBoneRobotConfig(boneRobotConf);
		if (boneRobotOK) {
			// This does nothing if there is no vWorld, or no human figure for this char in the vWorld.
			myModelHumoidMapper.connectToVirtualChar();
			// This was an antiquated way of controlling initial char position, left here as reminder of the issue.
			// myPHM.applyInitialBoneRotations();
			myModelHumoidMapper.startVisemePump(clsForRKConf);
		} else {
			getLogger().warn("connectBonyCharToRobokindSvcs() aborting due to failed boneRobot init, for charIdent: {}", myCharID);
		}
		return boneRobotOK;
	}
*/
}
