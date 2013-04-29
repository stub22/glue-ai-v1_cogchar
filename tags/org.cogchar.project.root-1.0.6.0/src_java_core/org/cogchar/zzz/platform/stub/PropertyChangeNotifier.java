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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.SwingUtilities;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public abstract class PropertyChangeNotifier {
	private	 transient PropertyChangeSupport		myPropertyChangeSupport;
	
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
			myPropertyChangeSupport = new PropertyChangeSupport(this);
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
	public void safelyFirePropertyChange(final String propertyName, final Object previous,
				final Object current) {
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
