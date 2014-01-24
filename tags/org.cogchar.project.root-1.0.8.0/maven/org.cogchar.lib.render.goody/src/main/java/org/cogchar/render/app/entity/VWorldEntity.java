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
import org.cogchar.render.sys.task.Queuer;
// import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.cogchar.render.sys.goody.GoodyRenderRegistryClient;
import org.cogchar.render.app.core.WorkaroundAppStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 * @author stub22
 */


public abstract class VWorldEntity extends Queuer {

	private	 GoodyRenderRegistryClient	myGoodyRRC;
	private	 Ident						myUri;
	
	
	protected VWorldEntity(GoodyRenderRegistryClient aRenderRegCli, Ident uri) {
		super(aRenderRegCli);
		myGoodyRRC = aRenderRegCli;
		myUri = uri;
	}
	protected GoodyRenderRegistryClient getRenderRegCli() {
		return myGoodyRRC;
	}
	
	// Number of ms this Impl will wait for goody to attach or detach from jMonkey root node before timing out
	// Currently not used -- timed futures are timing out for some reason
	//private final static long ATTACH_DETACH_TIMEOUT = 3000; //ms
	

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
		getLogger().warn("setUniformScaleFactor not supported by " + this);
	}

	// public abstract void attachToVirtualWorldNode(Node attachmentNode);
	// public abstract void detachFromVirtualWorldNode();
	public abstract void applyAction(GoodyActionExtractor ga, QueueingStyle style);
	

	public void attachToVirtualWorldNode(Node attachmentNode, QueueingStyle style) {
		getLogger().warn("attachToVirtualWorldNode not supported by " + this);
	}

	public  void detachFromVirtualWorldNode(QueueingStyle style) {
		getLogger().warn("detachFromVirtualWorldNode not supported by " + this);
	}
	
	public void applyScreenDimension(Dimension screenDimension) {}; // No operation necessary unless desired, as in BasicGoody2dImpl
	
}
