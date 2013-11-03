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

package org.cogchar.render.test;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.renderer.queue.RenderQueue;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.render.opengl.scene.TextMgr;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class TextSpatialFactory extends BasicDebugger {
	private RenderRegistryClient myRRC;
	public TextSpatialFactory(RenderRegistryClient rrc) {
		myRRC = rrc;
	}
	public BitmapText makeTextSpatial(String txtB, float renderScale, RenderQueue.Bucket bucket, int rectWidth) {

		TextMgr txtMgr = myRRC.getSceneTextFacade(null);
		BitmapText txtSpatial = txtMgr.getScaledBitmapText(txtB, renderScale);

		BitmapFont bf = txtSpatial.getFont();
		float fontRenderedSize = bf.getCharSet().getRenderedSize();

		getLogger().info("Font rendered size={}", fontRenderedSize);
		// This action disables culling for *all* spatials made with the *material* on this font, so it should really be happening
		// further out, in concert with font and material management.    If we want individual/group control over 
		// culling, seems we might clone() a material for the text instance(s), and enable/disable culling on that material.
		
		txtMgr.disableCullingForFont(bf);

		// This rectangle width controls how the text is wrapped.   Don't know if height gets used in any way.
		// For wrapping, explicit newlines embedded in the text also work.
		int rectHeight = 3;
		Rectangle rect = new Rectangle(0, 0, rectWidth, rectHeight);
		txtSpatial.setBox(rect);

		// Controls when in the cycle this this 
		txtSpatial.setQueueBucket(bucket);

		return txtSpatial;
	}
}
