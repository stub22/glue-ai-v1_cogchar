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

import com.jme3.animation.*;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.concurrent.Callable;
import org.appdapter.core.name.Ident;
import org.cogchar.render.app.goody.GoodyAction;
import org.cogchar.render.model.humanoid.HumanoidFigure;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 * A wrapper to turn HumanoidFigures into Goodies that can be acted on in the GoodySpace
 * Almost certainly a temporary kludge to implement move-the-robot-via-Robosteps functionality until
 * we generalize our Robosteps API capabilities via an EntitySpace or etc.
 * 
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class HumanoidFigureGoodyWrapper extends BasicGoody {
	
	HumanoidFigure myHumanoidFigure;
	
	public HumanoidFigureGoodyWrapper(RenderRegistryClient aRenderRegCli, Ident figureUri, HumanoidFigure hf) {
		myRenderRegCli = aRenderRegCli;
		myUri = figureUri;
		myHumanoidFigure = hf;
	}
	
	@Override
	public void setPosition(final Vector3f position) {
		enqueueForJmeAndWait(new Callable() { // Do this on main render thread
				@Override
				public Void call() throws Exception {
					myHumanoidFigure.getNode().setLocalTranslation(position);
					return null;
				}
			});	
	}
	
	public void setRotation(final Quaternion rotation) {
		enqueueForJmeAndWait(new Callable() { // Do this on main render thread
				@Override
				public Void call() throws Exception {
					myHumanoidFigure.getNode().setLocalRotation(rotation);
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
	
	protected void moveViaAnimation(Vector3f newPosition, Quaternion newOrientation, float duration) {
		final String moveAnimName = "HumanoidFigureMoveFactory";
		AnimationFactory aniFactory = new AnimationFactory(duration, moveAnimName);
		// First add starting position/rotation to timeline at index 0
		aniFactory.addKeyFrameTranslation(0, myHumanoidFigure.getNode().getLocalTranslation());
		aniFactory.addKeyFrameRotation(0, myHumanoidFigure.getNode().getLocalRotation());
		// Now add new position/rotation at duration:
		aniFactory.addTimeTranslation(duration, newPosition);
		aniFactory.addTimeRotation(duration, newOrientation);
		// Finally the Animation is generated and linked to the geometry via an AnimationControl
		Animation moveAnimation = aniFactory.buildAnimation();
		AnimControl figureControl = new AnimControl(); // Should this be retained for reuse?
		figureControl.addAnim(moveAnimation);
		myHumanoidFigure.getNode().addControl(figureControl);
		AnimChannel moveChannel = figureControl.createChannel();
		moveChannel.setAnim(moveAnimName, 0f);
		// Oddly, it seems this needs to be set *after* starting the animation with setAnim:
		moveChannel.setLoopMode(LoopMode.DontLoop);
	}
	
	@Override
	public void setScale(Float scale) {
		myLogger.warn("setScale not supported in HumanoidFigureGoodyWrapper");
	}
	
	@Override
	public void attachToVirtualWorldNode(Node attachmentNode) {
		myLogger.warn("attachToVirtualWorldNode not supported in HumanoidFigureGoodyWrapper");
	}
	@Override
	public  void detachFromVirtualWorldNode() {
		myLogger.warn("detachFromVirtualWorldNode not supported in HumanoidFigureGoodyWrapper");
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
				Float timeEnroute = ga.getTravelTime();
				if (timeEnroute == null) {	
					setNewPositionAndRotationIfNonNull(newLocation, newRotation);
				} else {
					moveViaAnimation(newLocation, newRotation, timeEnroute);
				}
			}
			default: {
				myLogger.error("Unknown action requested in HumanoidFigureGoodyWrapper {}: {}", myUri.getLocalName(), ga.getKind().name());
			}
		}
	};
	
}
