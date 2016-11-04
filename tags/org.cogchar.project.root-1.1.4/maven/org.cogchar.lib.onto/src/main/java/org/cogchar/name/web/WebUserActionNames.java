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
import org.cogchar.gen.oname.WebTier_owl2;

/**
 *
 * Names for actions invoked within the web UI by users
 * 
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class WebUserActionNames {
	
	public  static String	USER_ACTION_NS = NamespaceDir.NS_LifterUserAction;
	
	// A common method which should be factored out to a superclass
	public static Ident makeID (String nameTail) {
		return new FreeIdent(USER_ACTION_NS + nameTail);
	}
	
	public static Ident makeID (com.hp.hpl.jena.rdf.model.Resource jenaRsrc) {
		return new org.appdapter.bind.rdf.jena.model.SerialJenaResItem(jenaRsrc);
		// return new org.appdapter.core.item.JenaResourceItem(jenaRsrc);
	}	
	
	// These are properties.  We can tell their datatypes by looking at how they are used in 
	// WebSessionActionParamWriter.
	public	static Ident	SENDER = makeID("sender");
	public	static Ident	USER = makeID("user");
	public	static Ident	SESSION = makeID("session");
	public	static Ident	USER_CLASS = makeID("userclass");
	public	static Ident	USER_TEXT = makeID("inputtext");
	public	static Ident	ACTION = makeID("action");
	
	public static String heyNow = WebTier_owl2.getURI();
	public static String anotherGuy = WebTier_owl2.SENDER.getLocalName();
	
	public	static Ident   idFromOntoName = makeID(WebTier_owl2.SENDER);
	

}
