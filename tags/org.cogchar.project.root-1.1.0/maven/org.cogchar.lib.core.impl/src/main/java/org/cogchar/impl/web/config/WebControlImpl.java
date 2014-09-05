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
package org.cogchar.impl.web.config;

import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.appdapter.help.repo.Solution;
import org.appdapter.help.repo.SolutionHelper;
import org.appdapter.help.repo.SolutionList;
import org.cogchar.api.web.WebControl;
import org.cogchar.name.lifter.LiftCN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ryan Biggs
 */
public class WebControlImpl implements WebControl {

	static Logger theLogger = LoggerFactory.getLogger(WebControlImpl.class);
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

	public WebControlImpl() {
		// No need to do anything here; basically adding the default constructor for use by PageCommander in Lift
	}
	
	// A new constructor to build WebControlImpl from spreadsheet
	public WebControlImpl(RepoClient qi, Solution solution) {
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
	public WebControlImpl(WebControl configToCopy) {
		if (configToCopy instanceof WebControlImpl) {
			myURI_Fragment = ((WebControlImpl) configToCopy).myURI_Fragment;
		}
		controlType = configToCopy.getType();
		action = configToCopy.getAction();
		text = configToCopy.getText();
		style = configToCopy.getStyle();
		resource = configToCopy.getResource();
	}
	
	// A factory method to get a WebControlImpl by URI alone
	public static WebControlImpl getControlConfigFromUri(RepoClient qi, Ident graphIdent, Ident configUri) {
		WebControlImpl newConfig = null;
		SolutionList solutionList = qi.queryIndirectForAllSolutions(LiftCN.FREE_CONTROL_QUERY_TEMPLATE_URI, graphIdent, 
							LiftCN.CONTROL_QUERY_VAR_NAME, configUri);
		switch (solutionList.javaList().size()) {
			case 0:	theLogger.warn("Could not find control with URI {}", configUri); break;
			case 1: newConfig = new WebControlImpl(qi, solutionList.javaList().get(0));
								newConfig.myURI_Fragment = configUri.getLocalName();
								break;
			default: theLogger.error("Found multiple controls with URI {}", configUri); break;
		}
		return newConfig;
	}
	
	
	// Getters and setters to implement WebAppInterface.Control
	public void setType(String type) {
		controlType = type;
	}
	public String getType() {
		return controlType;
	}
	
	public void setAction(Ident action) {
		this.action = action;
	}
	public Ident getAction() {
		return action;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	public String getText() {
		return text;
	}
	
	public void setStyle(String style) {
		this.style = style;
	}
	public String getStyle() {
		return style;
	}
	
	public void setResource(String resource) {
		this.resource = resource;
	}
	public String getResource() {
		return resource;
	}
	
	
	/* No longer available unless we fix this up to support the new action URIs instead of strings
	public WebControlImpl(Item configItem) {
		myURI_Fragment = configItem.getIdent().getLocalName();
		controlType = ItemFuncs.getString(configItem, LiftConfigNames.P_controlType, "NULLTYPE");
		action = ItemFuncs.getString(configItem, LiftConfigNames.P_controlAction, "");
		text = ItemFuncs.getString(configItem, LiftConfigNames.P_controlText, "");
		style = ItemFuncs.getString(configItem, LiftConfigNames.P_controlStyle, "");
		resource = ItemFuncs.getString(configItem, LiftConfigNames.P_controlResource, "");
	}
	*/
}
