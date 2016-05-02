package org.cogchar.app.puma.boot;

import org.cogchar.app.puma.config.PumaConfigManager;
import org.cogchar.app.puma.config.TriggerConfig;

/**
 * Created by Owner on 5/1/2016.
 */
public interface PumaSysCtx {
	public void reloadBoneRobotConfig();

	public PumaSysCnfMgr getSysCnfMgr();

	public TriggerConfig reloadCommandSpace();

	public static interface BootSupport extends PumaSysCtx {
		public void startRepositoryConfigServices();
		public void connectAllBodies();
		public void connectWeb();
		public void reloadAll(boolean resetMainConfigFlag);
	}
}
