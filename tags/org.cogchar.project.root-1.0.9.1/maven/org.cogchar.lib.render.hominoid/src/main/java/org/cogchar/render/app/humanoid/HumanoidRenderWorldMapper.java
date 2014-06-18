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

import com.jme3.math.ColorRGBA;
import com.jme3.renderer.ViewPort;
import java.util.Map;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.render.app.core.WorkaroundAppStub;
import org.cogchar.render.app.entity.VWorldEntityActionConsumer;
import org.cogchar.render.model.humanoid.HumanoidFigure;
import org.cogchar.render.model.humanoid.VWorldHumanoidFigureEntity;
import org.cogchar.render.opengl.optic.CameraMgr;
import org.cogchar.render.opengl.optic.LightFactory;
import org.cogchar.render.scene.goody.PathMgr;
import org.cogchar.render.scene.goody.SpatialAnimMgr;
import org.cogchar.render.sys.goody.GoodyModularRenderContext;
import org.cogchar.render.sys.goody.GoodyRenderRegistryClient;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cogchar.api.cinema.AnimWaypointsConfig;
import org.cogchar.api.cinema.LightsCameraConfig;
import org.cogchar.api.cinema.PathConfig;
import org.cogchar.api.cinema.ThingAnimConfig;

import com.jme3.scene.Node;
import com.jme3.asset.AssetManager;
import com.jme3.util.SkyFactory;

/**
 *
 * @author Ryan Biggs
 */
public class HumanoidRenderWorldMapper {

    private static Logger theLogger = LoggerFactory.getLogger(HumanoidRenderWorldMapper.class);

    public void initLightsAndCamera(RepoClient qi, HumanoidRenderContext hrc, Ident qGraph) {
        // Note that this requires the magical hrc power object, whereas we would prefer it didn't.
        // Called from a stack like this:
        // PumaVirtualWorldMapper.initCinematicStuff()
        // PumaVirtualWorldMapper.initVirtualWorlds()
        // PumaAppContext.initCinema()
        // which may be called from 3 places:
        // PumaBooter.pumaBootUnsafeUnderOSGi(), PumaAppContext.reloadAll(), PumaContextCommandBox.processUpdateRequestNow()

        // Builds a config object set from the supplied graph:
        LightsCameraConfig lcc = new LightsCameraConfig(qi, qGraph);
        RenderRegistryClient rendRegCli = hrc.getRenderRegistryClient();
        CameraMgr cm = rendRegCli.getOpticCameraFacade(null);
        cm.initCamerasFromConfig(lcc, rendRegCli);
        LightFactory lf = rendRegCli.getOpticLightFacade(null);
        lf.initLightsFromConfig(lcc, hrc);

        setBackgroundColor(hrc, lcc);
        /*
         * Overrides the background color if a skybox image is available at
         * Content/textures/skybox/default.dds
         */
        setSkyBox(hrc);

    }

    private PathMgr getPathMgr(HumanoidRenderContext hrc) {
        return hrc.getGoodyRenderRegistryClient().getScenePathFacade(null);
    }

    private SpatialAnimMgr getSpatialAnimMgr(HumanoidRenderContext hrc) {
        return hrc.getGoodyRenderRegistryClient().getSceneAnimFacade(null);
    }

    public void initWaypoints(RepoClient qi, Ident qGraph) {
        AnimWaypointsConfig awc = new AnimWaypointsConfig(qi, qGraph);
        AnimWaypointsConfig.setMainConfig(awc);
    }

    public void initPaths(RepoClient qi, HumanoidRenderContext hrc, Ident qGraph) {
        PathConfig pc = new PathConfig(qi, qGraph);
        getPathMgr(hrc).storeAnimationsFromConfig(pc, hrc);
    }

    public void initThingAnims(RepoClient qi, HumanoidRenderContext hrc, Ident qGraph) {
        ThingAnimConfig tac = new ThingAnimConfig(qi, qGraph);
        getSpatialAnimMgr(hrc).storeAnimationsFromConfig(tac, hrc);
    }

    public void clearLights(HumanoidRenderContext hrc) {
        RenderRegistryClient rendRegCli = hrc.getRenderRegistryClient();
        LightFactory lf = rendRegCli.getOpticLightFacade(null);
        lf.clearLights(hrc);
    }

    public void clearCinematics(HumanoidRenderContext hrc) {
        // Now clears Paths / Spatial Animations
        getPathMgr(hrc).clearAnimations();
        getSpatialAnimMgr(hrc).clearAnimations();
    }

    public void clearViewPorts(HumanoidRenderContext hrc) {
        RenderRegistryClient rrc = hrc.getRenderRegistryClient();
        CameraMgr cm = rrc.getOpticCameraFacade(null);
        cm.clearViewPorts(rrc);
    }

    private void setBackgroundColor(HumanoidRenderContext hrc, LightsCameraConfig lcc) {
        ColorRGBA bgColor = new ColorRGBA(lcc.backgroundColor[0], lcc.backgroundColor[1], lcc.backgroundColor[2], lcc.backgroundColor[3]);
        WorkaroundAppStub stub = hrc.getAppStub();
        ViewPort vp = stub.getPrimaryAppViewPort();
        vp.setBackgroundColor(bgColor);
    }

    /**
     * Ben wrote this method, at Richard's request. This will load a skybox
     * background instead of a static color background. Asset is found in the
     * content project, textures/skybox/
     *
     * @param hrc
     */
    private void setSkyBox(HumanoidRenderContext hrc) {
        try {
            Node root = hrc.getGoodyRenderRegistryClient().getJme3RootDeepNode(null);
            AssetManager assetManager = hrc.getGoodyRenderRegistryClient().getJme3AssetManager(null);
            root.attachChild(SkyFactory.createSky(
                    assetManager, "textures/skybox/default.dds", false));
        } catch (Exception e) {
            theLogger.error("Skybox background image failed to load:", e);
        }
    }

    // A temporary way to make it possible to interact with figures... but ultimately, Humanoids aren't quite goodies!
    public void addHumanoidGoodies(VWorldEntityActionConsumer consumer, HumanoidRenderContext hrc) {
        GoodyRenderRegistryClient grrc = hrc.getGoodyRenderRegistryClient();
        Map<Ident, HumanoidFigure> humanoidFigures = hrc.getHumanoidFigureManager().getHumanoidFigures();
        for (Ident figureUri : humanoidFigures.keySet()) {
            theLogger.info("Adding a HumanoidFigureGoodyWrapper for {}", figureUri);
            HumanoidFigure figure = humanoidFigures.get(figureUri);
            VWorldHumanoidFigureEntity vhfe = new VWorldHumanoidFigureEntity(grrc, figureUri, figure);
            consumer.addGoody(vhfe);
        }
    }

    /* No longer needed unless we want to init from Turtle again
     public LightsCameraConfig readLightsCameraConfig(String rdfConfigFlexPath, ClassLoader optResourceClassLoader) {
     return AssemblerUtils.readOneConfigObjFromPath(LightsCameraConfig.class, rdfConfigFlexPath, optResourceClassLoader);
     }
     */
}
