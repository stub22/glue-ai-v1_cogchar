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
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Torus;
import org.appdapter.core.name.Ident;

/**
 * A class to implement the Robosteps "BitBox" objects, which may not turn out to be boxes at all
 * 
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class BitBox extends BasicVirtualThing {
	
	private boolean state = false;
	private Mesh zeroMesh;
	private Mesh oneMesh;
	
	// A new BitBox has false (0) state as currently implemented
	BitBox(Ident boxUri, Vector3f initialPosition, float size) {
		uri = boxUri;
		position = initialPosition;
		zeroMesh = new Box(size/5, size, size/5);
		oneMesh = new Torus(20,20,size/5,size*4/5);
		mesh = zeroMesh;
	}
	
	public void setZeroState() {
		changeMesh(zeroMesh);
		setMaterialColor(ColorRGBA.Blue);
		state = false;
	}
	
	public void setOneState() {
		changeMesh(oneMesh);
		setMaterialColor(ColorRGBA.Red);
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
