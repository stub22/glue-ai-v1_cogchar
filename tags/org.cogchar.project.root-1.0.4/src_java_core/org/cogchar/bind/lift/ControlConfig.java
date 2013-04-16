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

/**
 * @author Ryan Biggs
 */
public class ControlConfig {

	public String myURI_Fragment;
	public ControlType controlType;
	public int id;
	public String action;
	public String text;
	public String style;
	public String resource;

	@Override
	public String toString() {
		return "ControlConfig[uriFrag=" + myURI_Fragment + ", Type=" + controlType.name() + ", id=" + id + ", text=\""
				+ text + "\", style=" + style + "resource=" + resource + "]";
	}

	public ControlConfig(Item configItem) {
		myURI_Fragment = configItem.getIdent().getLocalName();
		String typeString = ItemFuncs.getString(configItem, LiftConfigNames.P_controlType, null);
		// No switch on strings in Java 1.6, too bad. At least for the moment there's only one option!
		if (typeString.equals("PUSHYBUTTON")) {
			controlType = ControlType.PUSHYBUTTON;
		}
		id = ItemFuncs.getInteger(configItem, LiftConfigNames.P_controlId, 0);
		action = ItemFuncs.getString(configItem, LiftConfigNames.P_controlAction, "");
		text = ItemFuncs.getString(configItem, LiftConfigNames.P_controlText, "");
		style = ItemFuncs.getString(configItem, LiftConfigNames.P_controlStyle, "");
		resource = ItemFuncs.getString(configItem, LiftConfigNames.P_controlResource, "");
	}

	public enum ControlType {

		PUSHYBUTTON
	}
}