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

package org.cogchar.api.event;

import org.cogchar.api.event.Listener;
import org.cogchar.api.event.Event;

/**
 * @author Stu B. <www.texpedient.com>
 */

public interface Notifier<Source, Time, E extends Event<Source, Time>> {
	public	void	addListener(Class<E> eventClassFilter, Listener<Source, Time, E> l);
	public	void	removeListener(Class<E> eventClassFilter, Listener<Source, Time, E> l);
}
