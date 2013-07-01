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

package org.cogchar.render.opengl.optic;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.control.CameraControl.ControlDirection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.api.cinema.PathInstanceConfig;
import org.cogchar.api.cinema.SpatialActionConfig;
import org.cogchar.api.cinema.WaypointConfig;
import org.cogchar.name.dir.NamespaceDir;
import org.cogchar.name.goody.GoodyNames;
import org.cogchar.render.app.entity.GoodyAction;
import org.cogchar.render.app.entity.GoodyFactory;
import org.cogchar.render.app.entity.VWorldEntity;
import org.cogchar.render.app.entity.VWorldEntityActionConsumer;
import org.cogchar.render.goody.basic.BasicGoodyEntity;
import org.cogchar.render.opengl.scene.PathMgr;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 * Much as with HumanoidFigureGoodyWrapper, this is a kludge to allow us to control Cameras from the Robosteps API
 * until the GoodySpace becomes the EntitySpace
 * 
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class VWorldCameraEntity extends VWorldEntity {
	
	private Camera myCamera;
	
	public VWorldCameraEntity(RenderRegistryClient aRenderRegCli, Ident cameraUri, Camera theCamera) {
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
	
	protected void moveViaPath(Vector3f newPosition, Quaternion newOrientation, float duration) {
		Vector3f currentPosition = myCamera.getLocation();
		Quaternion currentOrientation = myCamera.getRotation();
		if (newPosition == null) {
			//newPosition = currentPosition; // Eventually this should work, but...
			myLogger.warn("No new position specified for Camera MOVE operation -- currently MOVEs of rotation only are not supported.");
			return;
		}
		if (newOrientation == null) {
			newOrientation = currentOrientation;
		}
		List<WaypointConfig> waypoints = new ArrayList<WaypointConfig>();
		waypoints.add(new WaypointConfig(new FreeIdent(NamespaceDir.NS_CCRT_RT + "Start"), currentPosition.toArray(new float[3])));
		waypoints.add(new WaypointConfig(new FreeIdent(NamespaceDir.NS_CCRT_RT + "End"), newPosition.toArray(new float[3])));
		Ident pathUri = new FreeIdent(NamespaceDir.NS_CCRT_RT + "CamMovePath");
		PathInstanceConfig cameraPath = new PathInstanceConfig(myUri, SpatialActionConfig.AttachedItemType.CAMERA, 
				duration, newOrientation.toAngles(new float[3]), waypoints, pathUri);
		PathMgr pMgr = myRenderRegCli.getScenePathFacade(null);
		pMgr.buildAnimation(cameraPath);
		pMgr.controlAnimationByName(pathUri, PathMgr.ControlAction.PLAY);
	}
	
	public void attachToGoody(BasicGoodyEntity attachedGoody) {
		//create the camera Node
		final CameraNode camNode = new CameraNode("Camera Node of " + myUri.getLocalName(), myCamera);
		//Get the Goody's Node
		final Node goodyNode = attachedGoody.getNode();
		myLogger.info("Attaching camNode to goodyNode {}", attachedGoody.getUri()); // TEST ONLY
		enqueueForJmeAndWait(new Callable() { // Do this on main render thread
			@Override
			public Void call() throws Exception {
				//Move camNode, e.g. behind and above the target:
				camNode.setLocalTranslation(new Vector3f(0, 5, -5)); // ... for initial hack-up; probably these offsets need to come from GoodyAction
				//Rotate the camNode to look at the target:
				camNode.lookAt(goodyNode.getLocalTranslation(), Vector3f.UNIT_Y);
				//This mode means that camera copies the movements of the target:
				camNode.setControlDir(ControlDirection.SpatialToCamera);
				//Attach the camNode to the Goody Node:
				goodyNode.attachChild(camNode);
				//And the root node -- required?
				myRenderRegCli.getJme3RootDeepNode(null).attachChild(camNode);
				return null;
			}
		});
		
		camNode.setEnabled(true);
	}

	// Searches for the URI specified by the input string and attaches to the Goody if found
	// Fairly ugly getting into the list of goodies by GoodyFactory.getTheFactory().getActionConsumer().getGoody
	private void attachToGoody(String goodyUriString) {
		if (goodyUriString != null) {
			Ident goodyUri = new FreeIdent(goodyUriString);
			VWorldEntityActionConsumer consumer = GoodyFactory.getTheFactory().getActionConsumer();
			VWorldEntity goodyToAttach = consumer.getGoody(goodyUri);
			// Can avoid this instanceof code smell by refactoring here or in VWorldEntityActionConsumer;
			// All this stuff needs refactoring in any case to separate cameras out from the "Goody" concept, among many
			// other reasons...
			if ((goodyToAttach != null) && (goodyToAttach instanceof BasicGoodyEntity)) {
				attachToGoody((BasicGoodyEntity)goodyToAttach);
			}
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
		String attachToGoodyUriString = ga.getSpecialString(GoodyNames.ATTACH_TO_GOODY);
		switch (ga.getKind()) {
			case SET : {
				setNewPositionAndRotationIfNonNull(newLocation, newRotation);
				attachToGoody(attachToGoodyUriString);
				break;
			}
			case MOVE : {
				Float timeEnroute = ga.getTravelTime();
				if (timeEnroute == null) {	
					setNewPositionAndRotationIfNonNull(newLocation, newRotation);
				} else {
					moveViaPath(newLocation, newRotation, timeEnroute);
				}
				break;
			}
			default: {
				myLogger.error("Unknown action requested in CameraGoodyWrapper {}: {}", myUri.getLocalName(), ga.getKind().name());
			}
		}
	};
	
}
