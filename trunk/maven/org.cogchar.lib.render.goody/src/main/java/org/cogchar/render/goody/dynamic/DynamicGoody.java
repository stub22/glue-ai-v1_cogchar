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

import org.appdapter.core.log.BasicDebugger;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 *
  * @author Stu B. <www.texpedient.com>
  *
 * DynamicGoody is a base-class.
 *
 * Each DynaGoody is part of some space.  (For now, we assume there is just one space that any
  * DynaGoody is involved in).
 *
 * The DynaGoody is aware of its space and is able to interact with it.   This is a difference it has from
 * the older BasicGoodyEntity.

 *
  * A DynaGoody has a presence in all three of:
 *		1) V-World OpenGL Space: rendered 3D display for user
 *		This aspect it has in common with other goody types.
 *
  *		2) Semantic Space: a URI for a GoodySpec.  Models defining that spec may be edited by user code,
 *		either offline or while we are running.
  *    Question:  Does every DynaGoody necessarily have a concrete spec record of its own?  Or might some of these
 *    specs be implicit?
 *
 *    The URI for a particular dyna-goody is derivable from two pieces:
 *    A) the URI of its parent space
 *    B) the index of the dyna goody into its parent space
  * 
  *		3) Math Space : a set of parameters changing over time, related by functions, as defined by specs
  *	-- which might be shared among some DynaGoodies, and/or attached to another clump of objects on which the
  * DynaGoodies rely.
  *
  * 
  * A DynaGoody has an immutable index, representing its allocation-position within its space.
  * A DynaGoody does not change its index or its space.
  * (But what about its spec or its kind/subclass?  Can those change?)
  * The first DynaGoody in a space is at index 1 (not 0).
  * 
  * This idea is related to Cells found in org.cogchar.api.space, but here we are 
  * more concretely committed to the idea of each DynaGoody having an object representation,
  * and being updated as part of the VWorld update loop.
 */


public class DynamicGoody extends BasicDebugger {
	// This goody exists "within" myDGSpace, "at" index myGoodyIndex.
	private DynamicGoodyParent		myParent;
	private	Integer					myGoodyIndex;
	
	// This OpenGL node is always a child of the Node held in myDGSpace.
	private	Node					myDisplayNode;

	public DynamicGoody(int index) {
		myGoodyIndex = index;
	}
	public void setParent(DynamicGoodyParent dgParent) {
		myParent = dgParent;
	}
	protected void ensureAttachedToParentNode_onRendThrd() {
		Node childDNode = getDisplayNode();
		DynamicGoodyParent dgParent = getParent();
		if (dgParent != null) {
			Node parentDNode = dgParent.getDisplayNode();
			if ((parentDNode != null ) && (childDNode != null)) {
				if(!parentDNode.hasChild(childDNode)) {
					getLogger().debug("Attaching childDNode {} to parentDNode {}", childDNode, parentDNode);
					parentDNode.attachChild(childDNode);
				}
			}
		}
	}
	// Used to derive the URI of the dyna-goody.
	public String getUniqueName() {
		String parentName = myParent.getUniqueName();
		return parentName + "_" + myGoodyIndex;
	}
	protected DynamicGoodyParent getParent() {
		return myParent;
	}
	protected Integer getIndex() {
		return myGoodyIndex;
	}
	public Node getDisplayNode() {
		if (myDisplayNode == null) {
			if (myParent != null) { 
				// The parent is used in constructing our uniqueName
				myDisplayNode = new Node(getUniqueName());
			}  else {
				getLogger().warn("getDisplayNode() cannot make node yet, because no parent is attached");
			}
		}
		return myDisplayNode;
	}

	// This is the crucial entry point.  Default does nothing.  Override to update your goody display,
	// but don't hog the OpenGL thread, or you will make the display stutter.
	// Default impl of DynamicGoodySpace does call this for each child sub-goody, on every JME.doUpdate loop,
	// but a subclass of DGS might choose to be more selective.
	public void doFastVWorldUpdate_onRendThrd(RenderRegistryClient rrc)  { 
	}
	
	public void detachAndDispose_onRendThrd() {
		// Clean up called when we are resized out of existence.
		getLogger().debug("Detach and dispose called for goody at index {}", getIndex());
		Node displayNode = getDisplayNode();
		if (displayNode != null) {
			Node parentNode = displayNode.getParent();
			if (parentNode != null) {
				parentNode.detachChild(displayNode);
			}
		}
		
	}
}
