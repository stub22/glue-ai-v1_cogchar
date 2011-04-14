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

package org.cogchar.animoid.config;



import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.cogchar.animoid.oldconfig.StringMatrixFuncs;
import org.cogchar.animoid.protocol.Frame;
import org.cogchar.animoid.protocol.Joint;
import org.cogchar.animoid.protocol.JointPositionAROM;
import org.cogchar.animoid.protocol.Robot;
/**
 * @author Stu Baurmann
 */
public class VisemeConfig implements Serializable {
	private static Logger	theLogger = Logger.getLogger(VisemeConfig.class.getName());
	
	public static final int							VISEME_POSITION_COUNT = 22;
	public static final int							HEADER_ROW_COUNT = 1;
	public static final int							HEADER_COL_COUNT = 2;
	// Expect VISEME_POSITION_COUNT frames
	public List<Frame<JointPositionAROM>>			myFrames;
	
	public static VisemeConfig buildVisemeConfig(String visemeConfFile, 
				ServoChannelConfig[] servoConfigSparseArray, Robot robot) throws Throwable {
		VisemeConfig	vconf = new VisemeConfig();
		vconf.myFrames = new ArrayList<Frame<JointPositionAROM>>();

		String[][] vconfRows = StringMatrixFuncs.readAndVerifyMatrixFile(visemeConfFile);
		if (vconfRows.length != VISEME_POSITION_COUNT + 1) {
			throw new Exception("VisemeConfig expected 23 rows (1 header row and 22 data rows), but found " 
					+ vconfRows.length + " rows in file: " + visemeConfFile);
		}
		String[] headerRow = vconfRows[0];
		int totalColumns = headerRow.length;
		// First two columns must be header columns
		int jointColumns = totalColumns - HEADER_COL_COUNT;
		if (jointColumns < 1) {
			throw new Exception("VisemeConfig expected at least 3 columns (visemeNumber, sound, joint-1), but " 
						+ " found " + totalColumns + " columns");		
		}
		Set<String> knownJointNames = robot.getJointNameSet();
		theLogger.fine("Robot joint names: " + knownJointNames);
		Collection<Joint> knownJoints = robot.getJoints();
		theLogger.fine("Robot joint names: " + knownJoints);

		Joint[] joints = new Joint[jointColumns];
		for (int i=0; i < jointColumns; i++) {
			String rawJointName = headerRow[HEADER_COL_COUNT + i];
			// jointName may contain yucky leading/trailing quotes (from spreadsheet export)
			String jointName = rawJointName.replace("'", "").replace("\"", "");
			Joint j = robot.getJointForName(jointName);
			if (j == null) {
				throw new Exception("Cannot find robot joint for viseme column " + jointName);
			}
			joints[i] = j;
		}
		for (int j=0; j < VISEME_POSITION_COUNT; j++) {
			String[] valueRow = vconfRows[HEADER_ROW_COUNT + j];
			Frame f = new Frame();
			for (int i=0; i < jointColumns; i++) {
				String sval = valueRow[HEADER_COL_COUNT + i];
				double lopsidedPos  = Double.parseDouble(sval);
				if ((lopsidedPos < -1.0) || (lopsidedPos > 1.0)) {
					throw new Exception("Got out of range joint position value " + lopsidedPos + " at row=" + j + ", col=" + i);
				}
				Joint joint = joints[i];
				String physChannelString = joint.getDeviceChannelID();
				int physChannel = Integer.parseInt(physChannelString);
				ServoChannelConfig scc = servoConfigSparseArray[physChannel];
				double romPos = scc.convertLopsidedFloatToROM(lopsidedPos);
				theLogger.fine("Float: " + lopsidedPos + " converted to: " + romPos);
				JointPositionAROM jp = new JointPositionAROM(joints[i], romPos);
				f.addPosition(jp);
			}
			vconf.myFrames.add(f);
		}
		theLogger.info("Built visemeFrameList: " + vconf.myFrames);

		return vconf;
	}
	public Frame<JointPositionAROM> getFrameForVisemeNumber(int visNum) {
		return myFrames.get(visNum);
	}
}
