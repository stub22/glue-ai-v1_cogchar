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

import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector3f;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.light.Light;

import com.jme3.asset.AssetManager;
import com.jme3.renderer.Camera;

import org.appdapter.core.log.BasicDebugger;
import org.cogchar.blob.emit.DemoConfigEmitter;
import org.cogchar.render.opengl.optic.CameraMgr;
import org.cogchar.render.opengl.scene.DeepSceneMgr;
import org.cogchar.render.sys.core.RenderRegistryClient;
import org.cogchar.render.sys.physics.DemoVectorFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class CoreFeatureAdapter extends BasicDebugger {
	private ConfiguredPhysicalModularRenderContext		myCPMRC;
	
	protected CoreFeatureAdapter(ConfiguredPhysicalModularRenderContext cpmrc) {
		
	}
	static public void unrolledInitDRC(ConfiguredPhysicalModularRenderContext drc) {
		DemoConfigEmitter dce = drc.getConfigEmiiter();
		String jme3GuiFontPath = dce.getFontPath();
		// This method is actually supplied by plain old CogcharRenderContext
		initGuiFont(drc, jme3GuiFontPath);
	}
	
	static public void unrolledInitPRC(PhysicalModularRenderContext prc) {
		prc.initBulletAppState();
		//prc.enablePhysicsDebug();
		//prc.disablePhysicsDebug();
		prc.initPhysicsStuffBuilder();
	}
	static public void registerJMonkeyDefaultCameras(RenderRegistryClient rrc,  Camera defCam, FlyByCamera fbc) {
		CameraMgr cm  = rrc.getOpticCameraFacade(null);
		cm.registerCommonCamera(CameraMgr.CommonCameras.DEFAULT, defCam);
	}
        

	// Used by CameraMgr can create new viewports for cameras loaded from RDF
	static public void addViewPort(RenderRegistryClient rrc, String label, Camera c) {
		DeepSceneMgr dsm = rrc.getSceneDeepFacade(null);
		dsm.addViewPort(label, c);
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
