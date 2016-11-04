package org.cogchar.app.puma.boot;

import org.appdapter.fancy.rclient.RepoClient;
import org.cogchar.app.puma.config.PumaConfigManager;
import org.cogchar.app.puma.config.PumaContextMediator;
import org.cogchar.app.puma.config.PumaGlobalModeManager;
import org.cogchar.app.puma.registry.PumaRegistryClient;
import org.cogchar.bundle.app.puma.GruesomeTAProcessingFuncs;
import org.osgi.framework.BundleContext;

/**
 * Created by Owner on 5/1/2016.
 */
public class PumaSysCnfMgrImpl implements PumaSysCnfMgr {
	private PumaRegistryClient 		myRegClient;
	private BundleContext 			myOptBundCtxForLifecycle;
//	private PumaContextMediator		myMediator;
	public PumaSysCnfMgrImpl(PumaRegistryClient regCli, BundleContext optBundCtx) { // , PumaContextMediator mediator) {
		myRegClient = regCli;
		myOptBundCtxForLifecycle = optBundCtx;
		// myMediator = mediator;
	}
	@Override public PumaConfigManager getConfigManager() {
		return myRegClient.getConfigMgr(null);
	}
	protected RepoClient getOrMakeMainConfigRC() {
		final PumaConfigManager pcm = getConfigManager();
		PumaContextMediator mediator =  myRegClient.getCtxMediator(null); // getMediator();
		RepoClient repoCli = pcm.getOrMakeMainConfigRepoClient(mediator, myOptBundCtxForLifecycle);
		return repoCli;
	}

	@Override public RepoClient getMainConfigRC() {
		return getOrMakeMainConfigRC();
	}

	@Override public void reloadGlobalConfig() {
		final PumaConfigManager pcm = getConfigManager();
		final PumaGlobalModeManager pgmm = pcm.getGlobalModeMgr();
		RepoClient rc = getOrMakeMainConfigRC();
		// Below is needed for Lifter to obtain dependency from LifterLifecycle
		// Will revisit once repo functionality stabilizes a bit
		//PumaConfigManager.startRepoClientLifecycle(myBundleContext, rc);
		pgmm.startGlobalConfigService(myOptBundCtxForLifecycle);
	}
	// Temporary method for testing goody/thing actions until the repo auto-update trigger features are alive
	/**
	 * Called from PumaContextCommandBox.processUpdateRequestNow(THING_ACTIONS)
	 */
	@Override public void resetMainConfigAndCheckThingActions() {
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
