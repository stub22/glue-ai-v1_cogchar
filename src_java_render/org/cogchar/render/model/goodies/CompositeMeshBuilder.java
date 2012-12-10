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

package org.cogchar.render.model.goodies;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import java.util.ArrayList;
import java.util.List;
import jme3tools.optimize.GeometryBatchFactory;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class CompositeMeshBuilder {
	
	//static final Quaternion ROTATE_45_DEG_AROUND_Y =
	//static final Quaternion ROTATE_90_DEG_AROUND_Y = 
	
	public static class MeshComponent {
		Mesh myMesh;
		Quaternion myOrientation;
		Vector3f myOffset;
		
		public MeshComponent(Mesh aMesh, Quaternion anOrientation, Vector3f anOffset) {
			myMesh = aMesh;
			myOrientation = anOrientation;
			myOffset = anOffset;
		}
		
		public MeshComponent(Mesh aMesh, Quaternion anOrientation) {
			this(aMesh, anOrientation, new Vector3f());
		}
		
		public MeshComponent(Mesh aMesh, Vector3f anOffset) {
			this(aMesh, new Quaternion(), anOffset);
		}
		
		public MeshComponent(Mesh aMesh) {
			this(aMesh, new Quaternion(), new Vector3f());
		}
	}
	
	public Mesh makeCompositeMesh(List<MeshComponent> components) {
		List geoCollection = new ArrayList<Geometry>();
		for (MeshComponent component : components) {
			Geometry componentGeometry = new Geometry(null, component.myMesh);
			componentGeometry.setLocalRotation(component.myOrientation);
			componentGeometry.setLocalTranslation(component.myOffset);
			geoCollection.add(componentGeometry);
		}
		Mesh newMesh = new Mesh();
		GeometryBatchFactory.mergeGeometries(geoCollection, newMesh);
		return newMesh;
	}
	
}
