package org.cogchar.app.puma.boot;

import org.appdapter.fancy.rclient.RepoClient;
import org.cogchar.app.puma.config.PumaConfigManager;

/**
 * Created by Owner on 5/1/2016.
 */
public interface PumaSysCnfMgr {
	public PumaConfigManager getConfigManager();

	public void reloadGlobalConfig();

	public void resetMainConfigAndCheckThingActions();

	public RepoClient getMainConfigRC();
}
