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

package org.cogchar.api.animoid.config.bonus;

import org.cogchar.platform.util.BoundsAssertions;
import java.io.Serializable;
import org.cogchar.animoid.oldconfig.IntMatrixFuncs;
import org.cogchar.api.animoid.protocol.Joint;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class ServoChannelConfig implements Serializable {
	// These are the fields of the ServoConfig file, in order.
	public		int		physicalChannel;
    public      int     roboardChannel;
	public		int		logicalChannel;
	public		boolean	inverted;
	public		int		defaultPos;
	public		int		minPos;
	public		int		maxPos;

	public MuscleJoint getMuscleJoint() {
		return MuscleJoint.findJointForID(logicalChannel);
	}
	
	public static ServoChannelConfig[] readServoConfigFile(String filename, int maxChannels)
				throws Throwable {
		int [][] servoConfigRows = IntMatrixFuncs.readAndVerifyMatrixFile(filename, 7);
		ServoChannelConfig[] servoConfArray = 
					buildValidServoConfigArray(servoConfigRows, maxChannels);
		return servoConfArray;
	}
	
	public static ServoChannelConfig[]	buildValidServoConfigArray(int [][] rawServoConf, int numChannels) 
			throws Throwable {
		// Returned array is indexed by physical channel number (normally 0-31).
		// Conf will be null for all channels not explicitly configured.
		ServoChannelConfig[]	confArray = new ServoChannelConfig[numChannels];
		for (int i=0; i < rawServoConf.length; i++) {
			int[]	rawServoRow = rawServoConf[i];
            for(int j=0; j<rawServoRow.length; j++){
                System.out.print("" + rawServoRow[j] + ", ");
            }
            System.out.println();
			int physChan = rawServoRow[0];
			BoundsAssertions.checkInclusiveBounds(physChan, 0, numChannels-1, "Physical channel number");
			if (confArray[physChan] != null)  {
				throw new Exception("Got duplicate configuration entries for physical servo channel " + physChan);
			}
			ServoChannelConfig scc = new ServoChannelConfig();
			scc.physicalChannel = physChan;
            int rbChan = rawServoRow[6];
            //BoundsAssertions.checkInclusiveBounds(rbChan, -1, 23, "Physical channel number");
            scc.roboardChannel = rbChan;
            System.out.println(scc.roboardChannel);
			// No checks on logicalChannel #
			scc.logicalChannel = rawServoRow[1];
			int inversionValue = rawServoRow[2];
			BoundsAssertions.checkInclusiveBounds(inversionValue, 0, 1, "InversionFlag on channel " + physChan);
			scc.inverted = (inversionValue == 1);
			scc.minPos = rawServoRow[4];
			BoundsAssertions.checkInclusiveBounds(scc.minPos, 0, 250, "Servo minimum position on channel " + physChan);
			scc.maxPos = rawServoRow[5];
			BoundsAssertions.checkInclusiveBounds(scc.maxPos, scc.minPos, 250, "Servo maximum position on channel " + physChan);
			scc.defaultPos = rawServoRow[3];
			BoundsAssertions.checkInclusiveBounds(scc.defaultPos, scc.minPos, scc.maxPos, "Servo default position on channel " + physChan);
			confArray[physChan] = scc;
		}
		return confArray;
	}	
	public static ServoChannelConfig findConfigForLogicalChannel(ServoChannelConfig[] configArray, int logicalChannel) {
		ServoChannelConfig result = null;
		for (int i=0; i < configArray.length; i++) {
			ServoChannelConfig scc = configArray[i];
			// Empty channels are OK - ignore them.
			if (scc != null) {
				if (configArray[i].logicalChannel == logicalChannel) {
					result = configArray[i];
					break;
				}
			}
		}
		return result;
	}
	public double convertAbsServoIntToLopsidedFloat(int servoPos) {
		double	mjValue;
		int	 defP = defaultPos;
		int	 minP = minPos;
		int  maxP = maxPos;
		double lowSideRange = (double) defP - minP;
		double highSideRange = (double) maxP - defP;
		if (servoPos == defP) {
			mjValue = 0.0;
		} else if (servoPos < defP) {
			// animval - defP is negative in this case
			mjValue = ((double)(servoPos - defP))/lowSideRange;
		} else {
			// animval - defP is positive in this case
			mjValue = ((double)(servoPos - defP))/highSideRange;
		}
		if (inverted) {
			mjValue = mjValue * -1.0;
		}
		return mjValue;
	}
	public double convertLopsidedFloatToROM(double lopsidedPos) throws Throwable {
		return Joint.convertLopsidedFloatToROM(lopsidedPos, minPos, maxPos, defaultPos, inverted);
	}
}	
