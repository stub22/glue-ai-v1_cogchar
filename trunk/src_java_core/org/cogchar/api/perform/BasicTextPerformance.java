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

import org.cogchar.api.event.Event;

/**
 * @author Stu B. <www.texpedient.com>
 * We expect that the channel is usually a BasicTextChannel, 
 * and that the media is usually a Media.BasicText.
 * 
 */

public abstract class BasicTextPerformance<Cursor, M extends Media.Text<Cursor>, WorldTime>
				//	EPT extends Performance.TextPerf<Cursor, M, WorldTime>, 
				//	E extends Event<EPT, WorldTime>> 
			extends BasicPerformance<Cursor, M, WorldTime> // , EPT, E> 
			implements Performance.TextPerf<Cursor, M, WorldTime> {
	
	// public BasicTextPerformance(M media, Channel.Text<Cursor, M, WorldTime> chan, Cursor initCursor) {
	public BasicTextPerformance(M media, Channel chan, Cursor initCursor) {	
		super(media, chan, initCursor);
	}
}
