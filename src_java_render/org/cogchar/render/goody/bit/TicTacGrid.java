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

package org.cogchar.render.goody.bit;

import org.cogchar.render.goody.basic.BasicGoodyImpl;
import org.cogchar.render.app.goody.GoodyAction;
import org.cogchar.render.app.goody.GoodySpace;
import org.cogchar.render.app.goody.GoodyFactory;
import org.cogchar.name.goody.GoodyNames;
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
import org.cogchar.render.goody.basic.CompositeMeshBuilder;
import org.cogchar.render.goody.basic.CompositeMeshBuilder.MeshComponent;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 * A class to implement the Robosteps Tic-tac-toe grid objects and functionality
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class TicTacGrid extends BasicGoodyImpl {
	
	private static final ColorRGBA DEFAULT_GRID_COLOR = ColorRGBA.Blue;
	private static final float SIZE_MULTIPLIER = 9f;
	private static final float[] ROTATE_UPRIGHT = {(float)(Math.PI/2), 0f, 0f};
	private static final Ident CLEAR_IDENT = GoodyNames.makeID("clearMarks");
	
	private Map<Ident, TicTacMark> markMap = new HashMap<Ident, TicTacMark>();
	
	public TicTacGrid(RenderRegistryClient aRenderRegCli, Ident boxUri, Vector3f initialPosition, 
			Quaternion initialRotation, ColorRGBA color, Vector3f size) {
		super(aRenderRegCli, boxUri);
		setPositionRotationAndScale(initialPosition, initialRotation, size);
		Mesh gridMesh = makeCustomGridMesh();
		if (color == null) {
			color = DEFAULT_GRID_COLOR;
		}
		addGeometry(gridMesh, color, new Quaternion(ROTATE_UPRIGHT));
	}
	
	private Mesh makeCustomGridMesh() {
		Mesh gridLeg = new Cylinder(20, 20, 1f/5f, SIZE_MULTIPLIER, true);
		float offsetDistance = SIZE_MULTIPLIER/6f;
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
		Ident markUri = createMarkIdent(xPos, yPos);
		if ((xPos < 1) || (xPos > 3) || (yPos < 1) || (yPos > 3)) {
			myLogger.error("Can't add TicTacMark to grid; position of ({}, {}) is invalid", xPos, yPos);
		} else if (markMap.containsKey(markUri)) {
			myLogger.warn("Can't add TicTacMark to grid; there is already a mark at position ({}, {})", xPos, yPos);
		} else {
			Vector3f markPosition = getWorldPositionForMark(xPos, yPos);
			TicTacMark markGoody = 
					new TicTacMark(myRenderRegCli, markUri, markPosition, myRotation, myScale, isPlayerO);
			getTheGoodySpace().addGoody(markGoody);
			markGoody.attachToVirtualWorldNode(myRootNode);
			markMap.put(markUri, markGoody);
		}
	}
	
	public void removeMark(int xPos, int yPos) {
		Ident markIdent = createMarkIdent(xPos, yPos);
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
	
	private Ident createMarkIdent(int xPos, int yPos) {
		String uriString = myUri.getAbsUriString();
		uriString += "Mark" + xPos + yPos;
		return new FreeIdent(uriString);
	}
	
	private int[] getGridPosition(Ident markIdent) {
		String markName = markIdent.getLocalName();
		// Mark x and y are part of local name per createMarkIdent:
		int xPos = Integer.valueOf(String.valueOf(markName.charAt(markName.length() - 2)));
		int yPos = Integer.valueOf(String.valueOf(markName.charAt(markName.length() - 1)));
		return new int[]{xPos, yPos};
	}
	
	private Vector3f getWorldPositionForMark(int xPos, int yPos) {
		float markOffsetX = SIZE_MULTIPLIER*myScale.getX()/3f;
		float markOffsetY = SIZE_MULTIPLIER*myScale.getY()/3f;
		Vector3f relativeMarkPosition = new Vector3f(markOffsetX*(xPos-2), -markOffsetY*(yPos-2), 0);
		// Now rotate positions according to myRotation
		relativeMarkPosition = myRotation.mult(relativeMarkPosition);
		return myPosition.add(relativeMarkPosition); 
	}
	
	private Vector3f getWorldPositionForMark(TicTacMark mark) {
		int[] gridPosition = getGridPosition(mark.getUri());
		return getWorldPositionForMark(gridPosition[0], gridPosition[1]);
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
	public void setPositionAndRotation(Vector3f newPosition, Quaternion newRotation) {
		setPositionRotationAndScale(newPosition, newRotation, myScale);
	}
	
	@Override
	public void setScale(Float newScale) {
		setVectorScale(new Vector3f(newScale, newScale, newScale));
	}
	@Override
	public void setVectorScale(Vector3f newScale) {
		setPositionRotationAndScale(myPosition, myRotation, newScale);
	}
	
	final public void setPositionRotationAndScale(Vector3f newPosition, Quaternion newRotation, Vector3f newScale) {
		super.setPositionAndRotation(newPosition, newRotation);
		super.setVectorScale(newScale);
		for (TicTacMark markGoody : markMap.values()) {
			markGoody.setVectorScale(newScale);
			markGoody.setPositionAndRotation(getWorldPositionForMark(markGoody), newRotation);
		}
	}
	
	@Override
	protected void moveViaAnimation(Vector3f newPosition, Quaternion newOrientation, Vector3f newScale, float duration) {
		myLogger.warn("MOVE not yet supported for TicTacGrid, coming soon...");
	}
	
	@Override
	public void applyAction(GoodyAction ga) {
		super.applyAction(ga);
		switch (ga.getKind()) {
			case SET : {
				String removeString = ga.getSpecialString(CLEAR_IDENT);
				String stateString = ga.getSpecialString(GoodyNames.USE_O);
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
						int xCoord = Integer.valueOf(ga.getSpecialString(GoodyNames.COORDINATE_X));
						int yCoord = Integer.valueOf(ga.getSpecialString(GoodyNames.COORDINATE_Y));
						addMarkAt(xCoord, yCoord, Boolean.valueOf(stateString));
					} catch (Exception e) { // May not need try/catch after BasicTypedValueMap implementation is complete
						myLogger.error("Error interpreting parameters for adding mark to TicTacGrid", e);
					}
				}
				break;
			}
		}
	}
}
