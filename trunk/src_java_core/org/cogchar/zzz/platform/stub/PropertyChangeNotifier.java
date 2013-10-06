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

package org.cogchar.zzz.platform.stub;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.beans.PropertyChangeSupport;
import java.util.*;

import javax.swing.SwingUtilities;

import java.util.HashSet;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public abstract class PropertyChangeNotifier {
	private transient PropertyChangeSupport myPropertyChangeSupport;

	/**
	 * 
	 */
	public PropertyChangeNotifier() {
		completeInit();
	}

	/**
	 * 
	 */
	public void completeInit() {
		if (myPropertyChangeSupport == null) {
			myPropertyChangeSupport = new PropertyChangeSupport(this) {
				private java.util.Hashtable<PropertyChangeListener, Map<String, Integer>> subListeners;

				/**
				 * Add a PropertyChangeListener for a specific property.  The listener
				 * will be invoked only when a call on firePropertyChange names that
				 * specific property.
				 * The same listener object may be added more than once.  For each
				 * property,  the listener will be invoked the number of times it was added
				 * for that property.
				 * If <code>propertyName</code> or <code>listener</code> is null, no
				 * exception is thrown and no action is taken.
				 *
				 * @param propertyName  The name of the property to listen on.
				 * @param listener  The PropertyChangeListener to be added
				 */

				public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
					if (listener == null || propertyName == null) {
						return;
					}
					if (subListeners == null) {
						subListeners = new java.util.Hashtable();
					}
					Map<String, Integer> child = (Map<String, Integer>) subListeners.get(listener);
					if (child == null) {
						child = new HashMap<String, Integer>();
						child.put(propertyName, 1);
						subListeners.put(listener, child);
					} else {
						Integer was = child.get(propertyName);
						if (was == null) {
							child.put(propertyName, 1);
						} else {
							child.put(propertyName, was + 1);
						}
					}
					super.addPropertyChangeListener(propertyName, listener);
				}

				public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
					if (listener == null || propertyName == null) {
						return;
					}
					if (subListeners == null) {
						subListeners = new java.util.Hashtable();
					}
					Map<String, Integer> child = (Map<String, Integer>) subListeners.get(listener);
					if (child == null) {

					} else {
						Integer was = child.get(propertyName);
						if (was == null) {
							//child.put(propertyName, 1);
						} else if (was == 1) {
							child.remove(propertyName);
							if (child.isEmpty()) {
								subListeners.remove(listener);
							}
						} else {
							child.put(propertyName, was - 1);
						}
					}
					super.removePropertyChangeListener(propertyName, listener);
				}

				/**
				 * Fire an existing PropertyChangeEvent to any registered listeners.
				 * No event is fired if the given event's old and new values are
				 * equal and non-null.
				 * @param evt  The PropertyChangeEvent object.
				 */
				public void firePropertyChange(PropertyChangeEvent evt) {
					PropertyChangeListener[] listeners = null;
					PropertyChangeListener[] children = null;
					Object oldValue = evt.getOldValue();
					Object newValue = evt.getNewValue();

					if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
						return;
					}

					String propertyName = evt.getPropertyName();
					if (propertyName != null) {
						PropertyChangeListener[] pcl = getPropertyChangeListeners(propertyName);
						PropertyChangeListener[] pclWithChildren = getPropertyChangeListeners();
						HashSet nonChilredListeners = new HashSet(Arrays.asList(pclWithChildren));

						nonChilredListeners.removeAll(Arrays.asList(pcl));
						listeners = (PropertyChangeListener[]) nonChilredListeners.toArray(new PropertyChangeListener[0]);
					} else {
						listeners = getPropertyChangeListeners();
					}
				}
			};
		}
	}

	/**
	 * Add PropertyChangeListener.
	 *
	 * @param listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		myPropertyChangeSupport.addPropertyChangeListener(listener);
	}

	/**
	 * Remove PropertyChangeListener.
	 *
	 * @param listener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		myPropertyChangeSupport.removePropertyChangeListener(listener);
	}

	/**
	 * 
	 * @param propertyName
	 * @param previous
	 * @param current
	 */
	public void safelyFirePropertyChange(final String propertyName, final Object previous, final Object current) {
		if (SwingUtilities.isEventDispatchThread()) {
			myPropertyChangeSupport.firePropertyChange(propertyName, previous, current);
		} else {
			Runnable doFirePropertyChange = new Runnable() {
				public void run() {
					myPropertyChangeSupport.firePropertyChange(propertyName, previous, current);
				}
			};
			SwingUtilities.invokeLater(doFirePropertyChange);
		}
	}
}
