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

package org.cogchar.api.web;

import org.appdapter.core.name.Ident;

/**
 *
 * An interface which will be implemented by Lifter so that Cogchar modules won't have to depend on o.c.bundle.bind.lift
 * 
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public interface WebAppInterface {
	
	// What does it mean to "activateControl" ?
	
	// Does this mean we are displaying it for the user, or that the user has interacted with it and we
	// are receiving information from that user?
	
	public void activateControlAction(Ident controlActionUri);
	public void activateControlsFromUri(Ident configUri);
	public void activateControlsFromUriForUser(String user, Ident configUri);
	public void activateControlsFromUriForUserClass(String webUserClass, Ident configIdent);
	public void activateControlFromConfig(int slotNum, WebControl controlConfig);
	public void activateControlFromConfigForUser(String user, int slotNum, WebControl controlConfig);
	public void activateControlFromConfigForUserClass(String userClass, int slotNum, WebControl controlConfig);
	
	public String getGlobalWebappVariable(String varName);
	public void displayGlobalWebappError(String errorCode, String errorText);
	
	public WebControl getNewControl();
	
	
	// Reflects org.cogchar.bind.lift.ControlConfig

	
	
	
}
