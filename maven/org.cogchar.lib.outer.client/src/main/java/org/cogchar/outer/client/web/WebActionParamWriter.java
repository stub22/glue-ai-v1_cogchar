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
package org.cogchar.outer.client.web;

import org.appdapter.core.name.Ident;
import org.cogchar.api.thing.BasicTypedValueMap;
import org.cogchar.api.thing.TypedValueMap;
import org.cogchar.name.web.WebActionNames;

/**
 * Typically used from a remote client to capture values for encoding in SPARQL-Update.
 * Intended for web actions instead of virtual world actions as in GoodyActionParamWriter
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */
public class WebActionParamWriter {

	private BasicTypedValueMap myBTVMap;

	public WebActionParamWriter(BasicTypedValueMap btvMap) {
		myBTVMap = btvMap;
	}
	
	public void putSlotNum(int slotNum) {
		myBTVMap.putValueAtName(WebActionNames.SLOT, slotNum);
	}

	public void putType(Ident typeIdent) {
		myBTVMap.putValueAtName(WebActionNames.TYPE, typeIdent.getAbsUriString());
	}

	public void putText(String controlText) {
		myBTVMap.putValueAtName(WebActionNames.TEXT, controlText);
	}

	public void putStyle(String styleName) {
		myBTVMap.putValueAtName(WebActionNames.STYLE, styleName);
	}
	
	public void putResource(String resourceName) {
		myBTVMap.putValueAtName(WebActionNames.RESOURCE, resourceName);
	}
	
	public void putAction(Ident actionIdent) {
		myBTVMap.putValueAtName(WebActionNames.ACTION, actionIdent.getAbsUriString());
	}
	
	public TypedValueMap	getValueMap() { 
		return myBTVMap;
	}
	/**  type is not a param.
	public void putType(Ident someTypeID) {
		myBTVMap.putNameAtName(GoodyNames.RDF_TYPE, someTypeID);
	}
	*/

}
