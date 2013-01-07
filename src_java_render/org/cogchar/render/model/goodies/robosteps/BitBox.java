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

package org.cogchar.render.model.goodies.robosteps;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Torus;
import org.appdapter.core.name.Ident;
import org.cogchar.render.model.goodies.BasicGoodyImpl;
import org.cogchar.render.model.goodies.GoodyAction;
import org.cogchar.render.model.goodies.GoodyNames;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 * A class to implement the Robosteps "BitBox" objects, which may not turn out to be boxes at all
 * 
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class BitBox extends BasicGoodyImpl {
	
	private static final ColorRGBA FALSE_COLOR = ColorRGBA.Blue;
	private static final ColorRGBA TRUE_COLOR = ColorRGBA.Red;
	
	private boolean state = false;
	private int zeroIndex;
	private int oneIndex;
	
	public BitBox(RenderRegistryClient aRenderRegCli, Ident boxUri, Vector3f initialPosition, Quaternion initialRotation,
			Vector3f size, boolean boxState) {
		super(aRenderRegCli, boxUri);
		//myLogger.info("Making a BitBox: size={}, position={}, state={}, URI={}", //TEST ONLY
		//	new Object[]{size, initialPosition, boxState, boxUri.getAbsUriString()}); //TEST ONLY
		if (size == null) {
			myLogger.warn("No size specified for BitBox, defaulting to size = 1");
			size = new Vector3f(1.0f, 1.0f, 1.0f);
		} else {
			if (Math.abs(size.length() - 0.0f) < 0.001f) {
				myLogger.warn("BitBox being created with zero size!");
			}
		} 
		setPositionAndRotation(initialPosition, initialRotation);
		setVectorScale(size);
		Mesh zeroMesh = new Torus(40,20,1f/5f,5f/6f);
		Mesh oneMesh = new Cylinder(20, 20, 1f/5f, 2f, true);
		zeroIndex = addGeometry(zeroMesh, FALSE_COLOR);
		float[] oneRotationAngles = {(float)(Math.PI/2), 0f, 0f};
		oneIndex = addGeometry(oneMesh, TRUE_COLOR, new Quaternion(oneRotationAngles));
		state = boxState;
	}

	@Override
	public void attachToVirtualWorldNode(final Node rootNode) {
		attachToVirtualWorldNode(rootNode, state? oneIndex : zeroIndex);
	}
	public void attachToVirtualWorldNode(final Node rootNode, boolean boxState) {
		state = boxState;
		attachToVirtualWorldNode(rootNode);
	}

	public void setZeroState() {
		setState(false);
	}
	
	public void setOneState() {
		setState(true);
	}
	
	public void setState(boolean boxState) {
		int geometryIndex = boxState? oneIndex : zeroIndex;
		setGeometryByIndex(geometryIndex);
		state = boxState;
	}
	
	public void toggleState() {
		setState(!state);
	}
	
	@Override
	public void applyAction(GoodyAction ga) {
		super.applyAction(ga); // Applies "standard" set and move actions
		// Now we act on anything else that won't be handled by BasicGoodyImpl but which has valid non-null parameters
		switch (ga.getKind()) {
			case SET : {
				String stateString = ga.getSpecialString(GoodyNames.BOOLEAN_STATE);
				if (stateString != null) {
					try {
						setState(Boolean.valueOf(stateString));
					} catch (Exception e) { // May not need try/catch after BasicTypedValueMap implementation is complete
						myLogger.error("Error setting box state to state string {}", stateString, e);
					}
				}
				break;
			}
		}
	}
	
}
