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
package org.cogchar.render.opengl.bony.world;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class MatMgr {
	public static final String PATH_LOGO_MONKEY = "Interface/Logo/Monkey.jpg";
	public static final String PATH_MATERIAL_UNSHADED = "Common/MatDefs/Misc/Unshaded.j3md";

	public static final String PATH_TERRAIN_ROCK = "Textures/Terrain/Rock/Rock.PNG";
	
	private		AssetManager		myAssetMgr;
	
	public MatMgr(AssetManager assetMgr) {
		myAssetMgr = assetMgr;
	}
	public Material makeMatWithOptTexture(String matName, String matTextName, Texture t) {
		Material mat = new Material(myAssetMgr, matName);
		if ((mat != null) && (matTextName != null) && (t != null)) {
			mat.setTexture(matTextName, t);
		}
		return mat;
	}	
	public Material makeUnshadedMat() {
		return makeMatWithOptTexture(PATH_MATERIAL_UNSHADED, null, null);
	}
	public Material makeColoredUnshadedMat(ColorRGBA color) {
		Material mat = makeMatWithOptTexture(PATH_MATERIAL_UNSHADED, null, null);
		mat.setColor("Color", color);
		return mat;
	}
	public Material makeRandomlyColoredUnshadedMat() {
		return makeColoredUnshadedMat(ColorRGBA.randomColor());
	}	
	public Material makeJmonkeyLogoMat() { 
		Texture t = myAssetMgr.loadTexture(PATH_LOGO_MONKEY);
		return makeMatWithOptTexture(PATH_MATERIAL_UNSHADED, "ColorMap", t);
	}
	public Material makeRockMat() { 
		TextureKey rockTextureKey = new TextureKey(PATH_TERRAIN_ROCK);
		rockTextureKey.setGenerateMips(true);
		Texture rockTexture = myAssetMgr.loadTexture(rockTextureKey);
		return makeMatWithOptTexture(PATH_MATERIAL_UNSHADED, "ColorMap", rockTexture);
	}

	

}
