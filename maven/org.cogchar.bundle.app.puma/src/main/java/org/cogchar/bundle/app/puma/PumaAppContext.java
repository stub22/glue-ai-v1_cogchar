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

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;

import org.cogchar.api.humanoid.HumanoidConfig;
import org.cogchar.bind.rk.robot.svc.ModelBlendingRobotServiceContext;
import org.cogchar.bind.rk.robot.svc.RobotServiceContext;
import org.cogchar.bind.rk.robot.svc.RobotServiceFuncs;
import org.cogchar.blob.emit.GlobalConfigEmitter;

import org.osgi.framework.BundleContext;
import org.cogchar.api.skeleton.config.BoneCN;
import org.cogchar.app.buddy.busker.TriggerItems;
import org.cogchar.app.puma.cgchr.PumaDualCharacter;
import org.cogchar.app.puma.cgchr.PumaHumanoidMapper;
import org.cogchar.platform.trigger.CogcharScreenBox;
import org.cogchar.platform.trigger.BoxSpace;

import org.cogchar.platform.trigger.CommandSpace;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaAppContext extends CogcharScreenBox {
	
	private PumaRegistryClient	myRegClient;
	
	private BundleContext myBundleContext;

	private ClassLoader myInitialBonyRdfCL;

	private List<PumaDualCharacter> myCharList = new ArrayList<PumaDualCharacter>();

	public PumaAppContext(BundleContext bc, PumaContextMediator mediator, Ident ctxID) {
		myRegClient = new PumaRegistryClientImpl(bc, mediator);
		
		myBundleContext = bc;
		
		BoxSpace bs = myRegClient.getTargetBoxSpace(null);
		bs.addBox(ctxID, this);
	}

	protected BundleContext getBundleContext() {
		return myBundleContext;
	}
	public boolean hasVWorldMapper() {
		return (myRegClient.getVWorldMapper(null) != null);
	}

	public PumaVirtualWorldMapper getOrMakeVWorldMapper() {
		PumaVirtualWorldMapper pvwm = myRegClient.getVWorldMapper(null);
		if (pvwm == null) {
			pvwm = new PumaVirtualWorldMapper(this);
			myRegClient.putVWorldMapper(pvwm, null);
		}
		return pvwm;
	}
	public boolean hasWebMapper() { 
		return (myRegClient.getWebMapper(null) != null);
	}	
	public PumaWebMapper getOrMakeWebMapper() {
		PumaWebMapper pwm = myRegClient.getWebMapper(null);
		if (pwm == null) {
			pwm = new PumaWebMapper(this);
			myRegClient.putWebMapper(pwm, null);
		}
		return pwm;
	}

	protected PumaConfigManager getConfigManager() {
		return myRegClient.getConfigMgr(null);
	}
	public void setCogCharResourcesClassLoader(ClassLoader loader) {
		myInitialBonyRdfCL = loader;
	}

	public ClassLoader getCogCharResourcesClassLoader() {
		return myInitialBonyRdfCL;
	}
	public void startOpenGLCanvas(boolean wrapInJFrameFlag) throws Exception {
		if (hasVWorldMapper()) {
			PumaVirtualWorldMapper pvwm = myRegClient.getVWorldMapper(null);
			pvwm.startOpenGLCanvas(wrapInJFrameFlag);
		} else {
			getLogger().warn("Ignoring startOpenGLCanvas command - no vWorldMapper present");
		}

	}

	protected void initCinema() {
		if (hasVWorldMapper()) {
			PumaVirtualWorldMapper pvwm = myRegClient.getVWorldMapper(null);
			pvwm.initCinema();
		} else {
			getLogger().warn("Ignoring initCinema command - no vWorldMapper present");
		}
	}
	// TODO:  This should take some optional args that will start a different repo client instead.

	public void startRepositoryConfigServices() {
		PumaConfigManager pcm = getConfigManager();
		// This would happen by default anyway, if there were not already a MainConfigRepoClient in place.
		pcm.applyVanillaRepoClientAsMainConfig(myBundleContext);
		// This method performs the configuration actions associated with the developmental "Global Mode" concept
		// If/when "Global Mode" is replaced with a different configuration "emitter", the method(s) here will
		// be updated to relect that		
		pcm.applyGlobalConfig(myBundleContext);
	}

	/**
	 * Third (and last) stage init of OpenGL, and all other systems. Done AFTER startOpenGLCanvas().
	 *
	 * @return
	 * @throws Throwable
	 */
	public List<PumaDualCharacter> connectDualRobotChars() throws Throwable {
		final PumaConfigManager pcm = getConfigManager();
		GlobalConfigEmitter gce = pcm.getGlobalConfig();
		RepoClient rc = pcm.getOrMakeMainConfigRepoClient(myBundleContext);
		//List<PumaDualCharacter> pdcList = new ArrayList<PumaDualCharacter>();
		List<Ident> charIdents = new ArrayList<Ident>(); // A blank list, so if the try fails below, the for loop won't throw an Exception

		List<Ident> identsFromConfig = gce.entityMap().get(PumaModeConstants.CHAR_ENTITY_TYPE);
		if (identsFromConfig != null) {
			charIdents = identsFromConfig;
		} else {
			String msg = "Could not retrieve list of characters from global configuration, aborting all char setup";
			getLogger().error(msg);
			throw new RuntimeException(msg);
		}
		if (gce == null) {
			getLogger().warn("GlobalConfigEmitter not available, cannot setup characters!");
		} else if (myInitialBonyRdfCL == null) {
			// We may not need this check eventually - currently only jointGroup.xml files and BallBuilder Turtle loader
			// need access to this ClassLoader
			getLogger().warn("Cog Char resources ClassLoader not available, cannot setup characters!");
		} else {
			for (Ident charIdent : charIdents) {
				getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^^ Connecting dualRobotChar for charIdent: {}", charIdent);
				Ident graphIdentForBony;
				Ident graphIdentForHumanoid;
				try {
					graphIdentForBony = pcm.resolveGraphForCharAndRole(charIdent, PumaModeConstants.BONY_CONFIG_ROLE);
					graphIdentForHumanoid = pcm.resolveGraphForCharAndRole(charIdent, PumaModeConstants.HUMANOID_CONFIG_ROLE);
				} catch (Exception e) {
					getLogger().warn("Could not get valid graphs on which to query for config of {}", charIdent.getLocalName());
					break;
				}
				HumanoidConfig humConfig = new HumanoidConfig(rc, charIdent, graphIdentForHumanoid);
				PumaDualCharacter pdc = connectDualRobotChar(humConfig, graphIdentForBony);
			}
		}
		return myCharList;
	}
	
	public PumaDualCharacter connectDualRobotChar(HumanoidConfig humCfg, Ident graphIdentForBony) throws Throwable {
		Ident bonyCharIdent = humCfg.myCharIdent;
		PumaVirtualWorldMapper vWorldMapper = myRegClient.getVWorldMapper(null);
		PumaContextMediator pcMediator = myRegClient.getCtxMediator(null);
		BoxSpace bs = myRegClient.getTargetBoxSpace(null);
		// note that vWorldMapper may be null.
		PumaDualCharacter pdc = new PumaDualCharacter(vWorldMapper, myBundleContext, humCfg.myCharIdent, humCfg.myNickname);
		myCharList.add(pdc);
		bs.addBox(humCfg.myCharIdent, pdc);
		pdc.absorbContext(pcMediator);
		setupCharacterBindingToRobokind(pdc, graphIdentForBony, humCfg);
		setupAndStartBehaviorTheater(pdc);		
		return pdc;
	}
	public void reloadCommandSpace() { 
		final PumaConfigManager pcm = getConfigManager();
		RepoClient repoCli  = pcm.getOrMakeMainConfigRepoClient(myBundleContext);		
		CommandSpace cmdSpc = myRegClient.getCommandSpace(null);
		BoxSpace boxSpc = myRegClient.getTargetBoxSpace(null);
		// TODO:  stuff to clear out the command space
		TriggerItems.populateCommandSpace(repoCli, cmdSpc, boxSpc);
	}
	public void reloadVirtualWorldConfig(boolean resetMainConfigFlag) {
		PumaConfigManager pcm = getConfigManager();

		PumaVirtualWorldMapper pvwm = myRegClient.getVWorldMapper(null); // getVirtualWorldMapper();
		if (pvwm != null) {
			if (resetMainConfigFlag) {
				BundleContext bc = getBundleContext();
				pcm.applyFreshDefaultMainRepoClientToGlobalConfig(bc);
			}
			pvwm.clearCinematicStuff();
			pvwm.initCinema();
		} else {
			getLogger().warn("Ignoring command to reloadVirtualWorldConfig, because no vWorldMapper is present!");
		}
	}

	public void reloadBoneRobotConfig(boolean resetMainConfigFlag) {
		final PumaConfigManager pcm = getConfigManager();

		if (resetMainConfigFlag) {
			// This forces a complete system config reset, which might be good if our
			// goal is to cause an underlying spreadsheet repo to reload.
			BundleContext bc = getBundleContext();
			pcm.applyFreshDefaultMainRepoClientToGlobalConfig(bc);
		}
		RepoClient rc = pcm.getOrMakeMainConfigRepoClient(myBundleContext);

		BoneCN bqn = new BoneCN();
		for (PumaDualCharacter pdc : myCharList) {
			Ident charID = pdc.getCharIdent();
			logInfo("Updating bony config for char [" + pdc + "]");
			try {
				Ident graphIdent = pcm.resolveGraphForCharAndRole(charID, PumaModeConstants.BONY_CONFIG_ROLE);
				try {
					pdc.updateBonyConfig(rc, graphIdent, bqn);
				} catch (Throwable t) {
					getLogger().error("problem updating bony config from queries for {}", charID, t);
				}
			} catch (Exception e) {
				getLogger().warn("Could not get a valid graph on which to query for config update of {}", charID.getLocalName());
			}
		}
	}

	public void reloadGlobalConfig(boolean resetMainConfigFlag) {
		final PumaConfigManager pcm = getConfigManager();

		if (resetMainConfigFlag) {
			// This forces a complete system config reset, which might be good if our
			// goal is to cause an underlying spreadsheet repo to reload.
			BundleContext bc = getBundleContext();
			pcm.applyFreshDefaultMainRepoClientToGlobalConfig(bc);
		}
		RepoClient rc = pcm.getOrMakeMainConfigRepoClient(myBundleContext);
		pcm.startGlobalConfigService(myBundleContext);
	}

	protected void stopAndReleaseAllHumanoids() {
		for (PumaDualCharacter pdc : myCharList) {
			pdc.stopEverything();
			pdc.disconnectBonyCharFromRobokindSvcs();
		}
		RobotServiceFuncs.clearJointGroups();
		ModelBlendingRobotServiceContext.clearRobots();
		PumaVirtualWorldMapper pvwm = getOrMakeVWorldMapper();
		pvwm.detachAllHumanoidFigures();
		myCharList.clear();
		// Oops - but they are STILL in the box-space!!!
	}

	protected void disconnectAllCharsAndMappers() throws Throwable {
		BundleContext bunCtx = getBundleContext();		

		if (hasVWorldMapper()) {
			PumaVirtualWorldMapper vWorldMapper = getOrMakeVWorldMapper();
			vWorldMapper.clearCinematicStuff();
			vWorldMapper.clearSpecialInputTriggers();
			// Consider:  also set the context/registry vWorldMapper to null, expecting
			// PumaBooter or somesuch to find it again.
		}
		if (hasWebMapper()) {
			PumaWebMapper webMapper = getOrMakeWebMapper();
			webMapper.disconnectLiftSceneInterface(bunCtx);
			// Similarly, consider setting context/registry webMapper to null.
		}
		stopAndReleaseAllHumanoids();
		// If we did set our vWorldMapper and webMapper to null, above, then we'd
		// Which means the user will need to 
	}

	public void reloadAll(boolean resetMainConfigFlag) {
		try {
			BundleContext bunCtx = getBundleContext();
			// Here we make the cute assumption that vWorldMapper or webMapper would be null
			// if we weren't using those features.  Only problem is that is not true yet,
			// because these accessor methods

			disconnectAllCharsAndMappers();
		
			// NOW we are ready to load any new config.
			if (resetMainConfigFlag) {
				PumaConfigManager pcm = getConfigManager();
				// TODO:  This need to be a more general config source, either set earlier or supplied expliicitly.
				pcm.applyFreshDefaultMainRepoClientToGlobalConfig(bunCtx);
			}
			
			// So NOW what we want to examine is the difference between the state right here, and the
			// state at this moment during a full "boot" sequence.
			connectDualRobotChars();
			
			if (hasVWorldMapper()) {
				PumaVirtualWorldMapper vWorldMapper = getOrMakeVWorldMapper();
				vWorldMapper.initCinema();
			}

		} catch (Throwable t) {
			getLogger().error("Error attempting to reload all PUMA App config: ", t);
			// May be good to handle an exception by setting state of a "RebootResult" or etc...
		}
	}

	public void setupAndStartBehaviorTheater(PumaDualCharacter pdc) throws Throwable {
		//pdc.registerDefaultSceneTriggers(); // Seems this doesn't actually do anything at this point
		pdc.loadBehaviorConfig(false);
		PumaVirtualWorldMapper vWorldMapper = myRegClient.getVWorldMapper(null);
		if (vWorldMapper != null) {
			// ToDo:  Remove this stuff in favor of BoxSpace/CommandSpace setup.
			vWorldMapper.registerSpecialInputTriggers(pdc);
		}

		pdc.startTheater();

	}

	public boolean setupCharacterBindingToRobokind(PumaDualCharacter pdc, Ident graphIdentForBony, HumanoidConfig hc) {
		Ident charIdent = pdc.getCharIdent();
		getLogger().debug("Setup for {} using graph {} and humanoidConf {}", new Object[]{charIdent, graphIdentForBony, hc});
		try {
			final PumaConfigManager pcm = getConfigManager();
			BundleContext bunCtx = getBundleContext();
			RepoClient rc = pcm.getOrMakeMainConfigRepoClient(bunCtx);
			BoneCN bqn = new BoneCN();
			boolean connectedOK = pdc.connectBonyCharToRobokindSvcs(bunCtx, graphIdentForBony, hc, rc, bqn);
			if (connectedOK) {
				setupRobokindJointGroup(pdc, hc.myJointConfigPath);
				pdc.connectSpeechOutputSvcs(bunCtx);
				return true;
			} else {
				getLogger().warn("setupCharacterBindingToRobokind() aborting RK binding for character: {}", charIdent);
				return false;
			}
		} catch (Throwable t) {
			getLogger().error("Exception during setupCharacterBindingToRobokind for character: {}", charIdent, t);
			return false;
		}
	}

	public void setupRobokindJointGroup(PumaDualCharacter pdc, String jgFullPath) throws Throwable {
		//Ident chrIdent = pdc.getCharIdent();
		String tgtFilePath = pdc.getNickName() + "temporaryJointGroupResource.xml";
		File jgConfigFile = RobotServiceFuncs.copyJointGroupFile(tgtFilePath, jgFullPath, myInitialBonyRdfCL);

		if (jgConfigFile != null) {
			PumaHumanoidMapper phm = pdc.getHumanoidMapper();
			RobotServiceContext rsc = phm.getRobotServiceContext();
			rsc.startJointGroup(jgConfigFile);
		} else {
			getLogger().warn("jointGroup file not found: {}", jgFullPath);
		}
	}




	// A half baked (3/4 baked?) idea. Since PumaAppContext is basically in charge of global config right now, this will be a general
	// way to ask that config be updated. Why the string argument? See UpdateInterface comments...
	private boolean myUpdateInProgressFlag = false;
	// Here I have removed the method variable passed in for the RepoClient. Why? Because right now PumaAppContext really
	// is the central clearing house for the RepoClient for config -- ideally we want it to be passed down from one master instance here to
	// all the objects that use it. Methods calling for config updates via this method shouldn't be responsible for 
	// knowing what RepoClient is appropriate -- they are calling into this method because we are trying to handle that here.
	// So for now let's use the this.getQueryHelper way to get that interface here. We can continue to refine this thinking as we go.
	// - Ryan 2012-09-17

	public boolean updateConfigByRequest(String request, final boolean resetMainConfigFlag) {
		final PumaConfigManager pcm = getConfigManager();

		// Eventually we may decide on a good home for these constants:	
		final String WORLD_CONFIG = "worldconfig";
		final String BONE_ROBOT_CONFIG = "bonerobotconfig";
		final String MANAGED_GCS = "managedglobalconfigservice";
		final String ALL_HUMANOID_CONFIG = "allhumanoidconfig";

		// Do the actual updates on a new thread. That way we don't block the render thread. Much less intrusive, plus this way things
		// we need to enqueue on main render thread will actually complete -  it must not be blocked during some of the update operations!
		// This brings up an interesting point: we are probably doing far too much on the main render thread!
		logInfo("Updating config by request: " + request);
		boolean success = true;
		if (myUpdateInProgressFlag) {
			getLogger().warn("Update currently underway, ignoring additional request");
			success = false;
		} else if (WORLD_CONFIG.equals(request.toLowerCase())) {
			myUpdateInProgressFlag = true;
			Thread updateThread = new Thread("World Update Thread") {

				public void run() {
					reloadVirtualWorldConfig(resetMainConfigFlag);
					myUpdateInProgressFlag = false;
				}
			};
			updateThread.start();
		} else if (BONE_ROBOT_CONFIG.equals(request.toLowerCase())) {
			myUpdateInProgressFlag = true;
			Thread updateThread = new Thread("Bone Robot Update Thread") {

				public void run() {
					reloadBoneRobotConfig(resetMainConfigFlag);
					myUpdateInProgressFlag = false;
				}
			};
			updateThread.start();
		} else if (MANAGED_GCS.equals(request.toLowerCase())) {
			myUpdateInProgressFlag = true;
			pcm.clearOSGiComps();
			Thread updateThread = new Thread("Managed Global Config Service Update Thread") {

				public void run() {
					reloadGlobalConfig(resetMainConfigFlag);
					myUpdateInProgressFlag = false;
				}
			};
			updateThread.start();
		} else if (ALL_HUMANOID_CONFIG.equals(request.toLowerCase())) {
			myUpdateInProgressFlag = true;
			Thread updateThread = new Thread("Update Thread") {

				public void run() {
					reloadAll(resetMainConfigFlag);
					myUpdateInProgressFlag = false;
				}
			};
			updateThread.start();
		} else {
			getLogger().warn("PumaAppContext did not recognize the config update to be performed: {}", request);
			success = false;
		}
		return success;
	}
}
