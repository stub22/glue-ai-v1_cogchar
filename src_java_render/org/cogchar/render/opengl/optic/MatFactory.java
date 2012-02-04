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
package org.cogchar.render.opengl.optic;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class MatFactory {

	public static final String PATH_MATERIAL_UNSHADED = "Common/MatDefs/Misc/Unshaded.j3md";


	
	private		AssetManager		myAssetMgr;
	
	public MatFactory(AssetManager assetMgr) {
		myAssetMgr = assetMgr;
	}
	public Material makeMatWithOptTexture(String matName, String matTextName, Texture t) {
		Material mat = new Material(myAssetMgr, matName);
		if ((mat != null) && (matTextName != null) && (t != null)) {
			mat.setTexture(matTextName, t);
		}
		return mat;
	}
	public Material makeMatWithOptPixelMapTexture(String matName, Texture texture) {
		return makeMatWithOptTexture(matName, "ColorMap", texture);
	}
	public Material makeUnshadedMatWithOptPixelMapTexture(Texture texture) {
		return makeMatWithOptPixelMapTexture(PATH_MATERIAL_UNSHADED, texture);
	}
	
	public Material makeUnshadedMat() {
		return makeUnshadedMatWithOptPixelMapTexture(null);
	}
	public Material makeColoredUnshadedMat(ColorRGBA color) {
		Material mat = makeMatWithOptTexture(PATH_MATERIAL_UNSHADED, null, null);
		mat.setColor("Color", color);
		return mat;
	}
	public Material makeRandomlyColoredUnshadedMat() {
		return makeColoredUnshadedMat(ColorRGBA.randomColor());
	}	
	public Material makeTexturedUnshadedMat(String textureImagePath, boolean generateMips, Texture.WrapMode optWrapMode) { 
		TextureKey textureKey = new TextureKey(textureImagePath);
		textureKey.setGenerateMips(generateMips);
		Texture texture = myAssetMgr.loadTexture(textureKey);
		if (optWrapMode != null) {
			texture.setWrap(optWrapMode);
		}
		return makeMatWithOptTexture(PATH_MATERIAL_UNSHADED, "ColorMap", texture);
	}	
	public Material makeJmonkeyLogoMat() { 
		return makeTexturedUnshadedMat(TextureFactory.PATH_LOGO_MONKEY, false, null);
	}
	public Material makeRockMat() { 
		return makeTexturedUnshadedMat(TextureFactory.PATH_TERRAIN_ROCK, true, null);
	}
	public Material getBrickWallMat() { 
		return makeTexturedUnshadedMat(TextureFactory.PATH_BRICK_WALL, true, null);
	}
	public Material getPondMat() { 
		return makeTexturedUnshadedMat(TextureFactory.PATH_POND, true, Texture.WrapMode.Repeat);
	}	
/** 
 * Material mat = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
 * 
 */


}
