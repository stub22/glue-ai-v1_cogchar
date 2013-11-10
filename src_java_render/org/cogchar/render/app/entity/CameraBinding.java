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

package org.cogchar.render.app.entity;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import java.util.concurrent.Callable;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.Ident;
import org.cogchar.api.cinema.CameraConfig;
import org.cogchar.render.opengl.optic.CameraMgr;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.cogchar.render.sys.task.Queuer;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class CameraBinding extends BasicDebugger {
	public static class  ViewRectSpec { 
		public		float			myX1, myX2, myY1, myY2;
		public ViewRectSpec(float[] initVals) {
			myX1 = initVals[0]; myX2 = initVals[1]; myY1 = initVals[2]; myY2 = initVals[3];
		}
	}
	public enum Kind {
		DEFAULT,
//		TOP_VIEW,
//		WIDE_VIEW,
		HEAD_CAM
	}	
	private		Kind			myKind;
	protected	Ident			myIdent;	
	
	private		String			myCamName;
	private		Vector3f		myWorldPosVec3f, myPointDirVec3f;
	private		Vector3f		myDefWorldPosV3f, myDefPointDirV3f;
	
	private		ViewRectSpec	myViewRectSpec;
	
	
	private		Camera			myCam;
	private		ViewPort		myViewport;

	
	private		Queuer			myQueuer;
	// private		RenderRegistryClient	myRRC;

	// public CameraBinding(RenderRegistryClient rrc, Ident requiredID) {
	public CameraBinding(Queuer queuer, Ident requiredID) {		
		myIdent = requiredID;
		myQueuer = queuer; // new Queuer(rrc);
		// myRRC = rrc;
	}
	
	public Ident getIdent() { 
		return myIdent;
	}
	public CameraBinding makeClone(String name, Ident id) { 
		CameraBinding clonedCB = new CameraBinding(myQueuer, id);
		Camera clonedCam = myCam.clone();
		clonedCB.setCamera(clonedCam);
		return clonedCB;
	}
	public Camera getCamera() { 
		return myCam;
	}
	public void setCamera(Camera cam) {
		myCam = cam;
	}

	public void setValsFromConfig(CameraConfig cc, boolean flag_storeDefaults) { 
		if (cc.myCamName != null) {
			myCamName = cc.myCamName;
		}
		if (cc.myCamPos != null) {
			myWorldPosVec3f = new Vector3f(cc.myCamPos[0], cc.myCamPos[1], cc.myCamPos[2]);
			if (flag_storeDefaults) {
				myDefWorldPosV3f = myWorldPosVec3f.clone();
			}
		}
		if (cc.myCamPointDir != null) {
			myPointDirVec3f = new Vector3f(cc.myCamPointDir[0], cc.myCamPointDir[1], cc.myCamPointDir[2]);
			if (flag_storeDefaults) {
				myPointDirVec3f = myPointDirVec3f.clone();
			}
		}
		if (cc.myDisplayRect != null) { 
			myViewRectSpec = new ViewRectSpec(cc.myDisplayRect);
		}
	}
	public void resetToDefault() {
		if (myDefWorldPosV3f != null) {
			getLogger().info("Resetting worldPos to {} ", myDefWorldPosV3f);
			myWorldPosVec3f = myDefWorldPosV3f.clone();
		} else {
			getLogger().warn("Cannot reset worldPos - default is null for name={}", myCamName);
		}
		if (myDefPointDirV3f != null) {
			myPointDirVec3f = myPointDirVec3f.clone();
		}
	}
	public void setWorldPos(Vector3f worldPosVec3f) {
		myWorldPosVec3f = worldPosVec3f;
	}
	public void setPointDir(Vector3f pointDirVec3f) {
		myPointDirVec3f = pointDirVec3f;
	}
	public Vector3f getWorldPos() {
		return myWorldPosVec3f;
	}
	public Vector3f getPointDir() {
		return myPointDirVec3f;
	}
	
	public void applyInVWorld(Queuer.QueueingStyle qStyle) {
		myQueuer.enqueueForJme(new Callable() { // Do this on main render thread
				@Override public Void call() throws Exception {	
					applyCoordinatesOnJmeThread();
					return null;
				}
			}, qStyle);		
	}
	private void applyCoordinatesOnJmeThread() { 
		if (myCam != null) {
			if (myWorldPosVec3f != null) {
				myCam.setLocation(myWorldPosVec3f);
			}
			if (myPointDirVec3f != null) {
				myCam.lookAtDirection(myPointDirVec3f, Vector3f.UNIT_Y);
			}
			if (myViewRectSpec != null)	{
				myCam.setViewPort(myViewRectSpec.myX1, myViewRectSpec.myX2, myViewRectSpec.myY1, myViewRectSpec.myY2);
			}
		}
	}
	public void attachViewPort(RenderRegistryClient rrc) { 
		if (myViewport == null) {
			myViewport = rrc.getJme3RenderManager(null).createPostView(myCamName, myCam); // PostView or MainView?
			myViewport.setClearFlags(true, true, true);
			// BackroundColor is set for main window right now in WorkaroundFuncsMustDie.setupCameraLightAndViewport - yuck. 
			myViewport.setBackgroundColor(ColorRGBA.LightGray); 
			myViewport.attachScene(rrc.getJme3RootDeepNode(null));		
		}
	}
	public void detachViewPort(RenderRegistryClient rrc) { 
		if (myViewport != null) {
			rrc.getJme3RenderManager(null).removePostView(myViewport);
			myViewport = null;
		}
	}
}
