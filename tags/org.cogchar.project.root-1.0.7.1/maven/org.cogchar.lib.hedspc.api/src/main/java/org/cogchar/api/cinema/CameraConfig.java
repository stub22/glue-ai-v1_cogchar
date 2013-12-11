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

import org.cogchar.name.cinema.LightsCameraCN;
import java.util.Arrays;
import org.appdapter.core.name.Ident;

import org.appdapter.help.repo.Solution;
import org.appdapter.help.repo.SolutionHelper;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.name.cinema.LightsCameraAN;

/**
 * @author Ryan Biggs
 */
public class CameraConfig {

	//public String myURI_Fragment;
//	public String	myCamName;
	public Ident	myCamID;
	
	public float[] myCamPos = new float[3];
	
	public float[] myCamPointDir = new float[3];
	
	/*http://hub.jmonkeyengine.org/wiki/doku.php/jme3:advanced:multiple_camera_views
	 * The four values are read in the following order:
		cam.setViewPort(x1,x2 , y1,y2);
				X-axis from left to right
				Y-axis upwards from bottom to top
	 */			
	public float[] myDisplayRect = new float[4];
	
	public Ident	myAttachedRobotID;
	public String	myAttachedBoneName;
	
	public boolean	myBoneAttachmentFlag = false;

	@Override public String toString() {
		return "CameraConfig[id=" + myCamID + ", pos=" + Arrays.toString(myCamPos) + ", dir=" 
				+ Arrays.toString(myCamPointDir) + ", viewport=" + Arrays.toString(myDisplayRect) + "]";
	}

	// A new constructor to build CameraConfig from spreadsheet
	public CameraConfig(Solution qSoln) {
		

		SolutionHelper sh = new SolutionHelper();
		// myCamName = sh.pullIdent(qSoln, LightsCameraCN.CAMERA_NAME_VAR_NAME).getLocalName();
		myCamID = sh.pullIdent(qSoln, LightsCameraCN.CAMERA_NAME_VAR_NAME);
		for (int index = 0; index < 3; index++) {
			myCamPos[index] = sh.pullFloat(qSoln, LightsCameraCN.POSITION_VAR_NAME[index], 0f);
			myCamPointDir[index] = sh.pullFloat(qSoln, LightsCameraCN.DIRECTION_VAR_NAME[index], 0f);
		}
		for (int index = 0; index < myDisplayRect.length; index++) {
			myDisplayRect[index] = sh.pullFloat(qSoln, LightsCameraCN.VIEWPORT_VAR_NAME[index], Float.NaN);
		}
		myAttachedRobotID = sh.pullIdent(qSoln, LightsCameraCN.ATTACHED_ROBOT_VAR_NAME);
		myAttachedBoneName = sh.pullString(qSoln, LightsCameraCN.ATTACHED_BONE_VAR_NAME);
		if ((myAttachedRobotID != null) && (myAttachedBoneName != null)) {
			// Old way
			// boolean flag_isHeadCam = camID.getLocalName().contains(LightsCameraAN.suffix_HEAD_CAM);
			myBoneAttachmentFlag = true;
		}
	}
	
//	public CameraConfig(String camName, float[] camPos, float[] camPointDir, float[] displayRect) { 
	public CameraConfig(Ident camID, float[] camPos, float[] camPointDir, float[] displayRect) { 
		myCamID = camID;
		myCamPos = camPos;
		myCamPointDir = camPointDir;
		myDisplayRect = displayRect;
	}
	public void setAttachmentNodeParams(Ident	attachedRobotID, String	attachedBoneName) { 
		myAttachedRobotID = attachedRobotID;
		myAttachedBoneName = attachedBoneName;
		myBoneAttachmentFlag = true;
	}

	/* Disabled for now because we needed a method from HumanoidConfigEmitter, which is going away (see below). We can find a way to solve this problem if we decide we need assembler config again
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
		// We no longer have HumanoidConfigEmitter, since it didn't make sense in the context of different robots possibly having different graphs.
		// Will have to find a different way to get the robot ident here if we want to use Assembler config again
		attachedRobot = HumanoidConfigEmitter.getRobotIdent(attachedRobotId);
		attachedItem = ItemFuncs.getString(configItem, LightsCameraConfigNames.P_attachedItem, null);
	}
	*/ 

}
