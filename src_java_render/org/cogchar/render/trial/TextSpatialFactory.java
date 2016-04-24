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

package org.cogchar.render.trial;

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

		if (bucket == null) {
			bucket = RenderQueue.Bucket.Inherit;
		}
		TextMgr txtMgr = myRRC.getSceneTextFacade(null);
		BitmapText txtSpatial = txtMgr.getScaledBitmapText(txtB, renderScale);


		// This bounding rectangle width controls how the text is wrapped.   Don't know if height gets used in any
		// way. (Maybe for collision-detect?)
		// For wrapping, explicit newlines embedded in the text also work.
		int rectHeight = 3;
		boolean debugFlag = true;
		if (debugFlag) {
			String trimmedTxt = txtB.trim();
			int tlen = trimmedTxt.length();
			int lmax = 15;
			int endSmple = (lmax < tlen) ? lmax : tlen;
			String contPre = trimmedTxt.substring(0, endSmple);
			BitmapFont bf = txtSpatial.getFont();
			float fontRenderedSize = bf.getCharSet().getRenderedSize();
			getLogger().info("Text spatial cont=[{}], font rendered size={} rect width={} height={}", contPre, fontRenderedSize, rectWidth, rectHeight);
		}
		Rectangle rect = new Rectangle(0, 0, rectWidth, rectHeight);
		txtSpatial.setBox(rect);

		// Controls when in the cycle this spatial gets rendered;  relevant to transparency and 2D/3D assignment. 
		txtSpatial.setQueueBucket(bucket);

		return txtSpatial;
	}
}
