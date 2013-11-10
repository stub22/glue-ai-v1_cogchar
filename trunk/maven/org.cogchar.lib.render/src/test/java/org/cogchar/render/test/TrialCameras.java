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

package org.cogchar.render.test;

import com.jme3.math.Vector3f;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.render.opengl.optic.CameraMgr;
import org.cogchar.render.sys.context.CogcharRenderContext;
import org.cogchar.render.sys.registry.RenderRegistryClient;

import org.cogchar.api.cinema.CameraConfig;
import org.cogchar.render.app.entity.CameraBinding;
import org.cogchar.render.sys.task.Queuer;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class TrialCameras extends BasicDebugger implements ParamValueListener {

	enum CamCoord {
		AZIMUTH,
		ELEVATION,
		DEPTH
	}

	private		CameraBinding		myWackyCamBinding;
	
	private	Float	myAzimuth, myElevation, myDepth;
	
	public void setupCamerasAndViews(RenderRegistryClient rrc, CogcharRenderContext crc) { 

		float[] camPos = new float[] {0.0f, 10.0f, 10.0f}; 
		float[] camPointDir = new float[] {0.0f, -2.0f, -1.0f};
		float[] displayRect = new float[] {0.7f, 0.9f, 0.7f, 0.9f};
		
		Ident id01 = new FreeIdent("uri:TestCam#cam_01");
		CameraConfig cconf_ul = new CameraConfig(id01, camPos, camPointDir, displayRect);
		
		CameraMgr camMgr = rrc.getOpticCameraFacade(null);
		// In order for the resulting Queuer to be non-null
		myWackyCamBinding =  camMgr.findOrMakeCameraBinding(id01);

		boolean assignDefaults = true;
		
		myWackyCamBinding.setValsFromConfig(cconf_ul, assignDefaults);
		myWackyCamBinding.attachViewPort(rrc);
		myWackyCamBinding.applyInVWorld(Queuer.QueueingStyle.QUEUE_AND_RETURN);
		
	}
	
	protected void attachMidiCCs(TempMidiBridge tmb) { 
		tmb.putControlChangeParamBinding(27, CamCoord.AZIMUTH.name(), this); 
		tmb.putControlChangeParamBinding(28, CamCoord.ELEVATION.name(), this); 
		tmb.putControlChangeParamBinding(40, CamCoord.DEPTH.name(), this); 				
	}	
	
	@Override public void setNormalizedNumericParam(String paramName, float normZeroToOne) {
		Queuer.QueueingStyle qStyle = Queuer.QueueingStyle.QUEUE_AND_RETURN;
		CamCoord ccoord = CamCoord.valueOf(paramName);
		float halfPi = (float) (0.5 * Math.PI);
		switch (ccoord) {
			case AZIMUTH:
				myAzimuth = MathUtils.getFloatValInRange(-1 * halfPi,  halfPi, normZeroToOne);
			break;
			case ELEVATION:
				myElevation = MathUtils.getFloatValInRange(-1 * halfPi,  halfPi, normZeroToOne);				
			break;
			case DEPTH:
				myDepth = MathUtils.getFloatValInRange(-100.0f,  100.0f, normZeroToOne);				
			break;
			default:
				getLogger().warn("Unknown numeric-param channel name: {} resolved to: {}", paramName, ccoord);
		}
		applyToCamBinding();
	}
	
	private void applyToCamBinding() {
		float dirX = 0.0f, dirY = 0.0f;

		if (myAzimuth != null) {
			dirX = (float) Math.sin(myAzimuth);
		}
		if (myElevation != null) {
			dirY = (float) Math.sin(myElevation);
		}		
		Vector3f pointDir = myWackyCamBinding.getPointDir();
		pointDir.x = dirX;
		pointDir.y = dirY;
		// May or may not be necessary, depending on assumptoins about the get/set methods
		myWackyCamBinding.setPointDir(pointDir);
		
		myWackyCamBinding.applyInVWorld(Queuer.QueueingStyle.QUEUE_AND_RETURN);
	}
	
}
