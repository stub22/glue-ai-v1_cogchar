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

import org.appdapter.api.trigger.BoxAssemblyNames;

/**
 * @author Ryan Biggs
 */
public class ChatAN extends BoxAssemblyNames {

	public static final String NS_CgcChC = "http://www.cogchar.org/chat/config#";
	public static final String partial_P_config = "config";
	public static final String P_config = NS_CgcChC + partial_P_config;
	public static final String P_entry = NS_CgcChC + "hasEntry";
	public static final String P_name = NS_CgcChC + "hasName";
	public static final String P_url = NS_CgcChC + "hasURL";
	public static final String N_cogbotConvoUrl = "sendHeardAndGetSaid";
}
