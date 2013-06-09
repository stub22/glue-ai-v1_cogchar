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

package org.cogchar.outer.client.web;

import org.appdapter.core.name.Ident;
import org.cogchar.impl.thing.basic.BasicTypedValueMap;
import org.cogchar.name.web.WebUserActionNames;
import org.cogchar.outer.client.ActionParamWriter;

/**
 * This would be in o.c.lib.outer.client, except that's outer and we're not!
 * This probably ultimately belongs in a general location for Cogchar repo update bits instead of here.
 * 
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class WebUserActionParamWriter extends ActionParamWriter {
	public WebUserActionParamWriter(BasicTypedValueMap btvMap) {
		super(btvMap);
	}
	
	public void putSender(Ident senderId) {
		putIfNonNull(senderId, WebUserActionNames.SENDER);
	}
	
	public void putUserID(Ident userIdent) {
		putIfNonNull(userIdent, WebUserActionNames.USER);
	}
	
	public void putSessionID(String sessionId) {
		myBTVMap.putValueAtName(WebUserActionNames.SESSION, sessionId);
	}
	
	public void putUserClass(Ident userClass) {
		putIfNonNull(userClass, WebUserActionNames.USER_CLASS);
	}

	public void putOutputText(String controlText) {
		myBTVMap.putValueAtName(WebUserActionNames.USER_TEXT, controlText);
	}
	
	public void putActionUri(Ident actionUri) {
		putIfNonNull(actionUri, WebUserActionNames.ACTION);
	}
	
	
	private void putIfNonNull(Ident identToPut, Ident propIdent) {
		if (identToPut != null) {
			myBTVMap.putValueAtName(propIdent, identToPut.getAbsUriString());
		}
	}
}
