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
import org.cogchar.blob.emit.HumanoidConfigEmitter;
import org.cogchar.blob.emit.Solution;
import org.cogchar.blob.emit.QueryEmitter;

/**
 * @author Ryan Biggs
 */
public class CameraConfig {

	//public String myURI_Fragment;
	public String cameraName;
	public float[] cameraPosition = new float[3];
	public float[] cameraPointDir = new float[3];
	public float[] cameraViewPort = new float[4];
	public Ident attachedRobot;
	public String attachedItem;

	@Override
	public String toString() {
		return "CameraConfig[name=" + cameraName + ", pos=" + Arrays.toString(cameraPosition) + ", dir=" + Arrays.toString(cameraPointDir) + ", viewport=" + Arrays.toString(cameraViewPort) + "]";
	}

	// A new constructor to build CameraConfig from spreadsheet
	public CameraConfig(Solution querySolution) {
		cameraName = QueryEmitter.getIdentFromSolution(querySolution, LightsCameraQueryNames.CAMERA_NAME_VAR_NAME).getLocalName();
		for (int index = 0; index < 3; index++) {
			cameraPosition[index] = QueryEmitter.getFloatFromSolution(querySolution, LightsCameraQueryNames.POSITION_VAR_NAME[index], 0f);
			cameraPointDir[index] = QueryEmitter.getFloatFromSolution(querySolution, LightsCameraQueryNames.DIRECTION_VAR_NAME[index], 0f);
		}
		for (int index = 0; index < cameraViewPort.length; index++) {
			cameraViewPort[index] = QueryEmitter.getFloatFromSolution(querySolution, LightsCameraQueryNames.VIEWPORT_VAR_NAME[index], Float.NaN);
		}
		attachedRobot = QueryEmitter.getIdentFromSolution(querySolution, LightsCameraQueryNames.ATTACHED_ROBOT_VAR_NAME);
		attachedItem = QueryEmitter.getStringFromSolution(querySolution, LightsCameraQueryNames.ATTACHED_BONE_VAR_NAME);
	}

	public CameraConfig(Item configItem) {
		cameraName = configItem.getIdent().getLocalName();
		for (int index = 0; index < 3; index++) {
			cameraPosition[index] = ItemFuncs.getDouble(configItem, LightsCameraConfigNames.P_position[index], 0.0).floatValue();
			cameraPointDir[index] = ItemFuncs.getDouble(configItem, LightsCameraConfigNames.P_direction[index], 0.0).floatValue();
		}
		for (int index = 0; index < cameraViewPort.length; index++) {
			cameraViewPort[index] = ItemFuncs.getDouble(configItem, LightsCameraConfigNames.P_viewport[index], null).floatValue();
		}
		// Would be nice to get attachedRobot as an Ident, but not possible with ItemFuncs
		String attachedRobotId = ItemFuncs.getString(configItem, LightsCameraConfigNames.P_attachedRobot, null);
		attachedRobot = HumanoidConfigEmitter.getRobotIdent(attachedRobotId);
		attachedItem = ItemFuncs.getString(configItem, LightsCameraConfigNames.P_attachedItem, null);
	}
}
