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
	public String						myURI_Fragment;
        public String                                           cameraName;
	public float[]						cameraPosition = new float[3];
	public float[]						cameraPointDir = new float[3];
        public float[]                                          cameraViewPort = new float[4];
           
        @Override
	public String toString() {
		return "CameraConfig[uriFrag=" + myURI_Fragment + ", pos=" + Arrays.toString(cameraPosition) + ", dir=" + Arrays.toString(cameraPointDir) + ", viewport=" + Arrays.toString(cameraViewPort)+ "]";
	}

	public CameraConfig(Item configItem) {
		myURI_Fragment = configItem.getIdent().getLocalName();
		cameraName = ItemFuncs.getString(configItem, SceneConfigNames.P_cameraName, null);
		for (int index=0; index<3; index++) {
			cameraPosition[index] = ItemFuncs.getDouble(configItem, SceneConfigNames.P_position[index], null).floatValue();
			cameraPointDir[index] = ItemFuncs.getDouble(configItem, SceneConfigNames.P_direction[index], null).floatValue();
		}
		for (int index=0; index<cameraViewPort.length; index++) {
			cameraViewPort[index] = ItemFuncs.getDouble(configItem, SceneConfigNames.P_viewport[index], null).floatValue();
		}
	}

}
