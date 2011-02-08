/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.animoid.protocol;

import org.cogchar.animoid.protocol.IServoMonitor;
import org.cogchar.animoid.protocol.JPARFrame;
import org.cogchar.animoid.protocol.JointPositionSnapshot;

/**
 *
 * @author humankind
 */
public interface IServoPositionReporter {
	public enum Flavor {
		LOPSIDED,
		AROM
	}
	public JointPositionSnapshot getServoSnapshotLopsided();
	public JPARFrame getServoSnapshotAROM();
	// TODO:  Make this an add/remove observer pattern
	public void setMonitor(IServoMonitor monitor);
}
