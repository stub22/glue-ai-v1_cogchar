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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReaderImpl;
import org.appdapter.core.item.Item;
import org.appdapter.core.item.ItemFuncs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ryan Biggs
 */
public class ChatConfigResource {

	static Logger theLogger = LoggerFactory.getLogger(ChatConfigResource.class);
	public String myURI_Fragment;
	public Map<String, String> entries = new HashMap<String, String>();
	private static final ItemAssemblyReader reader = new ItemAssemblyReaderImpl();

	@Override
	public String toString() {
		return "ChatConfigResource[uriFrag=" + myURI_Fragment + ", Number of Entries: " + entries.size() + "]";
	}

	public ChatConfigResource(Item configItem) {
		myURI_Fragment = configItem.getIdent().getLocalName();
		List<Item> entryItems = reader.readLinkedItemSeq(configItem, ChatConfigNames.P_entry);
		theLogger.info("Number of entries found: " + entryItems.size());
		for (Item ei : entryItems) {
			String name = ItemFuncs.getString(ei, ChatConfigNames.P_name, null);
			String url = ItemFuncs.getString(ei, ChatConfigNames.P_url, "");
			if (name != null) {
				entries.put(name, url);
			} else {
				theLogger.warn("Found an entry with no listed name key in config resource " + myURI_Fragment);
			}
		}
	}
}
