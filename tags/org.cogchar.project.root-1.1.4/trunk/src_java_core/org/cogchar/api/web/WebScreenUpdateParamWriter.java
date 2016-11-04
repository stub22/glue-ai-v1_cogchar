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

import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;

import org.cogchar.name.lifter.LiftCN;
import org.cogchar.name.web.WebActionNames;
import org.cogchar.api.thing.ActionParamWriter;
import org.cogchar.api.thing.TypedValueMap;
import org.cogchar.api.thing.SerTypedValueMap;

/**
 * Typically used from a remote client to capture values for encoding in SPARQL-Update.
 * Intended for web actions instead of virtual world actions as in GoodyActionParamWriter
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */
public class WebScreenUpdateParamWriter extends ActionParamWriter {

	public WebScreenUpdateParamWriter(SerTypedValueMap tvMap) {
		super(tvMap);
	}
	
	
	// For setting entire LiftConfig (screen definition)
	public void putLiftConfigIdent(Ident configIdent) {
		// We can't do the following because ThingActionUpdater.buildActionParameterValueMap assumes all parameters are Strings in repo:
		//myBTVMap.putNameAtName(WebActionNames.CONFIG, configIdent);
		// So instead we convert to String:
		TypedValueMap tvm = getValueMap();
		tvm.putValueAtName(WebActionNames.CONFIG, configIdent.getAbsUriString());
	}
	
	
	// The next method is for setting a single control by repo-encoded "control action":
	public void putControlAction(Ident actionIdent) {
		SerTypedValueMap tvm = getValueMap();
		tvm.putValueAtName(WebActionNames.WEBCONTROL_ACTION, actionIdent.getAbsUriString());
	}
	
	// The next six methods are for setting a single control by specification:
	public void putSlotNum(int slotNum) {
		TypedValueMap tvm = getValueMap();
		tvm.putValueAtName(WebActionNames.SLOT, slotNum);
	}

	public void putType(Ident typeIdent) {
		TypedValueMap tvm = getValueMap();
		tvm.putValueAtName(WebActionNames.TYPE, typeIdent.getAbsUriString());
	}

	public void putText(String controlText) {
		TypedValueMap tvm = getValueMap();
		tvm.putValueAtName(WebActionNames.TEXT, blankNotNull(controlText));
	}

	public void putStyle(String styleName) {
		TypedValueMap tvm = getValueMap();
		tvm.putValueAtName(WebActionNames.STYLE, blankNotNull(styleName));
	}
	
	public void putResource(String resourceName) {
		TypedValueMap tvm = getValueMap();
		tvm.putValueAtName(WebActionNames.RESOURCE, blankNotNull(resourceName));
	}
	
	public void putAction(Ident actionIdent) {
		TypedValueMap tvm = getValueMap();
		if (actionIdent == null) {
			actionIdent = new FreeIdent(LiftCN.BLANK_ACTION); 
		}
		tvm.putValueAtName(WebActionNames.ACTION, actionIdent.getAbsUriString());
	}
	
	
	public void putUserName(String userName) {
		TypedValueMap tvm = getValueMap();
		tvm.putValueAtName(WebActionNames.USERNAME, userName);
	}
	
	public void putUserClass(String userClass) {
		TypedValueMap tvm = getValueMap();
		tvm.putValueAtName(WebActionNames.USERCLASS, userClass);
	}
	
}
