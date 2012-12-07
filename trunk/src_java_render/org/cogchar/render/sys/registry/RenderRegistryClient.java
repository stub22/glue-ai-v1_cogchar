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

package org.cogchar.render.sys.registry;

import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.input.InputManager;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import org.cogchar.render.opengl.mesh.FancyMeshFactory;
import org.cogchar.render.opengl.mesh.ShapeMeshFactory;
import org.cogchar.render.opengl.mesh.WireMeshFactory;
import org.cogchar.render.opengl.optic.*;
import org.cogchar.render.opengl.scene.*;
import org.cogchar.render.sys.asset.AssetContext;
import org.cogchar.render.app.core.WorkaroundAppStub;

/**
 * Preferred registry interface for the Cogchar rendering system.
 * Code using this interface is isolated from the concrete type of
 * the RenderRegistryClient, and the facade types it returns.
 * Thus, a reimplementation of this interface can provide a
 * customized rendering system, although clearly some dependence
 * on JMonkey types is required.
 * 
 * An instance of this class is the primary precursor to creation of
 * a CogcharRenderContext.
 * 
 * @author Stu B. <www.texpedient.com>
 */

public interface RenderRegistryClient {
	
	public AssetManager getJme3AssetManager(String optionalName);
	public void putJme3AssetManager(AssetManager am, String optionalName);
	
	public Node getJme3RootDeepNode(String optionalName);
	public void putJme3RootDeepNode(Node n, String optionalName);
	public Node getJme3RootOverlayNode(String optionalName);
	public void putJme3RootOverlayNode(Node n, String optionalName);
	
	public AppStateManager getJme3AppStateManager(String optionalName);
	public void putJme3AppStateManager(AppStateManager asm, String optionalName);
	public InputManager getJme3InputManager(String optionalName);
	public void putJme3InputManager(InputManager im, String optionalName) ;
	public RenderManager getJme3RenderManager(String optionalName);
	public void putJme3RenderManager(RenderManager im, String optionalName);

	public BulletAppState getJme3BulletAppState(String optionalName); 
	public void putJme3BulletAppState(BulletAppState bas, String optionalName);
	public PhysicsSpace getJme3BulletPhysicsSpace();
	
	public ViewportFacade getOpticViewportFacade(String optionalName);
	public CameraMgr getOpticCameraFacade(String optionalName);
	public LightFactory getOpticLightFacade(String optionalName);
	public MatFactory getOpticMaterialFacade(String optionalName, String optAssetContextName) ;
	
	public TextureFactory getOpticTextureFacade(String optionalName);
	
	public ShapeMeshFactory getMeshShapeFacade(String optionalName);
	public WireMeshFactory getMeshWireFacade(String optionalName);
	public FancyMeshFactory getMeshFancyFacade(String optionalName) ;
	

	public GeomFactory getSceneGeometryFacade(String optionalName) ;
	public DeepSceneMgr getSceneDeepFacade(String optionalName) ;
	public FlatOverlayMgr getSceneFlatFacade(String optionalName) ;
	public ModelSpatialFactory getSceneSpatialModelFacade(String optionalName);
	public TextMgr getSceneTextFacade(String optionalName);
	public CinematicMgr getSceneCinematicsFacade(String optionalName);

		
	public AssetContext getAssetContext(String optionalName, String optJme3AssetManagerName);

	public AssetContext getAssetContext(String optionalName, 	String optJme3AssetManagerName, Class optCredClaz);
	
	public WorkaroundAppStub getWorkaroundAppStub();
	public void putWorkaroundAppStub(WorkaroundAppStub stub);
	
}
