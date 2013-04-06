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

package org.cogchar.name.web;

import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.name.dir.NamespaceDir;

/**
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */

public class WebActionNames  {
	
	public  static String	WEB_NS = NamespaceDir.WEB_NS;
	
	public static Ident makeID (String nameTail) {
		return new FreeIdent(WEB_NS + nameTail);
	}
	
	public	static Ident	CONFIG = makeID("configIdent");
	
	public	static Ident	SLOT = makeID("slotID");
	public	static Ident	TYPE = makeID("type");
	public	static Ident	TEXT = makeID("text");
	public	static Ident	STYLE = makeID("style");
	public	static Ident	RESOURCE = makeID("resource");
	public	static Ident	ACTION = makeID("action");
	
	
	public	static Ident	WEBCONTROL = makeID("control");
	public	static Ident	WEBCONFIG = makeID("liftconfig");

}
