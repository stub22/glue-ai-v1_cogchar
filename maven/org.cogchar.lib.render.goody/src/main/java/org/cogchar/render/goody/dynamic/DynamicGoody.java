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
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.item.Item;
import org.appdapter.core.store.ModelClient;
//import org.appdapter.help.repo.{RepoClient, RepoClientImpl, InitialBindingImpl} 
//import org.appdapter.impl.store.{FancyRepo};
//import org.appdapter.core.matdat.{SheetRepo,_}
//import com.hp.hpl.jena.query.{QuerySolution} // Query, QueryFactory, QueryExecution, QueryExecutionFactory, , QuerySolutionMap, Syntax};
import com.hp.hpl.jena.rdf.model.Model;

import org.appdapter.core.log.BasicDebugger;

/**
 *
  * @author Stu B. <www.texpedient.com>
  * 
  * Manages a single DynaGoody that is part of some space.  (For now, we assume there is just one space that any
  * DynaGoody is involved in)
  * A DynaGoody has a presence in all three of:
  *		1) Semantic Space: a GoodySpec, which may be edited by user, either offline or while we are running
  * -- Does every DynaGoody necessarily have a concrete spec record of its own? 
  *    Or might some of these specs be implicit? 
  * 
  *		2) Math Space : a set of parameters changing over time, related by functions, as defined by specs
  *	-- which might be shared among some DynaGoodies, and/or attached to another clump of objects on which the
  * DynaGoodies rely.
  * 
  *		3) V-World OpenGL Space: rendered 3D display for user
  * 
  * DynamicGoody is a base-class 
  * 
  * A DynaGoody has an immutable index, representing its allocation-position within its space.
  * A DynaGoody does not change its index or its space.
  * (But what about its spec or its subclass?  Can those change?)
  * The first DynaGoody in a space is at index 1 (not 0).
  * 
  * This idea is related to Cells found in org.cogchar.api.space, but here we are 
  * more concretely committed to the idea of each DynaGoody having an object representation,
  * and being updated as part of the VWorld update loop.
 */


public class DynamicGoody extends BasicDebugger {
	// This goody exists "within" myDGSpace, "at" index myGoodyIndex.
	private DynamicGoodySpace		myDGSpace;
	private	Integer					myGoodyIndex;
	
	// This OpenGL node is always a child of the Node held in myDGSpace.
	private	Node					myDisplayNode;

	public DynamicGoody(int index) {
		myGoodyIndex = index;
	}
	public void setParentSpace(DynamicGoodySpace<?> dgSpace) {
		myDGSpace = dgSpace;
	}

	protected DynamicGoodySpace getParentSpace() {
		return myDGSpace;
	}
	protected Integer getIndex() {
		return myGoodyIndex;
	}
	protected Node getDisplayNode() {
		return myDisplayNode;
	}

	// This is the crucial entry point.  Default does nothing.  Override to update your goody display,
	// but don't hog the OpenGL thread, or you will make the display stutter.
	public void doFastVWorldUpdate_onRendThrd() { 
	}
	protected void detachAndDispose() {
		// Clean up called when we are resized out of existence.
	}
}
