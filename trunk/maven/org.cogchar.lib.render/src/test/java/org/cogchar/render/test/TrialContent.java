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
import com.jme3.material.RenderState.FaceCullMode;
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
 * 
 * Ultimate goal is a highly usable parameter tweaking+viz system, utilizing: colors, 3D matrices, text, motion, midi in/out.
 * A 3D musical multicolored animating spreadsheet, or spacesheet, if you will.
 * 
 * We organize code around these JME3 gotchas:
 * 
 *  per-spatial
 *		1) Which queue bucket is your spatial in?   Constants from RenderQueue.Bucket  are Transparent, GUI, ...
 *			[Which is the default?  Inherit or Opaque?  Does it vary by spatial type?]
 * 
 *		If you want transparency/alpha-blend for 3D objects, must set this explicitly, and see also #4 below.
 * 
 			 Gui -   This is a special mode, for drawing 2D object without perspective (such as GUI or HUD parts).
			Inherit-  A special mode, that will ensure that this spatial uses the same mode as the parent Node does.
			Opaque - The renderer will try to find the optimal order for rendering all objects using this mode.
			Sky -   A special mode used for rendering really far away, flat objects - e.g.
			Translucent -  A special mode used for rendering transparent objects that should not be effected by SceneProcessor.
			Transparent -    This is the mode you should use for object with transparency in them.  
			
		2) What culling is applied to your spatial?   :  Never, Inherit, Always

		3) What parent node are you attaching to?  (Regular/"deep",  GUI/"flat", other?)
	-------
 
	per-material

 		4) What blend mode does your material have?  Off [default], Alpha, AlphaAdditive ...
 			See RenderStates, blending, and culling info here:
			* http://hub.jmonkeyengine.org/wiki/doku.php/jme3:advanced:materials_overview
 
 		5) What face-cull mode is applied to your material?  :  Back [default], Off, FrontAndBack [good for quick visual disable]
		
 * ---------
     per-app-feature
 		6) What thread are you writing to your OpenGL scene graph from?  
		During app.start() and app.update() calls, we are automatically and locally on the scene-graph thread.  
		But otherwise, we are not, and must pass a message to it, either async or sync. 
		See our Queuer.QueueingStyle stuff.
		Of course, such messages can be bundled or complex, and then we are into typical client-server computing concerns.
		
		By sharing state with the app's update() method, we can avoid explicit message passing, but must usually
		synchronize somewhere to avoid racy glitches.
	
		Currently our default policy is to use Queuer.QueueingStyle.QUEUE_AND_RETURN when not on the update() 
		thread, e.g. in a MIDI callback.  This asynchronously solves the problem, and is viable for a small to
		medium sized message flow.  But as we do heavier lifting, we will want to hook up with the update() callback
		in a more sophsiticated way.

*   per text-block
		7) A font includes a material, which determines color and transparency.
		However, the BitmapText object also has methods for setColor() and setAlpha().    Hrmmmmm.
		* 
		8) Text runs "downward" = decreasing 3D Y value, but increasing 2D Y value.  [Todo:  recheck this interp]
		* 
		9) "Size" parameter of text-font is in some texty frame of reference
 
 */
public class TrialContent extends BasicDebugger {

	private String letters = "abcd\nABCD\nEFGHIKLMNOPQRS\nTUVWXYZ";
	private String digits = "1\n234567890";
	private String syms = "`~!@#$\n%^&*()-=_+[]\\;',./{}|:<>?";
	private String myCamStatTxt = "cam stat\ntext goest\nhere";
	
	private Node myMainDeepNode, myMainGuiNode;
	private BitmapText		myLettersBTS, myDigitsBTS, mySymsBTS, myFlatDigitsBTS, myOverlayEqnBT, myCamStatBT;
	
	
	private TextBox2D		myFloatingStatBox;
	

	
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
		
		AssetManager assetMgr = rrc.getJme3AssetManager(null);
		
		TrialNexus tNexus = new TrialNexus(rrc);
		makeRectilinearParamViz(tNexus, assetMgr);
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
		myCamStatBT = tsf.makeTextSpatial(myCamStatTxt, txtRegScale * 0.7f, null, 95 );
		myOverlayEqnBT = tsf.makeTextSpatial("X+Y", txtDoubleScale, null, textWrapPixWidth);
		
		// Text is rendered downward and right from the origin (local 0.0,0.0) position of the spatial.
		// Thus overlay text is "off the screen" unless the Y-coordinate of the spatial is high enough.
		// If positioned at y=10.0f, we can just barely see the top edge of the first line of text.
		
		myFlatDigitsBTS.setLocalTranslation(200.0f, 60.0f, 0.0f);
		myCamStatBT.setLocalTranslation(540.0f, 80.0f, -5.0f);
		myOverlayEqnBT.setLocalTranslation(300.0f, 250.0f, 0.0f);
		
		myMainGuiNode.attachChild(myCamStatBT);
		myMainGuiNode.attachChild(myFlatDigitsBTS);
		myMainGuiNode.attachChild(myOverlayEqnBT);
		

		myFloatingStatBox = new TextBox2D(rrc, new FreeIdent("uri:org.cogchar/goody_inst#camStatBox2D"), "opt init txt", 
					ColorRGBA.White, ColorRGBA.Magenta);
		// param-set ops can be called in any order, but setupAndAttach should generally be called only
		// once in the lifetime of the box.

		myFloatingStatBox.setCoordinates(380, 150, -2.5f, 110, 90, Queuer.QueueingStyle.INLINE);  // x, y, z-order, width, height
		// This should be called only once, but can be at any point relative to the parameter/content setting calls,
		// which can then be repeated anytime to update the contents (modulo thread concerns).  
		myFloatingStatBox.setupContentsAndAttachToParent(myMainGuiNode, rrc, assetMgr);
		
	}
		// rrc.getSceneFlatFacade(null).detachAllOverlays();

		
		// guiNode.attachChild(crossBT);  is equiv to   rrc.getSceneFlatFacade(null).attachOverlaySpatial(crossBT);	


	
	protected void attachMidiCCs(TempMidiBridge tmb) { 
		// Controller numbers starting with 21 are the default CC numbers Automap applies to the knobs of a Nocturn
		tmb.putControlChangeParamBinding(21, RectangularWidget2D.CoordName.X.name(), myFloatingStatBox); 
		tmb.putControlChangeParamBinding(22, RectangularWidget2D.CoordName.Y.name(), myFloatingStatBox); 
		tmb.putControlChangeParamBinding(23, RectangularWidget2D.CoordName.Width.name(), myFloatingStatBox); 
		tmb.putControlChangeParamBinding(24, RectangularWidget2D.CoordName.Height.name(), myFloatingStatBox); 
		tmb.putControlChangeParamBinding(25, TextBox2D.TEXT_VAL_PARAM_NAME, myFloatingStatBox); 
		

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
	
	public void makeRectilinearParamViz(TrialNexus tNexus, AssetManager assetMgr) { 

		
		Node paramVizNode = new Node("param_viz_root");

		// On a mat, we frequently set color, and for transparency/lucency we set the BlendMode on addtlRenderState.
		// On a mat, we could choose to set the cull mode, but that can also be done on shapes - as below.
		// 
		Material unshMat = new Material(assetMgr, "Common/MatDefs/Misc/Unshaded.j3md");
		unshMat.setColor("Color", new ColorRGBA(0, 1.0f, 0, 0.5f));
		
		RenderState.BlendMode matBlendMode = RenderState.BlendMode.Alpha;
		RenderQueue.Bucket spatRenderBucket  = RenderQueue.Bucket.Transparent;
		FaceCullMode matFaceCullMode = FaceCullMode.Off;  		// Render both sides
		Spatial.CullHint spatCullHint = Spatial.CullHint.Never;  // Others are CullHint.Always, CullHint.Inherit
		
		// To get transparency, we also need to put spatials into eligible buckets
		unshMat.getAdditionalRenderState().setBlendMode(matBlendMode);
		
		unshMat.getAdditionalRenderState().setFaceCullMode(matFaceCullMode);	
		for (int i =0; i< 10; i++) {
			float d = i * 25.0f;
			Geometry qg = new Geometry("pvq_" + i, new Quad(10, 20));
			qg.setMaterial(unshMat);
			qg.setQueueBucket(spatRenderBucket);
			qg.setCullHint(spatCullHint);
			qg.setLocalTranslation(0.8f * d, -20.0f + 0.5f * d , -3.0f - 1.0f * d);
			paramVizNode.attachChild(qg);
		}
		paramVizNode.setLocalTranslation(-10.0f, 10.0f, 5.0f);
		myMainDeepNode.attachChild(paramVizNode);
		tNexus.makeSheetspace(myMainDeepNode, unshMat);		
	}

	public void setCamDebugText(String dbgTxt) { 
		myCamStatBT.setText(dbgTxt);
	}



}
