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
package org.cogchar.bundle.app.puma;

import org.appdapter.core.log.BasicDebugger;
import org.cogchar.bind.cogbot.main.CogbotCommunicator;
import org.cogchar.bind.lift.LiftAmbassador;
import org.cogchar.render.app.trigger.SceneActions;
import org.cogchar.render.opengl.scene.CinematicMgr;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.model.databalls.BallBuilder;
import org.osgi.framework.BundleContext;
import org.robokind.api.common.lifecycle.ServiceLifecycleProvider;
import org.robokind.api.common.lifecycle.utils.SimpleLifecycle;
import org.robokind.api.common.osgi.lifecycle.OSGiComponent;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaWebMapper extends BasicDebugger {
	
	private LiftInterface		myLiftInterface; // The LiftInterface allows Lift app to hook in and trigger cinematics
	private String				myCogbotConvoUrl;
	private CogbotCommunicator	myCogbotComm;
	private OSGiComponent		myLiftAppComp;
	private OSGiComponent		myLiftSceneComp;
	private PumaAppContext		myAppContext;
	
	// Make default constuctor private to prevent PumaWebMapper from being instantiated without a PumaAppContext
	private PumaWebMapper() {}
	
	PumaWebMapper(PumaAppContext pac) {
		myAppContext = pac;
	}
	
	public void connectCogCharResources(ClassLoader bonyRdfCl, HumanoidRenderContext hrc) {
		BallBuilder theBallBuilder = BallBuilder.getTheBallBuilder();
		theBallBuilder.setClassLoader("Cog Char", bonyRdfCl);
		theBallBuilder.initialize(hrc);
		hrc.setTheBallBuilder(theBallBuilder);
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
	public void connectHrkindWebContent(ClassLoader hrkindResourceCL) {
		BallBuilder.getTheBallBuilder().setClassLoader("hrkind.content.preview", hrkindResourceCL); // Adds this classloader to the ones Databalls know about
	}

	public LiftInterface getLiftInterface() {
		if (myLiftInterface == null) {
			myLiftInterface = new LiftInterface();
		}
		return myLiftInterface;
	}

	class LiftInterface implements LiftAmbassador.LiftAppInterface {

		@Override
		public boolean triggerNamedCinematic(String name) {
			return CinematicMgr.controlCinematicByName(name, CinematicMgr.ControlAction.PLAY);

		}

		@Override
		public boolean stopNamedCinematic(String name) {
			return CinematicMgr.controlCinematicByName(name, CinematicMgr.ControlAction.STOP);
		}

		@Override
		public String queryCogbot(String query, String url) {
			if ((myCogbotComm == null) || (!url.equals(myCogbotConvoUrl))) {
				myCogbotConvoUrl = url;
				myCogbotComm = new CogbotCommunicator(myCogbotConvoUrl);
			}
			return myCogbotComm.getResponse(query).getResponse();
		}

		@Override
		public boolean performDataballAction(String action, String text) {
			return BallBuilder.getTheBallBuilder().performAction(action, text);
		}
		
		@Override
		public boolean performUpdate(String request) {
			boolean success = false;
			if (myAppContext != null) {
				success = myAppContext.updateConfigByRequest(request);
			} else {
				logWarning("Update requested, but PumaWebMapper cannot find PumaAppContext: " + request);
			}
			return success;
		}
	}
}
