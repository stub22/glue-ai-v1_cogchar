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

package org.cogchar.app.puma.boot;

import org.appdapter.help.repo.RepoClient;
import org.cogchar.app.puma.vworld.PumaVirtualWorldMapper;
import org.cogchar.app.puma.config.PumaConfigManager;
import org.cogchar.blob.emit.RenderConfigEmitter;
import org.cogchar.platform.trigger.CogcharScreenBox;
import org.cogchar.render.app.bony.BonyGameFeatureAdapter;
import org.cogchar.render.app.bony.BonyRenderContext;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.model.humanoid.HumanoidFigure;
import org.cogchar.render.model.humanoid.HumanoidFigureManager;
import org.cogchar.render.opengl.scene.PathMgr;
import org.cogchar.render.opengl.scene.SpatialAnimMgr;
/**
 * This class is intended to be the "public" API to the PUMA "system",
 * for use from direct commands sent by GUIs or network.  
 * 
 * An instance of this object is referred to in our repositories, as 
 * registered with the URI returned by an application's  
 * PumaContextMediator.getSysContextURI() method.
 * 
 * Having "public" methods on this object helps us to keep more
 * of PumaAppContext's methods "protected", and also keep track
 * in one place of what we're officially "exposing" to the command 
 * layer.
 * 
 * @author Stu B. <www.texpedient.com>
 */

public class PumaContextCommandBox extends CogcharScreenBox {
	private		PumaAppContext		myPAC;
	
	protected PumaContextCommandBox(PumaAppContext pac) {
		myPAC = pac;
	}
	protected HumanoidRenderContext getHRC() { 
		return myPAC.getOrMakeVWorldMapper().getHumanoidRenderContext();
	}
	public BonyGameFeatureAdapter getGameFeatureAdapter() { 
		return getHRC().getGameFeatureAdapter();
	}
	public HumanoidFigureManager getFigureManager() { 
		return getHRC().getHumanoidFigureManager();
	}
	public PathMgr getPathMgr() {
		return getHRC().getRenderRegistryClient().getScenePathFacade(null);
	}
	public SpatialAnimMgr getThingAnimMgr() {
		return getHRC().getRenderRegistryClient().getSceneAnimFacade(null);
	}
	public void resetMainCameraLocation() { 
		getHRC().setDefaultCameraLocation();
	}

	public HumanoidFigure getSinbad() { 
		BonyRenderContext brc = getHRC();
		RenderConfigEmitter bce = brc.getConfigEmitter();
		HumanoidFigureManager hfm = getFigureManager();
		return hfm.getHumanoidFigure(bce.SINBAD_CHAR_IDENT());
	}	
	public PumaVirtualWorldMapper getVWM() { 
		return myPAC.getOrMakeVWorldMapper();
	}
	
	// A half baked (3/4 baked?) idea. Since PumaAppContext is basically in charge of global config right now, this will be a general
	// way to ask that config be updated. Why the string argument? See UpdateInterface comments...
	private boolean myUpdateInProgressFlag = false;
	// Here I have removed the method variable passed in for the RepoClient. Why? Because right now PumaAppContext really
	// is the central clearing house for the RepoClient for config -- ideally we want it to be passed down from one master instance here to
	// all the objects that use it. Methods calling for config updates via this method shouldn't be responsible for 
	// knowing what RepoClient is appropriate -- they are calling into this method because we are trying to handle that here.
	// So for now let's use the this.getQueryHelper way to get that interface here. We can continue to refine this thinking as we go.
	// - Ryan 2012-09-17
		// Eventually we may decide on a good home for these constants:	
	
	
	final public static String WORLD_CONFIG = "worldconfig";
	final public static String BONE_ROBOT_CONFIG = "bonerobotconfig";
	final public static String MANAGED_GCS = "managedglobalconfigservice";
	final public static String ALL_HUMANOID_CONFIG = "allhumanoidconfig";
	final public static String THING_ACTIONS = "thingactions";
	// Currently used from two places:
	// org/cogchar/app/puma/cgchr/PumaVirtualWorldMapper.java:[74,15] 
	// org/cogchar/app/puma/cgchr/CommandTargetForUseFromWeb.java:[66,25] 
	public boolean updateConfigByRequest(final String request, final boolean resetMainConfigFlag) {
		// Do the actual updates on a new thread. That way we don't block the render thread. Much less intrusive, plus this way things
		// we need to enqueue on main render thread will actually complete -  it must not be blocked during some of the update operations!
		// This brings up an interesting point: we are probably doing far too much on the main jME thread!
		logInfo("Updating config by request: " + request);
		boolean success = true;
		if (myUpdateInProgressFlag) {
			getLogger().warn("Update currently underway, ignoring additional request");
			success = false;
		} else {
			myUpdateInProgressFlag = true;
			//  Such direct thread spawning is not as good an approach as submitting a Callable object to an existing thread.		
			Thread updateThread = new Thread("GoofyUpdateThread") {
				public void run() {
					processUpdateRequestNow(request, resetMainConfigFlag);
					myUpdateInProgressFlag = false;
				}
			};
			updateThread.start();
		}
		return success;
	}

	private boolean processUpdateRequestNow(String request, final boolean resetMainConfigFlag) {
		boolean successFlag = true;
		if (WORLD_CONFIG.equals(request.toLowerCase())) {
			myPAC.initCinema(true);
		} else if (BONE_ROBOT_CONFIG.equals(request.toLowerCase())) {
			myPAC.reloadBoneRobotConfig();
		} else if (MANAGED_GCS.equals(request.toLowerCase())) {
			final PumaConfigManager pcm = myPAC.getConfigManager();
			pcm.clearOSGiComps();
			myPAC.reloadGlobalConfig();
		} else if (ALL_HUMANOID_CONFIG.equals(request.toLowerCase())) {
			myPAC.reloadAll(resetMainConfigFlag);
		} else if (THING_ACTIONS.equals(request.toLowerCase())) {
			myPAC.resetMainConfigAndCheckThingActions();
		} else {
			getLogger().warn("PumaAppContext did not recognize the config update to be performed: {}", request);
			successFlag = false;
		}
		return successFlag;
	}
	public 	RepoClient getMainConfigRepoClient() {
		PumaConfigManager pcm = myPAC.getConfigManager();
		return pcm.getMainConfigRepoClient();
	}
}
