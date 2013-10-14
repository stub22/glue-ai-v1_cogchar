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
import org.cogchar.impl.thing.basic.BasicTypedValueMap;
import org.cogchar.name.web.WebUserActionNames;
import org.cogchar.api.thing.ActionParamWriter;

/**
 * This has something to do with session-level state management.
 * But it facilitates sending a message from what sources to what destinations?
 * 
 */


public class WebSessionActionParamWriter extends ActionParamWriter {
	public WebSessionActionParamWriter(BasicTypedValueMap btvMap) {
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
