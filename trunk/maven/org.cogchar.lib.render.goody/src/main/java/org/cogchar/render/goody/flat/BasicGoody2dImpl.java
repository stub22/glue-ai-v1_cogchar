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
import org.appdapter.core.name.Ident;
import org.cogchar.render.app.entity.VWorldEntity;
import org.cogchar.render.app.entity.VWorldEntityActionConsumer;
import org.cogchar.render.opengl.scene.FlatOverlayMgr;
// import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.cogchar.render.sys.goody.GoodyRenderRegistryClient;
/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class BasicGoody2dImpl extends VWorldEntity { 
	
	protected FlatOverlayMgr myOverlayMgr;
	protected Vector3f myPosition = new Vector3f(); // default: at lower left corner (0,0)
	protected Vector3f myScalePosition = new Vector3f();
	protected BitmapText myOverlayText;
	protected int myScreenWidth, myScreenHeight;
	
	//protected Node myRootNode;
	
	protected BasicGoody2dImpl(GoodyRenderRegistryClient aRenderRegCli, Ident uri) {
		myRenderRegCli = aRenderRegCli;
		myUri = uri;
		myOverlayMgr = myRenderRegCli.getSceneFlatFacade(null);
		VWorldEntityActionConsumer vweac = GoodyFactory.getTheFactory().getActionConsumer();
		Dimension screenDimension = vweac.getScreenDimension();
		if (screenDimension != null) {
			applyScreenDimension(screenDimension);
		} else {
			getLogger().warn("Cannot find screen dimension.");
		}
	}
	
	// Currently just uses default font for everything -- ok for what we need now, but ultimately may want to 
	// add provisions to specify font
	protected BitmapText setGoodyAttributes(String text, float scale) {
		myOverlayText = myRenderRegCli.getSceneTextFacade(null).getScaledBitmapText(text, scale);
		setAbsolutePosition(myPosition, QueueingStyle.QUEUE_AND_RETURN);
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
			getLogger().warn("Attempting to set text for goody {}, but its attributes have not yet been specified", 
					myUri.getLocalName());
		}
	}
	
	// Position is specified as fraction of screen width/height
	// Usually we want wait = true, but not for repositioning during window size change
	@Override  public void setPosition(Vector3f scalePosition, QueueingStyle qStyle) {
		//myLogger.info("Setting scalePosition: {}", scalePosition); // TEST ONLY
		if (scalePosition != null) {
			myScalePosition = scalePosition.clone();
			Vector3f absolutePosition = scalePosition.multLocal(myScreenWidth, myScreenHeight, 0);
			setAbsolutePosition(absolutePosition, qStyle);
		}
	}
	
	@Override public void setUniformScaleFactor(Float scale, QueueingStyle qStyle) {
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
	
	// Usually we want wait = true, but not for repositioning during window size change
	private void setAbsolutePosition(final Vector3f position, QueueingStyle qStyle) {
		//myLogger.info("Setting position: {}", position); // TEST ONLY
		myPosition = position.clone();
		if (myOverlayText != null) {
			Callable positioningCallable = new Callable() { // Do this on main render thread
				@Override
				public Void call() throws Exception {
					myOverlayText.setLocalTranslation(position);
					return null;
				}
			};
			enqueueForJme(positioningCallable, qStyle);
		}
	}
	
	@Override public void attachToVirtualWorldNode(Node vWorldNode, QueueingStyle qStyle) {
		// Currently any specified node is ignored since we are attaching via the FlatOverlayMgr
		attachToOverlaySpatial(qStyle);
	}
	protected void attachToOverlaySpatial(QueueingStyle style) {
		if (myOverlayText != null) {
			//myRootNode = rootNode;
			getLogger().debug("Attaching 2d goody to virtual world: {} at location {}", myUri.getLocalName(), myPosition);
			enqueueForJme(new Callable() { // Do this on main render thread

				@Override
				public Void call() throws Exception {
					myOverlayMgr.attachOverlaySpatial(myOverlayText);
					return null;
				}
			}, style);
		} else {
			getLogger().warn("Attempting to attach 2D Goody {} to virtual world, but its attributes have not been set",
					myUri.getLocalName());
		}
	}
	
	@Override public void detachFromVirtualWorldNode(QueueingStyle style) {
		enqueueForJme(new Callable() { // Do this on main render thread

				@Override
				public Void call() throws Exception {
					myOverlayMgr.detachOverlaySpatial(myOverlayText);
					return null;
				}
			}, style);
	}
	
	// Override this method to add functionality; be sure to call this super method to apply standard Goody actions
	@Override	public void applyAction(GoodyActionExtractor ga, QueueingStyle qStyle) {
		switch (ga.getKind()) {
			case MOVE : 
			case SET : {
				setPosition(ga.getLocationVec3f(), qStyle);
				setUniformScaleFactor(ga.getScaleUniform(), qStyle);
				setColor(ga.getColor());
				break;
			}
			default: {
				getLogger().error("Unknown action requested in Goody {}: {}", myUri.getLocalName(), ga.getKind().name());
			}
		}
	};
	
	@Override public final void applyScreenDimension(Dimension screenDimension) {
		myScreenWidth = screenDimension.width;
		myScreenHeight = screenDimension.height;
		setPosition(myScalePosition, QueueingStyle.QUEUE_AND_RETURN); // Reset absolute position using new screen dimensions. No waiting!
	}


}
