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
package org.cogchar.name.lifter;

import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;

/**
 * @author Ryan Biggs
 */
public class ChatCN {
	public static final String gri = "http://www.cogchar.org/general/config/instance#";
	public static final String CATEGORY = "chatconfig";
	
	public static final Ident CATEGORY_URI = new FreeIdent(gri + CATEGORY, CATEGORY);
	
	public static final String GENRAL_CONFIG_TEMPLATE_URI = "ccrt:template_general_items_99";
	
	public static final String VARIABLE_VAR_NAME = "ident";
	public static final String VALUE_VAR_NAME = "url";
	
	public static final String CATEGORY_QUERY_VAR_NAME = "category";
}
