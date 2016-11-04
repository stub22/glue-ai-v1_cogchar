package org.cogchar.app.puma.boot;

import org.appdapter.core.name.Ident;
import org.appdapter.fancy.rclient.RepoClient;
import org.cogchar.app.puma.body.PumaDualBody;
import org.cogchar.app.puma.config.PumaConfigManager;
import org.cogchar.app.puma.config.PumaContextMediator;
import org.cogchar.app.puma.config.PumaGlobalModeManager;
import org.cogchar.app.puma.web.PumaWebMapper;
import org.osgi.framework.BundleContext;

import org.cogchar.blob.emit.GlobalConfigEmitter;

import java.util.List;

/**
 * Created by Owner on 5/1/2016.
 */
public class PumaSysCtxImplBootable extends PumaSysCtxImpl implements  PumaSysCtx.BootSupport {
	public PumaSysCtxImplBootable(BundleContext bc, PumaContextMediator mediator, Ident ctxID) {
		super(bc, mediator, ctxID);
	}
	@Override public void startRepositoryConfigServices() {
		PumaConfigManager pcm = getInnerConfigManager();
		PumaContextMediator mediator = myRegClient.getCtxMediator(null);
		// This would happen by default anyway, if there were not already a MainConfigRepoClient in place.
		pcm.applyDefaultRepoClientAsMainConfig(mediator, myBundleContext);
		// This method performs the configuration actions associated with the developmental "Global Mode" concept
		// If/when "Global Mode" is replaced with a different configuration "emitter", the method(s) here will
		// be updated to reflect that
		RepoClient rc = getMainConfigRC();
		PumaGlobalModeManager pgmm = pcm.getGlobalModeMgr();
		pgmm.applyGlobalConfig(myBundleContext, rc);

		myBehavMgr.initConfigLinks(myRegClient);
	}


	@Override public void connectAllBodies() {
		List<Ident> charIDs = getAllCharIdents();
		connectDualBodies(charIDs);
		makeAgentsForAllBodies(charIDs);
	}

	protected void makeAgentsForAllBodies(List<Ident> charIdents) {
		BundleContext bunCtx = getBundleContext();
		final PumaConfigManager pcm = getInnerConfigManager();
		final PumaGlobalModeManager pgmm = pcm.getGlobalModeMgr();
		GlobalConfigEmitter gce = pgmm.getGlobalConfig();

		for (Ident charID : charIdents) {
			PumaDualBody pdb = myBodyMgr.getBody(charID);
			if (pdb != null) {
				getLogger().info("Making agent for char={} and body={} ", charID, pdb);
				myBehavMgr.makeAgentForBody(bunCtx, myRegClient, pdb, charID);
			}
		}
	}
	@Override public void connectWeb() {
		PumaWebMapper webMapper = getOrMakeWebMapper();
		BundleContext bunCtx = getBundleContext();
		webMapper.connectLiftSceneInterface(bunCtx);
		webMapper.connectAvailableCommands(bunCtx);
	}
	private PumaWebMapper getOrMakeWebMapper() {
		PumaWebMapper pwm = myRegClient.getWebMapper(null);
		if (pwm == null) {
			pwm = new PumaWebMapper(myPCCB);
			myRegClient.putWebMapper(pwm, null);
		}
		pwm.attachContext(myBundleContext);
		return pwm;
	}
	@Override public void reloadAll(boolean resetMainConfigFlag) {
		try {
			// BundleContext bunCtx = getBundleContext();
			// Here we make the cute assumption that vWorldMapper or webMapper would be null
			// if we weren't using those features.  Only problem is ...that is not true.

			disconnectAllCharsAndMappers();

			// NOW we are ready to load any new config.
			if (resetMainConfigFlag) {
				resetToDefaultConfig();
			}

			// So NOW what we want to examine is the difference between the state right here, and the
			// state at this moment during a full "boot" sequence.
			connectAllBodies();

		} catch (Throwable t) {
			getLogger().error("Error attempting to reload all PUMA App config: ", t);
			// May be good to handle an exception by setting state of a "RebootResult" or etc...
		}
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

}
