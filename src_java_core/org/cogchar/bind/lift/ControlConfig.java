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

import org.appdapter.core.item.Item;
import org.appdapter.core.item.ItemFuncs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ryan Biggs
 */
public class ControlConfig {

	static Logger theLogger = LoggerFactory.getLogger(ControlConfig.class);
	public String myURI_Fragment;
	//public ControlType controlType; // Probably a good idea to do away with this enum type - see comments at bottom
	public String controlType;
	public int id;
	public String action;
	public String text;
	public String style;
	public String resource;

	@Override
	public String toString() {
		return "ControlConfig[uriFrag=" + myURI_Fragment + ", Type=" + controlType + ", id=" + id + ", text=\""
				+ text + "\", style=" + style + " resource=" + resource + "]";
	}

	public ControlConfig(Item configItem) {
		myURI_Fragment = configItem.getIdent().getLocalName();
		/* This is for using ControlType enum type, which we are probably phasing out
		String typeString = ItemFuncs.getString(configItem, LiftConfigNames.P_controlType, null);
		controlType = ControlType.NULLTYPE;
		for (ControlType testType : ControlType.values()) {
			if (typeString.equals(testType.name())) {
				controlType = testType;
			}
		}
		if (controlType == ControlType.NULLTYPE) {
			theLogger.warn("Lift Control with URI Fragment " + myURI_Fragment + " does not indicate a valid type!");
		}
		*/
		controlType = ItemFuncs.getString(configItem, LiftConfigNames.P_controlType, "NULLTYPE");
		id = ItemFuncs.getInteger(configItem, LiftConfigNames.P_controlId, 0);
		action = ItemFuncs.getString(configItem, LiftConfigNames.P_controlAction, "");
		text = ItemFuncs.getString(configItem, LiftConfigNames.P_controlText, "");
		style = ItemFuncs.getString(configItem, LiftConfigNames.P_controlStyle, "");
		resource = ItemFuncs.getString(configItem, LiftConfigNames.P_controlResource, "");
	}

	/* Let's try doing away with this. It makes sense to have enum type for jME stuff internal to Cog Char, but 
	 * here it requires changes to Cog Char every time we add web app functionality
	public enum ControlType {

		NULLTYPE, PUSHYBUTTON, TEXTINPUT, SELECTBOXES, RADIOBUTTONS, LISTBOX
	}
	*/
}
