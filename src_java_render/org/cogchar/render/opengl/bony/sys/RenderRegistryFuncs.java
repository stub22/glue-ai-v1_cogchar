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
package org.cogchar.render.opengl.bony.sys;

import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.input.InputManager;
import com.jme3.scene.Node;
import org.appdapter.api.registry.Description;
import org.appdapter.api.registry.VerySimpleRegistry;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.registry.basic.BasicDescription;
import org.cogchar.blob.emit.RegistryClient;
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
 * @author Stu B. <www.texpedient.com>
 */
public class RenderRegistryFuncs extends BasicDebugger {
	
	private final static Logger		theLogger = LoggerFactory.getLogger(RenderRegistryFuncs.class);
	
	protected enum FacadeKind {
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
	
	protected static class FacadeSpec<FT> {

		private final Class<FT> myObjClazz; 
		private FacadeKind		myKind;
		
		FacadeSpec(FacadeKind kind, Class<FT> sClz) {
			myKind = kind;
			myObjClazz = sClz;
		}
		
		public Class<FT> getFacadeClass() { 
			return myObjClazz;
		}
		public FacadeKind getKind() { 
			return myKind;
		}
		public String getNameString() { 
			return getKind().toString();
		}
	}
	
	protected static final FacadeSpec<AssetManager>		THE_JME3_ASSET_MANAGER;
	protected static final FacadeSpec<Node>				THE_JME3_ROOT_DEEP_NODE;
	protected static final FacadeSpec<Node>				THE_JME3_ROOT_OVERLAY_NODE;
	protected static final FacadeSpec<AppStateManager>	THE_JME3_APP_STATE_MANAGER;
	protected static final FacadeSpec<InputManager>		THE_JME3_INPUT_MANAGER;
	
	protected static final FacadeSpec<PhysicsSpace>		THE_BULLET_PHYSICS_SPACE;
	
	static {
		THE_JME3_ASSET_MANAGER = new FacadeSpec<AssetManager>(FacadeKind.JME3_ASSET_MANAGER, AssetManager.class);
		THE_JME3_ROOT_DEEP_NODE = new FacadeSpec<Node>(FacadeKind.JME3_ROOT_DEEP_NODE, Node.class);
		THE_JME3_ROOT_OVERLAY_NODE = new FacadeSpec<Node>(FacadeKind.JME3_ROOT_OVERLAY_NODE, Node.class);	
		
		THE_JME3_APP_STATE_MANAGER = new FacadeSpec<AppStateManager>(FacadeKind.JME3_APP_STATE_MANAGER, AppStateManager.class);	
		THE_JME3_INPUT_MANAGER = new FacadeSpec<InputManager>(FacadeKind.JME3_INPUT_MANAGER, InputManager.class);	
		
		THE_BULLET_PHYSICS_SPACE  = new FacadeSpec<PhysicsSpace>(FacadeKind.JME3_ROOT_DEEP_NODE, PhysicsSpace.class);	
	}

	protected static FacadeSpec<AssetContext>	THE_CC_ASSET_CONTEXT;
	static {
		THE_CC_ASSET_CONTEXT = new FacadeSpec<AssetContext>(FacadeKind.CC_ASSET_CONTEXT, AssetContext.class);
	}
		
	protected static FacadeSpec<ViewportFacade>	THE_CC_OPTIC_VIEWPORT_FACADE;
	protected static FacadeSpec<CameraMgr>		THE_CC_OPTIC_CAMERA_FACADE;
	protected static FacadeSpec<LightFactory>	THE_CC_OPTIC_LIGHT_FACADE;
	protected static FacadeSpec<MatFactory>		THE_CC_OPTIC_MATERIAL_FACADE;
	protected static FacadeSpec<TextureFactory>	THE_CC_OPTIC_TEXTURE_FACADE;
	
	static {
		THE_CC_OPTIC_VIEWPORT_FACADE = new FacadeSpec<ViewportFacade>(FacadeKind.CC_OPTIC_VIEWPORT_FACADE, ViewportFacade.class);
		THE_CC_OPTIC_CAMERA_FACADE = new FacadeSpec<CameraMgr>(FacadeKind.CC_OPTIC_CAMERA_FACADE, CameraMgr.class);		
		THE_CC_OPTIC_LIGHT_FACADE = new FacadeSpec<LightFactory>(FacadeKind.CC_OPTIC_LIGHT_FACADE, LightFactory.class);
		THE_CC_OPTIC_MATERIAL_FACADE = new FacadeSpec<MatFactory>(FacadeKind.CC_OPTIC_MATERIAL_FACADE, MatFactory.class);
		THE_CC_OPTIC_TEXTURE_FACADE = new FacadeSpec<TextureFactory>(FacadeKind.CC_OPTIC_TEXTURE_FACADE, TextureFactory.class);
	}	

	protected static FacadeSpec<ShapeMeshFactory>	THE_CC_MESH_SHAPE_FACADE;
	protected static FacadeSpec<WireMeshFactory>	THE_CC_MESH_WIRE_FACADE;
	protected static FacadeSpec<FancyMeshFactory>	THE_CC_MESH_FANCY_FACADE;
	
	static {
		THE_CC_MESH_SHAPE_FACADE = new FacadeSpec<ShapeMeshFactory>(FacadeKind.CC_MESH_SHAPE_FACADE, ShapeMeshFactory.class);
		THE_CC_MESH_WIRE_FACADE = new FacadeSpec<WireMeshFactory>(FacadeKind.CC_MESH_WIRE_FACADE, WireMeshFactory.class);
		THE_CC_MESH_FANCY_FACADE = new FacadeSpec<FancyMeshFactory>(FacadeKind.CC_MESH_FANCY_FACADE, FancyMeshFactory.class);		
	}



	protected static FacadeSpec<GeomFactory>			THE_CC_SCENE_GEOMETRY_FACADE;	
	protected static FacadeSpec<DeepSceneMgr>			THE_CC_SCENE_DEEP_FACADE;
	protected static FacadeSpec<FlatOverlayMgr>			THE_CC_SCENE_FLAT_FACADE;
	protected static FacadeSpec<ModelSpatialFactory>	THE_CC_SCENE_SPATIAL_MODEL_FACADE;
	protected static FacadeSpec<TextMgr>				THE_CC_SCENE_TEXT_FACADE;

	static {
		THE_CC_SCENE_GEOMETRY_FACADE = new FacadeSpec<GeomFactory>(FacadeKind.CC_SCENE_GEOMETRY_FACADE, GeomFactory.class);
		THE_CC_SCENE_DEEP_FACADE = new FacadeSpec<DeepSceneMgr>(FacadeKind.CC_SCENE_DEEP_FACADE, DeepSceneMgr.class);		
		THE_CC_SCENE_FLAT_FACADE = new FacadeSpec<FlatOverlayMgr>(FacadeKind.CC_SCENE_FLAT_FACADE, FlatOverlayMgr.class);
		THE_CC_SCENE_SPATIAL_MODEL_FACADE = new FacadeSpec<ModelSpatialFactory>(FacadeKind.CC_SCENE_SPATIAL_MODEL_FACADE, ModelSpatialFactory.class);
		THE_CC_SCENE_TEXT_FACADE = new FacadeSpec<TextMgr>(FacadeKind.CC_SCENE_TEXT_FACADE, TextMgr.class);		
	}

	// protected static FacadeSpec<PhysicsStuffBuilder>	THE_CC_PHYSICS_FACADE;
	
//		CC_PHYSICS_FACADE	

	
	protected static VerySimpleRegistry getOverRegistry() { 
		VerySimpleRegistry vsr = RegistryClient.getVerySimpleRegistry();
		if (vsr == null) {
			theLogger.error("Somehow got a null OverRegistry");
		}
		return vsr;
	}
	
	protected static interface Maker<OT extends Object> {
		/**
		 * Makes an object of type OT in response to a finder-fault.
		 * 
		 * @return 
		 */
		public OT makeObj();
		/**
		 * Currently we're not telling you "which registry" to describe the object for,
		 * which might be helpful.
		 * 
		 * @param obj
		 * @param objName
		 * @return 
		 */
		public Description getRegistryDesc(OT obj, String objName);
	}
	
	protected static abstract class BasicMaker<OT extends Object> implements Maker<OT> {
		@Override public Description getRegistryDesc(OT obj, String objName) {
			return new BasicDescription(objName);
		}
	}
	/**
	 * Used to find or flexibly construct an object that is to be placed in a registry.
	 * @param <OT>
	 * @param objClaz
	 * @param objName
	 * @param maker
	 * @return 
	 */
	protected static <OT> OT  findOrMakeUniqueNamedObject(VerySimpleRegistry vsr, Class<OT> objClaz, String objName, Maker<OT> maker) {
		OT result = null;
		try {
			result = vsr.findOptionalUniqueNamedObject(objClaz, objName);
			if (result == null) {
				result = maker.makeObj();
				Description regDesc = maker.getRegistryDesc(result, objName);
				vsr.registerObject(result, regDesc);
			}
		} catch (Throwable t) {
			theLogger.error("findOrMakeUniqueNamedObject got finder or maker exception: ", t);
		}
		return result;
	}	
	protected static <OT> OT  findOrMakeUniqueNamedObject(Class<OT> objClaz, String objName, Maker<OT> maker) {
		VerySimpleRegistry vsr = getOverRegistry();
		return (vsr != null) ? findOrMakeUniqueNamedObject(vsr, objClaz, objName, maker) : null;

	}
	/**
	 * Further simplified "findOrMake" method, with longer name!  Uses objClaz.newInstance() as the maker.
	 * 
	 * @param <OT>
	 * @param objClaz
	 * @param objName
	 * @return Found object OR made (and now registered) object OR null on error.
	 */
	protected static <OT> OT  findOrMakeUniqueNamedObjectWithDefCons(final Class<OT> objClaz, final String objName) {	
		// TODO:  Optimization:   Keep a cache of these DefCons makers, to avoid unnecessary object construction
		// (of the anon-class Makers themselves), which is happening on every call to this method).
		return findOrMakeUniqueNamedObject(objClaz, objName, new BasicMaker<OT>() {
			@Override public OT makeObj() {
				try {
					theLogger.info("Making new object named " + objName + " using default constructor of " + objClaz);
					return objClaz.newInstance();
				} catch (InstantiationException ie) {
					theLogger.error("findOrMakeUniqueNamedObjectWithDefCons got default constructor exception: ", ie);
					return null;
				} catch (IllegalAccessException iae) {
					theLogger.error("findOrMakeUniqueNamedObjectWithDefCons got default constructor exception: ", iae);
					return null;
				}					
			}
		});
	}
	
	private static <FT> String determineFacadeName(FacadeSpec<FT> fs, String optOverrideName) {
		String actualName = fs.getNameString();
		if (optOverrideName != null) {
			actualName = optOverrideName;
		}
		return actualName;
	}
	/**
	 * 
	 * @param <IFT>
	 * @param fs
	 * @param optOverrideName
	 * @return 
	 */
	protected static <IFT> IFT  findOrMakeInternalFacade(FacadeSpec<IFT> fs, String optOverrideName) {	
		Class<IFT> facadeClaz = fs.getFacadeClass();
		String actualName = determineFacadeName(fs, optOverrideName);
		return findOrMakeUniqueNamedObjectWithDefCons(facadeClaz, actualName);
	}

	/**
	 * Paired with findExternalFacade, used for objects supplied from outside, mainly from JME3:
	 * 
	 * <ol><li>AssetManager</li>
	 * <li>root node</li>
	 * <li>flat GUI node</li>
	 * </ol>
	 * 
	 * @param obb
	 * @param objName 
	 */
	protected static <EFT>  void registerExternalFacade(FacadeSpec<EFT> fs, EFT facade, String optOverrideName) {
		VerySimpleRegistry vsr = getOverRegistry();
		String actualName = determineFacadeName(fs, optOverrideName);
		vsr.registerNamedObject(facade, actualName);
	}
	
	/**
	 * 
	 * Paired with registerExternalFacade, used for external objects, like JME3 assetManager, rootNode, guiNode.
	 * 
	 * @param <EFT>
	 * @param objClaz
	 * @param objName
	 * @return 
	 */
	protected static <EFT> EFT  findExternalFacade(FacadeSpec<EFT> fs, String optOverrideName) {
		EFT result = null;
		Class<EFT> facadeClaz = fs.getFacadeClass();
		VerySimpleRegistry vsr = getOverRegistry();
		String actualName = determineFacadeName(fs, optOverrideName);
		try { 
			result = vsr.findOptionalUniqueNamedObject(facadeClaz, actualName);
		} catch (Throwable t) {
			theLogger.error("findOptionalUniqueNamedObject got unexpected error ",  t);
		}
		return result;
	}
	
	protected static AssetManager findJme3AssetManager(String optionalName) {
		return findExternalFacade(THE_JME3_ASSET_MANAGER, optionalName);
	}
	protected static void registerJme3AssetManager(AssetManager am, String optionalName) {
		registerExternalFacade(THE_JME3_ASSET_MANAGER, am, optionalName);	
	}
	protected static Node findJme3RootDeepNode(String optionalName) {
		return findExternalFacade(THE_JME3_ROOT_DEEP_NODE, optionalName);
	}
	protected static void registerJme3RootDeepNode(Node n, String optionalName) {
		registerExternalFacade(THE_JME3_ROOT_DEEP_NODE, n, optionalName);
	}
	protected static Node findJme3RootOverlayNode(String optionalName) {
		return findExternalFacade(THE_JME3_ROOT_OVERLAY_NODE, optionalName);
	}
	protected static void registerJme3RootOverlayNode(Node n, String optionalName) {
		registerExternalFacade(THE_JME3_ROOT_OVERLAY_NODE, n, optionalName);
	}	
	
	protected static AppStateManager findJme3AppStateManager(String optionalName) {
		return findExternalFacade(THE_JME3_APP_STATE_MANAGER, optionalName);
	}
	protected static void registerJme3AppStateManager(AppStateManager asm, String optionalName) {
		registerExternalFacade(THE_JME3_APP_STATE_MANAGER, asm, optionalName);
	}
	protected static InputManager findJme3InputManager(String optionalName) {
		return findExternalFacade(THE_JME3_INPUT_MANAGER, optionalName);
	}
	protected static void registerJme3InputManager(InputManager im, String optionalName) {
		registerExternalFacade(THE_JME3_INPUT_MANAGER, im, optionalName);
	}	
	
	
	// This one needs to be public, so that BundleActivators can find it, to register their classloader-markers.
	public static AssetContext findOrMakeAssetContext(String optionalName, String optJme3AssetManagerName) {
		// TODO - do something cool to make sure that optJme3AssetManagerName is compatible with the
		// named JME3_ASSET_MANAGER, because this is the *constraint* being supplied by the application.
		return findOrMakeInternalFacade(THE_CC_ASSET_CONTEXT, optionalName);
	}




	protected static ViewportFacade findOrMakeOpticViewportFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_OPTIC_VIEWPORT_FACADE, optionalName);
	}
	protected static CameraMgr findOrMakeOpticCameraFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_OPTIC_CAMERA_FACADE, optionalName);
	}
	protected static LightFactory findOrMakeOpticLightFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_OPTIC_LIGHT_FACADE, optionalName);
	}
	protected static MatFactory findOrMakeOpticMaterialFacade(String optionalName, String optAssetContextName) {
		// TODO - do something cool to make sure that optAssetContextName is compatible
		return findOrMakeInternalFacade(THE_CC_OPTIC_MATERIAL_FACADE, optionalName);
	}	
	
	protected static TextureFactory findOrMakeOpticTextureFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_OPTIC_TEXTURE_FACADE, optionalName);
	}
	

	protected static ShapeMeshFactory findOrMakeMeshShapeFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_MESH_SHAPE_FACADE, optionalName);
	}	
	protected static WireMeshFactory findOrMakeMeshWireFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_MESH_WIRE_FACADE, optionalName);
	}	
	protected static FancyMeshFactory findOrMakeMeshFancyFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_MESH_FANCY_FACADE, optionalName);
	}	
	

	protected static GeomFactory findOrMakeSceneGeometryFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_SCENE_GEOMETRY_FACADE, optionalName);
	}		
	protected static DeepSceneMgr findOrMakeSceneDeepFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_SCENE_DEEP_FACADE, optionalName);
	}
	protected static FlatOverlayMgr findOrMakeSceneFlatFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_SCENE_FLAT_FACADE, optionalName);
	}
	protected static ModelSpatialFactory findOrMakeSceneSpatialModelFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_SCENE_SPATIAL_MODEL_FACADE, optionalName);
	}
	protected static TextMgr findOrMakeSceneTextFacade(String optionalName) {
		return findOrMakeInternalFacade(THE_CC_SCENE_TEXT_FACADE, optionalName);
	}

}
