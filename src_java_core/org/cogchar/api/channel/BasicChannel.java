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
package org.cogchar.api.channel;

import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.Ident;
import org.appdapter.core.name.FreeIdent;
import org.cogchar.api.perform.Performance.Instruction;


/**
 * @author Stu B. <www.texpedient.com>
 */
public class BasicChannel extends BasicDebugger implements Channel {
// <Cursor, M extends Media<Cursor>, WorldTime> extends BasicDebugger implements Channel<Cursor, M, WorldTime> {

	private Ident myIdent;

	public BasicChannel(Ident ident) {
		myIdent = ident;
	}

	@Override public Ident getIdent() {
		return myIdent;
	}


	public String getName() {
		return myIdent.getLocalName();
	}
}
