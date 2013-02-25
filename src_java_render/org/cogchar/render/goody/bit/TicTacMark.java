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

import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Torus;
import java.util.ArrayList;
import java.util.List;
import org.appdapter.core.name.Ident;
import org.cogchar.render.goody.basic.BasicGoodyImpl;
import org.cogchar.render.goody.basic.CompositeMeshBuilder;
import org.cogchar.render.goody.basic.CompositeMeshBuilder.MeshComponent;
import org.cogchar.render.app.goody.GoodyAction;
import org.cogchar.name.goody.GoodyNames;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 * A class to implement the Robosteps Tic-tac-toe mark objects
 * 
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */

// Very similar to BitBox, so should probably be refactored as a descendant of a common "BinaryGoody" class
public class TicTacMark extends BasicGoodyImpl {
	
	private static final ColorRGBA X_COLOR = ColorRGBA.Black;
	private static final ColorRGBA O_COLOR = ColorRGBA.Red;
	
	private boolean playerO = false;
	private int indexX;
	private int indexO;

	public TicTacMark(RenderRegistryClient aRenderRegCli, Ident boxUri, Vector3f initialPosition, Quaternion initialRotation,
			Vector3f size, boolean isPlayerO) {
		super(aRenderRegCli, boxUri);
		setPositionAndRotation(initialPosition, initialRotation);
		setVectorScale(size);
		Mesh meshX = makeCustomXMesh();
		Mesh meshO = new Torus(40,20,1f/5f,5f/6f);
		float[] xRotationAngles = {(float)(Math.PI/2), 0f, 0f};
		indexO = addGeometry(meshO, O_COLOR);
		indexX = addGeometry(meshX, X_COLOR, new Quaternion(xRotationAngles));
		playerO = isPlayerO;
	}
	
	private Mesh makeCustomXMesh() {
		CompositeMeshBuilder builder = new CompositeMeshBuilder();
		Mesh meshXLeg = new Cylinder(20, 20, 1f/5f, 2.25f, true);
		Quaternion rotate45DegAroundY = new Quaternion();
		rotate45DegAroundY.fromAngleAxis((float)Math.PI/4, new Vector3f(0f,1f,0f));
		List<MeshComponent> meshComponents = new ArrayList<MeshComponent>();
		meshComponents.add(new MeshComponent(meshXLeg, rotate45DegAroundY));
		meshComponents.add(new MeshComponent(meshXLeg, rotate45DegAroundY.inverse()));
		return builder.makeCompositeMesh(meshComponents);
	}
	
	@Override
	public void attachToVirtualWorldNode(final Node rootNode) {
		attachToVirtualWorldNode(rootNode, playerO? indexO : indexX);
	}
	public void attachToVirtualWorldNode(final Node rootNode, boolean isAnO) {
		playerO = isAnO;
		attachToVirtualWorldNode(rootNode);
	}
	
	public void setAsX() {
		setState(false);
	}
	
	public void setAsO() {
		setState(true);
	}
	
	private void setState(boolean isAnO) {
		int geometryIndex = isAnO? indexO : indexX;
		setGeometryByIndex(geometryIndex);
		playerO = isAnO;
	}
	
	@Override
	public void applyAction(GoodyAction ga) {
		switch (ga.getKind()) {
			case SET : {
				String stateString = ga.getSpecialString(GoodyNames.USE_O);
				if (stateString != null) {
					try {
						setState(Boolean.valueOf(stateString));
					} catch (Exception e) { // May not need try/catch after BasicTypedValueMap implementation is complete
						myLogger.error("The TicTacMark {} parameter must be either \"true\" or \"false\"; observed value is {}",
								new Object[]{GoodyNames.USE_O.getLocalName(), stateString}, e);
					}
				}
				break;
			}
			default: super.applyAction(ga);
		}
	}
	
}
