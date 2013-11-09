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
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import java.util.concurrent.Callable;
import org.appdapter.core.name.Ident;
import org.cogchar.api.cinema.CameraConfig;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.cogchar.render.sys.task.Queuer;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class CameraBinding  {
	public enum Kind {
		DEFAULT,
		TOP_VIEW,
		WIDE_VIEW,
		HEAD_CAM
	}	
	private		Kind			myKind;
	protected	Ident			myIdent;	
	
	private		String			myCamName;
	private		Vector3f		myWorldPosVec3f, myPointDirVec3f;
	
	private		float			myViewX1, myViewX2, myViewY1, myViewY2;
	
	private		Camera			myCam;
	private		ViewPort		myViewport;
	private		Node			myAttachmentNode;
	
	private		Queuer			myQueuer;
	private		RenderRegistryClient	myRRC;

	public CameraBinding(RenderRegistryClient rrc, Ident requiredID) {
		myIdent = requiredID;
		myQueuer = new Queuer(rrc);
		myRRC = rrc;
	}

	public void setupCamera() { 
		
	}
	
	public void setValsFromConfig(CameraConfig cc) { 
		if (cc.myCamName != null) {
			myCamName = cc.myCamName;
		}
		if (cc.myCamPos != null) {
			myWorldPosVec3f = new Vector3f(cc.myCamPos[0], cc.myCamPos[1], cc.myCamPos[2]);
		}
		if (cc.myCamPointDir != null) {
			myPointDirVec3f = new Vector3f(cc.myCamPointDir[0], cc.myCamPointDir[1], cc.myCamPointDir[2]);
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
			myCam.setViewPort(myViewX1, myViewX2, myViewY1, myViewY2);
		}
		if (myViewport != null) {
			
		}
	}
}
