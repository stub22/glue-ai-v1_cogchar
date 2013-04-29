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

import org.appdapter.core.name.Ident;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class NameAndAction implements Comparable {
		protected String name;
		protected Ident action;
		NameAndAction(String newName, Ident newAction) {
			name = newName;
			action = newAction;
		}
		public String getName() {
			return name;
		}
		public Ident getAction() {
			return action;
		}
		@Override
		public int compareTo(Object otherObject) {
			// Would be good to add type checking
			return getName().compareTo(((NameAndAction)otherObject).getName());
		}
	}
