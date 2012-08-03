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

import org.appdapter.core.item.Ident;
import org.appdapter.core.item.FreeIdent;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.bind.cogbot.main.CogbotCommunicator;
import org.cogchar.bind.lift.LiftAmbassador;
import org.cogchar.render.app.humanoid.SceneActions;
import org.cogchar.render.model.databalls.*;
import org.cogchar.render.opengl.scene.CinematicMgr;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.osgi.framework.BundleContext;
import org.robokind.api.common.lifecycle.ServiceLifecycleProvider;
import org.robokind.api.common.lifecycle.utils.SimpleLifecycle;
import org.robokind.api.common.osgi.lifecycle.OSGiComponent;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaWebMapper extends BasicDebugger {

	LiftInterface liftInterface; // The LiftInterface allows Lift app to hook in and trigger cinematics
	static String cogbotConvoUrl;
	CogbotCommunicator cogbot;
	OSGiComponent liftAppComponent;
	OSGiComponent liftSceneComponent;

	public void connectCogCharResources(ClassLoader bonyRdfCl, HumanoidRenderContext hrc) {
		BallBuilder.setClassLoader("Cog Char", bonyRdfCl);
		BallBuilder.initialize(hrc);
	}

	public void connectLiftSceneInterface(BundleContext bundleCtx) {
		if (liftSceneComponent == null) {
			ServiceLifecycleProvider lifecycle = new SimpleLifecycle(SceneActions.getLauncher(), LiftAmbassador.LiftSceneInterface.class);
			liftSceneComponent = new OSGiComponent(bundleCtx, lifecycle);
		}
		liftSceneComponent.start();
	}

	public void disconnectLiftSceneInterface(BundleContext bundleCtx) {
		liftSceneComponent.stop();
	}

	public void connectLiftInterface(BundleContext bundleCtx) {
		ServiceLifecycleProvider lifecycle = new SimpleLifecycle(getLiftInterface(), LiftAmbassador.LiftAppInterface.class);
		liftAppComponent = new OSGiComponent(bundleCtx, lifecycle);
		liftAppComponent.start();
	}

	// Now mostly done from within LifterLifecycle on create(). 
	// Retaining for now for legacy BallBuilder classloader hookup
	public void connectHrkindWebContent(ClassLoader hrkindResourceCL) {
		BallBuilder.setClassLoader("hrkind.content.preview", hrkindResourceCL); // Adds this classloader to the ones Databalls know about
	}

	public LiftInterface getLiftInterface() {
		if (liftInterface == null) {
			liftInterface = new LiftInterface();
		}
		return liftInterface;
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
			if ((cogbot == null) || (!url.equals(cogbotConvoUrl))) {
				cogbotConvoUrl = url;
				cogbot = new CogbotCommunicator(cogbotConvoUrl);
			}
			return cogbot.getResponse(query).getResponse();
		}

		@Override
		public boolean performDataballAction(String action, String text) {
			return BallBuilder.performAction(action, text);
		}
	}
}
