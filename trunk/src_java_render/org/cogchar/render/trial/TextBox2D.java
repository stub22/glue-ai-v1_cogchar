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

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.Ident;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.cogchar.render.sys.task.Queuer;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class TextBox2D extends RectangularWidget2D {
	private	static Material theBaseUnshadedMat;
	// Remember:  Both nodes and geometries are spatials.  Spatial is an abstract class, not an interface.
	// A BitmapText is a node, and thus also a spatial.
	// The setMaterial method, and the setLocalTrans[++] are defined on *spatial* and all of its subclasses.
	// A mesh is not a spatial or any of the other things mentioned above.
	// Mesh has these direct subclasses for simpler shapes:  
	// AbstractBox, Arrow, Curve, Cylinder, Dome, Grid, Line, ParticleMesh, PQTorus, Quad, SkeletonPoints, 
	// SkeletonWire, Sphere, Surface, Torus, WireBox, WireFrustum, WireSphere
	// When loading meshes from Ogre, we get some other private Mesh sub-class from that plugin MeshLoader.
	
	private	ColorRGBA	myQuadColor, myTextColor;

	private BitmapText	myBmapTextSpat;
	private Geometry	myQuadGeo;

	private String		myTextString;



	// private TextSpatialFactory	myTextSpatialFactory;
	
	public TextBox2D(RenderRegistryClient rrc, Ident requiredID, String optInitText, ColorRGBA optInitTextColor, ColorRGBA optInitQuadColor) {
		super(rrc, requiredID);
		if (optInitText != null) {
			myTextString = optInitText;
		} else {
			myTextString = requiredID.getAbsUriString();
		}
		myQuadColor = optInitQuadColor;
		myTextColor = optInitTextColor;
	}


	public void setTextVal(String val) { 
		// This does not need to be execed on the JME3 thread, even if myBmapTextSpat is already in the scene graph.
		myTextString = val;
		myBmapTextSpat.setText(myTextString);
	}

	public void setupContentsAndAttachToParent(Node parentNode, RenderRegistryClient rrc, AssetManager assetMgr) { 
		// We expect this is done on the JME3 render thread.
		Node mn = getMyNode();
		
		String quadGeoName = "TextBox2D-mainNode:" + myIdent.getLocalName();
		
		// BEGIN - general form, unused
		Material anyMat = getBaseMat(assetMgr);
		// Here insert the mat.clone and modify operation, then pass that mat on the next line
		Geometry junkGeoWithSomeMat = makeQuadGeoWithOptMat(quadGeoName, myWidth, myHeight, anyMat);
		// END - general form
		
		// Begin - more specific form - quad has a unique material with an alpha-blended color.
		ColorRGBA purplyTransparentColor = new ColorRGBA(0.5f, 0.0f, 0.7f, 0.5f);
		myQuadGeo = makeQuadGeoWithBlendedMat(assetMgr, quadGeoName, myWidth, myHeight, purplyTransparentColor, RenderState.BlendMode.Alpha);
		TextSpatialFactory tsf = new TextSpatialFactory(rrc);
		float renderScale = 1.0f;
		int textLineWrapWidthPixels = 90;
		myBmapTextSpat = tsf.makeTextSpatial(myTextString, renderScale, RenderQueue.Bucket.Inherit, textLineWrapWidthPixels);
		// We note that text runs downward, so we need to move the text up by about the height of the rect.
		int leftMarginWidth = 5;
		int topMarginHeight = 5;
		myBmapTextSpat.setLocalTranslation(leftMarginWidth, myHeight - topMarginHeight, 0.5f);
		mn.attachChild(myBmapTextSpat);
		mn.attachChild(myQuadGeo);
		attachToParentGuiNode(parentNode);	
	}
	// This must be done on the JME3 thread, right?
	private void attachToParentGuiNode(Node parentNode) {
		myParentNode = parentNode;
		Node mn = getMyNode();
		myParentNode.attachChild(mn);
		applyTranslation(Queuer.QueueingStyle.QUEUE_AND_RETURN);
	}
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
		
	public static Geometry makeQuadGeoWithBlendedMat(AssetManager assetMgr, String name, int width, int height, ColorRGBA col, RenderState.BlendMode blendMode) {
		Material baseMat = getBaseMat(assetMgr);
		Material ourMat = baseMat.clone();
		ourMat.getAdditionalRenderState().setBlendMode(blendMode);		
		ourMat.setColor("Color", col);
		
		return makeQuadGeoWithOptMat(name, width, height, ourMat);
	} 
	public static Geometry makeQuadGeoWithOptMat(String name, int width, int height, Material optMatToUseDirectly) { 
		Geometry quadGeo = null;
		quadGeo = new Geometry("name", new Quad(width, height));
		if (optMatToUseDirectly != null) {
			quadGeo.setMaterial(optMatToUseDirectly);
			// quadGeo.setCullHint(Spatial.CullHint.Never); // Others are CullHint.Always, CullHint.Inherit
		}
		return quadGeo;
	}	
	private static Material getBaseMat(AssetManager assetMgr) { 
		if (theBaseUnshadedMat == null) {
			theBaseUnshadedMat = new Material(assetMgr, "Common/MatDefs/Misc/Unshaded.j3md");
			theBaseUnshadedMat.setColor("Color", new ColorRGBA(0, 1.0f, 0, 0.5f));
		}
		return theBaseUnshadedMat;
	}	
	public static String TEXT_VAL_PARAM_NAME = "TextVal";
	public static String T_VAL_PARAM_NAME = "TextVal";
	@Override public void setNormalizedNumericParam(String paramName, float normZeroToOne) {
		if (TEXT_VAL_PARAM_NAME.equals(paramName)) {
			setTextVal("Param=" + normZeroToOne);
		} else {
			super.setNormalizedNumericParam(paramName, normZeroToOne);
		}
	}	
}
