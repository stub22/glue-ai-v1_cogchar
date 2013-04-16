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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.appdapter.core.log.BasicDebugger;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BasicNotifier<E extends Event<?, ?>> extends BasicDebugger implements Notifier<E> {
	 // BasicNotifier<Source, Time, E extends Event<Source, Time>> extends BasicDebugger implements Notifier<Source, Time, E> {

	private Map<Class<E>, // List<Listener<Source, Time, E>>> myListenerListsByFilterClaz;
			List<Listener<E>>> myListenerListsByFilterClaz;

	public BasicNotifier() {
		myListenerListsByFilterClaz = new HashMap<Class<E>, List<Listener<E>>>(); // Source, Time, E>>>();
	}

	protected List<Listener<E>> findOrMakeListenerList(Class<E> eventClass) {
	// protected List<Listener<Source, Time, E>> findOrMakeListenerList(Class<E> eventClass) {
		// List<Listener<Source, Time, E>> lstnrLst = myListenerListsByFilterClaz.get(eventClass);
		List<Listener<E>> lstnrLst = myListenerListsByFilterClaz.get(eventClass);
		if (lstnrLst == null) {
			lstnrLst = new ArrayList<Listener<E>>();  //Source, Time, E>>();
			myListenerListsByFilterClaz.put(eventClass, lstnrLst);
		}
		return lstnrLst;

	}

	public void addListener(Class<E> eventClassFilter, Listener<E> lstnr) {
		List<Listener<E>> lstnrLst = findOrMakeListenerList(eventClassFilter);
		lstnrLst.add(lstnr);
	}

	public void removeListener(Class<E> eventClassFilter, Listener<E> lstnr) {
		List<Listener<E>> lstnrLst = findOrMakeListenerList(eventClassFilter);
		lstnrLst.remove(lstnr);
	}

	protected void notifyListeners(E event) {
		Class eventClaz = event.getClass();
		for (Class<E> candFilterClaz : myListenerListsByFilterClaz.keySet()) {
			if (candFilterClaz.isAssignableFrom(eventClaz)) {
				List<Listener<E>> lstnrLst = myListenerListsByFilterClaz.get(candFilterClaz);
				for (Listener<E> lstnr : lstnrLst) {
					try {
						lstnr.notify(event);
					} catch (Throwable t) {
						getLogger().error("Notifier caught exception: ", t);
					}
				}
			}
		}
	}
	/*
	protected void notifyStateChange() {
		
	}
	* 
	*/
}
