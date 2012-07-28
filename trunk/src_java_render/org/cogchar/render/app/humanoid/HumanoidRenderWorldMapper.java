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

package org.cogchar.render.app.humanoid;

import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.cogchar.api.cinema.CinematicConfig;
import org.cogchar.api.cinema.LightsCameraConfig;
import org.cogchar.render.opengl.optic.CameraMgr;
import org.cogchar.render.opengl.optic.LightFactory;
import org.cogchar.render.opengl.scene.CinematicMgr;
import org.cogchar.render.sys.core.RenderRegistryClient;

/**
 *
 * @author Ryan Biggs
 */

public class HumanoidRenderWorldMapper {
	public void initLightsAndCamera(HumanoidRenderContext hrc, ClassLoader optRdfResourceCL) { 
		/*
		 * Below is largely moot as soon as this config becomes query-based
		 * 
		 * Load cameras/lights config from charWorldConfig RDF resource. Obviously we don't want the path hardcoded here
		 * as it is currently. Do we want a new ConfigEmitter for this? Probably doesn't make sense to use the
		 * BonyConfigEmitter since we are separating this from BoneConfig. Also probably doesn't make sense to have the
		 * Turtle file in the rk_bind_config/motion/ path, but for the moment...
		 */
		String vworldConfigPath = "rk_bind_config/motion/charWorldConfig.ttl";
		LightsCameraConfig lcc = readLightsCameraConfig(vworldConfigPath, optRdfResourceCL);
		
		RenderRegistryClient rendRegCli = hrc.getRenderRegistryClient();
		CameraMgr cm = rendRegCli.getOpticCameraFacade(null);
		cm.initCamerasFromConfig(lcc, hrc);
		LightFactory lf = rendRegCli.getOpticLightFacade(null);
		lf.initLightsFromConfig(lcc, hrc);		
	}

	public void initCinematics(HumanoidRenderContext hrc, ClassLoader optRdfResourceCL) { 
		/*
		 * And now, we introduce the delightful RDF definitions for cinematics:
		 */
		String cineConfigPath = "rk_bind_config/motion/cinematicConfig.ttl";
		CinematicConfig cc = AssemblerUtils.readOneConfigObjFromPath(CinematicConfig.class, cineConfigPath, optRdfResourceCL);
		CinematicMgr.storeCinematicsFromConfig(cc, hrc);
	}
	public LightsCameraConfig readLightsCameraConfig(String rdfConfigFlexPath, ClassLoader optResourceClassLoader) {
		return AssemblerUtils.readOneConfigObjFromPath(LightsCameraConfig.class, rdfConfigFlexPath, optResourceClassLoader );
	}	
}
