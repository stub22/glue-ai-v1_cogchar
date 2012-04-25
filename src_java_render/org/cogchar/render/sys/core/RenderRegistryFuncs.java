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
package org.cogchar.render.sys.core;

import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.input.InputManager;
import com.jme3.scene.Node;

import org.appdapter.api.facade.FacadeSpec;
import org.appdapter.api.facade.Maker;
import org.appdapter.api.facade.FacadeRegistryFuncs;
import org.appdapter.api.registry.Description;
import org.appdapter.api.registry.VerySimpleRegistry;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.registry.basic.BasicDescription;
import org.cogchar.blob.emit.RegistryClient;
import org.cogchar.blob.emit.FacadeHandle;
import org.cogchar.render.opengl.mesh.FancyMeshFactory;
import org.cogchar.render.opengl.mesh.ShapeMeshFactory;
import org.cogchar.render.opengl.mesh.WireMeshFactory;
import org.cogchar.render.opengl.optic.CameraMgr;
import org.cogchar.render.opengl.optic.LightFactory;
import org.cogchar.render.opengl.optic.MatFactory;
import org.cogchar.render.opengl.optic.TextureFactory;
import org.cogchar.render.opengl.optic.ViewportFacade;
import org.cogchar.render.opengl.scene.DeepSceneMgr;
import org.cogchar.render.opengl.scene.FlatOverlayMgr;
import org.cogchar.render.opengl.scene.GeomFactory;
import org.cogchar.render.opengl.scene.ModelSpatialFactory;
import org.cogchar.render.opengl.scene.TextMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a set of functions which statelessly defines the create/find behavior of Cogchar rendering core services.
 * <br/>Does not make direct use of the (appdapter) BasicDebugger base class features, but includes it so that a class 
 * extending this one can use those features.  You'll need 'em, buddy!
 * @author Stu B. <www.texpedient.com>
 */
public abstract class RenderRegistryFuncs extends FacadeRegistryFuncs {
	
	private final static Logger		theLogger = LoggerFactory.getLogger(RenderRegistryFuncs.class);
	
	protected enum RFKind {
		JME3_ASSET_MANAGER,
		
		JME3_ROOT_DEEP_NODE,
		JME3_ROOT_OVERLAY_NODE,
		
		JME3_APP_STATE_MANAGER,
		JME3_INPUT_MANAGER,
		
		CC_ASSET_CONTEXT,

		CC_OPTIC_VIEWPORT_FACADE,
		CC_OPTIC_CAMERA_FACADE,
		CC_OPTIC_LIGHT_FACADE,
		CC_OPTIC_MATERIAL_FACADE,
		CC_OPTIC_TEXTURE_FACADE,
		
		CC_MESH_SHAPE_FACADE,
		CC_MESH_WIRE_FACADE,
		CC_MESH_FANCY_FACADE,
		
		CC_SCENE_DEEP_FACADE,
		CC_SCENE_FLAT_FACADE,
		CC_SCENE_GEOMETRY_FACADE,
		CC_SCENE_SPATIAL_MODEL_FACADE,
		CC_SCENE_TEXT_FACADE,
		
		CC_PHYSICS_FACADE
	}
	
	protected static class RFSpec<RFType> extends FacadeSpec<RFType, RFKind> {
		RFSpec(RFKind kind, Class<RFType> sClz) {
			super(kind, sClz);
		}
	}
	
	protected static final RFSpec<AssetManager>		THE_JME3_ASSET_MANAGER;
	protected static final RFSpec<Node>				THE_JME3_ROOT_DEEP_NODE;
	protected static final RFSpec<Node>				THE_JME3_ROOT_OVERLAY_NODE;
	protected static final RFSpec<AppStateManager>	THE_JME3_APP_STATE_MANAGER;
	protected static final RFSpec<InputManager>		THE_JME3_INPUT_MANAGER;
	
	protected static final RFSpec<PhysicsSpace>		THE_BULLET_PHYSICS_SPACE;
	
	static {
		THE_JME3_ASSET_MANAGER = new RFSpec<AssetManager>(RFKind.JME3_ASSET_MANAGER, AssetManager.class);
		THE_JME3_ROOT_DEEP_NODE = new RFSpec<Node>(RFKind.JME3_ROOT_DEEP_NODE, Node.class);
		THE_JME3_ROOT_OVERLAY_NODE = new RFSpec<Node>(RFKind.JME3_ROOT_OVERLAY_NODE, Node.class);	
		
		THE_JME3_APP_STATE_MANAGER = new RFSpec<AppStateManager>(RFKind.JME3_APP_STATE_MANAGER, AppStateManager.class);	
		THE_JME3_INPUT_MANAGER = new RFSpec<InputManager>(RFKind.JME3_INPUT_MANAGER, InputManager.class);	
		
		THE_BULLET_PHYSICS_SPACE  = new RFSpec<PhysicsSpace>(RFKind.JME3_ROOT_DEEP_NODE, PhysicsSpace.class);	
	}

	protected static RFSpec<AssetContext>	THE_CC_ASSET_CONTEXT;
	static {
		THE_CC_ASSET_CONTEXT = new RFSpec<AssetContext>(RFKind.CC_ASSET_CONTEXT, AssetContext.class);
	}
		
	protected static RFSpec<ViewportFacade>	THE_CC_OPTIC_VIEWPORT_FACADE;
	protected static RFSpec<CameraMgr>		THE_CC_OPTIC_CAMERA_FACADE;
	protected static RFSpec<LightFactory>	THE_CC_OPTIC_LIGHT_FACADE;
	protected static RFSpec<MatFactory>		THE_CC_OPTIC_MATERIAL_FACADE;
	protected static RFSpec<TextureFactory>	THE_CC_OPTIC_TEXTURE_FACADE;
	
	static {
		THE_CC_OPTIC_VIEWPORT_FACADE = new RFSpec<ViewportFacade>(RFKind.CC_OPTIC_VIEWPORT_FACADE, ViewportFacade.class);
		THE_CC_OPTIC_CAMERA_FACADE = new RFSpec<CameraMgr>(RFKind.CC_OPTIC_CAMERA_FACADE, CameraMgr.class);		
		THE_CC_OPTIC_LIGHT_FACADE = new RFSpec<LightFactory>(RFKind.CC_OPTIC_LIGHT_FACADE, LightFactory.class);
		THE_CC_OPTIC_MATERIAL_FACADE = new RFSpec<MatFactory>(RFKind.CC_OPTIC_MATERIAL_FACADE, MatFactory.class);
		THE_CC_OPTIC_TEXTURE_FACADE = new RFSpec<TextureFactory>(RFKind.CC_OPTIC_TEXTURE_FACADE, TextureFactory.class);
	}	

	protected static RFSpec<ShapeMeshFactory>	THE_CC_MESH_SHAPE_FACADE;
	protected static RFSpec<WireMeshFactory>	THE_CC_MESH_WIRE_FACADE;
	protected static RFSpec<FancyMeshFactory>	THE_CC_MESH_FANCY_FACADE;
	
	static {
		THE_CC_MESH_SHAPE_FACADE = new RFSpec<ShapeMeshFactory>(RFKind.CC_MESH_SHAPE_FACADE, ShapeMeshFactory.class);
		THE_CC_MESH_WIRE_FACADE = new RFSpec<WireMeshFactory>(RFKind.CC_MESH_WIRE_FACADE, WireMeshFactory.class);
		THE_CC_MESH_FANCY_FACADE = new RFSpec<FancyMeshFactory>(RFKind.CC_MESH_FANCY_FACADE, FancyMeshFactory.class);		
	}



	protected static RFSpec<GeomFactory>			THE_CC_SCENE_GEOMETRY_FACADE;	
	protected static RFSpec<DeepSceneMgr>			THE_CC_SCENE_DEEP_FACADE;
	protected static RFSpec<FlatOverlayMgr>			THE_CC_SCENE_FLAT_FACADE;
	protected static RFSpec<ModelSpatialFactory>	THE_CC_SCENE_SPATIAL_MODEL_FACADE;
	protected static RFSpec<TextMgr>				THE_CC_SCENE_TEXT_FACADE;

	static {
		THE_CC_SCENE_GEOMETRY_FACADE = new RFSpec<GeomFactory>(RFKind.CC_SCENE_GEOMETRY_FACADE, GeomFactory.class);
		THE_CC_SCENE_DEEP_FACADE = new RFSpec<DeepSceneMgr>(RFKind.CC_SCENE_DEEP_FACADE, DeepSceneMgr.class);		
		THE_CC_SCENE_FLAT_FACADE = new RFSpec<FlatOverlayMgr>(RFKind.CC_SCENE_FLAT_FACADE, FlatOverlayMgr.class);
		THE_CC_SCENE_SPATIAL_MODEL_FACADE = new RFSpec<ModelSpatialFactory>(RFKind.CC_SCENE_SPATIAL_MODEL_FACADE, ModelSpatialFactory.class);
		THE_CC_SCENE_TEXT_FACADE = new RFSpec<TextMgr>(RFKind.CC_SCENE_TEXT_FACADE, TextMgr.class);		
	}

	// protected static FacadeSpec<PhysicsStuffBuilder>	THE_CC_PHYSICS_FACADE;
	
//		CC_PHYSICS_FACADE	

	protected static <EFT, EFK> EFT  findExternalFacadeOrNull(FacadeSpec<EFT, EFK> fs, String optOverrideName) {		
		EFT result = null;
		FacadeHandle<EFT> fh = RegistryClient.findExternalFacade(fs, optOverrideName);
		if (fh.isReady()) {
			result = fh.getOrElse(null);
		} 
		return result;
	}
	
	protected static AssetManager findJme3AssetManager(String optionalName) {
		return findExternalFacadeOrNull(THE_JME3_ASSET_MANAGER, optionalName);
	}
	protected static void registerJme3AssetManager(AssetManager am, String optionalName) {
		RegistryClient.registerExternalFacade(THE_JME3_ASSET_MANAGER, am, optionalName);	
	}
	protected static Node findJme3RootDeepNode(String optionalName) {
		return findExternalFacadeOrNull(THE_JME3_ROOT_DEEP_NODE, optionalName);
	}
	protected static void registerJme3RootDeepNode(Node n, String optionalName) {
		RegistryClient.registerExternalFacade(THE_JME3_ROOT_DEEP_NODE, n, optionalName);
	}
	protected static Node findJme3RootOverlayNode(String optionalName) {
		return findExternalFacadeOrNull(THE_JME3_ROOT_OVERLAY_NODE, optionalName);
	}
	protected static void registerJme3RootOverlayNode(Node n, String optionalName) {
		RegistryClient.registerExternalFacade(THE_JME3_ROOT_OVERLAY_NODE, n, optionalName);
	}	
	
	protected static AppStateManager findJme3AppStateManager(String optionalName) {
		return findExternalFacadeOrNull(THE_JME3_APP_STATE_MANAGER, optionalName);
	}
	protected static void registerJme3AppStateManager(AppStateManager asm, String optionalName) {
		RegistryClient.registerExternalFacade(THE_JME3_APP_STATE_MANAGER, asm, optionalName);
	}
	protected static InputManager findJme3InputManager(String optionalName) {
		return findExternalFacadeOrNull(THE_JME3_INPUT_MANAGER, optionalName);
	}
	protected static void registerJme3InputManager(InputManager im, String optionalName) {
		RegistryClient.registerExternalFacade(THE_JME3_INPUT_MANAGER, im, optionalName);
	}	
	
	// This one needs to be public, so that BundleActivators can find it, to register their classloader-markers.
	public static AssetContext findOrMakeAssetContext(String optionalName, String optJme3AssetManagerName) {
		// TODO - do something cool to make sure that optJme3AssetManagerName is compatible with the
		// named JME3_ASSET_MANAGER, because this is the *constraint* being supplied by the application.
		return RegistryClient.findOrMakeInternalFacade(THE_CC_ASSET_CONTEXT, optionalName);
	}

	protected static ViewportFacade findOrMakeOpticViewportFacade(String optionalName) {
		return RegistryClient.findOrMakeInternalFacade(THE_CC_OPTIC_VIEWPORT_FACADE, optionalName);
	}
	protected static CameraMgr findOrMakeOpticCameraFacade(String optionalName) {
		return RegistryClient.findOrMakeInternalFacade(THE_CC_OPTIC_CAMERA_FACADE, optionalName);
	}
	protected static LightFactory findOrMakeOpticLightFacade(String optionalName) {
		return RegistryClient.findOrMakeInternalFacade(THE_CC_OPTIC_LIGHT_FACADE, optionalName);
	}
	protected static MatFactory findOrMakeOpticMaterialFacade(String optionalName, String optAssetContextName) {
		// TODO - do something cool to make sure that optAssetContextName is compatible
		return RegistryClient.findOrMakeInternalFacade(THE_CC_OPTIC_MATERIAL_FACADE, optionalName);
	}	
	
	protected static TextureFactory findOrMakeOpticTextureFacade(String optionalName) {
		return RegistryClient.findOrMakeInternalFacade(THE_CC_OPTIC_TEXTURE_FACADE, optionalName);
	}
	
	protected static ShapeMeshFactory findOrMakeMeshShapeFacade(String optionalName) {
		return RegistryClient.findOrMakeInternalFacade(THE_CC_MESH_SHAPE_FACADE, optionalName);
	}	
	protected static WireMeshFactory findOrMakeMeshWireFacade(String optionalName) {
		return RegistryClient.findOrMakeInternalFacade(THE_CC_MESH_WIRE_FACADE, optionalName);
	}	
	protected static FancyMeshFactory findOrMakeMeshFancyFacade(String optionalName) {
		return RegistryClient.findOrMakeInternalFacade(THE_CC_MESH_FANCY_FACADE, optionalName);
	}	
	

	protected static GeomFactory findOrMakeSceneGeometryFacade(String optionalName) {
		return RegistryClient.findOrMakeInternalFacade(THE_CC_SCENE_GEOMETRY_FACADE, optionalName);
	}		
	protected static DeepSceneMgr findOrMakeSceneDeepFacade(String optionalName) {
		return RegistryClient.findOrMakeInternalFacade(THE_CC_SCENE_DEEP_FACADE, optionalName);
	}
	protected static FlatOverlayMgr findOrMakeSceneFlatFacade(String optionalName) {
		return RegistryClient.findOrMakeInternalFacade(THE_CC_SCENE_FLAT_FACADE, optionalName);
	}
	protected static ModelSpatialFactory findOrMakeSceneSpatialModelFacade(String optionalName) {
		return RegistryClient.findOrMakeInternalFacade(THE_CC_SCENE_SPATIAL_MODEL_FACADE, optionalName);
	}
	protected static TextMgr findOrMakeSceneTextFacade(String optionalName) {
		return RegistryClient.findOrMakeInternalFacade(THE_CC_SCENE_TEXT_FACADE, optionalName);
	}

}
