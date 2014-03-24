 /*
 *  Copyright 2013 by The Friendularity Project (www.friendularity.org).
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
package org.cogchar.render.goody.dynamic;

import com.jme3.scene.Node;
import org.appdapter.core.name.Ident;
import org.appdapter.core.item.Item;

import org.appdapter.core.store.ModelClient;
import org.cogchar.bind.symja.MathGate;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.render.opengl.scene.DeepSceneMgr;
import org.cogchar.render.sys.registry.RenderRegistryClient;

import java.util.Set;
import java.util.HashSet;
import org.cogchar.render.trial.TrialUpdater;

/**
 *
 * @author Stu B. <www.texpedient.com>
 * 
 * Goal is flexibility + performance in animating fixed-size blocks V-World goodies using MathSpace and SpecGraphs
 * (which come from a user-editable source).  SpecGraph contains expressions which populate the MathSpace.
 * SpecGraph also defines instructions for how to construct + apply updates to particular goodies.
 * 
 * Under what conditions does every DynamicGoody have a spec?
 * Does individual DynaGoodySpec have authority to assert a subclass? 
 * 
 * A DGSpace is an openGL-bound container and CPU-delegator.
 * We 
 */

public abstract class DynamicGoodySpace<DGT extends DynamicGoody> extends DynamicGoody implements TrialUpdater {
		
	private	Node			myGroupDisplayNode, myParentDisplayNode;
	//  We use an array to emphasize the indexed nature of this space.
	// However, "goodyIndex" always starts at 1, so we have to subtract 1
	private	DynamicGoody	myGoodies[] = new DynamicGoody[0];
	
	public DynamicGoodySpace(DynamicGoodySpace<?> optParentSpace, int idxIntoParentOrNeg) { 
		super(idxIntoParentOrNeg);
		if (optParentSpace != null) {
			setParentSpace(optParentSpace);
		}
		
	}
	@Override public void doUpdate(RenderRegistryClient rrc, float tpf) {
		doFastVWorldUpdate_onRendThrd();
	}	
	public void doFastVWorldUpdate_onRendThrd() { 
		for (int idx = 0; idx < myGoodies.length; idx++) {
			myGoodies[idx].doFastVWorldUpdate_onRendThrd();
		}
	}
	// Override to set good node names.
	protected String getUniqueSpaceName() { 
		return "generatedName_99";
	}
	protected Node getGroupDisplayNode() { 
		if (myGroupDisplayNode == null) {
			myGroupDisplayNode = new Node(getUniqueSpaceName());
		}
		return myGroupDisplayNode;
	}
	// Called by resizeSpace.  Our default impl just makes a default DynamicGoody, which doesn't do much.
	// Override this method to create useful goodies of appropriate types.  
	protected abstract DGT makeGoody(Integer oneBasedIndex);
//		return new DynamicGoody(oneBasedIndex);
//	}
	
	public DGT getGoodyAtIndex(int oneBasedIndex) {
		if ((oneBasedIndex >= 1) && (oneBasedIndex <= myGoodies.length)) {
			return (DGT) myGoodies[oneBasedIndex - 1];
		} else {
			getLogger().error("Supplied index {} is out of bounds [1, {}]", oneBasedIndex, myGoodies.length);
			throw new RuntimeException("out of bounds goody index sent to space: " + getUniqueSpaceName());
		}
	}
	/**
	 * The only way to create or destroy goodies is to resize the space (which is usually done only by updating 
	 * the space-level spec).
	 * On expansion, existing goodies survive.  On contraction, all goodies
	 * up to the new size survive, higher than that size are logically forgotten.
	 *		...and we must detach+dispose of their OpenGL resources.
	 * @param size 
	 */
	public void resizeSpace(int size) { 
		int oldSize = myGoodies.length;
		if (oldSize == size) {
			return;
		}
		DynamicGoody nGoodies[] = new DynamicGoody[size];
		int maxCopy = Math.min(oldSize, size);
		for (int idx =0; idx < maxCopy; idx++) {
			nGoodies[idx] = myGoodies[idx];
		}
		// Make any new ones required
		for (int jdx = maxCopy; jdx < size; jdx++) {
			// jdx is zero-based, so we add one to set the 1-based Goody-space index.
			nGoodies[jdx] = makeGoody(jdx + 1);
		}
		// Detach + Dispose any old ones no longer in scope.
		for (int kdx = maxCopy; kdx < oldSize; kdx++) {
			myGoodies[kdx].detachAndDispose();
		}
		myGoodies = nGoodies;
	}
	@Override protected void detachAndDispose() {
		resizeSpace(0);
	}

	public void setParentDisplayNode_onRendThrd(Node n) { 
		// create and/or reparent the GroupDisplayNode
		myParentDisplayNode = n;
		myParentDisplayNode.attachChild(myGroupDisplayNode);
	}
	public void attachChildDisplayNodes_onRendThrd() { 
		// For all goodies, attach the child node to our groupDisplayNode
	}
	/*
	public void a_onRendThrd(RenderRegistryClient	rrc) {
		if (mySubsysNode != null) {
			DeepSceneMgr dsm = rrc.getSceneDeepFacade(null);
			dsm.attachTopSpatial(mySubsysNode);
		}
	}	
	*/
	
	public void activateDisplay() {
		
	}

}
