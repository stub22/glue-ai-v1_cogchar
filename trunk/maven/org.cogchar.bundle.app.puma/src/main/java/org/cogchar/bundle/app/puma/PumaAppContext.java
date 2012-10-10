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
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.help.repo.RepoClientImpl;
import org.appdapter.help.repo.RepoClient;
import org.appdapter.impl.store.FancyRepo;

import org.cogchar.api.humanoid.HumanoidConfig;
import org.cogchar.app.buddy.busker.TriggerItem;
import org.cogchar.app.buddy.busker.TriggerItems;
import org.cogchar.bind.rk.robot.svc.ModelBlendingRobotServiceContext;
import org.cogchar.bind.rk.robot.svc.RobotServiceContext;
import org.cogchar.bind.rk.robot.svc.RobotServiceFuncs;
import org.cogchar.blob.emit.GlobalConfigEmitter;
import org.cogchar.blob.emit.KeystrokeConfigEmitter;

import org.cogchar.blob.emit.RepoClientTester;
import org.cogchar.platform.trigger.CogcharActionBinding;

import org.cogchar.render.app.humanoid.HumanoidRenderContext;  // Perhaps we want to fetch this from a context instead, but it's a singleton, so no harm in getting it directly for the moment
import org.cogchar.render.app.humanoid.HumanoidRenderWorldMapper;

import org.osgi.framework.BundleContext;
import org.robokind.api.common.lifecycle.ServiceLifecycleProvider;
import org.robokind.api.common.lifecycle.utils.SimpleLifecycle;
import org.robokind.api.common.osgi.lifecycle.OSGiComponent;
import org.cogchar.api.skeleton.config.BoneCN;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaAppContext extends BasicDebugger {

	private BundleContext			myBundleContext;
	private	PumaContextMediator		myMediator;
	private	PumaVirtualWorldMapper	myVirtualWorldMapper;
	
	private	PumaConfigManager		myConfigManager;
	
	private ClassLoader				myInitialBonyRdfCL;
	// We now have a single instance of the web mapper here [via this.getWebMapper and PumaWebMapper.getWebMapper],
	// instead of separate instances for each PumaDualCharacter.
	private PumaWebMapper			myWebMapper; 
	// This method for updating bony config is not very flexible (to configuring only single characters in the future)
	// and requires multiple sheet reloads for multiple characters. So I'm trying out the idea of moving this functionality
	// into updateConfigByRequest - Ryan
	//private TriggerItems.UpdateBonyConfig myUpdateBonyConfigTI;
	// Let's try making this a field of PumaAppContext. That way, refresh of bony config can be handled here in a nice
	// clean, consistent way. May also have additional advantages. Might have some disadvantages too, we'll see!
	private List<PumaDualCharacter>	myCharList = new ArrayList<PumaDualCharacter>();


	
	public PumaAppContext(BundleContext bc) {
		myBundleContext = bc;
		myVirtualWorldMapper = new PumaVirtualWorldMapper(this);
		myConfigManager = new PumaConfigManager();
	}
	
	protected BundleContext getBundleContext() {
		return myBundleContext;
	}
	public PumaVirtualWorldMapper getVirtualWorldMapper() { 
		return myVirtualWorldMapper;
	}
	public void setCogCharResourcesClassLoader(ClassLoader loader) {
		myInitialBonyRdfCL = loader;
	}
	public ClassLoader getCogCharResourcesClassLoader() {
		return myInitialBonyRdfCL;
	}
	public PumaWebMapper getWebMapper() {
		if (myWebMapper == null) {
			myWebMapper = new PumaWebMapper(this);
		}
		return myWebMapper;
	}
	protected PumaConfigManager getConfigManager() { 
		return myConfigManager;
	}

	public void startOpenGLCanvas(boolean wrapInJFrameFlag) throws Exception {
		myVirtualWorldMapper.startOpenGLCanvas(wrapInJFrameFlag);
	}	
	protected void initCinema() { 
		myVirtualWorldMapper.initCinema();
	}
	// TODO:  This should take some optional args that will start a different repo client instead.
	public void startRepositoryConfigServices() {
		// This would happen by default anyway, if there were not already a MainConfigRepoClient in place.
		myConfigManager.applyVanillaRepoClientAsMainConfig(myBundleContext);
			// This method performs the configuration actions associated with the developmental "Global Mode" concept
			// If/when "Global Mode" is replaced with a different configuration "emitter", the method(s) here will
			// be updated to relect that		
		myConfigManager.applyGlobalConfig(myBundleContext);
	}



	public void setContextMediator(PumaContextMediator mediator) {
		myMediator = mediator;
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
			logWarning("GlobalConfigEmitter not available, cannot setup characters!");
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
					graphIdentForHumanoid =  pcm.resolveGraphForCharAndRole(charIdent, PumaModeConstants.HUMANOID_CONFIG_ROLE);
				} catch (Exception e) {
					getLogger().warn("Could not get valid graphs on which to query for config of {}", charIdent.getLocalName());
					break;
				}
				HumanoidConfig myHumanoidConfig = new HumanoidConfig(rc, charIdent, graphIdentForHumanoid);
				PumaDualCharacter pdc = connectDualRobotChar(charIdent, myHumanoidConfig.nickname);
				myCharList.add(pdc);
				pdc.absorbContext(myMediator);
				setupCharacterBindingToRobokind(pdc, graphIdentForBony, myHumanoidConfig);
				setupAndStartBehaviorTheater(pdc);
			}
		}
		return myCharList;
	}

	public void reloadVirtualWorldConfig(boolean resetMainConfigFlag) {
		PumaConfigManager pcm = getConfigManager();
		PumaVirtualWorldMapper pvwm = getVirtualWorldMapper();
		if (resetMainConfigFlag) {
			BundleContext bc = getBundleContext();
			pcm.applyFreshDefaultMainRepoClientToGlobalConfig(bc);
		}
		pvwm.clearCinematicStuff();
		pvwm.initCinema();
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
		PumaVirtualWorldMapper pvwm = getVirtualWorldMapper();
		pvwm.detachAllHumanoidFigures();
		myCharList.clear();
	}
	public void reloadAll(boolean resetMainConfigFlag) {
		try {
			BundleContext bunCtx = getBundleContext();
			// Here we make the cute assumption that vWorldMapper or webMapper would be null
			// if we weren't using those features.  Only problem is that is not true yet.
			
			PumaVirtualWorldMapper vWorldMapper = getVirtualWorldMapper();
			PumaWebMapper webMapper = getWebMapper();

			if (vWorldMapper != null) {
				vWorldMapper.clearCinematicStuff();
				vWorldMapper.clearSpecialInputTriggers();
			}
			if (webMapper != null) {
				webMapper.disconnectLiftSceneInterface(bunCtx);
			}
			stopAndReleaseAllHumanoids();
		
			if (resetMainConfigFlag) {
				PumaConfigManager pcm = getConfigManager();
				pcm.applyFreshDefaultMainRepoClientToGlobalConfig(bunCtx);
			}			
			
			connectDualRobotChars();
			if (vWorldMapper != null) {
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

		myVirtualWorldMapper.registerSpecialInputTriggers(pdc);

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
				setupRobokindJointGroup(pdc, hc.jointConfigPath);
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
		File jgConfigFile = makeJointGroupTempFile(pdc, jgFullPath);
		if (jgConfigFile != null) {
			PumaHumanoidMapper phm = pdc.getHumanoidMapper();
			RobotServiceContext rsc = phm.getRobotServiceContext();
			rsc.startJointGroup(jgConfigFile);
		} else {
			getLogger().warn("jointGroup file not found: {}", jgFullPath);
		}
	}

	private File makeJointGroupTempFile(PumaDualCharacter pdc, String jgFullPath) {
		File outputFile = null;
		try {
			outputFile = new File(pdc.getNickName() + "temporaryJointGroupResource.xml");
			InputStream stream = myInitialBonyRdfCL.getResourceAsStream(jgFullPath);
			OutputStream out = new FileOutputStream(outputFile);
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = stream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			stream.close();
			out.flush();
			out.close();
		} catch (Exception e) {
			getLogger().warn("Exception trying to load jointGroup from resource into temp file: ", e);
		}
		return outputFile;
	}

	public PumaDualCharacter connectDualRobotChar(Ident bonyCharIdent, String nickName)
			throws Throwable {

		HumanoidRenderContext hrc = myVirtualWorldMapper.getHumanoidRenderContext();
		if (hrc == null) {
			throw new Exception("HumanoidRenderContext is null");
		}
		PumaDualCharacter pdc = new PumaDualCharacter(hrc, myBundleContext, this, bonyCharIdent, nickName);

		return pdc;
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
