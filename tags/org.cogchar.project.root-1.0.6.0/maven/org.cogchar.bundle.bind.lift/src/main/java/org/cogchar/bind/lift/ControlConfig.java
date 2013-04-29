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
import org.appdapter.help.repo.RepoClient;
import org.appdapter.help.repo.Solution;
import org.appdapter.help.repo.SolutionHelper;
import org.appdapter.help.repo.SolutionList;
import org.cogchar.name.lifter.LiftCN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public ControlConfig(RepoClient qi, Solution solution) {
		SolutionHelper sh = new SolutionHelper();
		Ident myIdent = sh.pullIdent(solution, LiftCN.CONTROL_VAR_NAME);
		if (myIdent != null) { // This might be false (myIdent = null) if this is instantiated via getControlConfigFromUri
			myURI_Fragment = myIdent.getLocalName();
		}
		controlType = sh.pullIdent(solution, LiftCN.CONTROL_TYPE_VAR_NAME).getLocalName();
		if (controlType == null) {
			controlType = "NULLTYPE";
		} else {
			controlType = controlType.toUpperCase(); // Ensures lc:type property is case insensitive to local name
		}
		action = sh.pullIdent(solution, LiftCN.ACTION_VAR_NAME);
		// If no action specified, add the "blank action". This is mainly to protect us from NPEs in legacy code from
		// the days of action strings, which assumed no action would result in a blank string instead of a null pointer.
		if (action == null) {
			action = LiftCN.BLANK_ACTION;
		}
		text = sh.pullString(solution, LiftCN.TEXT_VAR_NAME, "");
		style = sh.pullString(solution, LiftCN.STYLE_VAR_NAME, "");
		resource = sh.pullString(solution, LiftCN.RESOURCE_VAR_NAME, "");
	}
	
	// A copy constructor - currently needed by PageCommander (but probably better if it wasn't...)
	public ControlConfig(ControlConfig configToCopy) {
		myURI_Fragment = configToCopy.myURI_Fragment;
		controlType = configToCopy.controlType;
		action = configToCopy.action;
		text = configToCopy.text;
		style = configToCopy.style;
		resource = configToCopy.resource;
	}
	
	// A factory method to get a ControlConfig by URI alone
	public static ControlConfig getControlConfigFromUri(RepoClient qi, Ident graphIdent, Ident configUri) {
		ControlConfig newConfig = null;
		SolutionList solutionList = qi.queryIndirectForAllSolutions(LiftCN.FREE_CONTROL_QUERY_TEMPLATE_URI, graphIdent, 
							LiftCN.CONTROL_QUERY_VAR_NAME, configUri);
		switch (solutionList.javaList().size()) {
			case 0:	theLogger.warn("Could not find control with URI {}", configUri); break;
			case 1: newConfig = new ControlConfig(qi, solutionList.javaList().get(0));
								newConfig.myURI_Fragment = configUri.getLocalName();
								break;
			default: theLogger.error("Found multiple controls with URI {}", configUri); break;
		}
		return newConfig;
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
