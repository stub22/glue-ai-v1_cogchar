/*
 *  Copyright 2008 Hanson Robotics Inc.
 *  All Rights Reserved.
 */

package org.cogchar.platform.stub;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.SwingUtilities;

/**
 *
 * @author Stu Baurmann
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
