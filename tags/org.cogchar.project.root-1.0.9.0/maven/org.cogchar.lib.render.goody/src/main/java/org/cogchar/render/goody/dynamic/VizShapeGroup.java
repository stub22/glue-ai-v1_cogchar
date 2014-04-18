/*
 *  Copyright 2013 by The Friendularity Project (www.friendularity.org).
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

package org.cogchar.render.goody.dynamic;

import org.cogchar.render.goody.dynamic.VizShape;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;

import org.appdapter.core.name.Ident;
import org.cogchar.render.goody.basic.DataballGoodyBuilder;
import org.cogchar.render.opengl.optic.CameraMgr;
import org.cogchar.render.opengl.optic.MatFactory;
import org.cogchar.render.opengl.scene.DeepSceneMgr;
import org.cogchar.render.opengl.scene.FlatOverlayMgr;
import org.cogchar.render.opengl.scene.GeomFactory;
import org.cogchar.render.opengl.scene.TextMgr;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.slf4j.Logger;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * The purpose of this Group is to keep track of some set of shapes, which generally have
 * common material + shape properties, but may or may not have any spatial relationship to each other.
 * Includes a map of VizShapes by Ident, which each have a geom.
 */

public class VizShapeGroup {
	private		Ident	myGroupID;
	
	private		Map<Ident, VizShape> myShapesByIdent = new HashMap<Ident, VizShape>();
	
	// private		RenderRegistryClient	myRRC;
	private		Material	myStandardMaterial;
	
	public VizShapeGroup(Ident groupID) {
		myGroupID = groupID;
	}
	public void setupMaterials(RenderRegistryClient	rrc) { 
		MatFactory matFactory = rrc.getOpticMaterialFacade(null, null);
		myStandardMaterial = matFactory.makeMatWithOptTexture("Common/MatDefs/Light/Lighting.j3md", "SpecularMap", null);
	}
	public Material getStandardMat() { 
		return myStandardMaterial;
	}
	
	public void configureMemberGeom_onRendThrd(RenderRegistryClient	rrc, VizShape child) {
		Ident childID = child.getIdent();
		Geometry childGeom = child.getGeom();
		myShapesByIdent.put(childID, child);
		if (childGeom == null) {
			child.setupGeom(this, rrc);
			childGeom = child.getGeom();
		}
	}
}
