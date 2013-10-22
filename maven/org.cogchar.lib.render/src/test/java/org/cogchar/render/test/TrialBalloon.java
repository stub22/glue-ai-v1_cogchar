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
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Quad;
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

	private		TempMidiBridge			myTMB = new TempMidiBridge();
	// In this test, we have the luxury of knowing the exact class of our associated context.
	private		TB_RenderContext		myTBRC;

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
	// This is an important setup callback, linking us in to the Cogchar rendering abstraction layer.
	// We minimize our dependence on JME3 by coding against the Cogchar RenderContext APIs, rather than
	// in our "Application" class (TrialBalloon, in this case).
	@Override protected CogcharRenderContext makeCogcharRenderContext() {
		getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^ Making CogcharRenderContext");
		TB_RenderContext rc = new TB_RenderContext();
		myTBRC = rc;
		return rc;
	}

	@Override public void simpleInitApp() {
		getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^ Calling super.simpleInitApp()");
		super.simpleInitApp();
		getLogger().info("^^^^^^^^^^^^^^^^^^^^^^^^ Returned from super.simpleInitApp()");
		// At this point, the R
		flyCam.setMoveSpeed(20);
		initContent3D();
		initContent2D();
	}
		String letters = "abcd\nABCD\nEFGHIKLMNOPQRS\nTUVWXYZ";
		String digits = "1\n234567890";
		String syms = "`~!@#$\n%^&*()-=_+[]\\;',./{}|:<>?";
		
	public void initContent3D() {
		Node myMainNode;
		myMainNode = new Node("my_main");
		rootNode.attachChild(myMainNode);
		//	BonyGameFeatureAdapter.initCrossHairs(settings, getRenderRegistryClient()); // a "+" in the middle of the screen to help aiming

		shedLight();

		// This scale factor will be multiplied by the getRenderedSize value of the font, which is 17.0f
		// for the default font (on JDK6.Win7, YMMV).

		// Not really clear what "size" of the font means.  It
		// seems that if we want the rectangle to really contain

		BitmapText lettersBTS = makeTextSpatial(letters, 0.2f, RenderQueue.Bucket.Transparent, 6); // eff-scale 3.4f, wraps after 2-3 chars
		BitmapText digitsBTS = makeTextSpatial(digits, 0.1f, RenderQueue.Bucket.Transparent, 6);  // eff-scale 1.7f, wraps after 6 chars
		BitmapText symsBTS = makeTextSpatial(syms, 0.05f, RenderQueue.Bucket.Transparent, 6);   // eff-scale 1.05f, wraps after ~ 18 oddly shaped chars
		myMainNode.attachChild(lettersBTS);
		lettersBTS.move(3.0f, 3.0f, -50.0f);
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
	public void initContent2D () {
		Node myGuiNode = new Node("my_gui");
		
		guiNode.attachChild(myGuiNode);
		guiNode.setLocalTranslation(20.0f, 40.0f, 0.0f);
		BitmapText flatDigitsBTS = makeTextSpatial(digits, 1.0f, RenderQueue.Bucket.Gui, 30);
		
		flatDigitsBTS.setQueueBucket(RenderQueue.Bucket.Gui); // Inherit, Opaque, Trans{parent, lucent}, ...
		// flatDigitsBTS.move(20.0f, 20.0f, 0.0f);
		flatDigitsBTS.setLocalTranslation(200.0f, 60.0f, 0.0f);
		myGuiNode.attachChild(flatDigitsBTS);
		
		RenderRegistryClient rrc = myTBRC.getRenderRegistryClient();
		// rrc.getSceneFlatFacade(null).detachAllOverlays();
		BitmapText bt = rrc.getSceneTextFacade(null).getScaledBitmapText("X+Y", 2.0f);
		// Text is rendered downward and right from the origin (local 0.0,0.0) position of the spatial.
		// Thus overlay text is "off the screen" unless the Y-coordinate of the spatial is high enough.
		// If positioned at y=10.0f, we can just barely see the top edge of the first line of text.
		bt.setLocalTranslation(300.0f, 250.0f, 0.0f);
		// guiNode.attachChild(crossBT);  is equiv to   rrc.getSceneFlatFacade(null).attachOverlaySpatial(crossBT);	
		myGuiNode.attachChild(bt);
		
		Material unshMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        unshMat.setColor("Color", new ColorRGBA(0,1.0f,0,0.5f));
		
	/*
	 // http://hub.jmonkeyengine.org/javadoc/com/jme3/material/RenderState.BlendMode.html
	  
	Additive - Additive blending.
Alpha - Alpha blending, interpolates to source color from dest color using source alpha.
AlphaAdditive -      Additive blending that is multiplied with source alpha.
Color -     Color blending, blends in color from dest color using source color.
Modulate -   Multiplies the source and dest colors.
ModulateX2 -  Multiplies the source and dest colors then doubles the result.
Off - No blending mode is used.
PremultAlpha -  Premultiplied alpha blending, for use with premult alpha textures.
	 */
        unshMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
		Geometry quadGeo = new Geometry("wideQuad", new Quad(200, 100));
		quadGeo.setMaterial(unshMat);
		
		quadGeo.setCullHint(Spatial.CullHint.Never); // Others are CullHint.Always, CullHint.Inherit
		quadGeo.setLocalTranslation(50.0f, 300.0f, -1.0f);
		myGuiNode.attachChild(quadGeo);
		
	}

	private BitmapText makeTextSpatial(String txtB, float renderScale, RenderQueue.Bucket bucket, int rectWidth) {

		RenderRegistryClient rrc = myTBRC.getRenderRegistryClient();
		TextMgr txtMgr = rrc.getSceneTextFacade(null);
		BitmapText txtSpatial = txtMgr.getScaledBitmapText(txtB, renderScale);

		BitmapFont bf = txtSpatial.getFont();
		float fontRenderedSize = bf.getCharSet().getRenderedSize();

		getLogger().info("Font rendered size={}", fontRenderedSize);
		// This action disables culling for *all* spatials made with the font, so it should really be happening
		// further out, in concert with font and material management.    Would using a material [cloned from 
		// font-mat, then cached] on our spatials make this  management cleaner?

		txtMgr.disableCullingForFont(bf);
		
		// This rectangle controls how the text is wrapped.   
		// Explicit newlines embedded in the text also work.
		Rectangle rect = new Rectangle(0, 0, rectWidth, 3);
		txtSpatial.setBox(rect);
		

		txtSpatial.setQueueBucket(bucket);

		return txtSpatial;
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
	@Override public void destroy(){
		getLogger().info("JME3 destroy() called");
		super.destroy();
		getLogger().info("Cleaing up MIDI bridge");
		myTMB.cleanup();
		getLogger().info("MIDI cleanup finished");
	}
	public class TB_RenderContext extends ConfiguredPhysicalModularRenderContext {
		@Override public void doUpdate(float tpf) {
		}
	}
}
