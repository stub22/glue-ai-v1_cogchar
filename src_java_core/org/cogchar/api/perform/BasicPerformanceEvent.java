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
package org.cogchar.api.perform;

import org.cogchar.api.event.StateChangeEvent;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 */

public class BasicPerformanceEvent<Cursor, M extends Media<Cursor>, WorldTime> 
			extends StateChangeEvent<BasicPerformance<Cursor, M, WorldTime>, WorldTime, Performance.State> {
	
	public Cursor myCursor;
	
	public BasicPerformanceEvent(BasicPerformance<Cursor, M, WorldTime> src, WorldTime worldTime,
				Performance.State prevState, Performance.State nextState, Cursor cursor) {
		super(src, worldTime, prevState, nextState);
		myCursor = cursor;
	}
}
