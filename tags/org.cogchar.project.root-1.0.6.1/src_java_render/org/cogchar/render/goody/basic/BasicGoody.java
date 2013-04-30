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

package org.cogchar.render.goody.basic;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.awt.Dimension;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.appdapter.core.name.Ident;
import org.cogchar.render.app.goody.GoodyAction;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public abstract class BasicGoody {
	
	// OK to have a logger instance for each goody instance?
	protected Logger myLogger = LoggerFactory.getLogger(this.getClass()); 
	
	protected RenderRegistryClient myRenderRegCli;
	protected Ident myUri;
	
	// Number of ms this Impl will wait for goody to attach or detach from jMonkey root node before timing out
	// Currently not used -- timed futures are timing out for some reason
	//private final static long ATTACH_DETACH_TIMEOUT = 3000; //ms
	
	public Ident getUri() {
		return myUri;
	}
	public abstract void setPosition(Vector3f position);
	public abstract void setScale(Float scale);
	public abstract void attachToVirtualWorldNode(Node attachmentNode);
	public abstract void detachFromVirtualWorldNode();
	public abstract void applyAction(GoodyAction ga);
	
	public void applyScreenDimension(Dimension screenDimension) {}; // No operation necessary unless desired, as in BasicGoody2dImpl
	
	protected void enqueueForJmeAndWait(Callable task) {
		Future<Void> jmeFuture = myRenderRegCli.getWorkaroundAppStub().enqueue(task);
			// Method should block until detach completes to avoid collision with subsequent V-world operations
			waitForJmeFuture(jmeFuture);
	}
	
	// Added for repositioning 2d goodies on size change, which hangs if we wait for the enqueue while the window is being resized
	protected void enqueueForJmeButDontWait(Callable task) {
		myRenderRegCli.getWorkaroundAppStub().enqueue(task);
	}
	
	private void waitForJmeFuture(Future jmeFuture) {
		try {
			// Timed return seems to always time out -- are other things often blocking the render thread,
			// or another reason?
			//jmeFuture.get(ATTACH_DETACH_TIMEOUT, TimeUnit.MILLISECONDS);
			jmeFuture.get();
		} catch (Exception e) {
			myLogger.warn("Exception attempting to attach or detach goody: ", e);
		}
		//theLogger.info("Jme Future has arrived"); // TEST ONLY
	}

}
