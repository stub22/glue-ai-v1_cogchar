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

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.Vector3f;
import org.appdapter.core.name.Ident;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class CrossHairGoody extends BasicGoody2dImpl {
	
	public CrossHairGoody(RenderRegistryClient aRenderRegCli, Ident uri, Vector3f positionOffset, float scale) {
		super(aRenderRegCli, uri);
		makeCrossHairs(scale, positionOffset);
	}
	
	private void makeCrossHairs(float scale, Vector3f position) {
		BitmapText bt = setGoodyAttributes("+", scale);
		BitmapFont bf = bt.getFont();
		float crossHalfWidthFraction = (bf.getCharSet().getRenderedSize() / 3.0f * 2.0f) / myScreenWidth;
		float crossHalfHeightFraction = (bt.getLineHeight() / 2.0f) / myScreenHeight;
		Vector3f relativePosition = new Vector3f(-crossHalfWidthFraction, crossHalfHeightFraction, 0f);
		// This conditional is somewhat dependent on the TypedValueMap implementation and may need to be 
		// revisited. Will this catch position==null and continue before throwing an NPE?
		if ((position == null) || ((position.getX() == 0f) && (position.getY()) == 0f)) {
			// Default to center position if none specified
			position = new Vector3f(0.5f, 0.5f, 0f);
		}
		setPosition(position.add(relativePosition));
	}
}
