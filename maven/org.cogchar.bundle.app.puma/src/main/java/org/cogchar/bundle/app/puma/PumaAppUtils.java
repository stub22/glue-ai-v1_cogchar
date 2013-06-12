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
import org.appdapter.core.name.Ident;
import org.cogchar.app.puma.config.PumaConfigManager;
import org.cogchar.app.puma.config.PumaGlobalModeManager;
import org.cogchar.app.puma.registry.PumaRegistryClient;
import org.cogchar.app.puma.registry.PumaRegistryClientFinder;
import org.cogchar.app.puma.web.PumaWebMapper;
import org.cogchar.blob.emit.GlobalConfigEmitter;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.impl.thing.basic.BasicThingActionRouter;
import org.osgi.framework.BundleContext;
import org.robokind.api.motion.Robot;
import org.cogchar.bind.rk.robot.motion.CogcharMotionSource;
import org.cogchar.blob.emit.BehaviorConfigEmitter;
import org.cogchar.impl.channel.AnimFileSpecReader;
import org.cogchar.impl.channel.FancyFile;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaAppUtils extends BasicDebugger {
	
	private static BasicThingActionRouter	theRouter;
	public static BasicThingActionRouter	getActionRouter() {
		if (theRouter == null) {
			theRouter = new BasicThingActionRouter();
		}
		return theRouter;
	}
	static class StuffRec {
		public PumaRegistryClientFinder prcFinder = new PumaRegistryClientFinder();
		public PumaRegistryClient pumaRegClient = prcFinder.getPumaRegClientOrNull(null, PumaRegistryClient.class);
		public final PumaConfigManager pcm = pumaRegClient.getConfigMgr(null);
		public final PumaGlobalModeManager pgmm = pcm.getGlobalModeMgr();
		public RepoClient rc = pcm.getMainConfigRepoClient();
		public GlobalConfigEmitter gce = pgmm.getGlobalConfig();
		public PumaWebMapper pwm = pumaRegClient.getWebMapper(null);
		private Ident animPathGraphID = rc.makeIdentForQName(AnimFileSpecReader.animGraphQN());
		public BehaviorConfigEmitter animBCE = new BehaviorConfigEmitter(rc, animPathGraphID);
	}
	public static void registerActionConsumers() { 
		StuffRec srec = new StuffRec();
		// The VWorld does its own registration in a separate ballet.
		// Here we are just handling the reg for Web + Behavior.

		BasicThingActionRouter router = getActionRouter();
		srec.pwm.registerActionConsumers(router, srec.rc, srec.gce);		
	}
	public static void processPendingThingActions() {
		StuffRec srec = new StuffRec();	
		BasicThingActionRouter router = getActionRouter();
		router.consumeAllActions(srec.rc);
	}
	public static List<FancyFile> getKnownAnimationFiles() { 
		StuffRec srec = new StuffRec();
		return AnimFileSpecReader.findAnimFileSpecsForJava(srec.animBCE);
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
