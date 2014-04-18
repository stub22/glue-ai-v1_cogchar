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
package org.cogchar.render.trial;

import org.cogchar.bind.midi.in.CCParamRouter;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Dome;
import com.jme3.scene.shape.Quad;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.FreeIdent;
import org.cogchar.render.opengl.scene.TextMgr;
import org.cogchar.render.sys.context.CogcharRenderContext;
import org.cogchar.render.sys.context.CoreFeatureAdapter;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.cogchar.render.sys.task.Queuer;

import java.util.List;
import java.util.ArrayList;

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
			
		2) What culling is applied to your spatial?   :  Never, Inherit, Always  [use Always to disable display of this spatial]

		3) What parent node are you attaching to?  (Regular/"deep",  GUI/"flat", other?)
-----------
 
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
-------------
*   per text-block
		7) A font includes a material, which determines color and transparency.
		However, the BitmapText object also has methods for setColor() and setAlpha().    Hrmmmmm.
		* 
		8) Text runs "downward" = decreasing 3D Y value, but increasing 2D Y value.  [Todo:  recheck this interp]
		* 
		9) "Size" parameter of text-font is in some texty frame of reference
 
 */
public class TrialContent extends BasicDebugger implements TrialUpdater {

	private String letters = "abcd\nABCD\nEFGHIKLMNOPQRS\nTUVWXYZ";
	private String digits = "1\n234567890";
	private String syms = "`~!@#$\n%^&*()-=_+[]\\;',./{}|:<>?";
	private String myCamStatTxt = "cam stat\ntext goest\nhere";
	
	private Node myMainDeepNode, myMainGuiNode;
	private BitmapText		myLettersBTS, myDigitsBTS, mySymsBTS, myFlatDigitsBTS, myOverlayEqnBT, myCamStatBT;
	
	
	private TextBox2D		myFloatingStatBox;
	
	private	List<PointerCone>	myPointerCones = new ArrayList<PointerCone>();

	public Node getMainDeepNode() { 
		return myMainDeepNode;
	}
	
	// The other args are really implied by the rrc, so can be factored out
	public void initContent3D_onRendThread(RenderRegistryClient rrc, Node appRootNode) {
		
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
		
		TrialNexus tNexus = new TrialNexus(rrc);
		makeRectilinearParamViz(tNexus, rrc);
		makeDisplayTestCones(rrc);
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


	
	public void attachMidiCCs(CCParamRouter ccpr) { 
		// Controller numbers starting with 21 are the default CC numbers Automap applies to the knobs of a Nocturn
		ccpr.putControlChangeParamBinding(21, RectangularWidget2D.CoordName.X.name(), myFloatingStatBox); 
		ccpr.putControlChangeParamBinding(22, RectangularWidget2D.CoordName.Y.name(), myFloatingStatBox); 
		ccpr.putControlChangeParamBinding(23, RectangularWidget2D.CoordName.Width.name(), myFloatingStatBox); 
		ccpr.putControlChangeParamBinding(24, RectangularWidget2D.CoordName.Height.name(), myFloatingStatBox); 
		ccpr.putControlChangeParamBinding(25, TextBox2D.TEXT_VAL_PARAM_NAME, myFloatingStatBox); 
		
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
	
	// Attaches some fun rectangular quad arrays
	public void makeRectilinearParamViz(TrialNexus tNexus, RenderRegistryClient rrc) { 	
		// On a mat, we could choose to set the cull mode, but that can also be done on shapes - as below.
		Material mat = makeAlphaBlendedUnshadedMaterial(rrc, 0f, 1.0f, 0, 0.5f);
		
		// quick "can we draw?" sanity test - optional
		attachSomeQuads(myMainDeepNode, mat);
		
		// Here is the main 3D grid of boxes
		tNexus.makeSheetspace(myMainDeepNode, mat);		
	}
	public void attachSomeQuads(Node parentNode, Material mat) { 
		Node quadVizNode = new Node("quad_viz_root");		
		quadVizNode.setLocalTranslation(-10.0f, 10.0f, 5.0f);
		parentNode.attachChild(quadVizNode);
		
		for (int i =0; i< 10; i++) {
			// These 10 quads just stream off into space, proving that we can attach geoms to OpenGL parent.
			float d = i * 25.0f;
			Geometry qg = new Geometry("pvq_" + i, new Quad(10, 20));
			qg.setMaterial(mat);
			configureRenderingForSpatial(qg);  // Sets the rendering bucket and cull mode

			qg.setLocalTranslation(0.8f * d, -20.0f + 0.5f * d , -3.0f - 1.0f * d);
			quadVizNode.attachChild(qg);
		}		
	}
	public void setCamDebugText(String dbgTxt) { 
		myCamStatBT.setText(dbgTxt);
	}

	public Node makePointerCone(RenderRegistryClient rrc, String nameSuffix) { 
		PointerCone pc = new PointerCone(nameSuffix);
		pc.setup(rrc);
		pc.setTextPositionFraction(0.75f);
		myPointerCones.add(pc);
		return pc.getAssemblyNode();
	}
	public void makeDisplayTestCones(RenderRegistryClient rrc) { 

		Node coneDemoNode = new Node("cone_demo_root");
		Material mat = makeAlphaBlendedUnshadedMaterial(rrc, 0.9f, 0.5f, 0.3f, 0.4f);

		for (int i =0; i< 10; i++) {
			float p = i * 3.0f;
			// Javadocs say this is insideView, but source code calls it outsideView.
			// Huh?  
			// TODO: recheck
			// boolean insideView = false;
			boolean insideView = ((i %2) == 0);
			Vector3f innerCenter = new Vector3f(0.0f, 0.0f, 0.0f);
			Vector3f outerPos = new Vector3f(-15.0f + p, 25.0f, -3.0f);
			int numPlanes = 2;
			float radius = 1.5f;
			int numBasePoints = i+1;
			Dome d = new Dome(innerCenter, numPlanes, numBasePoints, radius, insideView);
			
			Geometry dg = new Geometry("dome_" + i, d);
			dg.setMaterial(mat);
			configureRenderingForSpatial(dg);  // Sets the rendering bucket and cull mode
			// Make em taller
			dg.setLocalScale(1.0f, 10.0f, 1.0f);
			dg.setLocalTranslation(outerPos);
			coneDemoNode.attachChild(dg);
		}
		myMainDeepNode.attachChild(coneDemoNode);
			
	}

	protected static Material makeAlphaBlendedUnshadedMaterial(RenderRegistryClient rrc, float red,
				float green, float blue, float alpha) { 

		AssetManager assetMgr = rrc.getJme3AssetManager(null);
		ColorRGBA color = new ColorRGBA(red, green, blue, alpha);
		return makeAlphaBlendedUnshadedMaterial(assetMgr, color);	
	}
	protected static Material makeAlphaBlendedUnshadedMaterial(AssetManager assetMgr, ColorRGBA color) { 
		FaceCullMode matFaceCullMode = FaceCullMode.Off;  		// Render both sides
		Material unshMat = new Material(assetMgr, "Common/MatDefs/Misc/Unshaded.j3md");
		// For transparency/lucency we set the BlendMode on addtlRenderState.
		RenderState.BlendMode matBlendMode = RenderState.BlendMode.Alpha;
		// But note that to get transparency, we also need to put spatials into eligible buckets
		unshMat.getAdditionalRenderState().setBlendMode(matBlendMode);
		unshMat.getAdditionalRenderState().setFaceCullMode(matFaceCullMode);
		unshMat.setColor("Color", color);
		return unshMat;
	}
	protected static void configureRenderingForSpatial(Spatial spat) {
		RenderQueue.Bucket spatRenderBucket  = RenderQueue.Bucket.Transparent;
		Spatial.CullHint spatCullHint = Spatial.CullHint.Never;  // Others are CullHint.Always, CullHint.Inherit
		// Setup transparency for the spatia, but note that the material blend-mode must 
		// also support transparency.
		spat.setQueueBucket(spatRenderBucket); 
		spat.setCullHint(spatCullHint);	
	}
	
	@Override public void doUpdate(RenderRegistryClient rrc, float tpf)	{
		for (PointerCone pc : myPointerCones) {
			pc.doUpdate(rrc, tpf);
		}
	}
}
