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
package org.cogchar.api.cinema;

import java.util.Arrays;
import org.appdapter.core.item.Ident;
import org.appdapter.core.item.Item;
import org.appdapter.core.item.ItemFuncs;
import org.cogchar.blob.emit.Solution;
import org.cogchar.blob.emit.QueryInterface;
import org.cogchar.blob.emit.QuerySheet;

/**
 * @author Ryan Biggs
 */
public class LightConfig {

	//public String myURI_Fragment;
	public String lightName;
	public LightType lightType;
	public float[] lightDirection = new float[3];
	public float[] lightColor = new float[4];
	
	private static QueryInterface queryEmitter = QuerySheet.getInterface();

	@Override
	public String toString() {
		return "LightConfig[name=" + lightName + ", type=" + lightType.name()
				+ ", dir=" + Arrays.toString(lightDirection) + ", color=" + Arrays.toString(lightColor) + "]";
	}

	// A new constructor to build CameraConfig from spreadsheet
	public LightConfig(Solution querySolution) {
		lightName = queryEmitter.getIdentFromSolution(querySolution, LightsCameraQueryNames.LIGHT_NAME_VAR_NAME).getLocalName();
		lightType = LightType.AMBIENT; // For now, we assume light is ambient (no direction required) if type is not specified
		Ident typeIdent = queryEmitter.getIdentFromSolution(querySolution, LightsCameraQueryNames.LIGHT_TYPE_VAR_NAME);
		if (typeIdent.getLocalName().equals("DIRECTIONAL")) {
			lightType = LightType.DIRECTIONAL;
		}
		for (int index = 0; index < lightDirection.length; index++) {
			lightDirection[index] = queryEmitter.getFloatFromSolution(querySolution, LightsCameraQueryNames.DIRECTION_VAR_NAME[index], 0f);
		}
		for (int index = 0; index < lightColor.length; index++) {
			lightColor[index] = queryEmitter.getFloatFromSolution(querySolution, LightsCameraQueryNames.COLOR_VAR_NAME[index], Float.NaN);
		}
	}

	public LightConfig(Item configItem) {
		lightName = configItem.getIdent().getLocalName();
		lightType = LightType.AMBIENT; // For now, we assume light is ambient (no direction required) if type is not specified
		String typeString = ItemFuncs.getString(configItem, LightsCameraConfigNames.P_lightType, null);
		if (typeString.equals("DIRECTIONAL")) {
			lightType = LightType.DIRECTIONAL;
		}
		for (int index = 0; index < lightDirection.length; index++) {
			lightDirection[index] = ItemFuncs.getDouble(configItem, LightsCameraConfigNames.P_direction[index], 0.0).floatValue();
		}
		for (int index = 0; index < lightColor.length; index++) {
			lightColor[index] = ItemFuncs.getDouble(configItem, LightsCameraConfigNames.P_color[index], null).floatValue();
		}
	}

	public enum LightType {

		DIRECTIONAL, AMBIENT
	} // We can add POINT and SPOT later if we want, will need to extend the RDF definition slightly to do so
}
