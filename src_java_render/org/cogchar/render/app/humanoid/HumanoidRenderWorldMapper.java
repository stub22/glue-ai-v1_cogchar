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
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.QueryInterface;
import org.cogchar.api.cinema.CinematicConfig;
import org.cogchar.api.cinema.LightsCameraConfig;
import org.cogchar.render.opengl.optic.CameraMgr;
import org.cogchar.render.opengl.optic.LightFactory;
import org.cogchar.render.opengl.scene.CinematicMgr;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 *
 * @author Ryan Biggs
 */
public class HumanoidRenderWorldMapper {

	public void initLightsAndCamera(QueryInterface qi, HumanoidRenderContext hrc, Ident qGraph) {
		// The LightsCameraConfig constructor now automatically loads config from sheet
		LightsCameraConfig lcc = new LightsCameraConfig(qi, qGraph);
		RenderRegistryClient rendRegCli = hrc.getRenderRegistryClient();
		CameraMgr cm = rendRegCli.getOpticCameraFacade(null);
		cm.initCamerasFromConfig(lcc, hrc);
		LightFactory lf = rendRegCli.getOpticLightFacade(null);
		lf.initLightsFromConfig(lcc, hrc);
	}

	public void initCinematics(QueryInterface qi, HumanoidRenderContext hrc, Ident qGraph) {
		// The CinematicConfig constructor now automatically loads config from sheet
		CinematicConfig cc = new CinematicConfig(qi, qGraph);
		CinematicMgr.storeCinematicsFromConfig(cc, hrc);
	}
	
	public void clearLights(HumanoidRenderContext hrc) {
		RenderRegistryClient rendRegCli = hrc.getRenderRegistryClient();
		LightFactory lf = rendRegCli.getOpticLightFacade(null);
		lf.clearLights(hrc);
	}
	
	public void clearCinematics(HumanoidRenderContext hrc) {
		CinematicMgr.clearCinematics(hrc);
	}
	
	public void clearViewPorts(HumanoidRenderContext hrc) {
		CameraMgr cm = hrc.getRenderRegistryClient().getOpticCameraFacade(null);
		cm.clearViewPorts(hrc);
	}

	/* No longer needed unless we want to init from Turtle again
	public LightsCameraConfig readLightsCameraConfig(String rdfConfigFlexPath, ClassLoader optResourceClassLoader) {
		return AssemblerUtils.readOneConfigObjFromPath(LightsCameraConfig.class, rdfConfigFlexPath, optResourceClassLoader);
	}
	*/
}
