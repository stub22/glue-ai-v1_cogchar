/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.render.model.goodies.robosteps;

import org.appdapter.core.name.Ident;
import org.cogchar.render.model.goodies.BasicGoodyImpl;
import org.cogchar.render.app.goody.GoodyAction;
import org.cogchar.name.goody.GoodyNames;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 * An abstract class containing elements common to Goody "bit" objects
 * 
 * @author Ryan Biggs
 */


public abstract class AbstractBitGoody extends BasicGoodyImpl {
	
	protected boolean state = false;
	
	protected AbstractBitGoody(RenderRegistryClient aRenderRegCli, Ident uri) {
		super(aRenderRegCli, uri);
	}
	
	public void setZeroState() {
		setState(false);
	}
	
	public void setOneState() {
		setState(true);
	}
	
	public void toggleState() {
		setState(!state);
	}
	
	public abstract void setState(boolean boxState);
	
	@Override
	public void applyAction(GoodyAction ga) {
		super.applyAction(ga); // Applies "standard" set and move actions
		// Now we act on anything else that won't be handled by BasicGoodyImpl but which has valid non-null parameters
		switch (ga.getKind()) {
			case SET : {
				String stateString = ga.getSpecialString(GoodyNames.BOOLEAN_STATE);
				if (stateString != null) {
					try {
						setState(Boolean.valueOf(stateString));
					} catch (Exception e) { // May not need try/catch after BasicTypedValueMap implementation is complete
						myLogger.error("Error setting box state to state string {}", stateString, e);
					}
				}
				break;
			}
		}
	}
}
