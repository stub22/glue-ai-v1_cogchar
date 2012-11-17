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
package org.cogchar.app.puma.cgchr;

import org.appdapter.core.log.BasicDebugger;
import org.cogchar.bind.cogbot.main.CogbotCommunicator;
import org.cogchar.bind.lift.LiftAmbassador;
import org.cogchar.app.puma.boot.PumaAppContext;
import org.cogchar.app.puma.boot.PumaContextCommandBox;
import org.cogchar.render.app.trigger.SceneActions;
import org.cogchar.render.opengl.scene.CinematicMgr;


import org.osgi.framework.BundleContext;
import org.robokind.api.common.lifecycle.ServiceLifecycleProvider;
import org.robokind.api.common.lifecycle.utils.SimpleLifecycle;
import org.robokind.api.common.osgi.lifecycle.OSGiComponent;
import org.cogchar.render.model.databalls.BallBuilder;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaWebMapper extends BasicDebugger {
	
	private CommandTargetForUseFromWeb		myLiftInterface; // The LiftInterface allows Lift app to hook in and trigger cinematics

	private OSGiComponent					myLiftAppComp;
	private OSGiComponent					myLiftSceneComp;
	private PumaContextCommandBox					myPCCB;
	
	// Make default constuctor private to prevent PumaWebMapper from being instantiated without a PumaAppContext
	private PumaWebMapper() {}
	
	public PumaWebMapper(PumaContextCommandBox pccb) {
		myPCCB = pccb;
	}
	


	public void connectLiftSceneInterface(BundleContext bundleCtx) {
		if (myLiftSceneComp == null) {
			ServiceLifecycleProvider lifecycle = new SimpleLifecycle(SceneActions.getLauncher(), LiftAmbassador.LiftSceneInterface.class);
			myLiftSceneComp = new OSGiComponent(bundleCtx, lifecycle);
		}
		myLiftSceneComp.start();
	}

	public void disconnectLiftSceneInterface(BundleContext bundleCtx) {
		myLiftSceneComp.stop();
	}

	public void connectLiftInterface(BundleContext bundleCtx) {
		ServiceLifecycleProvider lifecycle = new SimpleLifecycle(getLiftInterface(), LiftAmbassador.LiftAppInterface.class);
		myLiftAppComp = new OSGiComponent(bundleCtx, lifecycle);
		myLiftAppComp.start();
	}

	// Previous functions now mostly done from within LifterLifecycle on create(). 
	// Retaining for now for legacy BallBuilder classloader hookup
//	public void connectHrkindWebContent(ClassLoader hrkindResourceCL) {
//	}

	public CommandTargetForUseFromWeb getLiftInterface() {
		if (myLiftInterface == null) {
			myLiftInterface = new CommandTargetForUseFromWeb(myPCCB, this);
		}
		return myLiftInterface;
	}
	public PumaContextCommandBox getCommandBox() { 
		return myPCCB;
	}
	
}
