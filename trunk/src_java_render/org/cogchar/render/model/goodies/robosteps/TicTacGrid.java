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
import com.jme3.scene.shape.Cylinder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.render.model.goodies.CompositeMeshBuilder.MeshComponent;
import org.cogchar.render.model.goodies.*;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 * A class to implement the Robosteps Tic-tac-toe grid objects and functionality
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class TicTacGrid extends BasicGoodyImpl {
	
	private static final ColorRGBA GRID_COLOR = ColorRGBA.Blue;
	private static final float SIZE_MULTIPLIER = 9f;
	private static final float[] ROTATE_UPRIGHT = {(float)(Math.PI/2), 0f, 0f};
	private static final Ident CLEAR_IDENT = GoodyNames.makeID("clearMarks");
	
	private float mySize;
	
	private Map<Ident, BasicGoodyImpl> markMap = new HashMap<Ident, BasicGoodyImpl>();
	
	public TicTacGrid(RenderRegistryClient aRenderRegCli, Ident boxUri, Vector3f initialPosition, float size) {
		super(aRenderRegCli, boxUri);
		mySize = size;
		setPosition(initialPosition);
		Mesh gridMesh = makeCustomGridMesh(size);
		addGeometry(gridMesh, GRID_COLOR, new Quaternion(ROTATE_UPRIGHT));
	}
	
	private Mesh makeCustomGridMesh(float size) {
		Mesh gridLeg = new Cylinder(20, 20, size/5, size*SIZE_MULTIPLIER, true);
		float offsetDistance = SIZE_MULTIPLIER*size/6f;
		List<MeshComponent> meshComponents = new ArrayList<MeshComponent>();
		meshComponents.add(new MeshComponent(gridLeg, new Vector3f(offsetDistance, 0f, 0f)));
		meshComponents.add(new MeshComponent(gridLeg, new Vector3f(-offsetDistance, 0f, 0f)));
		Quaternion rotate90DegAroundY = new Quaternion();
		rotate90DegAroundY.fromAngleAxis((float)Math.PI/2, new Vector3f(0f,1f,0f));
		meshComponents.add(new MeshComponent(gridLeg, rotate90DegAroundY, new Vector3f(0f, 0f, offsetDistance)));
		meshComponents.add(new MeshComponent(gridLeg, rotate90DegAroundY, new Vector3f(0f, 0f, -offsetDistance)));
		CompositeMeshBuilder builder = new CompositeMeshBuilder();
		return builder.makeCompositeMesh(meshComponents);
	}
	
	public void addMarkAt(int xPos, int yPos, boolean isPlayerO) {
		Ident markUri = getMarkIdent(xPos, yPos);
		if ((xPos < 1) || (xPos > 3) || (yPos < 1) || (yPos > 3)) {
			myLogger.error("Can't add TicTacMark to grid; position of ({}, {}) is invalid", xPos, yPos);
		} else if (markMap.containsKey(markUri)) {
			myLogger.warn("Can't add TicTacMark to grid; there is already a mark at position ({}, {})", xPos, yPos);
		} else {
			float markOffset = SIZE_MULTIPLIER*mySize/3f;
			Vector3f relativeMarkPosition = new Vector3f(markOffset*(xPos-2), -markOffset*(yPos-2), 0);
			
			BasicGoodyImpl markGoody = 
					new TicTacMark(myRenderRegCli, markUri, myPosition.add(relativeMarkPosition), mySize, isPlayerO);
			getTheGoodySpace().addGoody(markGoody);
			markGoody.attachToVirtualWorldNode(myRootNode);
			markMap.put(markUri, markGoody);
		}
	}
	
	public void removeMark(int xPos, int yPos) {
		Ident markIdent = getMarkIdent(xPos, yPos);
		BasicGoodyImpl markToRemove = markMap.get(markIdent);
		if (markToRemove != null) {
			getTheGoodySpace().removeGoody(markToRemove);
			markMap.remove(markIdent);
		} else {
			myLogger.warn("No TicTacMark to remove at location ({}, {})", xPos, yPos);
		}
	}
	
	public void clearMarks() {
		for (BasicGoodyImpl mark : markMap.values()) {
			getTheGoodySpace().removeGoody(mark);
		}
		markMap.clear();
	}
	
	private Ident getMarkIdent(int xPos, int yPos) {
		String uriString = myUri.getAbsUriString();
		uriString += "Mark" + xPos + yPos;
		return new FreeIdent(uriString);
	}
	
	private GoodySpace getTheGoodySpace() {
		return GoodyFactory.getTheFactory().getTheGoodySpace();
	}
	
	// On detach, we also want to remove all marks
	@Override
	public void detachFromVirtualWorldNode() {
		clearMarks();
		super.detachFromVirtualWorldNode();
	}
	
	@Override
	public void applyAction(GoodyAction ga) {
		switch (ga.getKind()) {
			case SET : {
				String removeString = ga.getSpecialString(CLEAR_IDENT);
				String stateString = ga.getSpecialString(GoodyNames.USE_O);
				Vector3f markGridLocation = ga.getLocationVector();
				if (removeString != null) {
					try {
						if (Boolean.valueOf(removeString)) {
							clearMarks();
						}
					} catch (Exception e) {	
						myLogger.error("Error interpreting string for {}", CLEAR_IDENT.getLocalName());
					}
				} else if (stateString != null) {
					try {
						addMarkAt((int)markGridLocation.getX(), (int)markGridLocation.getY(), 
								Boolean.valueOf(stateString));
					} catch (Exception e) { // May not need try/catch after BasicTypedValueMap implementation is complete
						myLogger.error("Error interpreting parameters for adding mark to TicTacGrid", e);
					}
				}
				break;
			}
			default: super.applyAction(ga);
		}
	}
}
