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
package org.cogchar.app.puma.boot;

import org.cogchar.app.puma.config.PumaContextMediator;
import org.cogchar.app.puma.config.PumaConfigManager;
//import org.cogchar.app.puma.vworld.PumaVirtualWorldMapper;
import org.cogchar.app.puma.web.PumaWebMapper;
import org.cogchar.name.entity.EntityRoleCN;
import org.cogchar.app.puma.registry.PumaRegistryClient;
import org.cogchar.app.puma.registry.PumaRegistryClientImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;

import org.cogchar.api.humanoid.FigureConfig;
import org.cogchar.impl.thing.basic.BasicThingActionRouter;
import org.cogchar.bind.mio.robot.svc.ModelBlendingRobotServiceContext;
import org.cogchar.bind.mio.robot.svc.RobotServiceFuncs;
import org.cogchar.blob.emit.GlobalConfigEmitter;

import org.osgi.framework.BundleContext;
//import org.cogchar.app.buddy.busker.TriggerItems;
import org.cogchar.app.puma.behavior.PumaBehaviorManager;
import org.cogchar.app.puma.body.PumaDualBody;
import org.cogchar.app.puma.config.PumaGlobalModeManager;
import org.cogchar.app.puma.config.BodyHandleRecord;
import org.cogchar.app.puma.body.PumaDualBodyManager;
import static org.cogchar.app.puma.boot.PumaContextCommandBox.THING_ACTIONS;
import org.cogchar.app.puma.registry.PumaRegistryClientFinder;
import org.cogchar.app.puma.registry.ResourceFileCategory;
import org.cogchar.bundle.app.puma.GruesomeTAProcessingFuncs;
import org.cogchar.bundle.app.puma.PumaAppUtils;
import org.cogchar.platform.trigger.BoxSpace;

import org.cogchar.platform.trigger.CommandSpace;
import org.cogchar.app.puma.config.TriggerConfig;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponent;
import org.jflux.impl.services.rk.lifecycle.ManagedService;
import org.jflux.impl.services.rk.lifecycle.ServiceLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.utils.SimpleLifecycle;


/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaAppContext extends BasicDebugger {

	private PumaRegistryClient			myRegClient;
	private OSGiComponent				myRegClientOSGiComp;
	
	private BundleContext				myBundleContext;

	private	PumaDualBodyManager			myBodyMgr;
	
	private PumaBehaviorManager			myBehavMgr;
	
	private	PumaContextCommandBox		myPCCB;
    
    private ArrayList<BodyHandleRecord>   bodyConfigSpecs;
	
	public PumaAppContext(BundleContext bc, PumaContextMediator mediator, Ident ctxID) {
		myRegClient = new PumaRegistryClientImpl(bc, mediator);
		advertisePumaRegClient(myRegClient);
		myBundleContext = bc;
        
		BoxSpace bs = myRegClient.getTargetBoxSpace(null);
		myPCCB = new PumaContextCommandBox(this);
		bs.addBox(ctxID, myPCCB);
		
		myBodyMgr = new PumaDualBodyManager();
		myBehavMgr = new PumaBehaviorManager();
        bodyConfigSpecs= new ArrayList<BodyHandleRecord>();
        
        ServiceLifecycleProvider<PumaRegistryClient> lifecycle =
                new SimpleLifecycle<PumaRegistryClient>(myRegClient,PumaRegistryClient.class.getName());
//            Properties props=new Properties();
//            props.put("theRegistryClient","theRegistryClient");
            ManagedService<PumaRegistryClient> ms = new OSGiComponent<PumaRegistryClient>(myBundleContext, lifecycle, null);
            ms.start();
        
	}
	
	private void advertisePumaRegClient(PumaRegistryClient prc) {
		PumaRegistryClientFinder prcFinder = new PumaRegistryClientFinder();
		prcFinder.registerPumaRegClient(prc, null, PumaAppContext.class);
	}
	
	public BundleContext getBundleContext() {
		return myBundleContext;
	}

	protected boolean hasWebMapper() {
		return (myRegClient.getWebMapper(null) != null);
	}

	public PumaWebMapper getOrMakeWebMapper() {
		PumaWebMapper pwm = myRegClient.getWebMapper(null);
		if (pwm == null) {
			pwm = new PumaWebMapper(myPCCB);
			myRegClient.putWebMapper(pwm, null);
		}
        pwm.attachContext(myBundleContext);
		return pwm;
	}

	protected RepoClient getOrMakeMainConfigRC() {
		final PumaConfigManager pcm = getConfigManager();
		PumaContextMediator mediator = getMediator();
		RepoClient repoCli = pcm.getOrMakeMainConfigRepoClient(mediator, myBundleContext);
		return repoCli;
	}

	protected PumaContextMediator getMediator() {
		return myRegClient.getCtxMediator(null);
	}

	public PumaConfigManager getConfigManager() {
		return myRegClient.getConfigMgr(null);
	}

	public void startRepositoryConfigServices() {
		PumaConfigManager pcm = getConfigManager();
		PumaContextMediator mediator = myRegClient.getCtxMediator(null);
		// This would happen by default anyway, if there were not already a MainConfigRepoClient in place.
		pcm.applyDefaultRepoClientAsMainConfig(mediator, myBundleContext);
		// This method performs the configuration actions associated with the developmental "Global Mode" concept
		// If/when "Global Mode" is replaced with a different configuration "emitter", the method(s) here will
		// be updated to relect that	
		RepoClient rc = getOrMakeMainConfigRC();
		PumaGlobalModeManager pgmm = pcm.getGlobalModeMgr();
		pgmm.applyGlobalConfig(myBundleContext, rc);
		
		myBehavMgr.initConfigLinks(myRegClient);
	}

	/**
	 * Third (and last) stage init of OpenGL, and all other systems. Done AFTER startOpenGLCanvas().
	 *
	 * @return
	 * @throws Throwable
	 */
	
	public void connectAllBodies() {
		List<Ident> charIDs = getAllCharIdents();
		connectDualBodies(charIDs);
		makeAgentsForAllBodies(charIDs);
	}
	public void makeAgentsForAllBodies(List<Ident> charIdents) {
		BundleContext bunCtx = getBundleContext();
		final PumaConfigManager pcm = getConfigManager();
		final PumaGlobalModeManager pgmm = pcm.getGlobalModeMgr();
		GlobalConfigEmitter gce = pgmm.getGlobalConfig();
		RepoClient rc = getOrMakeMainConfigRC();		
		for (Ident charID : charIdents) {
			PumaDualBody pdb = myBodyMgr.getBody(charID);
			if (pdb != null) {
				getLogger().info("Making agent for char={} and body={} ", charID, pdb);
				myBehavMgr.makeAgentForBody(bunCtx, myRegClient, pdb, charID);
			}
		}
	}
	protected List<Ident> getAllCharIdents() {
		final PumaConfigManager pcm = getConfigManager();
		final PumaGlobalModeManager pgmm = pcm.getGlobalModeMgr();
		GlobalConfigEmitter gce = pgmm.getGlobalConfig();
		RepoClient rc = getOrMakeMainConfigRC();
		//List<PumaDualCharacter> pdcList = new ArrayList<PumaDualCharacter>();
		List<Ident> charIdents = new ArrayList<Ident>(); // A blank list, so if the try fails below, the for loop won't throw an Exception

		List<Ident> identsFromConfig = gce.entityMap().get(EntityRoleCN.CHAR_ENTITY_TYPE);

		if (identsFromConfig != null) {
			charIdents = identsFromConfig;
		} else {
			String msg = "Could not retrieve list of characters from global configuration, aborting all char setup";
			getLogger().error(msg);
			throw new RuntimeException(msg);
		}
		return charIdents;
	}
	protected void connectDualBodies(List<Ident> charIdents) {
		final PumaConfigManager pcm = getConfigManager();
		final PumaGlobalModeManager pgmm = pcm.getGlobalModeMgr();
		GlobalConfigEmitter gce = pgmm.getGlobalConfig();
		RepoClient rc = getOrMakeMainConfigRC();
		
		if (gce == null) {
			getLogger().warn("GlobalConfigEmitter not available, cannot setup characters!");
		} else {
			for (Ident charIdent : charIdents) {
				try {
					getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^^ Connecting dualRobotChar for charIdent: {}", charIdent);
					Ident graphIdentForBony;
					Ident graphIdentForHumanoid;
					try {
						graphIdentForBony = pgmm.resolveGraphForCharAndRole(charIdent, EntityRoleCN.BONY_CONFIG_ROLE);
						graphIdentForHumanoid = pgmm.resolveGraphForCharAndRole(charIdent, EntityRoleCN.HUMANOID_CONFIG_ROLE);
					} catch (Exception e) {
						getLogger().warn("Could not get valid graphs on which to query for config of {}", charIdent.getLocalName());
						break;
					}
					FigureConfig humConfig = new FigureConfig(rc, charIdent, graphIdentForHumanoid);
					PumaDualBody pdb = connectDualBody(humConfig, graphIdentForBony);
				} catch (Throwable t) {
					getLogger().error("Problem initing dualBody for charIdent: " + charIdent, t);
				}
			}
            
            ServiceLifecycleProvider<ArrayList> lifecycle =
                new SimpleLifecycle<ArrayList>(bodyConfigSpecs,ArrayList.class.getName());
            Properties props=new Properties();
            props.put("bodyConfigSpec","bodyConfigSpec");
            ManagedService<ArrayList> ms = new OSGiComponent<ArrayList>(myBundleContext, lifecycle, props);
            ms.start();
            
            ClassLoader vizResCL = getSingleClassLoaderOrNull(ResourceFileCategory.RESFILE_OPENGL_JME3_OGRE);
            
//            ServiceLifecycleProvider<ClassLoader> clLifecycle =
//                new SimpleLifecycle<ClassLoader>(vizResCL,ClassLoader.class.getName());
//            Properties clProps=new Properties();
//            props.put("classLoader","classLoader");
//            ManagedService<ClassLoader> clMS = new OSGiComponent<ClassLoader>(myBundleContext, clLifecycle, clProps);
//            clMS.start();
		}
	}

	protected PumaDualBody connectDualBody(FigureConfig humCfg, Ident graphIdentForBony) throws Throwable {
		Ident bonyCharID = humCfg.getFigureID();
		BundleContext bunCtx = getBundleContext();
		RepoClient rc = getOrMakeMainConfigRC();
        //bodyConfigSpecs.add(new BodyConfigSpec(rc, bonyCharID, humCfg));
		PumaDualBody pdb = new PumaDualBody(bonyCharID, humCfg.getNickname());
		BodyHandleRecord bConfig=new BodyHandleRecord(rc, bonyCharID, humCfg);
        pdb.setBodyConfigSpec(bConfig);
        pdb.absorbContext(myRegClient, bunCtx, rc, humCfg, graphIdentForBony);
		bodyConfigSpecs.add(bConfig);
        myBodyMgr.addBody(pdb);
		return pdb;
	}


	private ClassLoader getSingleClassLoaderOrNull(ResourceFileCategory cat) {
		List<ClassLoader> classLoaders = myRegClient.getResFileCLsForCat(cat);
		ClassLoader singleCL_orNull = (classLoaders.size() == 1) ? classLoaders.get(0) : null;
		return singleCL_orNull;
	}

	/**
	 * Would also need to reload keybindings for this to be effective
	 */
	public TriggerConfig reloadCommandSpace() {
		final PumaConfigManager pcm = getConfigManager();
		RepoClient repoCli = getOrMakeMainConfigRC();
		CommandSpace cmdSpc = myRegClient.getCommandSpace(null);
		BoxSpace boxSpc = myRegClient.getTargetBoxSpace(null);
        
        TriggerConfig tConfig=new TriggerConfig();
        tConfig.setBoxSpace(boxSpc);
        tConfig.setCommandSpace(cmdSpc);
        tConfig.setRepoClient(repoCli);
        
        return tConfig;
        
		// TODO:  stuff to clear out the command space
		//TriggerItems.populateCommandSpace(repoCli, cmdSpc, boxSpc);
	}
/**
 * 	 * Called from one of these three places:
	 * 
	 *  1) PumaBooter.pumaBootUnsafeUnderOSGi
	 * 
	 *  2)  PumaContextCommandBox.processUpdateRequestNow(WORLD_CONFIG)
	 * 
	 *	3)  PumaAppContext.reloadAll
	 *		PumaContextCommandBox.processUpdateRequestNow(ALL_HUMANOID_CONFIG)
	 * 
 * @param clearFirst 
 */


	/**
	 * Called only from 	 PumaBooter.pumaBootUnsafeUnderOSGi
	 */
	protected void connectWeb() {
		PumaWebMapper webMapper = getOrMakeWebMapper();
		BundleContext bunCtx = getBundleContext();
		webMapper.connectLiftSceneInterface(bunCtx);
		webMapper.connectLiftInterface(bunCtx);
	}

	public void resetToDefaultConfig() {
		PumaConfigManager pcm = getConfigManager();
		BundleContext bc = getBundleContext();
		pcm.clearMainConfigRepoClient();
		// pcm.applyFreshDefaultMainRepoClientToGlobalConfig(bc);	
	}

	public void reloadBoneRobotConfig() {
		final PumaConfigManager pcm = getConfigManager();
		
		RepoClient rc = getOrMakeMainConfigRC();
		myBodyMgr.reloadAllBoneRobotConfigs(pcm, rc);
	}

	public void reloadGlobalConfig() {
		final PumaConfigManager pcm = getConfigManager();
		final PumaGlobalModeManager pgmm = pcm.getGlobalModeMgr();		
		RepoClient rc = getOrMakeMainConfigRC();
		// Below is needed for Lifter to obtain dependency from LifterLifecycle
		// Will revisit once repo functionality stabilizes a bit
		//PumaConfigManager.startRepoClientLifecyle(myBundleContext, rc);
		pgmm.startGlobalConfigService(myBundleContext);
	}

	protected void stopAndReleaseAllHumanoids() {
		myBehavMgr.stopAllAgents();
		myBodyMgr.disconnectAllBodies();
		RobotServiceFuncs.clearJointGroups();
		ModelBlendingRobotServiceContext.clearRobots();
		
		myBodyMgr.clear();
		// Oops - but they are STILL in the box-space!!!
	}

	protected void disconnectAllCharsAndMappers() throws Throwable {
		BundleContext bunCtx = getBundleContext();

		
			// Consider:  also set the context/registry vWorldMapper to null, expecting
			// PumaBooter or somesuch to find it again.
		
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
				resetToDefaultConfig();
			}

			// So NOW what we want to examine is the difference between the state right here, and the
			// state at this moment during a full "boot" sequence.
			connectAllBodies();

			//initCinema(true);

		} catch (Throwable t) {
			getLogger().error("Error attempting to reload all PUMA App config: ", t);
			// May be good to handle an exception by setting state of a "RebootResult" or etc...
		}
	}
	
	// Temporary method for testing goody/thing actions until the repo auto-update trigger features are alive
	/**
	 * Called from PumaContextCommandBox.processUpdateRequestNow(THING_ACTIONS)
	 */
	public void resetMainConfigAndCheckThingActions() {
		final PumaConfigManager pcm = getConfigManager();
		final PumaGlobalModeManager pgmm = pcm.getGlobalModeMgr();
		pcm.clearMainConfigRepoClient();
		GruesomeTAProcessingFuncs.processPendingThingActions();
		/*
		 * Old way, where the GlobalConfigEmitter was passed in explicitly on each update.
		 * That is not necessarily a bad approach!
		* 
		RepoClient rc = getOrMakeMainConfigRC();
		GlobalConfigEmitter gce = pgmm.getGlobalConfig();
		if (hasVWorldMapper()) {
			PumaVirtualWorldMapper vWorldMapper = getOrMakeVWorldMapper();
			vWorldMapper.updateVWorldEntitySpaces(rc, gce);
		}
		* 
		*/ 
	}
	
    
}
