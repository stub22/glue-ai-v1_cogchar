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
package org.cogchar.api.scene;


import java.util.Arrays;
import org.appdapter.core.item.Item;
import org.appdapter.core.item.ItemFuncs;


/**
 * @author Ryan Biggs
 */
public class LightConfig {
	public String						myURI_Fragment;
	public String                       lightName;
	public LightType                    lightType;
	public float[]						lightDirection = new float[3];
	public float[]						lightColor = new float[4];
           
	@Override
	public String toString() {
		return "LightConfig[uriFrag=" + myURI_Fragment + ", type=" + lightType.name() + /* ", pos=" + Arrays.toString(cameraPosition) + */ ", dir=" + Arrays.toString(lightDirection) + ", color=" + Arrays.toString(lightColor) + "]";
	}

	public LightConfig(Item configItem) {
		myURI_Fragment = configItem.getIdent().getLocalName();
		lightName = ItemFuncs.getString(configItem, SceneConfigNames.P_lightName, null);
		lightType = LightType.AMBIENT; // For now, we assume light is ambient (no direction required) if type is not specified
		String typeString = ItemFuncs.getString(configItem, SceneConfigNames.P_lightType, null);
		if (typeString.equals("DIRECTIONAL")) {lightType = LightType.DIRECTIONAL;}
		for (int index=0; index<lightDirection.length; index++) {
			lightDirection[index] = ItemFuncs.getDouble(configItem, SceneConfigNames.P_direction[index], 0.0).floatValue();
		}
		for (int index=0; index<lightColor.length; index++) {
			lightColor[index] = ItemFuncs.getDouble(configItem, SceneConfigNames.P_color[index], null).floatValue();
		}
	}
        
	public enum LightType {DIRECTIONAL, AMBIENT} // We can add POINT and SPOT later if we want, will need to extend the RDF definition to do so

}