/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
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


package org.cogchar.render.opengl.bony.model;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class SpatialManipFuncs {
	public static void dumpNodeTree(Node sceneNode, String childPrefix) {
		AnimControl ac = sceneNode.getControl(AnimControl.class);
		if (ac != null) {
			System.out.println (childPrefix + " CONTROL " + ac);
			Skeleton skel = ac.getSkeleton();
			if (skel != null) {
				System.out.println (childPrefix + "    SKELETON " + skel);
			}
		}
		for (Spatial sk : sceneNode.getChildren()) {
			System.out.println(childPrefix + sk);
			if (sk instanceof Node) {
				dumpNodeTree ((Node) sk, "  " + childPrefix);
			}
		}
	}
	public static List<AnimControl> findAnimControls(Node sceneNode) {
		List<AnimControl> results = new ArrayList<AnimControl>();
		AnimControl ac = sceneNode.getControl(AnimControl.class);
		if (ac != null) {
			results.add(ac);
		}
	// else {
			for (Spatial sceneChild : sceneNode.getChildren()) {
				if (sceneChild instanceof Node) {
					List<AnimControl> subRes = findAnimControls((Node) sceneChild);
					results.addAll(subRes);
				} else {
					// We wind up seeing some "Geo
					System.out.println("Ignoring scene child " + sceneChild + " of class " + sceneChild.getClass());
				}
			}
	//	}
		return results;
	}
	public static void resetBonesAndPrintInfo(List<AnimControl> animControls) { 
		for (AnimControl ac : animControls) {
			Skeleton csk = ac.getSkeleton();
			csk.reset();  		// Forces bones to absorb default-bind-pose coords from nodes.
			Bone roots[] = csk.getRoots();
			Bone rb = roots[0];
			Vector3f localPos = rb.getLocalPosition();
			Vector3f modelPos = rb.getModelSpacePosition();
			System.out.println("root bone=" + rb + ", localPos=" + localPos + ", modelPos=" + modelPos);
			List<Bone> bkl = rb.getChildren();
			for (Bone bk : bkl) {
				System.out.println("child bone=" + bk + ", localPos=" + bk.getLocalPosition() + ", modelPos=" + bk.getModelSpacePosition());
			}
		}			
	}		
		/*
		 * These mesh.xml files are excluded from assets.jar in JME default setup
		 * (see "assets" section of project properties)
		 */
		// excludes: */*.mesh.xml,**/*.skeleton.xml,**/*.scene,**/*.material,**/*.meshxml,**/*.skeletonxml,**/*.obj,**/*.mtl
/*
		otoNode = (Node) assetManager.loadModel("Models/OtoSamp/Oto.mesh.xml");
		otoNode.setLocalScale(0.5f);
		rootNode.attachChild(otoNode);

		control = otoNode.getControl(AnimControl.class);
		control.addListener(this);
		channel = control.createChannel();
		channel.setAnim("stand");
		// Dodge, push, pull, Walk, Stand

		SkeletonDebugger skeletonDebug = new SkeletonDebugger("skeleton", control.getSkeleton());
		Material mat = new Material(assetManager, "Common/MatDefs/Misc/WireColor.j3md");
		mat.setColor("m_Color", ColorRGBA.Green);
		mat.getAdditionalRenderState().setDepthTest(false);
		skeletonDebug.setMaterial(mat);
		otoNode.attachChild(skeletonDebug);
		 */
	
	/*
	 * found sceneKid spatial: body-node (Node)
found sceneKid spatial: rightArm-node (Node)
java.lang.NullPointerException
found sceneKid spatial: leftArm-node (Node)
found sceneKid spatial: head-node (Node)
	 */
/*
 *
 void	setLocalRotation(Matrix3f rotation)
          setLocalRotation sets the local rotation of this node.
 void	setLocalRotation(Quaternion quaternion)
          setLocalRotation sets the local rotation of this node, using a quaterion to build the matrix.
 void	setLocalScale(float localScale)
          setLocalScale sets the local scale of this node.
 void	setLocalScale(float x, float y, float z)
          setLocalScale sets the local scale of this node.
 void	setLocalScale(Vector3f localScale)
          setLocalScale sets the local scale of this node.
 void	setLocalTransform(Transform t)
          setLocalTransform sets the local transform of this spatial.
 void	setLocalTranslation(float x, float y, float z)
          setLocalTranslation sets the local translation of this spatial.
 void	setLocalTranslation(Vector3f localTranslation)
          setLocalTranslation sets the local translation of this spatial.
 *
 * updateGeometricState()
          updateGeometricState updates the lightlist, computes the world transforms, and computes the world bounds for this Spatial.
 *
 protected  void	updateWorldTransforms()
          Should only be called from updateGeometricState().
 Vector3f	worldToLocal(Vector3f in, Vector3f store)
          Convert a vector (in) from world coordinate space to this spatials' local coordinate space.
 */
}
