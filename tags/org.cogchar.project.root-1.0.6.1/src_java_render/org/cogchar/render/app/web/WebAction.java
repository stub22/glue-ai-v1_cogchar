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

package org.cogchar.render.app.web;

import org.appdapter.core.name.Ident;
import org.cogchar.api.thing.ThingActionSpec;
import org.cogchar.name.web.WebActionNames;
import org.cogchar.render.app.entity.EntityAction;

/**
 * A class to hold actions for execution in the Lifter web interface
 * 
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class WebAction extends EntityAction {
	
	public WebAction(ThingActionSpec actionSpec) {
		super(actionSpec);
	}
	
	public Ident getConfigIdent() {
		return paramTVMap.getAsIdent(WebActionNames.CONFIG);
	}
	
	// For repo encoded control activation action URIs
	public Ident getControlActionUri() {
		return paramTVMap.getAsIdent(WebActionNames.WEBCONTROL_ACTION);
	}
	
	public Integer getSlotID() {
		return paramTVMap.getAsInteger(WebActionNames.SLOT);
	}
	
	public Ident getControlType() {
		return paramTVMap.getAsIdent(WebActionNames.TYPE);
	}
	
	public String getControlText() {
		return paramTVMap.getAsString(WebActionNames.TEXT);
	}
	
	public String getControlStyle() {
		return paramTVMap.getAsString(WebActionNames.STYLE);
	}
	
	public String getControlResource() {
		return paramTVMap.getAsString(WebActionNames.RESOURCE);
	}
	
	public Ident getControlAction() {
		return paramTVMap.getAsIdent(WebActionNames.ACTION);
	}
	
	public String getUserName() {
		return paramTVMap.getAsString(WebActionNames.USERNAME);
	}
	
	public String getUserClass() {
		return paramTVMap.getAsString(WebActionNames.USERCLASS);
	}
}
