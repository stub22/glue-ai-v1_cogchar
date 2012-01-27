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
import com.jme3.texture.Texture;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class MatMgr {
	public static final String PATH_LOGO_MONKEY = "Interface/Logo/Monkey.jpg";
	public static final String PATH_MATERIAL_UNSHADED = "Common/MatDefs/Misc/Unshaded.j3md";

	public static final String PATH_TERRAIN_ROCK = "Textures/Terrain/Rock/Rock.PNG";
	
	public static Material makeJmonkeyLogoMaterial(AssetManager assetManager) { 
		Texture t = assetManager.loadTexture(PATH_LOGO_MONKEY);
		return makeMaterialWithOptTexture(assetManager, PATH_MATERIAL_UNSHADED, "ColorMap", t);
	}
	public static Material makeRockMaterial(AssetManager assetManager) { 
		TextureKey rockTextureKey = new TextureKey(PATH_TERRAIN_ROCK);
		rockTextureKey.setGenerateMips(true);
		Texture rockTexture = assetManager.loadTexture(rockTextureKey);
		return makeMaterialWithOptTexture(assetManager, PATH_MATERIAL_UNSHADED, "ColorMap", rockTexture);
	}
	public static Material makeUnshadedMaterial(AssetManager assetManager) {
		return makeMaterialWithOptTexture(assetManager, PATH_MATERIAL_UNSHADED, null, null);
	}
	public static Material makeMaterialWithOptTexture(AssetManager assetManager, String matPath, String matTextName, Texture t) {
		Material mat = new Material(assetManager, matPath);
		if ((matTextName != null) && (matPath != null)) {
			mat.setTexture(matTextName, t);
		}
		return mat;
	}
}
