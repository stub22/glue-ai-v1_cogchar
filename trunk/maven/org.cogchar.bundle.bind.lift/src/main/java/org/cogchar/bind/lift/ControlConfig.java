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
package org.cogchar.bind.lift;

import org.appdapter.core.name.Ident;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.appdapter.help.repo.Solution;
import org.appdapter.help.repo.QueryInterface;

/**
 * @author Ryan Biggs
 */
public class ControlConfig {

	static Logger theLogger = LoggerFactory.getLogger(ControlConfig.class);
	public String myURI_Fragment;
	public String controlType = "NULLTYPE";
	public Ident action;
	public String text = "";
	public String style = "";
	public String resource = "";

	@Override
	public String toString() {
		return "ControlConfig[uriFrag=" + myURI_Fragment + ", Type=" + controlType + ", text=\""
				+ text + "\", style=" + style + " resource=" + resource + "]";
	}

	public ControlConfig() {
		// No need to do anything here; basically adding the default constructor for use by PageCommander in Lift
	}
	
	// A new constructor to build ControlConfig from spreadsheet
	public ControlConfig(QueryInterface qi, Solution solution) {
		Ident myIdent = qi.getIdentFromSolution(solution, LiftQueryNames.CONTROL_VAR_NAME);
		myURI_Fragment = myIdent.getLocalName();
		controlType = qi.getIdentFromSolution(solution, LiftQueryNames.CONTROL_TYPE_VAR_NAME).getLocalName();
		if (controlType == null) {
			controlType = "NULLTYPE";
		} else {
			controlType = controlType.toUpperCase(); // Ensures lc:type property is case insensitive to local name
		}
		action = qi.getIdentFromSolution(solution, LiftQueryNames.ACTION_VAR_NAME);
		// If no action specified, add the "blank action". This is mainly to protect us from NPEs in legacy code from
		// the days of action strings, which assumed no action would result in a blank string instead of a null pointer.
		if (action == null) {
			action = LiftQueryNames.BLANK_ACTION;
		}
		text = qi.getStringFromSolution(solution, LiftQueryNames.TEXT_VAR_NAME, "");
		style = qi.getStringFromSolution(solution, LiftQueryNames.STYLE_VAR_NAME, "");
		resource = qi.getStringFromSolution(solution, LiftQueryNames.RESOURCE_VAR_NAME, "");
	}
	
	// A copy constructor - currently needed by PageCommander, but probably better if it wasn't...
	public ControlConfig(ControlConfig configToCopy) {
		myURI_Fragment = configToCopy.myURI_Fragment;
		controlType = configToCopy.controlType;
		action = configToCopy.action;
		text = configToCopy.text;
		style = configToCopy.style;
		resource = configToCopy.resource;
	}

	/* No longer available unless we fix this up to support the new action URIs instead of strings
	public ControlConfig(Item configItem) {
		myURI_Fragment = configItem.getIdent().getLocalName();
		controlType = ItemFuncs.getString(configItem, LiftConfigNames.P_controlType, "NULLTYPE");
		action = ItemFuncs.getString(configItem, LiftConfigNames.P_controlAction, "");
		text = ItemFuncs.getString(configItem, LiftConfigNames.P_controlText, "");
		style = ItemFuncs.getString(configItem, LiftConfigNames.P_controlStyle, "");
		resource = ItemFuncs.getString(configItem, LiftConfigNames.P_controlResource, "");
	}
	*/
}
