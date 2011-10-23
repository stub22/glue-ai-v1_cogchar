/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.demo.bony;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.Bone;
import com.jme3.animation.LoopMode;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.RagdollCollisionListener;
import com.jme3.bullet.control.KinematicRagdollControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class HumanoidRagdollWrapper implements RagdollCollisionListener, AnimEventListener {
	private Node myHumanoidModelNode;
	private KinematicRagdollControl myHumanoidKRC;
	private AnimChannel myHumanoidAnimChannel;
	private static String 	
			ANIM_STAND_FRONT = "StandUpFront",
			ANIM_STAND_BACK = "StandUpBack",
			ANIM_DANCE = "Dance",
			ANIM_IDLE_TOP = "IdleTop",
			PATH_HUMANOID_MESH = "Models/Sinbad/Sinbad.mesh.xml";
	
	protected void initStuff(AssetManager asstMgr, Node parentNode, PhysicsSpace ps) {
		myHumanoidModelNode = (Node) asstMgr.loadModel(PATH_HUMANOID_MESH);

		// This was commented out in JMonkey code:
		//  myHumanoidModel.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));

		// Turn on the green bone skeleton debug.
		HumanoidMapping.attachDebugSkeleton(myHumanoidModelNode, asstMgr);

		//Note: PhysicsRagdollControl is still TODO, constructor will change
		myHumanoidKRC = new KinematicRagdollControl(0.5f);
		HumanoidMapping.addHumanoidBonesToRagdoll(myHumanoidKRC);
		myHumanoidKRC.addCollisionListener(this);
		myHumanoidModelNode.addControl(myHumanoidKRC);

		HumanoidMapping.applyHumanoidJointLimits(myHumanoidKRC);

		ps.add(myHumanoidKRC);

		parentNode.attachChild(myHumanoidModelNode);
		// rootNode.attachChild(skeletonDebug);

		AnimControl humanoidControl = myHumanoidModelNode.getControl(AnimControl.class);
		myHumanoidAnimChannel = humanoidControl.createChannel();
		humanoidControl.addListener(this);
		
	}
	public void toggleKinMode() {
		myHumanoidKRC.setEnabled(!myHumanoidKRC.isEnabled());
		myHumanoidKRC.setRagdollMode();
	}
	public void standUp() { 
				Vector3f v = new Vector3f();
		v.set(myHumanoidModelNode.getLocalTranslation());
		v.y = 0;
		myHumanoidModelNode.setLocalTranslation(v);
		Quaternion q = new Quaternion();
		float[] angles = new float[3];
		myHumanoidModelNode.getLocalRotation().toAngles(angles);
		q.fromAngleAxis(angles[1], Vector3f.UNIT_Y);
		myHumanoidModelNode.setLocalRotation(q);
		if (angles[0] < 0) {
			myHumanoidAnimChannel.setAnim(ANIM_STAND_BACK);
			myHumanoidKRC.blendToKinematicMode(0.5f);
		} else {
			myHumanoidAnimChannel.setAnim(ANIM_STAND_FRONT);
			myHumanoidKRC.blendToKinematicMode(0.5f);
		}
	}

	public void collide(Bone bone, PhysicsCollisionObject pco, PhysicsCollisionEvent pce) {
		Object userObj = pco.getUserObject();
		if ((userObj != null) && (userObj instanceof Geometry)) {
			Geometry geom = (Geometry) userObj;
			if (BowlAtHumanoidApp.GEOM_FLOOR.equals(geom.getName())) {
				return;
			}
		}
		myHumanoidKRC.setRagdollMode();
	}

	public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
//        if(channel.getAnimationName().equals("StandUpFront")){
//            channel.setAnim("Dance");
//        }

		if (channel.getAnimationName().equals(ANIM_STAND_BACK) || channel.getAnimationName().equals(ANIM_STAND_FRONT)) {
			channel.setLoopMode(LoopMode.DontLoop);
			channel.setAnim(ANIM_IDLE_TOP, 5);
			channel.setLoopMode(LoopMode.Loop);
		}
//        if(channel.getAnimationName().equals("IdleTop")){
//            channel.setAnim("StandUpFront");
//        }

	}

	public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
	}
	
	public void boogie() { 
		myHumanoidAnimChannel.setAnim(ANIM_DANCE);
		myHumanoidKRC.blendToKinematicMode(0.5f);
	}
}
