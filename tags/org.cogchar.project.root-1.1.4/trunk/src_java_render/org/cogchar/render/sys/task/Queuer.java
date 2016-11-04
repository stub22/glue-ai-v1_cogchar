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

package org.cogchar.render.sys.task;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.Ident;
import org.cogchar.render.app.core.WorkaroundAppStub;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class Queuer extends BasicDebugger {
	public enum QueueingStyle {
		INLINE,				// Works only if we are already on the JME3 thread.  Else exception.
		QUEUE_AND_RETURN,	// Should always work.
		QUEUE_AND_WAIT		// Works only if we are *not* already on the JME3 thread.  Else deadlock.
	} 
	
	private	 RenderRegistryClient	myRenderRegCli;
		
	public Queuer(RenderRegistryClient aRenderRegCli) {
		myRenderRegCli = aRenderRegCli;
	}
	public void enqueueForJme(Callable task, QueueingStyle style) {
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
			getLogger().error("Problem invoking task inline", t);
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
			getLogger().warn("Exception while waiting for JME3 future, probably while attempting to attach or detach goody: ", e);
		}
		//theLogger.info("Jme Future has arrived"); // TEST ONLY
	}
}
