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
package org.cogchar.render.test;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
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


import org.cogchar.render.sys.context.CogcharRenderContext;
import org.cogchar.render.sys.context.ConfiguredPhysicalModularRenderContext;
import org.cogchar.render.sys.registry.RenderRegistryClient;

// import org.cogchar.render.app.bony.BonyGameFeatureAdapter;
import org.cogchar.render.sys.context.CoreFeatureAdapter;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class TrialBalloon extends CogcharPresumedApp {

	private		TempMidiBridge myTMB = new TempMidiBridge();
	private		CogcharRenderContext myCRC;

	public static void main(String[] args) {
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);
		TrialBalloon app = new TrialBalloon();
		app.start();
	}

	@Override public void start() {
		super.start();
		myTMB.initMidiRouter();
	}

	@Override protected CogcharRenderContext makeCogcharRenderContext() {
		TB_RenderContext rc = new TB_RenderContext();
		myCRC = rc;
		return rc;
	}

	@Override public void simpleInitApp() {
		super.simpleInitApp();
		flyCam.setMoveSpeed(20);
		initContent();
	}

	public void initContent() {
		Node myMainNode;
		myMainNode = new Node("my_main");
		rootNode.attachChild(myMainNode);
		//	BonyGameFeatureAdapter.initCrossHairs(settings, getRenderRegistryClient()); // a "+" in the middle of the screen to help aiming

		shedLight();
		String letters = "abcd\nABCD\nEFGHIKLMNOPQRS\nTUVWXYZ";
		String digits = "1\n234567890";
		String syms = "`~!@#$\n%^&*()-=_+[]\\;',./{}|:<>?";
		// This scale factor will be multiplied by the getRenderedSize value of the font, which is 17.0f
		// for the default font (on JDK6.Win7, YMMV).

		// Not really clear what "size" of the font means.  It
		// seems that if we want the rectangle to really contain

		BitmapText lettersBTS = makeTextSpatial(letters, 0.2f); // eff-scale 3.4f, wraps after 2-3 chars
		BitmapText digitsBTS = makeTextSpatial(digits, 0.1f);  // eff-scale 1.7f, wraps after 6 chars
		BitmapText symsBTS = makeTextSpatial(syms, 0.05f);   // eff-scale 1.05f, wraps after ~ 18 oddly shaped chars
		myMainNode.attachChild(lettersBTS);
		lettersBTS.move(10.0f, 10.0f, 10.0f);
		//lettersBTS.setSize(0.5f);	//digitsBTS.setSize(0.5f); //symsBTS.setSize(0.5f);
		myMainNode.attachChild(digitsBTS);
		myMainNode.attachChild(symsBTS);

		digitsBTS.move(-10f, -10f, -10f);
		BillboardControl bbCont = new BillboardControl();
		/**
		 * AxialY Aligns this Billboard to the screen, but keeps the Y axis fixed. 
		 * AxialZ Aligns this Billboard to the screen, but keeps the Z axis fixed. 
		 * Camera Aligns this Billboard to the camera position. 
		 * Screen Aligns this Billboard to the screen.
		 */
		bbCont.setAlignment(BillboardControl.Alignment.Screen);
		lettersBTS.addControl(bbCont);
		viewPort.setBackgroundColor(ColorRGBA.Blue);
	}

	private BitmapText makeTextSpatial(String txtB, float renderScale) {

		RenderRegistryClient rrc = myCRC.getRenderRegistryClient();
		TextMgr txtMgr = rrc.getSceneTextFacade(null);
		BitmapText txtSpatial = txtMgr.getScaledBitmapText(txtB, renderScale);

		BitmapFont bf = txtSpatial.getFont();
		float fontRenderedSize = bf.getCharSet().getRenderedSize();

		getLogger().info("Font rendered size={}", fontRenderedSize);
		// This action disables culling for *all* spatials made with the font, so it should really be happening
		// further out, in concert with font and material management.    Would using a material [cloned from 
		// font-mat, then cached] on our spatials make this  management cleaner?

		txtMgr.disableCullingForFont(bf);
		Rectangle rect = new Rectangle(0, 0, 6, 3);

		txtSpatial.setBox(rect);
		// If we want effective transparency or translucency, need to use this bucket, marking the object to be 
		// processed during the transparent object rendering pass.
		txtSpatial.setQueueBucket(RenderQueue.Bucket.Transparent);

		return txtSpatial;
	}

	private void shedLight() {

		ConfiguredPhysicalModularRenderContext cpmrc = (ConfiguredPhysicalModularRenderContext) getRenderContext();
		CoreFeatureAdapter.setupLight(cpmrc);
		shedMoreLight(cpmrc);
	}

	private void shedMoreLight(CogcharRenderContext crc) {
		RenderRegistryClient rrc = crc.getRenderRegistryClient();
		Vector3f otherLightDir = new Vector3f(0.1f, 0.7f, 1.0f).normalizeLocal();
		DirectionalLight odl = rrc.getOpticLightFacade(null).makeWhiteOpaqueDirectionalLight(otherLightDir);
		CoreFeatureAdapter.addLightToRootNode(crc, odl);
	}

	public class TB_RenderContext extends ConfiguredPhysicalModularRenderContext {
	}
}
