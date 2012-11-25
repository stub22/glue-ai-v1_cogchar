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

import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Torus;
import org.appdapter.core.name.Ident;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 * A class to implement the Robosteps "BitBox" objects, which may not turn out to be boxes at all
 * 
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class BitBox extends BasicGoodyImpl {
	
	private boolean state = false;
	private int zeroIndex;
	private int oneIndex;
	
	// A new BitBox has false (0) state as currently implemented
	BitBox(RenderRegistryClient aRenderRegCli, Ident boxUri, Vector3f initialPosition, float size) {
		super(aRenderRegCli, boxUri);
		setPosition(initialPosition);
		Mesh zeroMesh = new Torus(40,20,size/5,size*5/6);
		Mesh oneMesh = new Cylinder(20, 20, size/5, size*2, true);
		zeroIndex = addGeometry(zeroMesh, ColorRGBA.Blue);
		float[] oneRotationAngles = {(float)(Math.PI/2), 0f, 0f};
		oneIndex = addGeometry(oneMesh, ColorRGBA.Red, new Quaternion(oneRotationAngles));
	}
	
	@Override
	void attachToVirtualWorldNode(final Node rootNode) {
		attachToVirtualWorldNode(rootNode, zeroIndex);
	}
	
	public void setZeroState() {
		setGeometryByIndex(zeroIndex);
		state = false;
	}
	
	public void setOneState() {
		setGeometryByIndex(oneIndex);
		state = true;
	}
	
	public void toggleState() {
		if (state) {
			setZeroState();
		} else {
			setOneState();
		}
	}
	
}
