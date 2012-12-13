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

import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.concurrent.Callable;
import org.appdapter.core.name.Ident;
import org.cogchar.render.opengl.scene.FlatOverlayMgr;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class BasicGoody2dImpl extends BasicGoody { 
	
	protected FlatOverlayMgr myOverlayMgr;
	protected Vector3f myPosition = new Vector3f(); // default: at lower left corner (0,0)
	protected BitmapText myOverlayText;
	protected int myScreenWidth, myScreenHeight;
	
	//protected Node myRootNode;
	
	protected BasicGoody2dImpl(RenderRegistryClient aRenderRegCli, Ident uri) {
		myRenderRegCli = aRenderRegCli;
		myUri = uri;
		myOverlayMgr = myRenderRegCli.getSceneFlatFacade(null);
		int[] dimensions = GoodyFactory.getTheFactory().getScreenDimensions();
		myScreenWidth = dimensions[0];
		myScreenHeight = dimensions[1];
	}
	
	// Currently just uses default font for everything -- ok for what we need now, but ultimately may want to 
	// add provisions to specify font
	protected BitmapText setGoodyAttributes(String text, float scale) {
		myOverlayText = myRenderRegCli.getSceneTextFacade(null).getScaledBitmapText(text, scale);
		setAbsolutePosition(myPosition);
		return myOverlayText;
	}
	protected BitmapText setGoodyAttributes(String text, float scale, ColorRGBA color) {
		myOverlayText = setGoodyAttributes(text, scale);
		myOverlayText.setColor(color);
		return myOverlayText;
	}
	
	public void setText(String goodyText) {
		if (myOverlayText != null) {
			myOverlayText.setText(goodyText);
		} else {
			myLogger.warn("Attempting to set text for goody {}, but its attributes have not yet been specified", 
					myUri.getLocalName());
		}
	}
	
	@Override
	// Position is specified as fraction of screen width/height
	public void setPosition(Vector3f scalePosition) {
		Vector3f absolutePosition = scalePosition.multLocal(myScreenWidth, myScreenHeight, 0);
		setAbsolutePosition(absolutePosition);
	}
	
	private void setAbsolutePosition(final Vector3f position) {
		myLogger.debug("Setting position: {}", position); // TEST ONLY
		myPosition = position;
		if (myOverlayText != null) {
			enqueueForJmeAndWait(new Callable() { // Do this on main render thread

				@Override
				public Void call() throws Exception {
					myOverlayText.setLocalTranslation(position);
					return null;
				}
			});
		}
	}
	
	@Override
	public void attachToVirtualWorldNode(Node vWorldNode) {
		// Currently any specified node is ignored since we are attaching via the FlatOverlayMgr
		attachToVirtualWorldNode();
	}
	public void attachToVirtualWorldNode() {
		if (myOverlayText != null) {
			//myRootNode = rootNode;
			myLogger.debug("Attaching 2d goody to virtual world: {} at location {}", myUri.getLocalName(), myPosition);
			enqueueForJmeAndWait(new Callable() { // Do this on main render thread

				@Override
				public Void call() throws Exception {
					myOverlayMgr.attachOverlaySpatial(myOverlayText);
					return null;
				}
			});
		} else {
			myLogger.warn("Attempting to attach 2D Goody {} to virtual world, but its attributes have not been set",
					myUri.getLocalName());
		}
	}
	
	@Override
	public void detachFromVirtualWorldNode() {
		enqueueForJmeAndWait(new Callable() { // Do this on main render thread

				@Override
				public Void call() throws Exception {
					myOverlayMgr.detachOverlaySpatial(myOverlayText);
					return null;
				}
			});
	}
	
	// Override this method to add functionality; be sure to call this super method if action is not handled
	// by overriding method
	@Override
		public void applyAction(GoodyAction ga) {
			switch (ga.getKind()) {
				case MOVE : {
					setPosition(ga.getLocationVector());
					break;
				}
				default: {
					myLogger.error("Unknown action requested in Goody {}: {}", myUri.getLocalName(), ga.getKind().name());
				}
			}
		};
	
	
}
