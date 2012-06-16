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

// import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils;
import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.core.log.BasicDebugger;

import org.cogchar.bind.lift.LiftConfig;
import org.cogchar.bind.lift.LiftAmbassador;

import org.cogchar.render.app.humanoid.SceneActions;

import org.cogchar.render.opengl.scene.CinematicMgr;
/**
 * @author Stu B. <www.texpedient.com>
 */

public class PumaWebMapper extends BasicDebugger {
	// The following LiftInterface stuff allows Lift app to hook in and trigger cinematics
	LiftInterface liftInterface;	
	
	public void connectWebStuff(ClassLoader optRdfResourceCL) { 
		/*
		 * Load lift webapp config from liftConfig RDF resource, since this is the place for all the RDF loads
		 * currently!
		 */
		String webConfigPath = "web/liftConfig.ttl";
		LiftConfig lc = AssemblerUtils.readOneConfigObjFromPath(LiftConfig.class, webConfigPath, optRdfResourceCL);
		LiftAmbassador.storeControlsFromConfig(lc);
	}
	public void connectMoreWebStuff() { 
		LiftAmbassador.setSceneLauncher(SceneActions.getLauncher()); // Connect Lift to SceneActions so scenes can be triggered from webapp
		LiftAmbassador.setAppInterface(getLiftInterface()); // Connect Lift so cinematics can be triggered from webapp		
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
	}	
}
