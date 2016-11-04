package org.cogchar.bundle.app.vworld.central;

import org.appdapter.core.name.Ident;
import org.cogchar.app.puma.boot.PumaSysCtx;
import org.cogchar.app.puma.config.PumaConfigManager;
import org.cogchar.app.puma.config.TriggerConfig;
import org.cogchar.bundle.app.vworld.busker.TriggerItems;
import org.cogchar.platform.trigger.BoxSpace;


/**
 * Created by Owner on 5/1/2016.
 */
public class VWCtxCmdBoxUpdatable extends VWCtxCmdBox {
	private PumaSysCtx myPSC;

	public VWCtxCmdBoxUpdatable(VWorldRegistry vr, BoxSpace boxSpc, Ident ctxID, PumaSysCtx psc) {
		super(vr, boxSpc, ctxID);
		myPSC = psc;
	}

	public void reloadCommandSpace()
	{
		TriggerConfig tcnf = myPSC.reloadCmdTrigConf();

		TriggerItems.populateCommandSpace(tcnf.getRepoClient(), tcnf.getCommandSpace(), tcnf.getBoxSpace());
	}
	/**	This simply forwards calls to myPSC, which is a PumaSysCtxImpl, currently (2016-04-27) required to be osgi-wired.
	 * Called only indirectly after scheduling by processUpdateRequestAsync() above
	 * above.
	 *
	 * @param request
	 * @param resetMainConfigFlag
	 * @return
	 */
	@Override protected boolean processUpdateRequestNow(String request, final boolean resetMainConfigFlag) {
		boolean successFlag = true;
		if (WORLD_CONFIG.equals(request.toLowerCase())) {
			myVWReg.initCinema(false, null);
		} else if (BONE_ROBOT_CONFIG.equals(request.toLowerCase())) {
			myPSC.reloadBoneRobotConfig();
		} else if (MANAGED_GCS.equals(request.toLowerCase())) {
			final PumaConfigManager pcm = myPSC.getSysCnfMgr().getConfigManager();
			pcm.clearOSGiComps();
			myPSC.getSysCnfMgr().reloadGlobalConfig();
		} else if (ALL_HUMANOID_CONFIG.equals(request.toLowerCase())) {
			// This also calls initCinema
			((PumaSysCtx.BootSupport) myPSC).reloadAll(resetMainConfigFlag);
			myVWReg.initCinema(false, null);
		} else if (THING_ACTIONS.equals(request.toLowerCase())) {
			myPSC.getSysCnfMgr().resetMainConfigAndCheckThingActions();
		} else {
			getLogger().warn("PumaSysCtxImpl did not recognize the config update to be performed: {}", request);
			successFlag = false;
		}
		return successFlag;
	}
	/*
	public RepoClient getMainConfigRepoClient() {
		PumaConfigManager pcm = myPSC.getSysCnfMgr().getConfigManager();
		return pcm.getMainConfigRepoClient();
	}
	*/
}
