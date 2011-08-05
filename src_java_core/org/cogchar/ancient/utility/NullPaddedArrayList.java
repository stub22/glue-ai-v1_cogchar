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

package org.cogchar.ancient.utility;

import java.util.ArrayList;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class NullPaddedArrayList<E> extends ArrayList<E> {
	@Override 
	public E get(int index) {
		int sz = size();
		// System.out.println("NullPaddedArrayList.get(" + index + ") called on list of size " + sz);
		if (index >= sz) {
			return null;
		} else {
			return super.get(index);
		}
	}

}
