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

package org.cogchar.render.goody.basic;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import java.util.concurrent.Callable;
import org.appdapter.core.name.Ident;
import org.cogchar.render.app.goody.GoodyAction;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 * Much as with HumanoidFigureGoodyWrapper, this is a kludge to allow us to control Cameras from the Robosteps API
 * until the GoodySpace becomes the EntitySpace
 * 
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class CameraGoodyWrapper extends BasicGoody {
	
	private Camera myCamera;
	
	public CameraGoodyWrapper(RenderRegistryClient aRenderRegCli, Ident cameraUri, Camera theCamera) {
		myRenderRegCli = aRenderRegCli;
		myUri = cameraUri;
		myCamera = theCamera;
	}
	
	@Override
	public void setPosition(final Vector3f position) {
		enqueueForJmeAndWait(new Callable() { // Do this on main render thread
				@Override
				public Void call() throws Exception {
					myCamera.setLocation(position);
					return null;
				}
			});	
	}
	
	public void setRotation(final Quaternion rotation) {
		enqueueForJmeAndWait(new Callable() { // Do this on main render thread
				@Override
				public Void call() throws Exception {
					myCamera.setRotation(rotation);
					return null;
				}
			});	
	}
	
	protected void setNewPositionAndRotationIfNonNull(Vector3f newPosition, Quaternion newRotation) {
		if (newPosition != null) {
			setPosition(newPosition);
		}
		if (newRotation != null) {
			setRotation(newRotation);
		}
	}
	
	@Override
	public void setScale(Float scale) {
		myLogger.warn("setScale not supported in CameraGoodyWrapper");
	}
	
	@Override
	public void attachToVirtualWorldNode(Node attachmentNode) {
		myLogger.warn("attachToVirtualWorldNode not supported in CameraGoodyWrapper");
	}
	@Override
	public  void detachFromVirtualWorldNode() {
		myLogger.warn("detachFromVirtualWorldNode not supported in CameraGoodyWrapper");
	}
	
	@Override
	public void applyAction(GoodyAction ga) {
		Vector3f newLocation = ga.getLocationVector();
		Quaternion newRotation = ga.getRotationQuaternion();
		switch (ga.getKind()) {
			case SET : {
				setNewPositionAndRotationIfNonNull(newLocation, newRotation);
				break;
			}
			case MOVE : {
				myLogger.warn("MOVE not currently supported in CameraGoodyWrapper");
				break;
			}
			default: {
				myLogger.error("Unknown action requested in CameraGoodyWrapper {}: {}", myUri.getLocalName(), ga.getKind().name());
			}
		}
	};
	
}
