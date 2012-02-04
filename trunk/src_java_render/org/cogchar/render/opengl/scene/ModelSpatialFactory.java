/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.render.opengl.scene;

import com.jme3.asset.AssetManager;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class ModelSpatialFactory {
	AssetManager	myAssetManager;
	
	private static String OTO_MESH_DEFAULT = "Models/Oto/Oto.mesh.xml";
	private static String ELEPHANT_MESH_DEFAULT = "Models/Elephant/Elephant.mesh.xml";
	private static String SINBAD_MESH_DEFAULT = "Models/Sinbad/Sinbad.mesh.xml";

	public ModelSpatialFactory(AssetManager assetMgr) {
		myAssetManager = assetMgr;
	}
	public Spatial makeSpatialFromMeshPath(String meshPath) {
		Spatial s = myAssetManager.loadModel(meshPath);	
		return s;
	}
	public Spatial makeOtoSpatialFromDefaultPath() {
		return makeSpatialFromMeshPath(OTO_MESH_DEFAULT);
	}
	public Spatial makeElephantSpatialFromDefaultPath() {
		return makeSpatialFromMeshPath(ELEPHANT_MESH_DEFAULT);
	}
	public Spatial makeSinbadSpatialFromDefaultPath() { 
		return makeSpatialFromMeshPath(SINBAD_MESH_DEFAULT);
	}
}
