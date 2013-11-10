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
package org.cogchar.render.sys.context;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.input.FlyByCamera;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.appdapter.core.name.Ident;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.blob.emit.RenderConfigEmitter;
import org.cogchar.render.app.core.WorkaroundAppStub;
import org.cogchar.render.app.entity.CameraBinding;

import org.cogchar.render.opengl.optic.CameraMgr;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.cogchar.render.sys.physics.DemoVectorFactory;
import org.cogchar.render.sys.task.Queuer;

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
		//	prc.initPhysicsStuffBuilder();
	}

	static public void registerJMonkeyDefaultCameras(RenderRegistryClient rrc, Camera defCam, FlyByCamera fbc) {
		CameraMgr cm = rrc.getOpticCameraFacade(null);
		Queuer q = new Queuer(rrc);
		cm.registerDefaultCamera(defCam, q);
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
	
	static public void addLightToRootNode(CogcharRenderContext crc, Light l) {
		crc.addLightToRootNode(l);
	}
	
}
