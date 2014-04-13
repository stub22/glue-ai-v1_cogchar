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

import org.cogchar.render.sys.registry.RenderRegistryClient;

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

public abstract class DynamicGoodySpace<DGT extends DynamicGoody> extends DynamicGoody implements TrialUpdater, DynamicGoodyParent {
		
	private	Node			myGroupDisplayNode; // , myParentDisplayNode;
	//  We use an array to emphasize the indexed nature of this space.
	// However, "goodyIndex" always starts at 1, so we have to subtract 1
	private	DynamicGoody	myGoodies[] = new DynamicGoody[0];
	
	protected Integer		myNextSize;
	
	public DynamicGoodySpace(DynamicGoodySpace<?> optParentSpace, int idxIntoParentOrNeg) { 
		super(idxIntoParentOrNeg);
		if (optParentSpace != null) {
			setParent(optParentSpace);
		}
		
	}
	protected void setDesiredSize(int size) {
		myNextSize = size;
	}
	// This will be called if we have been explicitly attached as a TrialUpdater.
	// Should not be called if we are a child space.
	@Override public void doUpdate(RenderRegistryClient rrc, float tpf) {
		doFastVWorldUpdate_onRendThrd(rrc);
	}	
	@Override public void doFastVWorldUpdate_onRendThrd(RenderRegistryClient rrc) {
		if (myNextSize != null) {
			resizeSpace_onRendThrd(myNextSize);
			myNextSize = null;
		}
		for (int idx = 0; idx < myGoodies.length; idx++) {
			// Some of these may be child spaces.
			myGoodies[idx].doFastVWorldUpdate_onRendThrd(rrc);
		}
	}
	// Override to set good node names.
	@Override public String getUniqueName() { 
		return "generatedName_99";
	}
	@Override public Node getDisplayNode() { 
		if (myGroupDisplayNode == null) {
			myGroupDisplayNode = new Node(getUniqueName());
		}
		return myGroupDisplayNode;
	}
	// Called by resizeSpace.  Our default impl just makes a default DynamicGoody, which doesn't do much.
	// Override this method to create useful goodies of appropriate types.  
	protected abstract DGT makeGoody(Integer oneBasedIndex);
//		return new DynamicGoody(oneBasedIndex);
//	}
	
	public synchronized boolean hasGoodyAtIndex(int oneBasedIndex) {
		return ((oneBasedIndex >= 1) && (oneBasedIndex <= myGoodies.length));
	}
	public synchronized DGT getGoodyAtIndex(int oneBasedIndex) {
		if ((oneBasedIndex >= 1) && (oneBasedIndex <= myGoodies.length)) {
			return (DGT) myGoodies[oneBasedIndex - 1];
		} else {
			getLogger().error("Supplied index {} is out of bounds [1, {}]", oneBasedIndex, myGoodies.length);
			throw new RuntimeException("out of bounds goody index sent to space: " + getUniqueName());
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
	private synchronized void resizeSpace_onRendThrd(int size) { 
		int oldSize = myGoodies.length;
		if (oldSize == size) {
			return;
		}
		DynamicGoody nGoodies[] = new DynamicGoody[size];
		// First, copy over the old goodies that still fit.
		int maxCopy = Math.min(oldSize, size);
		for (int idx =0; idx < maxCopy; idx++) {
			nGoodies[idx] = myGoodies[idx];
		}
		// Next, make any new goodies required
		Node groupDisplayNode = getDisplayNode();
		if (groupDisplayNode != null) {
			for (int jdx = maxCopy; jdx < size; jdx++) {
				// jdx is zero-based, so we add one to set the 1-based Goody-space index.
				nGoodies[jdx] = makeGoody(jdx + 1);
				nGoodies[jdx].setParent(this);
				//  This happens during update:   nGoodies[jdx].ensureAttachedToParentNode_onRendThrd();
			}
		} else {
			throw new RuntimeException("Cannot resize goody space before group display node is available");
		}
		// Finally, detach + dispose any old goodies that are no longer in scope.
		for (int kdx = maxCopy; kdx < oldSize; kdx++) {
			myGoodies[kdx].detachAndDispose_onRendThrd();
		}
		myGoodies = nGoodies;
	}
	@Override public void detachAndDispose_onRendThrd() {
		super.detachAndDispose_onRendThrd();
		setDesiredSize(0);
		resizeSpace_onRendThrd(0);
	}
}
