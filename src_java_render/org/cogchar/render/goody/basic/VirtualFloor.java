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

package org.cogchar.render.goody.basic;

import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Plane;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Box;
import org.appdapter.core.name.Ident;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class VirtualFloor extends BasicGoodyImpl {
	
	private final ColorRGBA DEFAULT_COLOR = ColorRGBA.LightGray;

	public VirtualFloor(RenderRegistryClient aRenderRegCli, Ident floorUri, Vector3f position, ColorRGBA color,
			boolean rigidBodyPhysFlag) {
		super(aRenderRegCli, floorUri);
		setPosition(position);
		// Constants and collision shape below taken from PhysicsStuffBuilder.
		// How much of this stuff do we want to come from repo instead?
		Mesh floorBox = new Box(140f, 0.25f, 140f);
		Plane plane = new Plane();
		plane.setOriginNormal(new Vector3f(0, 0.25f, 0), Vector3f.UNIT_Y);
		if (color == null) {
			color = DEFAULT_COLOR;
		}
		addGeometry(floorBox, null, color, new Quaternion(), new PlaneCollisionShape(plane), 0f);
	}
	
	/*
	@Override
	public void applyAction(GoodyAction ga) {
		// Needs to be populated
	}
	*/
}
