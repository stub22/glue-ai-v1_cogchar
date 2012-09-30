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

import org.cogchar.api.humanoid.HumanoidConfig;
import org.cogchar.app.buddy.busker.TriggerItem;
import org.cogchar.app.buddy.busker.TriggerItems;
import org.cogchar.bind.rk.robot.svc.ModelBlendingRobotServiceContext;
import org.cogchar.bind.rk.robot.svc.RobotServiceContext;
import org.cogchar.bind.rk.robot.svc.RobotServiceFuncs;
import org.cogchar.blob.emit.GlobalConfigEmitter;
import org.cogchar.blob.emit.KeystrokeConfigEmitter;

import org.cogchar.blob.emit.QueryTester;
import org.cogchar.platform.trigger.DummyBinding;
import org.cogchar.render.app.trigger.KeyBindingConfig;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;  // Perhaps we want to fetch this from a context instead, but it's a singleton, so no harm in getting it directly for the moment
import org.cogchar.render.app.humanoid.HumanoidRenderWorldMapper;
import org.cogchar.render.app.humanoid.HumanoidPuppetActions.PlayerAction;
import org.cogchar.render.opengl.osgi.RenderBundleUtils;
import org.osgi.framework.BundleContext;
import org.robokind.api.common.lifecycle.ServiceLifecycleProvider;
import org.robokind.api.common.lifecycle.utils.SimpleLifecycle;
import org.robokind.api.common.osgi.lifecycle.OSGiComponent;
import org.cogchar.api.skeleton.config.BoneQueryNames;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaAppContext extends BasicDebugger {

	private BundleContext			myBundleContext;
	private HumanoidRenderContext	myHRC;
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
	// A query interface instance we can reuse - right now just to trigger repo reloads. May want to do that via
	// GlobalConfigEmitter or some other interface in the long run...?
	RepoClient					myRepoClient;
	
	// A managed service instance of the GlobalConfigEmitter, currently used only by LifterLifecycle.
	// We need to keep track of it so we can stop and restart it for Lift "refresh"
	OSGiComponent					myGcComp;
	// Same with the managed queryinterface used by Lift
	OSGiComponent					myQueryComp;
	PumaContextMediator				myMediator;
	// Here's a GlobalConfigEmitter for our PUMA instance. Does it really belong here? Time will tell.
	private GlobalConfigEmitter		myGlobalConfig;

	
	public PumaAppContext(BundleContext bc) {
		myBundleContext = bc;
	}

	public HumanoidRenderContext getHumanoidRenderContext() {
		return myHRC;
	}

	public void setCogCharResourcesClassLoader(ClassLoader loader) {
		myInitialBonyRdfCL = loader;
	}

	public PumaWebMapper getWebMapper() {
		if (myWebMapper == null) {
			myWebMapper = new PumaWebMapper(this);
		}
		return myWebMapper;
	}

	public GlobalConfigEmitter getGlobalConfig() {
		return myGlobalConfig;
	}

	private void clearQueryHelper() {
		myRepoClient = null;
	}
	private RepoClient getQueryHelper() { 
		if (myRepoClient == null) {
			myRepoClient = QueryTester.makeVanillaQueryEmitter();
		}
		return myRepoClient;
	}
	
	// Registers the QueryEmitter service, currently with an empty lifecycle.
	// This service will be used by managed services needing query config
	// Currently, that's: LifterLifecycle
	// Moved here from PumaBooter because all "top level" RepoClient business is now handled in this class.
	// Also, we want this here so we can handle updates to Lifter config here, like with all other config.
	public RepoClient startVanillaRepoClient() {
		// We want to make explicity the assumptions about what goes into our QueryEmitter.
		// On 2012-09-12 Stu changed "new QueryEmitter()" to makeVanillaQueryEmitter,
		// but perhaps there is some more adjustment to do here for lifecycle compat.
		//QueryEmitter qemit = QueryTester.makeVanillaQueryEmitter();
		// On 2012-09-16 Ryan changed from the qemit declaration above to the one below. This allows us to use the 
		// same instance for the QueryEmitter here as is accessed by QueryTester.getInterface, preventing duplicate
		// (SLOW) resource loads and the possibility of unsynchronized state in PUMA.
		RepoClient qemit = getQueryHelper();
		ServiceLifecycleProvider lifecycle = new SimpleLifecycle(qemit, RepoClient.class);
    	myQueryComp = new OSGiComponent(myBundleContext, lifecycle);
    	myQueryComp.start();
		return qemit;
	}
	
	// Now here's something I was hoping to avoid, but it necessary for our experiment in making Lift a managed
	// service. This is best seen as a trial of one possible way to handle the "GlobalMode" graph configuration.
	// What we'll do here is tell the PumaAppContext to make the GlobalConfigEmitter available as a no-lifecycle
	// managed service. (Why no-lifecycle? Because these lifecycles have to end somewhere! But it would make sense
	// to make this service depend on the query interface if we decide to keep it.)
	// Then Lifter can access it to load its config.
	// The problem with this approach is that it elevates the GlobalConfigEmitter to a data structure of particular 
	// importance outside of PUMA (we're putting it on the OSGi registry for crying out loud!), when at this early
	// point I've been trying to keep non-PUMA code "agnostic" to any details of the graph "mode" config other than
	// the Idents of the graph.
	// So this may be a bad-idea-dead-end. Unless we decide we've fallen in love with both the GlobalConfigEmitter
	// and the idea of doing config via managed services, in which it may turn out to be just what we need.
	// For now, we'll restrict usage of this to the LifterLifeCycle only...
	boolean startGlobalConfigService() {
		boolean success = false;
		if (myGlobalConfig != null) {
			ServiceLifecycleProvider lifecycle =
					new SimpleLifecycle(new GlobalConfigServiceImpl(), GlobalConfigEmitter.GlobalConfigService.class);
			myGcComp = new OSGiComponent(myBundleContext, lifecycle);
			myGcComp.start();
			success = true;
		}
		return success;
	}

	// Right now this really feels wrong to make this a service! I don't believe in these maps enough yet.
	// Putting it here since it's more experimental than the GlobalConfigEmitter itself, but if this ends up
	// being the "preferred" solution this interface should probably go into o.c.blob.emit
	class GlobalConfigServiceImpl implements GlobalConfigEmitter.GlobalConfigService {

		@Override
		public java.util.HashMap<Ident, java.util.HashMap<Ident, Ident>> getErgMap() {
			return myGlobalConfig.ergMap();
		}

		@Override
		public java.util.HashMap<String, java.util.List<Ident>> getEntityMap() {
			return myGlobalConfig.entityMap();
		}
	}

	// This may be the same thing as updateGlobalConfig eventually. Right now we are holding open the possibility that Lifter is acting on
	// one global config and the rest of Cog Char on another. This allows us to update one but not the other, since Lifter uses the GlobalConfigService
	// and everything else uses myGlobalConfig in this class. (Lifter auto-updates when the GlobalConfigService restarts.)
	// But really this is a can of worms, so probably we should move to having both the
	// GlobalConfigService and myGlobalConfig always be updated at the same time. Not yet though, until the possible implications are worked through...
	private void applyGlobalConfig() {
		RepoClient qHelper = getQueryHelper();
		Ident gcIdent = new FreeIdent(PumaModeConstants.rkrt+PumaModeConstants.globalMode, PumaModeConstants.globalMode);
		myGlobalConfig = new GlobalConfigEmitter(qHelper, gcIdent);
	}
	public void applyGlobalConfigAndStartService() {
		applyGlobalConfig();
		startGlobalConfigService();
	}
	public void updateGlobalConfig() {
		// Now this is a little irregular. We're creating this initally in PumaBooter, but also the same 
		// (temporarily fixed) mode is reloaded here when we want to updateGlobalConfig. So far, that's mainly for our 
		// current "primitive" bony config reload. This all is a bit goofy and should be quite temporary; once we really 
		// figure out how best to handle changes to this "GlobalMode" stuff this should become less hodge-podge
		// Do we want to always reload the repo here? Might want to keep these functions separate in the future, but for
		// now I'll assume they will go together.
		clearQueryHelper();
		applyGlobalConfig();
	}
	// A half baked (3/4 baked?) idea. Since PumaAppContext is basically in charge of global config right now, this will be a general
	// way to ask that config be updated. Why the string argument? See UpdateInterface comments...
	private boolean updating = false;
	// Here I have removed the method variable passed in for the RepoClient. Why? Because right now PumaAppContext really
	// is the central clearing house for the RepoClient for config -- ideally we want it to be passed down from one master instance here to
	// all the objects that use it. Methods calling for config updates via this method shouldn't be responsible for 
	// knowing what RepoClient is appropriate -- they are calling into this method because we are trying to handle that here.
	// So for now let's use the this.getQueryHelper way to get that interface here. We can continue to refine this thinking as we go.
	// - Ryan 2012-09-17
	public boolean updateConfigByRequest(String request) {
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
		if (updating) {
			getLogger().warn("Update currently underway, ignoring additional request");
			success = false;
		} else if (WORLD_CONFIG.equals(request.toLowerCase())) {
			updating = true;
			Thread updateThread = new Thread("World Update Thread") {

				public void run() {
					reloadWorldConfig();
					updating = false;
				}
			};
			updateThread.start();
		} else if (BONE_ROBOT_CONFIG.equals(request.toLowerCase())) {
			updating = true;
			Thread updateThread = new Thread("Bone Robot Update Thread") {

				public void run() {
					reloadBoneRobotConfig();
					updating = false;
				}
			};
			updateThread.start();
		} else if (MANAGED_GCS.equals(request.toLowerCase())) {
			updating = true;
			if (myGcComp != null) {
				myGcComp.dispose();
			}
			if (myQueryComp != null) {
				myQueryComp.dispose();
			}
			Thread updateThread = new Thread("Managed Global Config Service Update Thread") {

				public void run() {
					updateGlobalConfig();
					startGlobalConfigService();
					startVanillaRepoClient();
					updating = false;
				}
			};
			updateThread.start();
		} else if (ALL_HUMANOID_CONFIG.equals(request.toLowerCase())) {
			updating = true;
			Thread updateThread = new Thread("Update Thread") {

				public void run() {
					reloadAll();
					updating = false;
				}
			};
			updateThread.start();
		} else {
			getLogger().warn("PumaAppContext did not recognize the config update to be performed: " + request);
			success = false;
		}
		return success;
	}

	class UpdateInterfaceImpl implements HumanoidRenderContext.UpdateInterface {

		@Override
		public boolean updateConfig(String request) {
			return updateConfigByRequest(request);
		}
	}

	/**
	 * First (of three) stage init of world, done BEFORE startOpenGLCanvas().
	 *
	 * @param panelKind
	 * @return
	 */
	public HumanoidRenderContext initHumanoidRenderContext(String panelKind) {
		myHRC = (HumanoidRenderContext) RenderBundleUtils.buildBonyRenderContextInOSGi(myBundleContext, panelKind);
		myHRC.setUpdateInterface(new UpdateInterfaceImpl());
		return myHRC;
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

		//List<PumaDualCharacter> pdcList = new ArrayList<PumaDualCharacter>();
		List<Ident> charIdents = new ArrayList<Ident>(); // A blank list, so if the try fails below, the for loop won't throw an Exception
		try {
			List<Ident> identsFromConfig = myGlobalConfig.entityMap().get(PumaModeConstants.CHAR_ENTITY_TYPE);
			if (identsFromConfig != null) {
				charIdents = identsFromConfig;
			} else {
				getLogger().warn("Did not find character entity list in global config map");
				throw new Throwable();
			}
		} catch (Throwable t) {
			getLogger().warn("Could not retrieve list of characters from global configuration");
		}

		if (myGlobalConfig == null) {
			logWarning("GlobalConfigEmitter not available, cannot setup characters!");
		} else if (myInitialBonyRdfCL == null) {
			// We may not need this check eventually - currently only jointGroup.xml files and BallBuilder Turtle loader
			// need access to this ClassLoader
			getLogger().warn("Cog Char resources ClassLoader not available, cannot setup characters!");
		} else {
			for (Ident charIdent : charIdents) {
				logInfo("^^^^^^^^^^^^^^^^^^^^^^^^^ Connecting dualRobotChar for charIdent: " + charIdent);
				Ident graphIdentForBony;
				Ident graphIdentForHumanoid;
				try {
					graphIdentForBony = myGlobalConfig.ergMap().get(charIdent).get(PumaModeConstants.BONY_CONFIG_ROLE);
					graphIdentForHumanoid = myGlobalConfig.ergMap().get(charIdent).get(PumaModeConstants.HUMANOID_CONFIG_ROLE);
				} catch (Exception e) {
					getLogger().warn("Could not get valid graphs on which to query for config of " + charIdent.getLocalName());
					break;
				}
				HumanoidConfig myHumanoidConfig = new HumanoidConfig(getQueryHelper(), charIdent, graphIdentForHumanoid);
				PumaDualCharacter pdc = connectDualRobotChar(charIdent, myHumanoidConfig.nickname);
				myCharList.add(pdc);
				pdc.absorbContext(myMediator);
				setupCharacterBindingToRobokind(pdc, graphIdentForBony, myHumanoidConfig);
				setupAndStartBehaviorTheater(pdc);
			}
		}
		return myCharList;
	}

	// The Lights/Camera/Cinematics init used to be done from HumanoidRenderContext, but the global config lives
	// here as does humanoid and bony config. So may make sense to have this here too, though we could move it
	// back to HRC if there are philosophical reasons for doing so. (We'd also have to pass two graph flavors to it for this.)
	// Added: since jMonkey key bindings are part of "virtual world" config like Lights/Camera/Cinematics, they are also 
	// set here
	public void initCinema() {
		myHRC.initCinema();
		RepoClient qi = getQueryHelper();
		PumaWebMapper theMapper = getWebMapper();
		theMapper.connectLiftSceneInterface(myBundleContext);
		theMapper.connectLiftInterface(myBundleContext);	
		// The connectCogCharResources call below is currently still needed only for the "legacy" BallBuilder functionality
		theMapper.connectCogCharResources(myInitialBonyRdfCL, myHRC);
		KeyBindingConfig currentBindingConfig = new KeyBindingConfig();
		HumanoidRenderWorldMapper myRenderMapper = new HumanoidRenderWorldMapper();
		Ident graphIdent = null;
		try {
			List<Ident> worldConfigIdents = myGlobalConfig.entityMap().get(PumaModeConstants.VIRTUAL_WORLD_ENTITY_TYPE);
			// Multiple worldConfigIdents? Possible. It's possible duplicate cinematic definitions might cause problems
			// but we'll leave that for later, so sure, go ahead and load on multiple configs if they are requested.
			for (Ident configIdent : worldConfigIdents) {
				try {
					graphIdent = myGlobalConfig.ergMap().get(configIdent).get(PumaModeConstants.LIGHTS_CAMERA_CONFIG_ROLE);
				} catch (Exception e) {
					getLogger().warn("Could not get valid graph on which to query for Lights/Cameras config of " + configIdent.getLocalName(), e);
				}
				try {
					myRenderMapper.initLightsAndCamera(qi, myHRC, graphIdent);
				} catch (Exception e) {
					getLogger().warn("Error attempting to initialize lights and cameras for " + configIdent.getLocalName() + ": " + e, e);
				}
				graphIdent = null;
				try {
					graphIdent = myGlobalConfig.ergMap().get(configIdent).get(PumaModeConstants.CINEMATIC_CONFIG_ROLE);
				} catch (Exception e) {
					getLogger().warn("Could not get valid graph on which to query for Cinematics config of " + configIdent.getLocalName(), e);
				}
				try {
					myRenderMapper.initCinematics(qi, myHRC, graphIdent);
				} catch (Exception e) {
					getLogger().warn("Error attempting to initialize Cinematics for " + configIdent.getLocalName() + ": " + e, e);
				}
				// Like with everything else dependent on global config's graph settings (except for Lift, which uses a managed service
				// version of GlobalConfigEmitter) it seems logical to set the key bindings here.
				// Multiple worldConfigIdents? We decided above this is possible (if messy). If key bindings are duplicated
				// between the multiple world configs, we can't be certain which will end up in the KeyBindingConfig map.
				// But for now we'll assume user is smart enough to watch out for that (perhaps a dangerous idea) and pile
				// bindings from all worldConfigIdents into our KeyBindingConfig instance.
				try {
					graphIdent = myGlobalConfig.ergMap().get(configIdent).get(PumaModeConstants.INPUT_BINDINGS_ROLE);
					KeystrokeConfigEmitter kce = new KeystrokeConfigEmitter();
					
					currentBindingConfig.addBindings(qi, graphIdent, kce);
				} catch (Exception e) {
					getLogger().error("Could not get valid graph on which to query for input bindings config of " + configIdent.getLocalName(), e);
				}

			}
		} catch (Exception e) {
			getLogger().error("Could not retrieve any specified VirtualWorldEntity for this global configuration!");
		}
		myHRC.initBindings(currentBindingConfig);
	}

	public void reloadWorldConfig() {
		updateGlobalConfig();
		HumanoidRenderWorldMapper myRenderMapper = new HumanoidRenderWorldMapper();
		myRenderMapper.clearLights(myHRC);
		myRenderMapper.clearCinematics(myHRC);
		myRenderMapper.clearViewPorts(myHRC);
		initCinema();
	}

	public void reloadBoneRobotConfig() {
		updateGlobalConfig();
		BoneQueryNames bqn = new BoneQueryNames();
		for (PumaDualCharacter pdc : myCharList) {
			logInfo("Updating bony config for char [" + pdc + "]");
			try {
				Ident graphIdent = myGlobalConfig.ergMap().get(pdc.getCharIdent()).get(PumaModeConstants.BONY_CONFIG_ROLE);
				try {
					pdc.updateBonyConfig(getQueryHelper(), graphIdent, bqn);
				} catch (Throwable t) {
					getLogger().error("problem updating bony config from queries for " + pdc.getCharIdent(), t);
				}
			} catch (Exception e) {
				getLogger().warn("Could not get a valid graph on which to query for config update of " + pdc.getCharIdent().getLocalName());
			}
		}
	}

	public void reloadAll() {
		try {
			updateGlobalConfig();
			HumanoidRenderWorldMapper myRenderMapper = new HumanoidRenderWorldMapper();
			myRenderMapper.clearLights(myHRC);
			myRenderMapper.clearCinematics(myHRC);
			myRenderMapper.clearViewPorts(myHRC);
			clearSpecialInputTriggers();
			getWebMapper().disconnectLiftSceneInterface(myBundleContext);
			for (PumaDualCharacter pdc : myCharList) {
				pdc.stopEverything();
				pdc.disconnectBonyCharFromRobokindSvcs();
			}
			RobotServiceFuncs.clearJointGroups();
			ModelBlendingRobotServiceContext.clearRobots();
			myHRC.detachHumanoidFigures();
			myCharList.clear();
			connectDualRobotChars();
			initCinema();
		} catch (Throwable t) {
			getLogger().error("Error attempting to reload all humanoid config: " + t);
			// May be good to handle an exception by setting state of a "RebootResult" or etc...
		}
	}

	public void setupAndStartBehaviorTheater(PumaDualCharacter pdc) throws Throwable {
		//pdc.registerDefaultSceneTriggers(); // Seems this doesn't actually do anything at this point
		pdc.loadBehaviorConfig(false);

		registerSpecialInputTriggers(pdc);

		pdc.startTheater();

	}

	public boolean setupCharacterBindingToRobokind(PumaDualCharacter pdc, Ident graphIdentForBony, HumanoidConfig hc) {

		try {
			RepoClient qi = getQueryHelper();
			BoneQueryNames bqn = new BoneQueryNames();
			boolean connectedOK = pdc.connectBonyCharToRobokindSvcs(myBundleContext, graphIdentForBony, hc, qi, bqn);
			if (connectedOK) {
				setupRobokindJointGroup(pdc, hc.jointConfigPath);
				pdc.connectSpeechOutputSvcs(myBundleContext);
				return true;
			} else {
				Ident charIdent = pdc.getCharIdent();
				getLogger().warn("setupCharacterBindingToRobokind() aborting RK binding for character: " + charIdent);
				return false;
			}
		} catch (Throwable t) {
			getLogger().error("Exception during setupCharacterBindingToRobokind()", t);
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
			getLogger().warn("jointGroup file not found: " + jgFullPath);
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
			getLogger().warn("Exception trying to load jointGroup from resource into temp file: " + e);
		}
		return outputFile;
	}

	public PumaDualCharacter connectDualRobotChar(Ident bonyCharIdent, String nickName)
			throws Throwable {

		HumanoidRenderContext hrc = getHumanoidRenderContext();
		if (hrc == null) {
			throw new Exception("HumanoidRenderContext is null");
		}
		PumaDualCharacter pdc = new PumaDualCharacter(hrc, myBundleContext, this, bonyCharIdent, nickName);

		return pdc;
	}

	/**
	 * Second (and most crucial) stage of OpenGL init. This method blocks until the canvas initialization is complete,
	 * which requires that the simpleInitApp() methods have all completed.
	 *
	 * @param wrapInJFrameFlag
	 * @throws Exception
	 */
	public void startOpenGLCanvas(boolean wrapInJFrameFlag) throws Exception {
		HumanoidRenderContext hrc = getHumanoidRenderContext();
		if (hrc != null) {
			hrc.startOpenGLCanvas(wrapInJFrameFlag);
		} else {
			logError("HumanoidRenderContext is NULL, cannot startOpenGLCanvas!");
		}
	}

	private void registerSpecialInputTriggers(PumaDualCharacter pdc) {

		hookItUp(PlayerAction.STOP_AND_RESET_CHAR, pdc, new TriggerItems.StopAndReset());
		hookItUp(PlayerAction.STOP_RESET_AND_RECENTER_CHAR, pdc, new TriggerItems.StopResetAndRecenter());

		hookItUp(PlayerAction.DANGER_YOGA, pdc, new TriggerItems.DangerYoga());
		hookItUp(PlayerAction.SAY_THE_TIME, pdc, new TriggerItems.SayTheTime());

		hookItUp(PlayerAction.USE_PERM_ANIMS, pdc, new TriggerItems.UsePermAnims());
		hookItUp(PlayerAction.USE_TEMP_ANIMS, pdc, new TriggerItems.UseTempAnims());

		hookItUp(PlayerAction.RELOAD_BEHAVIOR, pdc, new TriggerItems.ReloadBehavior());

		//myUpdateBonyConfigTI = new TriggerItems.UpdateBonyConfig();
		//myUpdateBonyConfigTI.myOptResourceClassLoader = null;

		//hookItUp(PlayerAction.UPDATE_BONY_CONFIG, pdc, myUpdateBonyConfigTI);
	}

	private void hookItUp(PlayerAction action, PumaDualCharacter pdc, TriggerItem trigItem) {
		// Hook up to a JME3 action (defined in our org.cogchar.lib.render project) to catch keypresses in OpenGL window.
		DummyBinding db = action.getBinding();
		db.setTargetBox(pdc);
		db.setTargetTrigger(trigItem);
	}

	private void clearSpecialInputTriggers() {

		unhookIt(PlayerAction.STOP_AND_RESET_CHAR);
		unhookIt(PlayerAction.STOP_RESET_AND_RECENTER_CHAR);

		unhookIt(PlayerAction.DANGER_YOGA);
		unhookIt(PlayerAction.SAY_THE_TIME);

		unhookIt(PlayerAction.USE_PERM_ANIMS);
		unhookIt(PlayerAction.USE_TEMP_ANIMS);

		unhookIt(PlayerAction.RELOAD_BEHAVIOR);

	}

	private void unhookIt(PlayerAction action) {
		DummyBinding db = action.getBinding();
		db.clearTargetBox();
	}
}
