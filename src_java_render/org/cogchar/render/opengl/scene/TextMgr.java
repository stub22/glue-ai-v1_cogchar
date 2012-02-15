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
package org.cogchar.render.opengl.scene;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.system.AppSettings;
import org.cogchar.render.opengl.bony.sys.RenderRegistryAware;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class TextMgr extends RenderRegistryAware {
	
	private		BitmapFont			myDefaultFont;

	
	public BitmapFont getDefaultFont() { 
		if (myDefaultFont == null) {
			AssetManager amgr = findJme3AssetManager(null);
			myDefaultFont = amgr.loadFont("Interface/Fonts/Default.fnt");
		}
		return myDefaultFont;
	}
	/* Is this still the most current info?
	 * http://code.google.com/p/lonedev/wiki/BitmapFont
	 * TODO: Still trying to get a handle on what the size stuff means.
	 */
	public BitmapText getScaledBitmapText(String txtString, float scale) {
		BitmapFont font = getDefaultFont();
		BitmapText bt  = new BitmapText(font, false);
		float renderScale = scale * font.getCharSet().getRenderedSize();
		bt.setSize(renderScale);
		bt.setText(txtString); 
		return bt;
	}
	public BitmapText makeCrossHairs(float scale, AppSettings settings) { 
		BitmapText bt = getScaledBitmapText("+", scale);
		BitmapFont bf = bt.getFont();
		float crossHalfWidth = bf.getCharSet().getRenderedSize() / 3.0f * 2.0f;  // WTF?     guiFont.getCharSet().getRenderedSize() / 3 * 2,
		float xPos = settings.getWidth() / 2 - crossHalfWidth;
		float crossHalfHeight = bt.getLineHeight() / 2.0f;
		float yPos = settings.getHeight() / 2 + crossHalfHeight;
		float zPos = 0.0f;
		bt.setLocalTranslation(xPos, yPos, zPos);
		return bt;
	}

		
		/*
		 * BitmapText(BitmapFont font) 
           
BitmapText(BitmapFont font, boolean rightToLeft) 
           
BitmapText(BitmapFont font, boolean rightToLeft, boolean arrayBased) 
		 * 
		 */
}
