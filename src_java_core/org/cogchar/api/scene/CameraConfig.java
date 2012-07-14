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
public class CameraConfig {

	//public String myURI_Fragment;
	public String cameraName;
	public float[] cameraPosition = new float[3];
	public float[] cameraPointDir = new float[3];
	public float[] cameraViewPort = new float[4];
	public String attachedItem;

	@Override
	public String toString() {
		return "CameraConfig[name=" + cameraName + ", pos=" + Arrays.toString(cameraPosition) + ", dir=" + Arrays.toString(cameraPointDir) + ", viewport=" + Arrays.toString(cameraViewPort) + "]";
	}

	public CameraConfig(Item configItem) {
		cameraName = configItem.getIdent().getLocalName();
		for (int index = 0; index < 3; index++) {
			cameraPosition[index] = ItemFuncs.getDouble(configItem, SceneConfigNames.P_position[index], 0.0).floatValue();
			cameraPointDir[index] = ItemFuncs.getDouble(configItem, SceneConfigNames.P_direction[index], 0.0).floatValue();
		}
		for (int index = 0; index < cameraViewPort.length; index++) {
			cameraViewPort[index] = ItemFuncs.getDouble(configItem, SceneConfigNames.P_viewport[index], null).floatValue();
		}
		attachedItem = ItemFuncs.getString(configItem, SceneConfigNames.P_attachedItem, null);
	}
}
