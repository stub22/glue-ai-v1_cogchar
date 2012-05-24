/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.api.integroid.cue;

import org.cogchar.sight.motion.PeakTracker;
import org.cogchar.api.sight.SightCue;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class MotionCue extends SightCue {
	transient	PeakTracker		myTracker;
	public MotionCue(PeakTracker pt) {
		myTracker = pt;
		pt.setCue(this);
	}
	public PeakTracker fetchPeakTracker() {
		return myTracker;
	}
}
