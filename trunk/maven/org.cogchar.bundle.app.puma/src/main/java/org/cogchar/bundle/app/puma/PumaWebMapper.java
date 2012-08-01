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

import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.core.item.Ident;
import org.appdapter.core.item.FreeIdent;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.bind.cogbot.main.CogbotCommunicator;
import org.cogchar.bind.lift.ChatConfig;
import org.cogchar.bind.lift.LiftAmbassador;
import org.cogchar.bind.lift.LiftConfig;
import org.cogchar.render.app.humanoid.SceneActions;
import org.cogchar.render.model.databalls.*;
import org.cogchar.render.opengl.scene.CinematicMgr;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaWebMapper extends BasicDebugger {

	//static final String WEB_CONFIG_PATH = "metadata/web/liftconfig/liftConfig.ttl";
	static final String CHAT_CONFIG_PATH = "metadata/web/chatbird/cogbotZenoAmazonEC.ttl";
	static final Ident HOME_LIFT_CONFIG_IDENT = new FreeIdent("urn:ftd:cogchar.org:2012:runtime#mainLiftConfig", "mainLiftConfig");
	LiftInterface liftInterface; // The LiftInterface allows Lift app to hook in and trigger cinematics
	static String cogbotConvoUrl;
	CogbotCommunicator cogbot;

	public void connectCogCharResources(ClassLoader bonyRdfCl, HumanoidRenderContext hrc) {
		BallBuilder.setClassLoader("Cog Char", bonyRdfCl);
		BallBuilder.initialize(hrc);
	}

	public void connectMoreWebStuff() {
		LiftAmbassador.setSceneLauncher(SceneActions.getLauncher()); // Connect Lift to SceneActions so scenes can be triggered from webapp
		connectLiftInterface(); // Connect Lift so cinematics, cogbot can be triggered from webapp
	}

	// Connects ONLY the LiftInterface. For use by org.friendularity.bundle.repo
	public void connectLiftInterface() {
		LiftAmbassador.setAppInterface(getLiftInterface()); // Connect Lift so cogbot can be queried		
	}

	public void connectHrkindWebContent(ClassLoader hrkindResourceCL) {
		// Load web app "home" screen config
		LiftConfig lc = new LiftConfig(HOME_LIFT_CONFIG_IDENT);
		LiftAmbassador.activateControlsFromConfig(lc);
		// Load "chat app" config
		ChatConfig cc = new ChatConfig();
		LiftAmbassador.storeChatConfig(cc);
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
