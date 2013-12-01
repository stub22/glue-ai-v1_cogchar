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

import com.jme3.collision.CollisionResult;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import org.cogchar.api.space.MultiDimGridSpace;
import org.cogchar.api.space.GridSpaceFactory;
import org.cogchar.api.space.CellBlock;
import org.cogchar.api.space.CellRangeFactory;
import org.cogchar.api.space.PosBlock;
import org.cogchar.api.space.PosRange;

import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.shape.Quad;

import org.appdapter.core.log.BasicDebugger;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class TrialNexus extends BasicDebugger {
	private RenderRegistryClient	myRRC;
	public TrialNexus(RenderRegistryClient rrc) {
		myRRC = rrc;
	}
	public void makeSheetspace(Node parentNode, Material  baseMat) {
		int xCount= 7, yCount = 5, zCount = 9;
		MultiDimGridSpace deepSpace = GridSpaceFactory.makeSpace3D(xCount, -40.0f, 40.0f, yCount, -20.0f, 20.0f, zCount, -50.0f, 20.0f);

		getLogger().info("Space description={}", deepSpace.describe()); // cellFrom == 1 -> base-1 labelling
		CellBlock extrudedCellBlock = CellRangeFactory.makeBlock3D(3, 5, -1, 6, 2, 7);
		PosBlock extrudedPosBlock = deepSpace.computePosBlockForCellBlock(extrudedCellBlock);
		getLogger().info("Computed result PosBlock description={}", extrudedPosBlock.describe());	
		
		Node vizNode = new Node("sspace_viz_node");
		parentNode.attachChild(vizNode);

		
		RenderState.BlendMode matBlendMode = RenderState.BlendMode.Alpha;
		RenderQueue.Bucket spatRenderBucket  = RenderQueue.Bucket.Transparent;
		RenderState.FaceCullMode matFaceCullMode = RenderState.FaceCullMode.Off;  		// Render both sides
		Spatial.CullHint spatCullHint = Spatial.CullHint.Never;  // Others are CullHint.Always, CullHint.Inherit
		
		// To get transparency, we also need to put spatials into eligible buckets
		baseMat.getAdditionalRenderState().setBlendMode(matBlendMode);
		baseMat.getAdditionalRenderState().setFaceCullMode(matFaceCullMode);	
		
		TextSpatialFactory tsf = new TextSpatialFactory(myRRC);
	
		int cellCount = xCount * yCount * zCount;
		int seq = 0;
		
		for (int xi = 1 ; xi <= xCount; xi++) {
			for (int yi = 1 ; yi <= yCount; yi++) {
				for (int zi = 1 ; zi <= zCount; zi++) {
					seq++;
					CellBlock unitCB = CellRangeFactory.makeUnitBlock3D(xi, yi, zi);
					PosBlock unitPB = deepSpace.computePosBlockForCellBlock(unitCB);
					getLogger().debug("Unit cell with seq#={} has cellBlock={} and posBlock.description={}", seq, unitCB, unitPB.describe());	
					Material localMat1 = baseMat.clone();		
					localMat1.setColor("Color", new ColorRGBA(0.5f, 0.1f, 0.9f, 0.5f));

					PosRange xpr = unitPB.myPRs()[0];
					PosRange ypr = unitPB.myPRs()[1];
					PosRange zpr = unitPB.myPRs()[2];
					
					String qlabTxt01 = "bq_" + seq  + "_1";
					Geometry qg1 = new Geometry(qlabTxt01, new Quad(5, 5));
					qg1.setMaterial(localMat1);
					qg1.setQueueBucket(spatRenderBucket);
					qg1.setCullHint(spatCullHint);
					
					qg1.setLocalTranslation(xpr.getMin(), ypr.getMin(), zpr.getMin());
					vizNode.attachChild(qg1);
					BitmapText qlabBT_01 = tsf.makeTextSpatial(qlabTxt01, 0.2f, RenderQueue.Bucket.Transparent, 6);	
					qlabBT_01.setLocalTranslation(xpr.getCenter(), ypr.getCenter(), zpr.getMin());
					vizNode.attachChild(qlabBT_01);
					
					Material localMat2 = baseMat.clone();		
					localMat1.setColor("Color", new ColorRGBA(0.9f, 0.8f, 0.1f, 0.5f));					
					
					String qlabTxt02 = "bq_" + seq + "_2";
					Geometry qg2 = new Geometry(qlabTxt02, new Quad(5, 5));
					qg2.setMaterial(localMat2);
					qg2.setQueueBucket(spatRenderBucket);
					qg2.setCullHint(spatCullHint);
					
					qg2.setLocalTranslation(xpr.getMin(), ypr.getMin(), zpr.getMin());
				
					Quaternion rotAboutY_90 = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y);
					qg2.setLocalRotation(rotAboutY_90);
					
					vizNode.attachChild(qg2);
	
									

				}
			}
		}
	}	
}
/*
 * Quaternion rotation = new Quaternion();
	rotation.lookAt(vectorToOther, Vector3f.UNIT_Y);
	* 
	* 
	* 						camNode.setLocalTranslation(cineCam.getLocation());
						camNode.setLocalRotation(cineCam.getRotation());
						camNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
						* 
					CollisionResult closest = coRes.getClosestCollision();
				myArrowMark.setLocalTranslation(closest.getContactPoint());

				Quaternion q = new Quaternion();
				q.lookAt(closest.getContactNormal(), Vector3f.UNIT_Y);
				myArrowMark.setLocalRotation(q);
 */