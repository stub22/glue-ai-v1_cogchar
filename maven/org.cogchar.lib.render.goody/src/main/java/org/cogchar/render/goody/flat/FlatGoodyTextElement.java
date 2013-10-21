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

import org.cogchar.render.app.entity.GoodyActionExtractor;
import org.cogchar.render.app.entity.GoodyFactory;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.awt.Dimension;
import java.util.concurrent.Callable;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.Ident;
import org.cogchar.render.app.entity.VWorldEntity;
import org.cogchar.render.app.entity.VWorldEntity.QueueingStyle;
import org.cogchar.render.app.entity.VWorldEntityActionConsumer;
import org.cogchar.render.opengl.scene.FlatOverlayMgr;
// import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.cogchar.render.sys.goody.GoodyRenderRegistryClient;
/**
 * "x,y locations of guiNode children are the x,y pixel on screen and z controls the render order.
 *  guiNode at 0,0 = lower left of the screen."
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class FlatGoodyTextElement extends BasicDebugger { 
	
	// Note that *lower* left corner is (0,0)
	private Vector3f					myScreenPosRelToParent = new Vector3f(); 

	private BitmapText					myOverlayText;
	private GoodyRenderRegistryClient	myRenderRegCli;
	
	public FlatGoodyTextElement(GoodyRenderRegistryClient aRenderRegCli) {
		myRenderRegCli = aRenderRegCli;
	}
	public void setScreenPosRelToParent(Vector3f sprtp, QueueingStyle qStyle) {
		myScreenPosRelToParent = sprtp;
	}
	
	// Currently just uses default font for everything -- ok for what we need now, but ultimately may want to 
	// add provisions to specify font
	protected BitmapText setGoodyAttributes(String text, float scale) {
		myOverlayText = myRenderRegCli.getSceneTextFacade(null).getScaledBitmapText(text, scale);
		// Stu 2013-10-20  setAbsolutePosition(myScreenPosRelToParent, QueueingStyle.QUEUE_AND_RETURN);
		return myOverlayText;
	}
	protected BitmapText setGoodyAttributes(String text, float scale, ColorRGBA color) {
		myOverlayText = setGoodyAttributes(text, scale);
		setColor(color);
		return myOverlayText;
	}
	
	public void setText(String goodyText) {
		if (myOverlayText != null) {
			myOverlayText.setText(goodyText);
		} else {
			getLogger().warn("Attempting to set text for goody to {}, but its attributes have not yet been specified", 
					goodyText);
		}
	}
	
	public void setUniformScaleFactor(Float scale, QueueingStyle qStyle) {
		//myLogger.info("Setting 2d Goody scale to {}", scale); // TEST ONLY
		if (myOverlayText == null) {
			getLogger().warn("Attemping to set scale on 2D Goody, but initial GoodyAttributes have not been set");
		} else if (scale != null) {
			myOverlayText.setSize(myOverlayText.getFont().getCharSet().getRenderedSize()*scale);
		}
	}
	
	public void setColor(ColorRGBA color) {
		if (color != null) {
			myOverlayText.setColor(color);
		}
	}
	public BitmapText getTextNode() { 
		return myOverlayText;
	}
}
