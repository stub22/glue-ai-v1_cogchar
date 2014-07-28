/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.integroid.broker;

/**
 * @author Stu B.
 * 
 * This interface exposes the "processWhenSafe" trigger method, which is used to
 * thread-safely and non-blockingly request that the
 * Integroid  process all its pending thoughts, i.e. run the rules engine.
 */
public interface ReactionProcessor {
	public void processWhenSafe();
}
