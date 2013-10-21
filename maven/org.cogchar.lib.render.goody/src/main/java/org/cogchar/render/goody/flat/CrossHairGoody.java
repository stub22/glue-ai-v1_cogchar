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

package org.cogchar.render.goody.flat;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.awt.Dimension;
import org.appdapter.core.name.Ident;
import org.cogchar.render.app.entity.VWorldEntity;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.cogchar.render.sys.goody.GoodyRenderRegistryClient;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class CrossHairGoody extends FlatGoodyWithScreenFracPos {
	Node myNode;
	FlatGoodyTextElement myTextEl;
	
	public CrossHairGoody(GoodyRenderRegistryClient aRenderRegCli, Ident uri, Vector3f positionOffset, Float scale) {
		super(aRenderRegCli, uri);
		myTextEl = new FlatGoodyTextElement(aRenderRegCli);
		makeCrossHairs(scale, positionOffset);
	}
	
	private void makeCrossHairs(Float scale, Vector3f position) {
		if (scale == null) {
			scale = 1f;
			getLogger().warn("Scale for CrossHair not specified; using default of 1.");
		}
		BitmapText bt = myTextEl.setGoodyAttributes("+", scale);
		myNode = bt;
		BitmapFont bf = bt.getFont();
		Dimension screenDim = getScreenDim();
		float crossHalfWidthFraction = (bf.getCharSet().getRenderedSize() / 3.0f * 2.0f) / screenDim.width;
		float crossHalfHeightFraction = (bt.getLineHeight() / 2.0f) / screenDim.height;
		Vector3f relativePosition = new Vector3f(-crossHalfWidthFraction, crossHalfHeightFraction, 0f);
		// This conditional is somewhat dependent on the TypedValueMap implementation and may need to be 
		// revisited. Will this catch position==null and continue before throwing an NPE?
		if ((position == null) || ((position.getX() == 0f) && (position.getY()) == 0f)) {
			// Default to center position if none specified
			position = new Vector3f(0.5f, 0.5f, 0f);
		}
		setPosition(position.add(relativePosition), VWorldEntity.QueueingStyle.QUEUE_AND_RETURN);
	}
	
	// TODO: Override setScale to recenter Crosshairs

	@Override protected Node getFlatGoodyNode() {
		return myNode;
	}
}
