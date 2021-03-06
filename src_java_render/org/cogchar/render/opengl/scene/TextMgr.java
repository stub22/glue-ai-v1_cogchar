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
import com.jme3.input.KeyNames;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.system.AppSettings;
import java.util.Map;
import org.cogchar.render.sys.registry.RenderRegistryAware;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class TextMgr extends RenderRegistryAware {

	private BitmapFont myDefaultFont;

	public BitmapFont getDefaultFont() {
		if (myDefaultFont == null) {
			AssetManager amgr = findJme3AssetManager(null);
			myDefaultFont = amgr.loadFont("Interface/Fonts/Default.fnt");
			// This action disables culling for *all* spatials made with the *material* on this font.
			// If we want individual/group control over culling, seems we might clone() a material for the
			// text instance(s), and enable/disable culling on that material.

			disableCullingForFont(myDefaultFont);
		}
		return myDefaultFont;
	}
	/*
	 * Is this still the most current info? http://code.google.com/p/lonedev/wiki/BitmapFont TODO: Still trying to get a
	 * handle on what the size stuff means.
	 */

	public BitmapText getScaledBitmapText(String txtString, float scale) {
		BitmapFont font = getDefaultFont();
		BitmapText bt = new BitmapText(font, false);
		float renderScale = scale * font.getCharSet().getRenderedSize();
		bt.setSize(renderScale);
		bt.setText(txtString);
		return bt;
	}

	public void disableCullingForFont(BitmapFont bf) {
		// We will only see the backside of the text unless we set the material of the font to not cull faces.
		// The materials of the font are called its "pages".
		// http://hub.jmonkeyengine.org/forum/topic/render-back-of-bitmaptext/
		int pageCount = bf.getPageSize();
		getLogger().info("TextMgr disabling culling for a total of {}  font materials", pageCount);
		for (int i = 0; i < pageCount; i++) {
			Material fontMat = bf.getPage(i);
			fontMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
		}
	}

	// Now added as a Goody, but this method retained for demo projects
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

	// KeyBindingTracker.getBindingMap()
	public BitmapText makeHelpScreen(float scale, AppSettings settings, Map<String, Integer> keyBindingMap) {
		String commandList = "";
		String commandLine;
		int longestLineLength = 0;
		KeyNames keyNamesConverter = new KeyNames();
		for (Map.Entry<String, Integer> entry : keyBindingMap.entrySet()) {
			String entryKey = entry.getKey();
			Integer entryVal = entry.getValue();
			// We use 0 = BUTTON_LEFT, only key values are avail 
			if ((entryVal != null) && (entryVal > 0)) {
				String convertedName = keyNamesConverter.getName(entryVal);
				commandLine = entryKey + ": " + convertedName + "\n";
				if (commandLine.length() > longestLineLength) {
					longestLineLength = commandLine.length();
				}
				commandList = commandList + commandLine;
			} else {
				getLogger().warn("Cannot make helpText for key={}, val={}", entryKey, entryVal);
			}
		}
		BitmapText bt = getScaledBitmapText(commandList, scale);
		// Sets offset to make sure text doesn't extend past right edge of screen - the last "fudge factors" might not want to be hard coded
		float xPos = settings.getWidth() - (longestLineLength * scale * 9.6f + 2f);
		float yPos = settings.getHeight() - 2f;
		float zPos = 0.0f;
		bt.setLocalTranslation(xPos, yPos, zPos);
		bt.setColor(ColorRGBA.Black);
		return bt;
	}
	/*
	 * BitmapText(BitmapFont font) 	 *
	 * BitmapText(BitmapFont font, boolean rightToLeft) 	 *
	 * BitmapText(BitmapFont font, boolean rightToLeft, boolean arrayBased)
	 *
	 */
}
