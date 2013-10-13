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

package org.cogchar.lib.render.goody;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.control.BillboardControl;
import org.cogchar.render.app.core.CogcharPresumedApp;
import org.cogchar.render.opengl.scene.TextMgr;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.ViewPort;
import com.jme3.input.FlyByCamera;
import java.util.Random;

import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.api.thing.ThingActionSpec;
import org.cogchar.api.vworld.GoodyActionParamWriter;
import org.cogchar.impl.thing.basic.BasicThingActionSpec;
import org.cogchar.impl.thing.basic.BasicTypedValueMap;
import org.cogchar.name.goody.GoodyNames;

import org.cogchar.render.app.core.CogcharPresumedApp;
import org.cogchar.render.goody.basic.GoodyBox;
import org.cogchar.render.goody.basic.VirtualFloor;
import org.cogchar.render.goody.bit.BitBox;
import org.cogchar.render.goody.bit.BitCube;
import org.cogchar.render.goody.bit.TicTacGrid;
import org.cogchar.render.goody.bit.TicTacMark;
import org.cogchar.render.goody.flat.CrossHairGoody;
import org.cogchar.render.goody.flat.ScoreBoardGoody;
import org.cogchar.render.goody.flat.TextGoody;
import org.cogchar.render.optic.goody.VWorldCameraEntity;
import org.cogchar.render.sys.context.CogcharRenderContext;
import org.cogchar.render.sys.context.ConfiguredPhysicalModularRenderContext;


import org.cogchar.render.sys.context.CoreFeatureAdapter;
import org.cogchar.render.sys.registry.RenderRegistryClient;

import static org.cogchar.name.goody.GoodyNames.*;
import org.cogchar.render.app.entity.GoodySpace;
import org.cogchar.render.sys.goody.GoodyModularRenderContext;
import org.cogchar.render.sys.goody.GoodyRenderRegistryClient;
import org.cogchar.render.sys.goody.GoodyRenderRegistryClientImpl;

import org.cogchar.blob.emit.RenderConfigEmitter;
import org.cogchar.name.dir.NamespaceDir;
import org.cogchar.platform.gui.keybind.KeyBindingConfig;
import org.cogchar.platform.trigger.CommandSpace;
import org.cogchar.render.app.bony.BonyVirtualCharApp;
import org.cogchar.render.app.entity.GoodyFactory;
import org.cogchar.render.app.entity.VWorldEntityActionConsumer;
import org.cogchar.render.sys.input.VW_InputBindingFuncs;


/**
 * @author Stu B. <www.texpedient.com>
 * 
 * Goal is to use the Goody-ThingAction API to directly (without a repo) make each of the Goody types display and do 
 * each of its "moves", in a way that is straightforward to check visually, allowing us to check position, size, 
 * color of everything displayed.
 */

public class GoodyRenderTestApp extends BonyVirtualCharApp<GoodyModularRenderContext> {

	public static void main(String[] args) {
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);
		RenderConfigEmitter rce = new RenderConfigEmitter();
		GoodyRenderTestApp app = new GoodyRenderTestApp(rce);
		app.start();
	}
	public GoodyRenderTestApp(RenderConfigEmitter rce) { 
		super(rce);
	}
	@Override protected GoodyModularRenderContext makeCogcharRenderContext() {
		GoodyRenderRegistryClient grrc = new GoodyRenderRegistryClientImpl();
		RenderConfigEmitter rce = getConfigEmitter();
		GoodyModularRenderContext gmrc = new GoodyModularRenderContext(grrc, rce);
		gmrc.setApp(this);
		return gmrc;
	}
	@Override public void simpleInitApp() {
		getLogger().info("Hooray for Goodies!");
		super.simpleInitApp();
		FlyByCamera fbc = getFlyByCamera();
		fbc.setMoveSpeed(20);
				
		GoodyModularRenderContext renderCtx = getBonyRenderContext();
		GoodyRenderRegistryClient grrc = renderCtx.getGoodyRenderRegistryClient();
		GoodyFactory gFactory = GoodyFactory.createTheFactory(grrc, renderCtx);	
		
		initContent();
		// hideJmonkeyDebugInfo();
	}

	private void initContent() {
		ViewPort  pvp = getPrimaryAppViewPort();		
		pvp.setBackgroundColor(ColorRGBA.Blue);
		shedLight();
		// Hook-in for Goody system
		
		LocalGoodyHarness lgh = new LocalGoodyHarness();
		
		LocalGoodyHarness.GARecipe gar01 = new LocalGoodyHarness.GARecipe();
		
		gar01.entityID =  new FreeIdent(NamespaceDir.CCRT_NS +  "ttg_01");
		
		gar01.entityTypeID = GoodyNames.TYPE_TICTAC_GRID;
		gar01.verbID = GoodyNames.ACTION_CREATE;
		gar01.colorG = gar01.colorA = 1.0f;
		gar01.scaleX = gar01.scaleY = gar01.scaleZ = 3.0f;
		
		lgh.makeActionSpecAndSend(gar01);
		
	//	GoodyRenderTestContent grtc = new GoodyRenderTestContent();
		// GoodySpace gSpace = getGoodySpace();
		// hrwMapper.addHumanoidGoodies(veActConsumer, hrc);		
	}
	

	private void shedLight() {

		// ConfiguredPhysicalModularRenderContext cpmrc = (ConfiguredPhysicalModularRenderContext) getRenderContext();
		CogcharRenderContext cpmrc = getRenderContext();
		CoreFeatureAdapter.setupLight(cpmrc);
		shedMoreLight(cpmrc);
	}

	private void shedMoreLight(CogcharRenderContext crc) {
		RenderRegistryClient rrc = crc.getRenderRegistryClient();
		Vector3f otherLightDir = new Vector3f(0.1f, 0.7f, 1.0f).normalizeLocal();
		DirectionalLight odl = rrc.getOpticLightFacade(null).makeWhiteOpaqueDirectionalLight(otherLightDir);
		CoreFeatureAdapter.addLightToRootNode(crc, odl);
	}
	/*
	public void refreshInputBindingsAndHelpScreen(KeyBindingConfig keyBindConfig, CommandSpace cspace) {
		RenderRegistryClient rrc = getRenderRegistryClient();
		VW_InputBindingFuncs.setupKeyBindingsAndHelpScreen(rrc, keyBindConfig, getAppStub(), 
					getJMonkeyAppSettings(), cspace);
	}	
	*/
}
