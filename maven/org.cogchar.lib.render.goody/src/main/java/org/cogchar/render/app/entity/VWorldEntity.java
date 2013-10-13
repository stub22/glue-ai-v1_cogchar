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

package org.cogchar.render.app.entity;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.awt.Dimension;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.appdapter.core.name.Ident;
// import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.cogchar.render.sys.goody.GoodyRenderRegistryClient;
import org.cogchar.render.app.core.WorkaroundAppStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public abstract class VWorldEntity {
	public enum QueueingStyle {
		INLINE,				// Works only if we are already on the JME3 thread.  Else exception.
		QUEUE_AND_RETURN,	// Should always work.
		QUEUE_AND_WAIT		// Works only if we are *not* already on the JME3 thread.  Else deadlock.
	} 
	
	
	// OK to have a logger instance for each goody instance?
	private Logger					myLogger = LoggerFactory.getLogger(this.getClass()); 
	
	protected GoodyRenderRegistryClient myRenderRegCli;
	protected Ident						myUri;
	
	// Number of ms this Impl will wait for goody to attach or detach from jMonkey root node before timing out
	// Currently not used -- timed futures are timing out for some reason
	//private final static long ATTACH_DETACH_TIMEOUT = 3000; //ms
	
	protected Logger getLogger() { 
		return myLogger;
	}
	public Ident getUri() {
		return myUri;
	}
	public abstract void setPosition(Vector3f position, QueueingStyle style);

	public void setRotation(Quaternion newRotation, QueueingStyle style) {
		throw new UnsupportedOperationException("Not supported by  " + this); 
	}

	public void setVectorScale(Vector3f scaleVector, QueueingStyle style) {
		throw new UnsupportedOperationException("Not supported by " + this); 
	}	
	public void setUniformScaleFactor(Float scale, QueueingStyle style) {
		myLogger.warn("setUniformScaleFactor not supported by " + this);
	}

	// public abstract void attachToVirtualWorldNode(Node attachmentNode);
	// public abstract void detachFromVirtualWorldNode();
	public abstract void applyAction(GoodyActionExtractor ga, QueueingStyle style);
	

	public void attachToVirtualWorldNode(Node attachmentNode, QueueingStyle style) {
		myLogger.warn("attachToVirtualWorldNode not supported by " + this);
	}

	public  void detachFromVirtualWorldNode(QueueingStyle style) {
		myLogger.warn("detachFromVirtualWorldNode not supported by " + this);
	}
	
	public void applyScreenDimension(Dimension screenDimension) {}; // No operation necessary unless desired, as in BasicGoody2dImpl
	

	
	protected void enqueueForJme(Callable task, QueueingStyle style) {
		if (style == null) {
			style = QueueingStyle.QUEUE_AND_RETURN;
		}
		switch (style) {
			case INLINE:
				invokeTask(task);
			break;
			case QUEUE_AND_RETURN:
				enqueueForJme(task, false);
			break;
			case QUEUE_AND_WAIT:
				enqueueForJme(task, true);
			break;
			default:
				throw new RuntimeException("Bad Queueing Style: " + style);
		}
	}
	private void invokeTask(Callable task) { 
		try {
			task.call();
		} catch (Throwable t) {
			myLogger.error("Problem invoking task inline", t);
		}
	}
	private void enqueueForJme(Callable task, boolean waitFlag) {
		if (myRenderRegCli == null) {
			throw new RuntimeException("No renderRegistryClient found");
		}
		WorkaroundAppStub stub = myRenderRegCli.getWorkaroundAppStub();
		if (stub == null) {
			throw new RuntimeException("No WorkaroundAppStub found");
		}
		Future<Void> jmeFuture = stub.enqueue(task);
		if (waitFlag) {
			// We will block until detach completes to avoid collision with subsequent V-world operations
			waitForJmeFuture(jmeFuture);
		}
	}
	
	private void waitForJmeFuture(Future jmeFuture) {
		try {
			// Timed return seems to always time out -- are other things often blocking the render thread,
			// or another reason?
			//jmeFuture.get(ATTACH_DETACH_TIMEOUT, TimeUnit.MILLISECONDS);
			jmeFuture.get();
		} catch (Exception e) {
			myLogger.warn("Exception while waiting for JME3 future, probably while attempting to attach or detach goody: ", e);
		}
		//theLogger.info("Jme Future has arrived"); // TEST ONLY
	}

}
