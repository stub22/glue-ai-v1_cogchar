/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Box;
import org.appdapter.core.name.Ident;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.cogchar.render.sys.goody.GoodyRenderRegistryClient;

/**
 *
 * @author Ryan Biggs
 */


public class GoodyBox extends BasicGoodyEntity {
	
	private final ColorRGBA DEFAULT_COLOR = ColorRGBA.LightGray;

	public GoodyBox(GoodyRenderRegistryClient aRenderRegCli, Ident boxUri, Vector3f position, Quaternion rotation, 
			ColorRGBA color, Vector3f size/*boolean rigidBodyPhysFlag*/) {
		super(aRenderRegCli, boxUri);
		// This check is shared with BitCube and BitBox and should be factored out:
		if (size == null) {
			myLogger.warn("No size specified for GoodyBox, defaulting to size = 1");
			size = new Vector3f(1.0f, 1.0f, 1.0f);
		} else {
			if (Math.abs(size.length() - 0.0f) < 0.001f) {
				myLogger.warn("GoodyBox being created with zero size!");
			}
		} 
		setPosition(position);
		Mesh goodyBox = new Box(size.getX(), size.getY(), size.getZ());
		if (color == null) {
			color = DEFAULT_COLOR;
		}
		addGeometry(goodyBox, color, rotation);
	}
}
