/*
 *  Copyright 2015 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.blob.entry

/**

 */

// These can be provided from main(), or an injected service (OSGi/JFlux).
// Knows about some ordered sequence of hosts to query.
// 
// This finder may have several impls, but all of them should be independent of the sub-types of EntryHost.
// (So we could access any kind of EntryHost from any kind of EntryHostFinder).

trait EntryHostFinder {
	// Generally these were registered by some impl-specific means.
	def findEntryHostsInOrder : Seq[EntryHost]
	
	def findFirstMatchingPlainEntry(path : String) : Option[PlainEntry]
	def findFirstMatchingFolderEntry(path : String) : Option[FolderEntry]
	
	def findAllMatchingPlainEntries(path : String) : Set[FolderEntry]
	def findAllMatchingFolderEntries(path : String) : Set[FolderEntry]
	
	def searchAllPlainEntriesBySuffix(path : String, suffixes : Set[String], deepSearch : Boolean, maxResultCount : Int):  Set[PlainEntry]
}

