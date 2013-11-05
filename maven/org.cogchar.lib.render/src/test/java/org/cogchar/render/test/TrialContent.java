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

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Quad;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.FreeIdent;
import org.cogchar.render.opengl.scene.TextMgr;
import org.cogchar.render.sys.context.CogcharRenderContext;
import org.cogchar.render.sys.context.CoreFeatureAdapter;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.cogchar.render.sys.task.Queuer;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class TrialContent extends BasicDebugger {

	private String letters = "abcd\nABCD\nEFGHIKLMNOPQRS\nTUVWXYZ";
	private String digits = "1\n234567890";
	private String syms = "`~!@#$\n%^&*()-=_+[]\\;',./{}|:<>?";
	private String myCamStatTxt = "cam stat\ntext goest\nhere";
	
	private Node myMainDeepNode, myMainGuiNode;
	private BitmapText		myLettersBTS, myDigitsBTS, mySymsBTS, myFlatDigitsBTS, myOverlayEqnBT, myCamStatBT;
	private Geometry		myQuadGeo;
	
	private TextBox2D		myCamStatBox;
	
	// The other args are really implied by the rrc, so can be factored out
	public void initContent3D_onRendThread(RenderRegistryClient rrc, Node appRootNode, ViewPort appViewPort) {
		
		myMainDeepNode = new Node("my_main_deep");
		appRootNode.attachChild(myMainDeepNode);
		
		// This scale factor will be multiplied by the getRenderedSize value of the font, which is 17.0f
		// for the default font (on JDK6.Win7, YMMV).

		// Not clear yet what "size" of the font means.  It seems that if we want the rectangle to really contain
		TextSpatialFactory tsf = new TextSpatialFactory(rrc);
		myLettersBTS = tsf.makeTextSpatial(letters, 0.2f, RenderQueue.Bucket.Transparent, 6); // eff-scale 3.4f, wraps after 2-3 chars
		myDigitsBTS = tsf.makeTextSpatial(digits, 0.1f, RenderQueue.Bucket.Transparent, 6);  // eff-scale 1.7f, wraps after 6 chars
		mySymsBTS = tsf.makeTextSpatial(syms, 0.05f, RenderQueue.Bucket.Transparent, 6);   // eff-scale 1.05f, wraps after ~ 18 oddly shaped chars
		myMainDeepNode.attachChild(myLettersBTS);
		myLettersBTS.move(3.0f, 3.0f, -50.0f);
		//lettersBTS.setSize(0.5f);	//digitsBTS.setSize(0.5f); //symsBTS.setSize(0.5f);
		myMainDeepNode.attachChild(myDigitsBTS);
		myMainDeepNode.attachChild(mySymsBTS);

		myDigitsBTS.move(-10f, -10f, -10f);
		BillboardControl bbCont = new BillboardControl();
		/**
		 * AxialY Aligns this Billboard to the screen, but keeps the Y axis fixed. AxialZ Aligns this Billboard to the
		 * screen, but keeps the Z axis fixed. Camera Aligns this Billboard to the camera position. Screen Aligns this
		 * Billboard to the screen.
		 */
		bbCont.setAlignment(BillboardControl.Alignment.Screen);
		myLettersBTS.addControl(bbCont);
		appViewPort.setBackgroundColor(ColorRGBA.Blue);
	}
	// The other args are actually available from the rrc, so can be factored out of these params.
	public void initContent2D_onRendThread(RenderRegistryClient rrc, Node parentGUInode, AssetManager assetMgr) {
		myMainGuiNode = new Node("my_main_gui");

		TextSpatialFactory tsf = new TextSpatialFactory(rrc);
		
		int textWrapPixWidth = 60;
		float txtRegScale = 1.0f;
		float txtDoubleScale = 2.0f;
		parentGUInode.attachChild(myMainGuiNode);
		
		// We could scoot the whole GUI over with something like:
		// myMainGuiNode.setLocalTranslation(20.0f, 40.0f, 0.0f);
		
		// Let's make some blocks of text, and position them.
		// The guiBucket isn't really necessary, since spatial will inherit the guiBucket from the gui parent Node.
		RenderQueue.Bucket guiBucket = RenderQueue.Bucket.Gui;
		myFlatDigitsBTS = tsf.makeTextSpatial(digits, txtRegScale, guiBucket, textWrapPixWidth);
		myCamStatBT = tsf.makeTextSpatial(myCamStatTxt, txtRegScale, null, textWrapPixWidth);
		myOverlayEqnBT = tsf.makeTextSpatial("X+Y", txtDoubleScale, null, textWrapPixWidth);
		
		// Text is rendered downward and right from the origin (local 0.0,0.0) position of the spatial.
		// Thus overlay text is "off the screen" unless the Y-coordinate of the spatial is high enough.
		// If positioned at y=10.0f, we can just barely see the top edge of the first line of text.
		
		myFlatDigitsBTS.setLocalTranslation(200.0f, 60.0f, 0.0f);
		myCamStatBT.setLocalTranslation(580.0f, 40.0f, -5.0f);
		myOverlayEqnBT.setLocalTranslation(300.0f, 250.0f, 0.0f);
		
		myMainGuiNode.attachChild(myCamStatBT);
		myMainGuiNode.attachChild(myFlatDigitsBTS);
		myMainGuiNode.attachChild(myOverlayEqnBT);
		

		myCamStatBox = new TextBox2D(rrc, new FreeIdent("uri:org.cogchar/goody_inst#camStatBox2D"), "opt init txt", 
					ColorRGBA.White, ColorRGBA.Magenta);
		// param-set ops can be called in any order, but setupAndAttach should generally be called only
		// once in the lifetime of the box.

		myCamStatBox.setCoordinates(380, 150, -2.5f, 110, 90, Queuer.QueueingStyle.INLINE);  // x, y, z-order, width, height
		// This should be called only once, but can be at any point relative to the parameter/content setting calls,
		// which can then be repeated anytime to update the contents (modulo thread concerns).  
		myCamStatBox.setupContentsAndAttachToParent(myMainGuiNode, rrc, assetMgr);
		
	}
		// rrc.getSceneFlatFacade(null).detachAllOverlays();

		
		// guiNode.attachChild(crossBT);  is equiv to   rrc.getSceneFlatFacade(null).attachOverlaySpatial(crossBT);	


	
	protected void attachMidiCCs(TempMidiBridge tmb) { 
		// Controller numbers starting with 21 are the default CC numbers Automap applies to the knobs of a Nocturn
		tmb.putControlChangeParamBinding(21, RectangularWidget2D.CoordName.X.name(), myCamStatBox); 
		tmb.putControlChangeParamBinding(22, RectangularWidget2D.CoordName.Y.name(), myCamStatBox); 
		tmb.putControlChangeParamBinding(23, RectangularWidget2D.CoordName.Width.name(), myCamStatBox); 
		tmb.putControlChangeParamBinding(24, RectangularWidget2D.CoordName.Height.name(), myCamStatBox); 
		tmb.putControlChangeParamBinding(25, TextBox2D.TEXT_VAL_PARAM_NAME, myCamStatBox); 
	}
	
	
	public void shedLight_onRendThread(CogcharRenderContext crc) {
		CoreFeatureAdapter.setupLight(crc);
		shedMoreLight_onRendThread(crc);
	}

	private void shedMoreLight_onRendThread(CogcharRenderContext crc) {
		RenderRegistryClient rrc = crc.getRenderRegistryClient();
		Vector3f otherLightDir = new Vector3f(0.1f, 0.7f, 1.0f).normalizeLocal();
		DirectionalLight odl = rrc.getOpticLightFacade(null).makeWhiteOpaqueDirectionalLight(otherLightDir);
		CoreFeatureAdapter.addLightToRootNode(crc, odl);
	}
	
	private void testOldQuadGeo(AssetManager assetMgr) { 

		// On a mat, we frequently set color, and for transparency/lucency we set the BlendMode on addtlRenderState.
		// On a mat, we could choose to set the cull mode, but that can also be done on shapes - as below.
		// 
		Material unshMat = new Material(assetMgr, "Common/MatDefs/Misc/Unshaded.j3md");
		unshMat.setColor("Color", new ColorRGBA(0, 1.0f, 0, 0.5f));


		unshMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
		myQuadGeo = new Geometry("wideQuad", new Quad(200, 100));
		myQuadGeo.setMaterial(unshMat);

		myQuadGeo.setCullHint(Spatial.CullHint.Never); // Others are CullHint.Always, CullHint.Inherit
		myQuadGeo.setLocalTranslation(50.0f, 300.0f, -1.0f);
		myMainGuiNode.attachChild(myQuadGeo);		
	}
}
