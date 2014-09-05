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

// import org.cogchar.render.scene.goody.SpatialAnimMgr;
// import org.cogchar.render.scene.goody.PathMgr;
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
 * @author Stu B. <www.texpedient.com>
 */

public class BasicRenderRegistryClientImpl implements RenderRegistryClient {

	@Override
	public AssetManager getJme3AssetManager(String optionalName) {
		return RenderRegistryFuncs.findJme3AssetManager(optionalName);
	}

	@Override
	public void putJme3AssetManager(AssetManager am, String optionalName) {
		RenderRegistryFuncs.registerJme3AssetManager(am, optionalName);
	}

	@Override
	public Node getJme3RootDeepNode(String optionalName) {
		return RenderRegistryFuncs.findJme3RootDeepNode(optionalName);
	}

	@Override
	public void putJme3RootDeepNode(Node n, String optionalName) {
		RenderRegistryFuncs.registerJme3RootDeepNode(n, optionalName);
	}

	@Override
	public Node getJme3RootOverlayNode(String optionalName) {
		return RenderRegistryFuncs.findJme3RootOverlayNode(optionalName);
	}

	@Override
	public void putJme3RootOverlayNode(Node n, String optionalName) {
		RenderRegistryFuncs.registerJme3RootOverlayNode(n, optionalName);
	}

	@Override
	public AppStateManager getJme3AppStateManager(String optionalName) {
		return RenderRegistryFuncs.findJme3AppStateManager(optionalName);
	}

	@Override
	public void putJme3AppStateManager(AppStateManager asm, String optionalName) {
		RenderRegistryFuncs.registerJme3AppStateManager(asm, optionalName);
	}

	@Override
	public InputManager getJme3InputManager(String optionalName) {
		return RenderRegistryFuncs.findJme3InputManager(optionalName);
	}

	@Override
	public void putJme3InputManager(InputManager im, String optionalName) {
		RenderRegistryFuncs.registerJme3InputManager(im, optionalName);
	}

	@Override
	public RenderManager getJme3RenderManager(String optionalName) {
		return RenderRegistryFuncs.findJme3RenderManager(optionalName);
	}

	@Override
	public void putJme3RenderManager(RenderManager im, String optionalName) {
		RenderRegistryFuncs.registerJme3RenderManager(im, optionalName);
	}
	
	@Override
	public BulletAppState getJme3BulletAppState(String optionalName) {
		return RenderRegistryFuncs.findJme3BulletAppState(optionalName);
	}

	@Override
	public void putJme3BulletAppState(BulletAppState bas, String optionalName) {
		RenderRegistryFuncs.registerJme3BulletAppState(bas, optionalName);
	}

	@Override
	public PhysicsSpace getJme3BulletPhysicsSpace() {
		BulletAppState bas = getJme3BulletAppState(null);
		if (bas != null) {
			return bas.getPhysicsSpace();
		} else {
			return null;
		}
	}	

	@Override
	public ViewportFacade getOpticViewportFacade(String optionalName) {
		return RenderRegistryFuncs.findOrMakeOpticViewportFacade(optionalName);
	}

	@Override
	public CameraMgr getOpticCameraFacade(String optionalName) {
		return RenderRegistryFuncs.findOrMakeOpticCameraFacade(optionalName);
	}

	@Override
	public LightFactory getOpticLightFacade(String optionalName) {
		return RenderRegistryFuncs.findOrMakeOpticLightFacade(optionalName);
	}

	@Override
	public MatFactory getOpticMaterialFacade(String optionalName, String optAssetContextName) {
		return RenderRegistryFuncs.findOrMakeOpticMaterialFacade(optionalName, optAssetContextName);
	}

	@Override
	public TextureFactory getOpticTextureFacade(String optionalName) {
		return RenderRegistryFuncs.findOrMakeOpticTextureFacade(optionalName);
	}

	@Override
	public ShapeMeshFactory getMeshShapeFacade(String optionalName) {
		return RenderRegistryFuncs.findOrMakeMeshShapeFacade(optionalName);
	}

	@Override
	public WireMeshFactory getMeshWireFacade(String optionalName) {
		return RenderRegistryFuncs.findOrMakeMeshWireFacade(optionalName);
	}

	@Override
	public FancyMeshFactory getMeshFancyFacade(String optionalName) {
		return RenderRegistryFuncs.findOrMakeMeshFancyFacade(optionalName);
	}

	@Override
	public GeomFactory getSceneGeometryFacade(String optionalName) {
		return RenderRegistryFuncs.findOrMakeSceneGeometryFacade(optionalName);
	}

	@Override
	public DeepSceneMgr getSceneDeepFacade(String optionalName) {
		return RenderRegistryFuncs.findOrMakeSceneDeepFacade(optionalName);
	}

	@Override
	public FlatOverlayMgr getSceneFlatFacade(String optionalName) {
		return RenderRegistryFuncs.findOrMakeSceneFlatFacade(optionalName);
	}

	@Override
	public ModelSpatialFactory getSceneSpatialModelFacade(String optionalName) {
		return RenderRegistryFuncs.findOrMakeSceneSpatialModelFacade(optionalName);
	}

	@Override
	public TextMgr getSceneTextFacade(String optionalName) {
		return RenderRegistryFuncs.findOrMakeSceneTextFacade(optionalName);
	}
	

	@Override
	public AssetContext getAssetContext(String optionalName, String optJme3AssetManagerName) {
		return RenderRegistryFuncs.findOrMakeAssetContext(optionalName, optJme3AssetManagerName);
	}

	@Override
	public AssetContext getAssetContext(String optionalName, String optJme3AssetManagerName, Class optCredClaz) {
		return RenderRegistryFuncs.findOrMakeAssetContext(optionalName, optJme3AssetManagerName, optCredClaz);
	}

	@Override public WorkaroundAppStub getWorkaroundAppStub() {
		return RenderRegistryFuncs.findWorkaroundAppStub();
	}

	@Override
	public void putWorkaroundAppStub(WorkaroundAppStub stub) {
		RenderRegistryFuncs.registerWorkaroundAppStub(stub);
	}



}
