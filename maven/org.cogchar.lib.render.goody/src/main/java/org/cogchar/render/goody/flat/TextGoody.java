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

package org.cogchar.render.goody.flat;

import org.cogchar.render.app.entity.GoodyAction;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import org.appdapter.core.name.Ident;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.cogchar.render.sys.goody.GoodyRenderRegistryClient;

/**
 * This class defines general purpose 2d text display goodies
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class TextGoody extends BasicGoody2dImpl {
	
	private static final ColorRGBA DEFAULT_COLOR = ColorRGBA.Black;
	private static final float DEFAULT_SCALE = 1.0f;
	private static final String DEFAULT_TEXT = "No text set";
	
	public TextGoody(GoodyRenderRegistryClient aRenderRegCli, Ident uri, Vector3f position, Float scale, ColorRGBA color, 
			String text) {
		super(aRenderRegCli, uri);
		if (color == null) color = DEFAULT_COLOR;
		if (scale == null) scale = DEFAULT_SCALE;
		if (text == null) text = DEFAULT_TEXT;
		setGoodyAttributes(text, scale, color);
		setPosition(position);
	}
	
	@Override
	public void applyAction(GoodyAction ga) {
		super.applyAction(ga); // Applies "standard" set and move actions
		switch (ga.getKind()) {
			case SET : {
				String text = ga.getText();
				if (text != null) {
					setText(text);
				}
				break;
			}
		}
	}

}
