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

package org.cogchar.render.trial;

import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Dome;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import static org.cogchar.render.trial.TrialContent.makeAlphaBlendedUnshadedMaterial;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class PointerCone {
	private	String		myNameSuffix;
	private	Node		myAssemblyNode;
	private	String		myStatusText;
	private	BitmapText	myStatusBTS;
	
	private float		myConeLength = 40.0f;
	private float		myConeWidth = 2.0f;
	
	public PointerCone( String nameSuffix) {
		myNameSuffix = nameSuffix;
	}
	public Node getAssemblyNode() { 
		return myAssemblyNode;
	}
	public void setup(RenderRegistryClient rrc) { 
		myAssemblyNode = new Node("visionPyramid_" + myNameSuffix);
						
		Material mat = TrialContent.makeAlphaBlendedUnshadedMaterial(rrc, 0.7f, 0.0f, 0.9f, 0.7f);
		
		boolean insideView = false;
		Vector3f innerCenter = new Vector3f(0.0f, 0.0f, 0.0f);
		int numPlanes = 2;
		float radius = 1.0f;	
		int numBasePoints = 4;
		// The dome extends in the positive-Y hemisphere, with the top at Y=radius, X=0.0, Z=0.0.
		Dome d = new Dome(innerCenter, numPlanes, numBasePoints, radius, insideView);
		Geometry dg = new Geometry("vpg_" + myNameSuffix, d);
		// Size the cone by width and length
		dg.setLocalScale(myConeWidth, myConeLength, myConeWidth);
		// Position the cone so that the tip is at y=0, and the base is at -ConeLength
		dg.setLocalTranslation(0.0f, -1 * myConeLength, 0.0f);
		dg.setMaterial(mat);
		TrialContent.configureRenderingForSpatial(dg);
	
		myAssemblyNode.attachChild(dg);
		TextSpatialFactory tsf = new TextSpatialFactory(rrc);
		int textWrapCharWidth = 35;
		float txtRegScale = 0.02f;		
		myStatusBTS = tsf.makeTextSpatial(myNameSuffix + "\nstatusText\nst-two\nst-three", txtRegScale, RenderQueue.Bucket.Transparent, textWrapCharWidth);
		myAssemblyNode.attachChild(myStatusBTS);
		// Rotate cone so that tip-to-base points in the Z direction, appropriate for indicating
		// the direction of a camera.
		Matrix3f rotMatrix = new Matrix3f();
		rotMatrix.fromAngleAxis(-1.0f * FastMath.HALF_PI, Vector3f.UNIT_X);
		myAssemblyNode.setLocalRotation(rotMatrix);
		// Rotate status-text the opposite way, so it is facing same direction as camera lens.
		Matrix3f oppRotMatrix = rotMatrix.transposeNew();
		myStatusBTS.setLocalRotation(oppRotMatrix);
	}
	public void setTextPositionFraction(float posFrac) {
		float lengthFrac = myConeLength * posFrac;
		myStatusBTS.setLocalTranslation(0.0f, -1.0f * lengthFrac, 0.0f);
		
	}
	public void doUpdate(RenderRegistryClient rrc, float tpf) {
		// TODO:  Make this update less frequent, and more thrify in terms of object alloc.
		Transform worldXform = myAssemblyNode.getWorldTransform();
		myStatusText = myNameSuffix + "\n" + worldXform.toString();
		myStatusBTS.setText(myStatusText);
	}
}
