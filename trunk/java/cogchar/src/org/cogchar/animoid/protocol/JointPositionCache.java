/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.animoid.protocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Stu Baurmann
 */
public class JointPositionCache<JP extends JointPosition> {
	public	int					myRegisteredCounter;
	public  int					myCacheHitCounter;
	private	Map<Joint, Line>	myLines = new HashMap<Joint, Line>();
	private class Line {
		Joint			myJoint;
		List<JP>		myPositions = new ArrayList<JP>();
		// LinkedList<JP>	mySortedPositions = new LinkedList<JP>();
		public JP findOrRegisterJP(JP candidate) {
			/*
			for (int i=0; i < mySortedPositions.size(); i++) {
				JP jp = mySortedPositions.get(i);
			 */
			for (JP jp : myPositions) {
				if (jp.equals(candidate)) {
					myCacheHitCounter++;
					return jp;
				}
			}
			myPositions.add(candidate);
			myRegisteredCounter++;
			return candidate;
		}
	}
	private Line findOrMakeLine(Joint j) {
		Line l = myLines.get(j);
		if (l == null) {
			l = new Line();
			l.myJoint = j;
			myLines.put(j, l);
		}
		return l;
	}
	public JP findOrRegisterJointPos(JP candidate) {
		Joint j = candidate.getJoint();
		Line l = findOrMakeLine(j);
		return l.findOrRegisterJP(candidate);
	}

}
