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

package org.cogchar.render.model.humanoid;

import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.appdapter.core.name.Ident;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.scene.Node;

import java.util.HashMap;
import java.util.Map;
import org.cogchar.blob.emit.RenderConfigEmitter;
import org.cogchar.render.app.core.WorkaroundAppStub;
import org.cogchar.render.app.bony.BonyRenderContext;
import org.cogchar.api.humanoid.FigureConfig;
import org.cogchar.api.humanoid.HumanoidFigureConfig;
import org.cogchar.render.model.humanoid.HumanoidFigureModule;
import org.cogchar.render.model.humanoid.HumanoidFigure;
import java.util.Iterator;
import java.util.concurrent.Callable;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.render.sys.task.BasicCallableRenderTask;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.cogchar.render.opengl.scene.FigureBoneNodeFinder;
import org.cogchar.api.humanoid.FigureBoneReferenceConfig;

import org.appdapter.core.log.BasicDebugger;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class HumanoidFigureManager extends BasicDebugger implements FigureBoneNodeFinder {

	private Map<Ident, HumanoidFigure> myFiguresByCharIdent = new HashMap<Ident, HumanoidFigure>();

	public HumanoidFigure getOrMakeHumanoidFigure(RepoClient qi, Ident charIdent, FigureConfig hc, 
				Ident bonyConfigGraph, RenderConfigEmitter rce) {
		HumanoidFigure hf = myFiguresByCharIdent.get(charIdent);
		if (hf == null) {
			//BonyConfigEmitter bce = getBonyConfigEmitter();
			String matPath = rce.getMaterialPath();
			HumanoidFigureConfig hfc = new HumanoidFigureConfig(qi, hc, matPath, bonyConfigGraph); // rce, bonyConfigGraph);
			if (hfc.isComplete()) {
				hf = new HumanoidFigure(hfc);
				myFiguresByCharIdent.put(charIdent, hf);
			}
		}
		return hf;
	}

	// A few places want to just get the HumanoidFigure and aren't interested in possibly creating it.
	// Those features don't want to have to worry about the graph idents, which are just for loading config
	// (CoreFeatureAdapter.attachToHumanoidBone, HumanoidPuppetActions.getSinbad)
	// I don't like overloading this method, but probably only a temporary fix
	public HumanoidFigure getHumanoidFigure(Ident charIdent) {
		return myFiguresByCharIdent.get(charIdent);
	}
	
	// This method is probably even more problematic than getHumanoidFigure(Ident), but for now it provides a way for 
	// the Goody/Entity system to get a list of figures to be able to control. Soon this will probably be done directly
	// from the repo.
	public Map<Ident, HumanoidFigure> getHumanoidFigures() {
		return myFiguresByCharIdent;
	}

	// Now does more, but does less on jME thread!
	public HumanoidFigure setupHumanoidFigure(final BonyRenderContext brc, RepoClient qi, final Ident charIdent, 
					Ident bonyConfigGraph, FigureConfig hc) throws Throwable {
		getLogger().info("beginning setup for charID={}", charIdent);
		RenderRegistryClient rrc = brc.getRenderRegistryClient();
		RenderConfigEmitter rce = brc.getConfigEmitter();
		final HumanoidFigure figure = getOrMakeHumanoidFigure(qi, charIdent, hc, bonyConfigGraph, rce);
		final AssetManager amgr = rrc.getJme3AssetManager(null);
		final Node rootNode = rrc.getJme3RootDeepNode(null);
		final PhysicsSpace ps = brc.getPhysicsSpace();
		if (figure == null) {
			getLogger().warn("aborting setup for charID={} - found null HumanoidFigure", charIdent);
			return null;
		}
		/**
		 * This task will eventually run async on the OpenGL render thread, and will load our OpenGL figure,
		 * and make it snazzy.
		 */
		brc.runTaskSafelyUntilComplete(new BasicCallableRenderTask(brc) {

			@Override public void performWithClient(RenderRegistryClient rrc) throws Throwable {
				boolean figureInitOK = figure.loadMeshAndSkeletonIntoVWorld(amgr, rootNode, ps);
				if (figureInitOK) {
					// Create a coroutine execution module to accept time slices, to 
					// allows us to animate the humanoid figure.
					final HumanoidFigureModule hfm = new HumanoidFigureModule(figure, brc);
					figure.setModule(hfm);
					// Activate coroutine threading for our  module.
					brc.attachModule(hfm);
					getLogger().warn("Async Result (not really a 'warning') : Figure initialized and HumanoidFigureModule attached for {}", charIdent);
				} else {
					getLogger().warn("Delayed problem in code launched from setupHumanoidFigure():  Figure init failed for: {}", charIdent);
				}
			}
		});
		// Now we are back to the main thread.    We do not know if figureInit will succeed later,
		// but regardless

		// Now back on the main thread again.
		return figure;
	}

	public void detachHumanoidFigures(final BonyRenderContext brc) {
		RenderRegistryClient rrc = brc.getRenderRegistryClient();
		final Node rootNode = rrc.getJme3RootDeepNode(null);
		final PhysicsSpace ps = brc.getPhysicsSpace();
		Iterator<HumanoidFigure> currentFigureIterator = myFiguresByCharIdent.values().iterator();
		while (currentFigureIterator.hasNext()) {
			final HumanoidFigure aHumanoid = currentFigureIterator.next();
			brc.enqueueCallable(new Callable<Void>() { // Do this on main render thread

				@Override public Void call() throws Exception {
					brc.detachModule(aHumanoid.getModule());
					aHumanoid.detachFromVirtualWorld(rootNode, ps);
					return null;
				}
			});
		}
		myFiguresByCharIdent.clear();
	}

	public void toggleDebugSkeletons() {
		for (HumanoidFigure hf : myFiguresByCharIdent.values()) {
			hf.toggleDebugSkeleton_onSceneThread();
		}
	}
	
	
	public Node findHumanoidBoneAttachNode(final Ident charFigID, final String boneName) {
		final HumanoidFigure humaFig = getHumanoidFigure(charFigID);
		Node attachmentNode = null;
		if (humaFig == null) {
			getLogger().warn("Failed to find bone {} due to missing robot for charFigID={}", boneName, charFigID);
		} else {
			attachmentNode =  humaFig.getBoneAttachmentsNode(boneName);
			if (attachmentNode == null) {
				getLogger().warn("Could not find bone {} on robot for charFigID={}", boneName, charFigID);	
			}
		}
		return attachmentNode;
	}


	@Override public com.jme3.scene.Node findFigureBoneNode(FigureBoneReferenceConfig figBoneRef) {
		return findHumanoidBoneAttachNode(figBoneRef.getFigureID(), figBoneRef.getBoneName());
	}
}
