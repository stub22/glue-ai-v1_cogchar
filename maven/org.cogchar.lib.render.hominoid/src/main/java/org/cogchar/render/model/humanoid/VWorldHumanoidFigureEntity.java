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

package org.cogchar.render.model.humanoid;

import org.cogchar.render.app.entity.VWorldEntity;

import com.jme3.animation.Animation;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimationFactory;
import com.jme3.animation.LoopMode;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.concurrent.Callable;
import org.appdapter.core.name.Ident;
import org.cogchar.render.app.entity.GoodyActionExtractor;
import org.cogchar.render.app.entity.GoodyActionExtractor;
import org.cogchar.render.app.entity.VWorldEntity;
import org.cogchar.render.model.humanoid.HumanoidFigure;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.cogchar.render.sys.goody.GoodyRenderRegistryClient;

/**
 * A wrapper to turn HumanoidFigures into Goodies that can be acted on in the GoodySpace
 * Almost certainly a temporary kludge to implement move-the-robot-via-Robosteps functionality until
 * we generalize our Robosteps API capabilities via an EntitySpace or etc.
 * 
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class VWorldHumanoidFigureEntity extends VWorldEntity {
	
	final static String MOVE_ANIM_NAME = "HumanoidFigureMoveFactory";
	
	private Node myNode;
	private AnimControl figureControl = new AnimControl();
	
	public VWorldHumanoidFigureEntity(RenderRegistryClient aRenderRegCli, Ident figureUri, HumanoidFigure hf) {
		super(aRenderRegCli, figureUri);
		myNode = hf.getNode();
	}
	
	@Override public void setPosition(final Vector3f position, QueueingStyle qStyle) {
		clearMoveAnimationBindings(); // Removes animation control so we can set position directly after MOVE
		enqueueForJme(new Callable() { // Do this on main render thread
				@Override
				public Void call() throws Exception {
					myNode.setLocalTranslation(position);
					return null;
				}
			}, qStyle);	
	}
	
	public void setRotation(final Quaternion rotation, QueueingStyle qStyle) {
		clearMoveAnimationBindings();  // Removes animation control so we can set rotation directly after MOVE
		enqueueForJme(new Callable() { // Do this on main render thread
				@Override
				public Void call() throws Exception {
					myNode.setLocalRotation(rotation);
					return null;
				}
			}, qStyle);	
	}
	
	protected void setNewPositionAndRotationIfNonNull(Vector3f newPosition, Quaternion newRotation, QueueingStyle qStyle) {
		if (newPosition != null) {
			setPosition(newPosition, qStyle);
		}
		if (newRotation != null) {
			setRotation(newRotation, qStyle);
		}
	}
	
	protected void moveViaAnimation(Vector3f newPosition, Quaternion newOrientation, float duration) {
		clearMoveAnimationBindings();
		Vector3f currentPosition = myNode.getLocalTranslation();
		Quaternion currentOrientation = myNode.getLocalRotation();
		if (newPosition == null) {
			newPosition = currentPosition;
		}
		if (newOrientation == null) {
			newOrientation = currentOrientation;
		}
		AnimationFactory aniFactory = new AnimationFactory(duration, MOVE_ANIM_NAME);
		// First add starting position/rotation to timeline at index 0
		aniFactory.addKeyFrameTranslation(0, currentPosition);
		aniFactory.addKeyFrameRotation(0, currentOrientation);
		// Now add new position/rotation at duration:
		aniFactory.addTimeTranslation(duration, newPosition);
		aniFactory.addTimeRotation(duration, newOrientation);
		// Finally the Animation is generated and linked to the geometry via an AnimationControl
		Animation moveAnimation = aniFactory.buildAnimation();
		figureControl.addAnim(moveAnimation);
		myNode.addControl(figureControl);
		AnimChannel moveChannel = figureControl.createChannel();
		moveChannel.setAnim(MOVE_ANIM_NAME, 0f);
		// Oddly, it seems this needs to be set *after* starting the animation with setAnim:
		moveChannel.setLoopMode(LoopMode.DontLoop);
	}
	
	private void clearMoveAnimationBindings() {
		myNode.removeControl(figureControl); // Just returns false if not attached
		Animation oldAnim = figureControl.getAnim(MOVE_ANIM_NAME);
		if (oldAnim != null) {
			figureControl.removeAnim(oldAnim);
		}
	}
	
	@Override public void setUniformScaleFactor(Float scale, QueueingStyle qStyle) {
		getLogger().warn("setScale not supported in HumanoidFigureGoodyWrapper");
	}
	
	@Override public void attachToVirtualWorldNode(Node attachmentNode, QueueingStyle qStyle) {
		getLogger().warn("attachToVirtualWorldNode not supported in HumanoidFigureGoodyWrapper");
	}
	@Override public  void detachFromVirtualWorldNode(QueueingStyle qStyle) {
		getLogger().warn("detachFromVirtualWorldNode not supported in HumanoidFigureGoodyWrapper");
	}
	
	@Override public void applyAction(GoodyActionExtractor ga, QueueingStyle qStyle) {
		Vector3f newLocation = ga.getLocationVec3f();
		Quaternion newRotation = ga.getRotationQuaternion();
		switch (ga.getKind()) {
			case SET : {
				setNewPositionAndRotationIfNonNull(newLocation, newRotation, qStyle);
				break;
			}
			case MOVE : {
				Float timeEnroute = ga.getTravelTime();
				if (timeEnroute == null) {	
					setNewPositionAndRotationIfNonNull(newLocation, newRotation, qStyle);
				} else {
					moveViaAnimation(newLocation, newRotation, timeEnroute);
				}
				break;
			}
			default: {
				getLogger().error("Unknown action requested in HumanoidFigureGoodyWrapper {}: {}", getUri().getLocalName(), ga.getKind().name());
			}
		}
	};
	
}
