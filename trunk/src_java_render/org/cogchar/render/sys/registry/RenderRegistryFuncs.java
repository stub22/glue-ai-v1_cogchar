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

import org.appdapter.api.facade.FacadeSpec;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.subreg.FacadeHandle;
import org.appdapter.subreg.SubsystemHandle;
import org.cogchar.blob.emit.SubsystemHandleFinder;
import org.cogchar.render.app.core.WorkaroundAppStub;
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
import org.cogchar.render.opengl.scene.PathMgr;
import org.cogchar.render.opengl.scene.SpatialAnimMgr;
import org.cogchar.render.opengl.scene.TextMgr;
import org.cogchar.render.sys.asset.AssetContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.InputManager;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;

/**
 * This is a set of functions which statelessly defines the create/find behavior of Cogchar rendering core services.
 * <br/>Does not make direct use of the (appdapter) BasicDebugger base class features, but includes it so that a class 
 * extending this one can use those features.  You'll need 'em, buddy!
 * @author Stu B. <www.texpedient.com>
 */
public abstract class RenderRegistryFuncs extends BasicDebugger {
	
	// private final static Logger		theLogger = LoggerFactory.getLogger(RenderRegistryFuncs.class);
	
	// Here are the kinds of things this registry knows about.
	protected enum RFKind {
		JME3_ASSET_MANAGER,
		
		JME3_ROOT_DEEP_NODE,
		JME3_ROOT_OVERLAY_NODE,
		
		JME3_APP_STATE_MANAGER,
		JME3_INPUT_MANAGER,
		JME3_RENDER_MANAGER,

		JME3_BULLET_APP_STATE,
		
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
		CC_SCENE_PATH_FACADE,
		CC_SCENE_ANIM_FACADE,
		
		CC_PHYSICS_FACADE,
		
		CC_WORKAROUND_APP_STUB
	}
	
	protected static class RFSpec<RFType> extends FacadeSpec<RFType, RFKind> {
		RFSpec(RFKind kind, Class<RFType> sClz, boolean extFlag) {
			super(kind, sClz, extFlag);
		}
	}
	// Here are the static specifications for the KINDS of hings this registry knows about.
	// These constant names should really each end with KIND_SPEC or similar.
	protected static final RFSpec<AssetManager>		THE_JME3_ASSET_MANAGER;
	protected static final RFSpec<Node>				THE_JME3_ROOT_DEEP_NODE;
	protected static final RFSpec<Node>				THE_JME3_ROOT_OVERLAY_NODE;
	protected static final RFSpec<AppStateManager>	THE_JME3_APP_STATE_MANAGER;
	protected static final RFSpec<InputManager>		THE_JME3_INPUT_MANAGER;
	protected static final RFSpec<RenderManager>	THE_JME3_RENDER_MANAGER;
	
	
	protected static final RFSpec<BulletAppState>	THE_JME3_BULLET_APP_STATE;
	
	static {
		THE_JME3_ASSET_MANAGER = new RFSpec<AssetManager>(RFKind.JME3_ASSET_MANAGER, AssetManager.class, true);
		THE_JME3_ROOT_DEEP_NODE = new RFSpec<Node>(RFKind.JME3_ROOT_DEEP_NODE, Node.class, true);
		THE_JME3_ROOT_OVERLAY_NODE = new RFSpec<Node>(RFKind.JME3_ROOT_OVERLAY_NODE, Node.class, true);	
		
		THE_JME3_APP_STATE_MANAGER = new RFSpec<AppStateManager>(RFKind.JME3_APP_STATE_MANAGER, AppStateManager.class, true);	
		THE_JME3_INPUT_MANAGER = new RFSpec<InputManager>(RFKind.JME3_INPUT_MANAGER, InputManager.class, true);	
		THE_JME3_RENDER_MANAGER  = new RFSpec<RenderManager>(RFKind.JME3_RENDER_MANAGER, RenderManager.class, true);
		
		THE_JME3_BULLET_APP_STATE  = new RFSpec<BulletAppState>(RFKind.JME3_BULLET_APP_STATE, BulletAppState.class, true);	
	}

	protected static RFSpec<AssetContext>	THE_CC_ASSET_CONTEXT;
	static {
		THE_CC_ASSET_CONTEXT = new RFSpec<AssetContext>(RFKind.CC_ASSET_CONTEXT, AssetContext.class, false);
	}
		
	protected static RFSpec<ViewportFacade>	THE_CC_OPTIC_VIEWPORT_FACADE;
	protected static RFSpec<CameraMgr>		THE_CC_OPTIC_CAMERA_FACADE;
	protected static RFSpec<LightFactory>	THE_CC_OPTIC_LIGHT_FACADE;
	protected static RFSpec<MatFactory>		THE_CC_OPTIC_MATERIAL_FACADE;
	protected static RFSpec<TextureFactory>	THE_CC_OPTIC_TEXTURE_FACADE;
	
	static {
		THE_CC_OPTIC_VIEWPORT_FACADE = new RFSpec<ViewportFacade>(RFKind.CC_OPTIC_VIEWPORT_FACADE, ViewportFacade.class, false);
		THE_CC_OPTIC_CAMERA_FACADE = new RFSpec<CameraMgr>(RFKind.CC_OPTIC_CAMERA_FACADE, CameraMgr.class, false);		
		THE_CC_OPTIC_LIGHT_FACADE = new RFSpec<LightFactory>(RFKind.CC_OPTIC_LIGHT_FACADE, LightFactory.class, false);
		THE_CC_OPTIC_MATERIAL_FACADE = new RFSpec<MatFactory>(RFKind.CC_OPTIC_MATERIAL_FACADE, MatFactory.class, false);
		THE_CC_OPTIC_TEXTURE_FACADE = new RFSpec<TextureFactory>(RFKind.CC_OPTIC_TEXTURE_FACADE, TextureFactory.class, false);
	}	

	protected static RFSpec<ShapeMeshFactory>	THE_CC_MESH_SHAPE_FACADE;
	protected static RFSpec<WireMeshFactory>	THE_CC_MESH_WIRE_FACADE;
	protected static RFSpec<FancyMeshFactory>	THE_CC_MESH_FANCY_FACADE;
	
	static {
		THE_CC_MESH_SHAPE_FACADE = new RFSpec<ShapeMeshFactory>(RFKind.CC_MESH_SHAPE_FACADE, ShapeMeshFactory.class, false);
		THE_CC_MESH_WIRE_FACADE = new RFSpec<WireMeshFactory>(RFKind.CC_MESH_WIRE_FACADE, WireMeshFactory.class, false);
		THE_CC_MESH_FANCY_FACADE = new RFSpec<FancyMeshFactory>(RFKind.CC_MESH_FANCY_FACADE, FancyMeshFactory.class, false);		
	}



	protected static RFSpec<GeomFactory>			THE_CC_SCENE_GEOMETRY_FACADE;	
	protected static RFSpec<DeepSceneMgr>			THE_CC_SCENE_DEEP_FACADE;
	protected static RFSpec<FlatOverlayMgr>			THE_CC_SCENE_FLAT_FACADE;
	protected static RFSpec<ModelSpatialFactory>	THE_CC_SCENE_SPATIAL_MODEL_FACADE;
	protected static RFSpec<TextMgr>				THE_CC_SCENE_TEXT_FACADE;
	protected static RFSpec<PathMgr>				THE_CC_SCENE_PATH_FACADE;
	protected static RFSpec<SpatialAnimMgr>			THE_CC_SCENE_ANIM_FACADE;

	static {
		THE_CC_SCENE_GEOMETRY_FACADE = new RFSpec<GeomFactory>(RFKind.CC_SCENE_GEOMETRY_FACADE, GeomFactory.class, false);
		THE_CC_SCENE_DEEP_FACADE = new RFSpec<DeepSceneMgr>(RFKind.CC_SCENE_DEEP_FACADE, DeepSceneMgr.class, false);		
		THE_CC_SCENE_FLAT_FACADE = new RFSpec<FlatOverlayMgr>(RFKind.CC_SCENE_FLAT_FACADE, FlatOverlayMgr.class, false);
		THE_CC_SCENE_SPATIAL_MODEL_FACADE = new RFSpec<ModelSpatialFactory>(RFKind.CC_SCENE_SPATIAL_MODEL_FACADE, ModelSpatialFactory.class, false);
		THE_CC_SCENE_TEXT_FACADE = new RFSpec<TextMgr>(RFKind.CC_SCENE_TEXT_FACADE, TextMgr.class, false);	
		THE_CC_SCENE_PATH_FACADE = new RFSpec<PathMgr>(RFKind.CC_SCENE_PATH_FACADE, PathMgr.class, false);
		THE_CC_SCENE_ANIM_FACADE = new RFSpec<SpatialAnimMgr>(RFKind.CC_SCENE_ANIM_FACADE, SpatialAnimMgr.class, false);
	}
	
	protected static RFSpec<WorkaroundAppStub>		THE_CC_WORKAROUND_APP_STUB;
	static {
		THE_CC_WORKAROUND_APP_STUB = new RFSpec<WorkaroundAppStub>(RFKind.CC_WORKAROUND_APP_STUB, WorkaroundAppStub.class, false);
	}

	// protected static FacadeSpec<PhysicsStuffBuilder>	THE_CC_PHYSICS_FACADE;
	
//		CC_PHYSICS_FACADE	


			
	private static <EFT, EFK> EFT  findExternalFacadeOrNull(FacadeSpec<EFT, EFK> fs, String optOverrideName, Class optCredClaz) {		
		EFT result = null;
		
		SubsystemHandle shand = SubsystemHandleFinder.getRenderSubsysHandle(fs,  optCredClaz);
		FacadeHandle<EFT> fh = shand.findExternalFacade(fs, optOverrideName);
		if (fh.isReady()) {
			result = fh.getOrElse(null);
		} 
		return result;
	}
	private static <EFT, EFK> void registerExternalFacade(FacadeSpec<EFT, EFK> fs, EFT facade, String optOverrideName, Class optCredClaz) {
		SubsystemHandle shand = SubsystemHandleFinder.getRenderSubsysHandle(fs,  optCredClaz);
		shand.registerExternalFacade(fs, facade, optOverrideName);	
	}
	private static <IFT, IFK> IFT  findOrMakeInternalFacade(FacadeSpec<IFT, IFK> fs, String optOverrideName, Class optCredClaz ) {		
		try {
		SubsystemHandle shand = SubsystemHandleFinder.getRenderSubsysHandle(fs,  optCredClaz);
		return shand.findOrMakeInternalFacade(fs, optOverrideName);
		} catch (java.lang.NoClassDefFoundError cnf) {
			Logger theLogger = LoggerFactory.getLogger(RenderRegistryFuncs.class);
			theLogger.error("Cannot findOrMakeInternalFacade " + fs + " ovn=" + optOverrideName + " {}", optCredClaz, cnf);
			throw cnf;
		}
	}
	protected static AssetManager findJme3AssetManager(String optionalName) {
		return findExternalFacadeOrNull(THE_JME3_ASSET_MANAGER, optionalName, null);
	}
	protected static void registerJme3AssetManager(AssetManager am, String optionalName) {
		registerExternalFacade(THE_JME3_ASSET_MANAGER, am, optionalName, null);	
	}
	protected static Node findJme3RootDeepNode(String optionalName) {
		return findExternalFacadeOrNull(THE_JME3_ROOT_DEEP_NODE, optionalName, null);
	}
	protected static void registerJme3RootDeepNode(Node n, String optionalName) {
		registerExternalFacade(THE_JME3_ROOT_DEEP_NODE, n, optionalName, null);
	}
	protected static Node findJme3RootOverlayNode(String optionalName) {
		return findExternalFacadeOrNull(THE_JME3_ROOT_OVERLAY_NODE, optionalName, null);
	}
	protected static void registerJme3RootOverlayNode(Node n, String optionalName) {
		registerExternalFacade(THE_JME3_ROOT_OVERLAY_NODE, n, optionalName, null);
	}	
	
	protected static AppStateManager findJme3AppStateManager(String optionalName) {
		return findExternalFacadeOrNull(THE_JME3_APP_STATE_MANAGER, optionalName, null);
	}
	protected static void registerJme3AppStateManager(AppStateManager asm, String optionalName) {
		registerExternalFacade(THE_JME3_APP_STATE_MANAGER, asm, optionalName, null);
	}
	protected static InputManager findJme3InputManager(String optionalName) {
		return findExternalFacadeOrNull(THE_JME3_INPUT_MANAGER, optionalName, null);
	}
	protected static void registerJme3InputManager(InputManager im, String optionalName) {
		registerExternalFacade(THE_JME3_INPUT_MANAGER, im, optionalName, null);
	}
	protected static RenderManager findJme3RenderManager(String optionalName) {
		return findExternalFacadeOrNull(THE_JME3_RENDER_MANAGER, optionalName, null);
	}
	protected static void registerJme3RenderManager(RenderManager im, String optionalName) {
		registerExternalFacade(THE_JME3_RENDER_MANAGER, im, optionalName, null);
	}
	protected static BulletAppState findJme3BulletAppState(String optionalName) {
		return findExternalFacadeOrNull(THE_JME3_BULLET_APP_STATE, optionalName, null);
	}
	protected static void registerJme3BulletAppState(BulletAppState bas, String optionalName) {
		registerExternalFacade(THE_JME3_BULLET_APP_STATE, bas, optionalName, null);
	}	

	protected static ViewportFacade findOrMakeOpticViewportFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_OPTIC_VIEWPORT_FACADE, optionalName, null);
	}
	protected static CameraMgr findOrMakeOpticCameraFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_OPTIC_CAMERA_FACADE, optionalName, null);
	}
	protected static LightFactory findOrMakeOpticLightFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_OPTIC_LIGHT_FACADE, optionalName, null);
	}
	protected static MatFactory findOrMakeOpticMaterialFacade(String optionalName, String optAssetContextName) {
		// TODO - do something cool to make sure that optAssetContextName is compatible
		return findOrMakeInternalFacade(THE_CC_OPTIC_MATERIAL_FACADE, optionalName, null);
	}	
	
	protected static TextureFactory findOrMakeOpticTextureFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_OPTIC_TEXTURE_FACADE, optionalName, null);
	}
	
	protected static ShapeMeshFactory findOrMakeMeshShapeFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_MESH_SHAPE_FACADE, optionalName, null);
	}	
	protected static WireMeshFactory findOrMakeMeshWireFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_MESH_WIRE_FACADE, optionalName, null);
	}	
	protected static FancyMeshFactory findOrMakeMeshFancyFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_MESH_FANCY_FACADE, optionalName, null);
	}	
	

	protected static GeomFactory findOrMakeSceneGeometryFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_SCENE_GEOMETRY_FACADE, optionalName, null);
	}		
	protected static DeepSceneMgr findOrMakeSceneDeepFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_SCENE_DEEP_FACADE, optionalName, null);
	}
	protected static FlatOverlayMgr findOrMakeSceneFlatFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_SCENE_FLAT_FACADE, optionalName, null);
	}
	protected static ModelSpatialFactory findOrMakeSceneSpatialModelFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_SCENE_SPATIAL_MODEL_FACADE, optionalName, null);
	}
	protected static TextMgr findOrMakeSceneTextFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_SCENE_TEXT_FACADE, optionalName, null);
	}
	protected static PathMgr findOrMakeScenePathFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_SCENE_PATH_FACADE, optionalName, null);
	}
	protected static SpatialAnimMgr findOrMakeSceneAnimFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_SCENE_ANIM_FACADE, optionalName, null);
	}

		
	// This one needs to be public, so that BundleActivators can find it, to register their classloader-markers.
	public static AssetContext findOrMakeAssetContext(String optionalName, String optJme3AssetManagerName) {
		// TODO - do something cool to make sure that optJme3AssetManagerName is compatible with the
		// named JME3_ASSET_MANAGER, because this is the *constraint* being supplied by the application.
		return findOrMakeInternalFacade(THE_CC_ASSET_CONTEXT, optionalName, null);
	}

	/*
	 We offer an explicit credClaz version, to account for the case where an Activator
	 wants to register a marker with assetContext BEFORE the target facade class (AssetContext)
	 is willing/able to return a bundle that has a valid bundleContext.
	 This circumstance occurs under Netigso (though RegistryClient was then the assumed credClaz, as shown below)
	 
 INFO [FelixStartLevel] (ResourceBundleActivator.java:22) - ******************* Registering assumed resource bundle with default AssetContext
 WARN [FelixStartLevel] (RegistryServiceFuncs.java:145) - %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  bundle getBundleContext() returned null - OSGi permissions or load-ordering problem for bundle [org.cogchar.org.cogchar.bundle.core [117]] via credClaz[class org.cogchar.blob.emit.RegistryClient$]
 INFO [FelixStartLevel] (RegistryServiceFuncs.java:112) - %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  Getting singleton WellKnownRegistry in non-OSGi context
 INFO [FelixStartLevel] (RegistryServiceFuncs.java:114) - %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%  Making singleton WellKnownRegistry for non-OSGi context
	 */
	public static AssetContext findOrMakeAssetContext(String optionalName, 	String optJme3AssetManagerName, Class optCredClaz) {
		return findOrMakeInternalFacade(THE_CC_ASSET_CONTEXT, optionalName, optCredClaz);
	}
/*
	public static AssetContext findOrMakeAssetContext(String optionalName, 	String optJme3AssetManagerName, BundleContext reqBundleCtx) {
		return null; // return findOrMakeInternalFacade(THE_CC_ASSET_CONTEXT, optionalName, optCredClaz);
	}
	* 
	*/ 
	
	protected static void registerWorkaroundAppStub(WorkaroundAppStub stub) { 
		registerExternalFacade(THE_CC_WORKAROUND_APP_STUB, stub, null, null);
	}
	protected static WorkaroundAppStub findWorkaroundAppStub() {
		return findExternalFacadeOrNull(THE_CC_WORKAROUND_APP_STUB, null, null);
	}	
}
