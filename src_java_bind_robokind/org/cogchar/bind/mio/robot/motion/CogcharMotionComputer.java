/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.bind.mio.robot.motion;

/**
 * @author Stu B. <www.texpedient.com>
 */

public abstract class CogcharMotionComputer {
	
	/**
	 * As currently formulated, during this computing cycle this CMC (computer) may call move() on the CMS (source),
	 * supplying a partial or complete joint-position-map.  However, at present this computer has no way to know
	 * any goal positions supplied by other computers.   In principle, it has the right to override the joint goal 
	 * positions set by all "previous" computers in the source's list, and may in turn be overridden by any "later"
	 * computers.  However, the source's present implementation of move() does not preserve joint-positions of
	 * previous computers, so at the moment, only the last computer's positions are heeded - a bug!   Fixing that bug
	 * will make poly-computer sources at least somewhat useful.  Question now is
	 * whether to just fix/augment the move() method of the source, or to establish a more cooperative protocol where
	 * the computers can see each other's requested joint positions or otherwise negotiate with/thru the source.
	
	*  The most salient thing we wish to accomplish is tying the general math capabilities of Symja into the motion
	 * computers.
	* 
	 * The ordered list of computers attached to a source defines a phased pipeline of stages carried out
	 * across the entire body (on each RK motion cycle).  State other than the RK current (last-sent, measured, or unspec?)
	 * goal position maps  may be maintained in this pipeline, which is ointment containing its own flies.
	 * State may easily reside in Symja math-spaces, with whatever lifecycle we specify (across cycles and phases).
	 * 
	 * One (source) cycle = one execution of an ordered list of (computer) phases
	 * Math Spaces are provided by the source (and shared by all computers) at the following time-grains.
	 *		persistent for N cycles  (for "semi-permanent", make N large, but first verify its clean refresh with N small!)
	 *			may optionally receive a "predecessor" space to use as data source, or may start "from scratch"
	 *		single-cycle
	 *			may receive predecessor, and whether it does is fundamental to its design.
	 * 
	 * Fixing the move() method in the obvious joint-factored way is the minimal extension beyond what Robokind does,
	 * and allows us to complete the above design to show off fun Symja EQs.
	 * 
	 * 
	 * @param source
	 * @param currentJavaTimeMsecUTC
	 * @param moveLengthMilliSec 
	 */
	public abstract void notifySourceComputingCycle(CogcharMotionSource source, long currentJavaTimeMsecUTC, 
			long moveLengthMilliSec);
}
