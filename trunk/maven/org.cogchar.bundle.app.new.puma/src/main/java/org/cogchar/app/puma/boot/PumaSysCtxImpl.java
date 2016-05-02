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
import org.appdapter.fancy.rclient.RepoClient;


import org.cogchar.api.humanoid.FigureConfig;
import org.cogchar.impl.thing.route.BasicThingActionRouter;
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
import org.cogchar.app.puma.registry.PumaRegistryClientFinder;
import org.cogchar.app.puma.registry.ResourceFileCategory;
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
public class PumaSysCtxImpl extends BasicDebugger implements PumaSysCtx {

	protected PumaRegistryClient				myRegClient;
	private PumaSysCnfMgr					mySysCnfMgr;

	private OSGiComponent					myRegClientOSGiComp;
	
	protected BundleContext					myBundleContext;

	protected	PumaDualBodyManager				myBodyMgr;
	
	protected PumaBehaviorManager				myBehavMgr;
	
	protected	PumaContextCommandBox			myPCCB;
    
    private ArrayList<BodyHandleRecord>		myBodyHandleRecs;

	// bc only really gets used for starting the PumaRegistryClient lifecycle.
	public PumaSysCtxImpl(BundleContext bc, PumaContextMediator mediator, Ident ctxID) {
		// bc is optional for this impl, may be null, and is not actually used.
		myRegClient = new PumaRegistryClientImpl(bc, mediator);
		// bc may be used by lifecycles in here.
		mySysCnfMgr = new PumaSysCnfMgrImpl(myRegClient, bc);
		advertisePumaRegClient(myRegClient);
		myBundleContext = bc;
        
        //Keeping thse commented out until the PumaContextCommandBox in this project is completely unwired. 
		//BoxSpace bs = myRegClient.getTargetBoxSpace(null);
		myPCCB = new PumaContextCommandBox(this);
		//bs.addBox(ctxID, myPCCB);
		
		myBodyMgr = new PumaDualBodyManager();
		myBehavMgr = new PumaBehaviorManager();
        myBodyHandleRecs= new ArrayList<BodyHandleRecord>();
        
        ServiceLifecycleProvider<PumaRegistryClient> lifecycle =
                new SimpleLifecycle<PumaRegistryClient>(myRegClient,PumaRegistryClient.class.getName());
//            Properties props=new Properties();
//            props.put("theRegistryClient","theRegistryClient");
            ManagedService<PumaRegistryClient> ms = new OSGiComponent<PumaRegistryClient>(myBundleContext, lifecycle, null);
            ms.start();
        
	}

	protected PumaConfigManager getInnerConfigManager() {
		return mySysCnfMgr.getConfigManager();
	}
	@Override public PumaSysCnfMgr getSysCnfMgr() {
		return mySysCnfMgr;
	}

	protected RepoClient getMainConfigRC() {
		return mySysCnfMgr.getMainConfigRC();
	}
	private void advertisePumaRegClient(PumaRegistryClient prc) {
		PumaRegistryClientFinder prcFinder = new PumaRegistryClientFinder();
		prcFinder.registerPumaRegClient(prc, null, PumaSysCtxImpl.class);
	}

	protected BundleContext getBundleContext() {
		return myBundleContext;
	}

	protected boolean hasWebMapper() {
		return (myRegClient.getWebMapper(null) != null);
	}

	protected PumaContextMediator getMediator() {
		return myRegClient.getCtxMediator(null);
	}


	/**
	 * Third (and last) stage init of OpenGL, and all other systems. Done AFTER startOpenGLCanvas().
	 *
	 * @return
	 * @throws Throwable
	 */

	protected List<Ident> getAllCharIdents() {
		final PumaConfigManager pcm = getInnerConfigManager();
		final PumaGlobalModeManager pgmm = pcm.getGlobalModeMgr();
		GlobalConfigEmitter gce = pgmm.getGlobalConfig();
		// RepoClient rc = getOrMakeMainConfigRC();
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
		final PumaConfigManager pcm = getInnerConfigManager();
		final PumaGlobalModeManager pgmm = pcm.getGlobalModeMgr();
		GlobalConfigEmitter gce = pgmm.getGlobalConfig();
		RepoClient rc = getMainConfigRC();
		
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
                new SimpleLifecycle<ArrayList>(myBodyHandleRecs,ArrayList.class.getName());
            Properties props=new Properties();
            props.put("bodyConfigSpec","bodyConfigSpec");
            ManagedService<ArrayList> ms = new OSGiComponent<ArrayList>(myBundleContext, lifecycle, props);
            ms.start();
            
            ClassLoader vizResCL = getSingleClassLoaderOrNull(ResourceFileCategory.RESFILE_OPENGL_JME3_OGRE);
            
		}
	}

	protected PumaDualBody connectDualBody(FigureConfig humCfg, Ident graphIdentForBony) throws Throwable {
		Ident bonyCharID = humCfg.getFigureID();
		BundleContext bunCtx = getBundleContext();
		RepoClient rc = getMainConfigRC();
        //bodyConfigSpecs.add(new BodyConfigSpec(rc, bonyCharID, humCfg));
		PumaDualBody pdb = new PumaDualBody(bonyCharID, humCfg.getNickname());
		// Create and publish a BodyHandleRecord so that other systems can discover this body.
		BodyHandleRecord bConfig = new BodyHandleRecord(rc, graphIdentForBony, humCfg);
        pdb.setBodyConfigSpec(bConfig);
        pdb.absorbContext(myRegClient, bunCtx, rc, humCfg, graphIdentForBony);
		myBodyHandleRecs.add(bConfig);
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
	@Override public TriggerConfig reloadCmdTrigConf() {
		// final PumaConfigManager pcm = getInnerConfigManager();
		RepoClient repoCli = getMainConfigRC();
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
	 *	3)  PumaSysCtxImpl.reloadAll
	 *		PumaContextCommandBox.processUpdateRequestNow(ALL_HUMANOID_CONFIG)
	 * 
 * @param clearFirst 
 */


	/**
	 * Called only from 	 PumaBooter.pumaBootUnsafeUnderOSGi
	 */


	protected void resetToDefaultConfig() {
		PumaConfigManager pcm = getInnerConfigManager();
		BundleContext bc = getBundleContext();
		pcm.clearMainConfigRepoClient();
		// pcm.applyFreshDefaultMainRepoClientToGlobalConfig(bc);	
	}


	protected void stopAndReleaseAllHumanoids() {
		myBehavMgr.stopAllAgents();
		myBodyMgr.disconnectAllBodies();
		RobotServiceFuncs.clearJointGroups();
		ModelBlendingRobotServiceContext.clearRobots();
		
		myBodyMgr.clear();
		// Oops - but they are STILL in the box-space!!!
	}

	@Override  public void reloadBoneRobotConfig() {
		final PumaConfigManager pcm = getInnerConfigManager();

		RepoClient rc = getMainConfigRC();
		myBodyMgr.reloadAllBoneRobotConfigs(pcm, rc);
	}

}
