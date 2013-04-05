/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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
import java.util.List;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.app.puma.config.PumaConfigManager;
import org.cogchar.app.puma.config.PumaGlobalModeManager;
import org.cogchar.app.puma.registry.PumaRegistryClient;
import org.cogchar.app.puma.registry.PumaRegistryClientFinder;
import org.cogchar.app.puma.vworld.PumaVirtualWorldMapper;
import org.cogchar.app.puma.web.PumaWebMapper;
import org.cogchar.blob.emit.GlobalConfigEmitter;
import org.appdapter.help.repo.RepoClient;
import org.osgi.framework.BundleContext;
import org.robokind.api.motion.Robot;
import org.cogchar.bind.rk.robot.motion.CogcharMotionSource;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaAppUtils extends BasicDebugger {

	public static void pumpGoodyUpdatesToVWorld() {
		PumaRegistryClientFinder prcFinder = new PumaRegistryClientFinder();
		PumaRegistryClient pumaRegClient = prcFinder.getPumaRegClientOrNull(null, PumaRegistryClient.class);
		PumaWebMapper pwm = pumaRegClient.getWebMapper(null);
		final PumaConfigManager pcm = pumaRegClient.getConfigMgr(null);
		final PumaGlobalModeManager pgmm = pcm.getGlobalModeMgr();
		RepoClient rc = pcm.getMainConfigRepoClient();
		GlobalConfigEmitter gce = pgmm.getGlobalConfig();
		PumaVirtualWorldMapper vWorldMapper = pumaRegClient.getVWorldMapper(null);
		vWorldMapper.updateGoodySpace(rc, gce);
	}
	public static 	void startMotionComputers(BundleContext bundleCtx) { 
		List<CogcharMotionSource> cogMotSrcList = CogcharMotionSource.findCogcharMotionSources(bundleCtx);
		for (CogcharMotionSource cms : cogMotSrcList) {
			Robot srcBot = cms.getRobot();
			Robot.Id srcBotID = srcBot.getRobotId();
			// getLogger().info("Found CogcharMotionSource for Robot-ID: " + srcBotID);
			DemoMotionComputer dmc = new DemoMotionComputer();
			cms.addJointComputer(dmc);
		}
	}
}