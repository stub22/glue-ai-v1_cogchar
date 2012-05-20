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

import java.util.Set;

import org.osgi.framework.BundleContext;

import org.appdapter.bind.rdf.jena.model.AssemblerUtils;
import org.appdapter.core.item.Ident;
import org.appdapter.core.item.FreeIdent;
import org.appdapter.core.log.BasicDebugger;


import org.cogchar.blob.emit.BonyConfigEmitter;
import org.cogchar.blob.emit.BehaviorConfigEmitter;

import org.cogchar.api.skeleton.config.BoneRobotConfig;

import org.cogchar.bind.rk.robot.client.RobotAnimClient;
import org.cogchar.bind.rk.speech.client.SpeechOutputClient;

import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.app.humanoid.SceneActions;

import org.cogchar.platform.trigger.DummyBox;
import org.cogchar.platform.trigger.DummyBinding;
import org.cogchar.platform.trigger.DummyBinder;
import org.cogchar.platform.trigger.DummyTrigger;

import org.cogchar.app.buddy.busker.TriggerItems;

import org.cogchar.impl.scene.BehaviorTrial;
import org.cogchar.impl.scene.Theater;
import org.cogchar.impl.scene.SceneBook;

import org.cogchar.impl.perform.ChannelNames;
import org.cogchar.impl.perform.FancyTextChan;

import org.cogchar.impl.trigger.FancyTriggerFacade;

/* This probably is only here for the short term - added so that we can initialize
 * lights and cameras (and webapp) from rdf via this class for testing
 */
import org.cogchar.api.scene.LightsCameraConfig;
import org.cogchar.render.opengl.optic.CameraMgr;
import org.cogchar.render.opengl.optic.LightFactory;
import org.cogchar.bind.lift.LiftConfig;
import org.cogchar.bind.lift.LiftAmbassador;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaDualCharacter extends BasicDebugger implements DummyBox {


	private SpeechOutputClient					mySOC;
	
	private	Ident								myCharIdent;
	private String								myNickName;
	
	private	PumaHumanoidMapper					myPHM;
	
	private ClassLoader							myInitialBonyRdfCL = org.cogchar.bundle.render.resources.ResourceBundleActivator.class.getClassLoader();
	
	public String								myUpdateBonyRdfPath;
	
	public Theater								myTheater;
	
	
	public PumaDualCharacter(HumanoidRenderContext hrc, BundleContext bundleCtx, Ident charIdent, String nickName) {
		myCharIdent = charIdent;
		myNickName = nickName;
		myPHM = new PumaHumanoidMapper(hrc, bundleCtx, charIdent);
		myTheater = new Theater();
	}
	public void connectBonyCharToRobokindSvcs(BundleContext bundleCtx) throws Throwable {

		BonyConfigEmitter bonyCE = myPHM.getHumanoidRenderContext().getBonyConfigEmitter();
		String bonyConfigPathTail = bonyCE.getBonyConfigPathTailForChar(myCharIdent);
		BehaviorConfigEmitter behavCE = bonyCE.getBehaviorConfigEmitter();
		String bonyConfigPathPerm = behavCE.getRKMotionPermPath(bonyConfigPathTail);
		myUpdateBonyRdfPath = behavCE.getRKMotionTempFilePath(bonyConfigPathTail);

		BoneRobotConfig brc = readBoneRobotConfig(bonyConfigPathPerm, myInitialBonyRdfCL);
		myPHM.initModelRobotUsingBoneRobotConfig(brc);

		/*
		 * Load cameras/lights config from charWorldConfig RDF resource. Obviously we don't want the path hardcoded here
		 * as it is currently. Do we want a new ConfigEmitter for this? Probably doesn't make sense to use the
		 * BonyConfigEmitter since we are separating this from BoneConfig. Also probably doesn't make sense to have the
		 * Turtle file in the rk_bind_config/motion/ path, but for the moment...
		 */
		LightsCameraConfig lcc = readLightsCameraConfig("rk_bind_config/motion/charWorldConfig.ttl", myInitialBonyRdfCL);
		CameraMgr cm = PumaRegistryOutlet.getCameraMgr();
		cm.initCamerasFromConfig(lcc, myPHM.getHumanoidRenderContext());
		LightFactory lf = PumaRegistryOutlet.getLightFactory();
		lf.initLightsFromConfig(lcc, myPHM.getHumanoidRenderContext());

		/*
		 * Load lift webapp config from liftConfig RDF resource, since this is the place for all the RDF loads
		 * currently!
		 */
		LiftConfig lc = (LiftConfig) readGeneralConfig("web/liftConfig.ttl", myInitialBonyRdfCL)[0];
		LiftAmbassador.storeControlsFromConfig(lc);


		// myPHM.initModelRobotUsingAvroJointConfig();
		myPHM.connectToVirtualChar();
		// myPHM.applyInitialBoneRotations();
		connectAnimOutChans();
	}
	private void connectAnimOutChans() { 
		FancyTextChan bestAnimOutChan = myPHM.getBestAnimOutChan();
		myTheater.registerChannel(bestAnimOutChan);
	}
	public void connectSpeechOutputSvcs(BundleContext bundleCtx) { 
		Ident speechChanIdent = ChannelNames.getOutChanIdent_SpeechMain();
		mySOC = new SpeechOutputClient(bundleCtx, speechChanIdent);
		myTheater.registerChannel(mySOC);		
	}
	public void loadBehaviorConfig(boolean useTempFiles) throws Throwable {
		// Currently we can only process
		String pathTail = "bhv_nugget_02.ttl";
		
		BonyConfigEmitter bonyCE = myPHM.getHumanoidRenderContext().getBonyConfigEmitter();
		// String bonyConfigPathTail = bonyCE.getBonyConfigPathTailForChar(myCharURI);
		BehaviorConfigEmitter behavCE = bonyCE.getBehaviorConfigEmitter();
		
		String behavPath = behavCE.getBehaviorPermPath(pathTail);
		if (useTempFiles) {
			behavPath = behavCE.getBehaviorTempFilePath(pathTail);
		}
		// true = Clear caches first
		boolean clearCachesFirst = true;
		ClassLoader optCLforJenaFM = null;
		myTheater.loadSceneBook(behavPath, optCLforJenaFM, clearCachesFirst);
	} 
	public void startTheater() {
		SceneBook sb = myTheater.getSceneBook();
		DummyBinder trigBinder = SceneActions.getBinder();
		
		FancyTriggerFacade.registerAllTriggers(trigBinder, myTheater, sb); 
		myTheater.startThread();
	}
	public void stopTheater() {
		// Should be long enough for the 100 Msec loop to cleanly exit.
		int killTimeWaitMsec = 200;
		myTheater.fullyStop(killTimeWaitMsec);
	}
	private void stopEverything() {
		logInfo("stopEverything - Stopping Theater.");
		stopTheater();
		logInfo("stopEverything - Stopping Anim Jobs.");
		myPHM.stopAndReset();
		logInfo("stopEverything - Stopping Speech-Output Jobs.");
		mySOC.cancelAllRunningSpeechTasks();
		
	}
	public void stopAndReset() {
		stopEverything();
		// TODO:  Send character to default positions.
		logInfo("stopAndReset - Restarting behavior theater.");
		startTheater();
		logInfo("stopAndReset - Complete.");
	}
	public void stopResetAndRecenter() {
		stopEverything();
		logWarning("stopResetAndRecenter - Recenter is not implemented yet!");
		logInfo("stopResetAndRecenter - Restarting behavior theater.");
		startTheater();
		logInfo("stopResetAndRecenter - Complete.");
	}
	public void registerDefaultSceneTriggers() { 
		for (int i=0; i < SceneActions.getSceneTrigKeyCount(); i++) {
			TriggerItems.SceneMsg smti = new TriggerItems.SceneMsg();
			smti.sceneInfo = "yowza " + i;
			registerTheaterBinding(i, smti);
		}
	}
	private void registerTheaterBinding(int sceneTrigIdx, DummyTrigger trig) {
		SceneActions.setTriggerBinding(sceneTrigIdx, myTheater, trig);
	}
	
//	private InputStream openAssetStream(String assetName) { 
//		return myBRC.openAssetStream(assetName);
//	}	
	public String getNickName() { 
		return myNickName;
	}
	public Ident getCharIdent() { 
		return myCharIdent;
	}
	public PumaHumanoidMapper getHumanoidMapper() { 
		return myPHM;
	}
	public void playDangerYogaTestAnim() { 
		myPHM.playDangerYogaTestAnim();
	}
	public void sayText(String txt) {
		// TODO:  Guard against concurrent activity through the channel/behavior systerm
		try {
			mySOC.speakText(txt);
		} catch (Throwable t) {
			logError("problem speaking", t);
		}
	}
	public void updateBonyConfig(String rdfConfigFlexPath, ClassLoader optRdfResourceCL) {
		try {
			BoneRobotConfig.Builder.clearCache();
			BoneRobotConfig brc = readBoneRobotConfig(rdfConfigFlexPath, optRdfResourceCL);
			myPHM.updateModelRobotUsingBoneRobotConfig(brc);
		} catch (Throwable t) {
			logError("problem updating bony config from flex-path[" + rdfConfigFlexPath + "]", t);
		}
	}
        
	public BoneRobotConfig readBoneRobotConfig(String rdfConfigFlexPath, ClassLoader optResourceClassLoader) {
		logInfo("Reading RDF for BoneRobotConfig");
		BoneRobotConfig brc = (BoneRobotConfig) readGeneralConfig(rdfConfigFlexPath, optResourceClassLoader)[0];
		return brc;
	}

	public LightsCameraConfig readLightsCameraConfig(String rdfConfigFlexPath, ClassLoader optResourceClassLoader) {
		logInfo("Reading RDF for LightsCameraConfig");
		LightsCameraConfig lcc = (LightsCameraConfig) readGeneralConfig(rdfConfigFlexPath, optResourceClassLoader)[0];
		return lcc;
	}

	// Added to hold code common to RDF config readers
	private Object[] readGeneralConfig(String rdfConfigFlexPath, ClassLoader optResourceClassLoader) {
		if (optResourceClassLoader != null) {
			logInfo("Ensuring registration of classLoader: " + optResourceClassLoader);
			AssemblerUtils.ensureClassLoaderRegisteredWithJenaFM(optResourceClassLoader);
		}
		logInfo("Loading triples from flex-path: " + rdfConfigFlexPath);
		Set<Object> loadedStuff = AssemblerUtils.buildAllObjectsInRdfFile(rdfConfigFlexPath);
		logInfo("Loaded " + loadedStuff.size() + " objects");
		for (Object o : loadedStuff) {
			logInfo("Loaded: " + o);
		}
		logInfo("=====================================================================");
		return loadedStuff.toArray();
	}

	@Override public String toString() { 
		return "PumaDualChar[uri=" + myCharIdent + ", nickName=" + myNickName + "]";
	}	
	public void usePermAnims() {
		logWarning("usePermAnims() not implemented yet");
	}
	public void useTempAnims() {
		logWarning("useTempAnims() not implemented yet");
	}

}
