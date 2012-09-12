/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.render.app.core;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.input.FlyByCamera;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.appdapter.core.name.Ident;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.blob.emit.RenderConfigEmitter;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.model.humanoid.HumanoidFigure;
import org.cogchar.render.opengl.optic.CameraMgr;
import org.cogchar.render.sys.core.RenderRegistryClient;
import org.cogchar.render.sys.physics.DemoVectorFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class CoreFeatureAdapter extends BasicDebugger {

	private ConfiguredPhysicalModularRenderContext myCPMRC;
	private static Logger logger = getLoggerForClass(CoreFeatureAdapter.class);

	protected CoreFeatureAdapter(ConfiguredPhysicalModularRenderContext cpmrc) {
	}

	static public void unrolledInitDRC(ConfiguredPhysicalModularRenderContext rctx) {
		RenderConfigEmitter rce = rctx.getConfigEmitter();
		String jme3GuiFontPath = rce.getFontPath();
		// This method is actually supplied by plain old CogcharRenderContext
		initGuiFont(rctx, jme3GuiFontPath);
	}

	static public void unrolledInitPRC(PhysicalModularRenderContext prc) {
		prc.initBulletAppState();
		//prc.enablePhysicsDebug();
		//prc.disablePhysicsDebug();
		prc.initPhysicsStuffBuilder();
	}

	static public void registerJMonkeyDefaultCameras(RenderRegistryClient rrc, Camera defCam, FlyByCamera fbc) {
		CameraMgr cm = rrc.getOpticCameraFacade(null);
		cm.registerCommonCamera(CameraMgr.CommonCameras.DEFAULT, defCam);
	}

	// Mainly for attaching cameras to parts of robot, but potentially somewhat general purpose so I'll leave it here
	static public void attachToHumanoidBone(HumanoidRenderContext hrc, final Node toAttach, Ident robotIdent, final String boneName) {
		final HumanoidFigure robot = hrc.getHumanoidFigure(robotIdent);
		if (robot == null) {
			logger.warn("Trying to attach node to humanoid bone, but robot " + robotIdent.getLocalName() + " not found");
		} else {
			hrc.enqueueCallable(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					// getBoneAttachmentsNode attaches things to the rootNode, so this next line must be enqueued for the main render thread. Convenient!
					Node attachToBone = robot.getBoneAttachmentsNode(boneName);
					attachToBone.attachChild(toAttach);
					return null;
				}
			});
		}
	}

	static public void initGuiFont(CogcharRenderContext crc, String fontPath) {
		RenderRegistryClient rrc = crc.getRenderRegistryClient();
		AssetManager amgr = rrc.getJme3AssetManager(null);
		BitmapFont defGuiFont = amgr.loadFont(fontPath);
		WorkaroundAppStub stub = crc.getAppStub();
		stub.setGuiFont(defGuiFont);
	}

	static public DemoVectorFactory getDemoVectoryFactory() {
		return new DemoVectorFactory();
	}
	// Not used from RDF config, but still used from demo.render.opengl

	static protected DirectionalLight makeDemoDirectionalLight(RenderRegistryClient rrc) {
		Vector3f dir = getDemoVectoryFactory().getUsualLightDirection();
		return rrc.getOpticLightFacade(null).makeWhiteOpaqueDirectionalLight(dir);
	}
	// Not used from RDF config, but still used from demo.render.opengl

	static public void addDemoDirLightToRootNode(CogcharRenderContext crc) {
		crc.addLightToRootNode(makeDemoDirectionalLight(crc.getRenderRegistryClient()));
	}
	// Not used from RDF config, but still used from demo.render.opengl

	static public void setupLight(CogcharRenderContext crc) {
		addDemoDirLightToRootNode(crc);
	}
}
