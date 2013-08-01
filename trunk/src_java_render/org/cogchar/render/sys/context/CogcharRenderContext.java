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
package org.cogchar.render.sys.context;

import com.jme3.system.AppSettings;
import org.cogchar.render.sys.registry.BasicRenderRegistryClientImpl;
import org.cogchar.render.sys.registry.RenderRegistryClient;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.cogchar.platform.task.CallableTask;
import org.cogchar.render.app.core.WorkaroundAppStub;

/**  Named to differentiate it from JMonkey "RenderContext".  
 * This base class does not maintain much instance data.
 * However, some of its methods do have side effects on the application
 * JMonkey state and registry state, therefore it is recommended to
 * have only one instance of this class in an application.  Also,
 * generally speaking the registerJMonkeyRoots and completeInit methods 
 * should only be called once in an application.
 * 
 * The main instance data is the pointer to a RenderRegistryClient,
 * which is required to construct the context.    
 * 
 * The WorkaroundAppStub and AppSettings are more incidental and 
 * JME3 specific.
 * 
 * @author Stu B. <www.texpedient.com>
 */
public class CogcharRenderContext extends BasicRenderContext {
	
	// private		WorkaroundAppStub			myAppStub;
	
	private		AppSettings					myJme3AppSettings;
	
	public CogcharRenderContext(RenderRegistryClient rrc) {
		super(rrc);
	}
	// Used only from single-feature tests
	public CogcharRenderContext() {
		this(new BasicRenderRegistryClientImpl());
	}
	
	
	@Override public void postInitLaunch() {
		logInfo("CogcharRenderContext.postInitLaunch() does nothing.  Override to make it juicy!");
	}

	/**
	 * Hmmmm.
	 * @param settings 
	 */
	public void registerJMonkeyAppSettings(AppSettings settings) {
		myJme3AppSettings = settings;
	}
	
	protected AppSettings getJMonkeyAppSettings() { 
		return myJme3AppSettings;
	}
		
	public void setAppStub(WorkaroundAppStub stub) {
		RenderRegistryClient rrc = getRenderRegistryClient();
		rrc.putWorkaroundAppStub(stub);
		// myAppStub = stub;
	}
	public WorkaroundAppStub getAppStub() {
		RenderRegistryClient rrc = getRenderRegistryClient();
		return rrc.getWorkaroundAppStub();
		// return myAppStub;
	}
	
	@Override public Future<Object> enqueueCallable(Callable callThis) {
		WorkaroundAppStub was = getAppStub();
		return was.enqueue(callThis);
	}	
	
	@Override public void runTaskSafelyUntilComplete(CallableTask task) throws Throwable {
		WorkaroundAppStub appStub = getAppStub();
		if (appStub == null) {
			int waitCount = 50;
			while ((appStub == null) && (waitCount > 0)) {
				getLogger().warn("AppStub is null, indicating that CogcharPresumedApp.initialize() has not executed yet, sleeping for 200ms");
				Thread.sleep(200);
				appStub = getAppStub();
				waitCount--;
			}
			if (appStub == null) {
				throw new Exception("Cannot schedule CallableTask because WorkaroundAppStub has not yet been set (i.e. JME3 App init has not run yet)");
			}
		}
		java.util.concurrent.Future<Throwable> taskFuture = appStub.enqueue(task);
	
		logInfo("%%%%%%%%%%%%%%%%%%%%%%%%% Task queued : runTaskSafelyUntilComplete(" + task + ")");
		Throwable thrownByFut = taskFuture.get();
		if (thrownByFut != null) {
			throw new Exception("runTaskSafelyUntilComplete() detected an error", thrownByFut);
		}		
	} 
}
