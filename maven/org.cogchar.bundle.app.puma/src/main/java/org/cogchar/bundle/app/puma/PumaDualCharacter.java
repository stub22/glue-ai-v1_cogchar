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



import org.cogchar.bind.rk.robot.client.RobotAnimClient;


import org.cogchar.bind.rk.robot.config.BoneRobotConfig;
import org.cogchar.render.opengl.bony.demo.HumanoidRenderContext;


import org.cogchar.bind.rk.speech.client.SpeechOutputClient;
import org.cogchar.platform.trigger.DummyBox;

import org.appdapter.bind.rdf.jena.model.AssemblerUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaDualCharacter implements DummyBox {
	
	static Logger theLogger = LoggerFactory.getLogger(PumaDualCharacter.class);

	private	RobotAnimClient						myRAC;
	private SpeechOutputClient					mySOC;
	
	private	String								myCharURI;
	
	private	PumaHumanoidMapper					myPHM;
	
	public PumaDualCharacter(HumanoidRenderContext hrc, BundleContext bundleCtx, String charURI) {
		myCharURI = charURI;
		myPHM = new PumaHumanoidMapper(hrc, bundleCtx, charURI);
	}
	public void connectBonyCharToRobokindSvcs(BundleContext bundleCtx) throws Throwable {
		String rdfFilePath = BoneRobotConfig.DEV_TEST_RDF_PATH;
		ClassLoader rdfOptionalCL = null;
		
		BoneRobotConfig brc = readBoneRobotConfig(rdfFilePath, rdfOptionalCL);
		myPHM.initModelRobotUsingBoneRobotConfig(brc);
		// myPHM.initModelRobotUsingAvroJointConfig();
		myPHM.connectToVirtualChar();
		// myPHM.applyInitialBoneRotations();
		myRAC = new RobotAnimClient(bundleCtx); 
		mySOC = new SpeechOutputClient(bundleCtx);
	}
//	private InputStream openAssetStream(String assetName) { 
//		return myBRC.openAssetStream(assetName);
//	}	

	public void triggerTestAnim() { 
		try {
			myRAC.createAndPlayTestAnim();
		} catch (Throwable t) {
			theLogger.error("problem playing test anim", t);
		}
	}
	public void sayText(String txt) {
		try {
			mySOC.speakText(txt);
		} catch (Throwable t) {
			theLogger.error("problem speaking", t);
		}
	}
	public void updateBonyConfig(String rdfConfigFlexPath, ClassLoader optRdfResourceCL) {
		try {
			BoneRobotConfig brc = readBoneRobotConfig(rdfConfigFlexPath, optRdfResourceCL);
			myPHM.updateModelRobotUsingBoneRobotConfig(brc);
		} catch (Throwable t) {
			theLogger.error("problem updating bony config from flex-path[" + rdfConfigFlexPath + "]", t);
		}
	}
	public String toString() { 
		return "PumaDualChar[" + myCharURI + "]";
	}
	public BoneRobotConfig readBoneRobotConfig(String rdfConfigFlexPath, ClassLoader optResourceClassLoader) {
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
		Object[] loadedObjs = loadedStuff.toArray();
		BoneRobotConfig brc = (BoneRobotConfig) loadedObjs[0];
		return brc;
	}
	private void logInfo(String txt) { 
		theLogger.info(txt);
	}	
}
