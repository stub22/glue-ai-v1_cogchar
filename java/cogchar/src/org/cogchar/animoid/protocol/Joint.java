/*
 *  Copyright 2008 Hanson Robotics Inc.
 *  All Rights Reserved.
 */

package org.cogchar.animoid.protocol;

import java.io.Serializable;

/**
 * @author Stu Baurmann
 */
public class Joint implements Serializable {
	private Device		myDevice;
	private	Robot		myRobot;
	private String		myDeviceChannelID;
	private String		myJointName;
	public  Integer		oldLogicalJointNumber;
	public	Integer		oldMinServoPos, oldMaxServoPos, oldDefServoPos;
	public  Boolean		oldInvertedFlag;
	
	private JointPosition	myCenterPosition;
	
	public Joint(Device d, Robot r, String deviceChannelID, String jointName) {
		myDevice = d;
		myRobot = r;
		myDeviceChannelID = deviceChannelID;
		myJointName = jointName;
	}
	public Device getDevice() {
		return myDevice;
	}
	public Robot getRobot() {
		return myRobot;
	}
	public String getDeviceChannelID() {
		return myDeviceChannelID;
	}
	public String getJointName() {
		return myJointName;
	}
	public String getDeviceAndRobotDesc() {
		Device dev = getDevice();
		Robot rob = getRobot();
		String devDesc = "device=" + ((dev != null) ? dev.getName() : "NULL");
		String robDesc = "robot=" + ((rob != null) ? rob.getName() : "NULL");
		return devDesc + ", " + robDesc;
	}

	public boolean equals(Object other) {
		// Strict object equality, for now.
		boolean result = false;
		if (other == this) {
			result = true;
		}
		return result;
	}
	public JointPosition getCenterPosition() {
		if (myCenterPosition == null) {
			
			JointPosition jpLopsided = new JointPosition(this);
			jpLopsided.setCoordinateFloat(
					JointStateCoordinateType.FLOAT_ABS_LOPSIDED_PIECEWISE_LINEAR, 0.0);
			// TODO: Should not need to do this explicit conversion
			JointPosition jpAbsROM = jpLopsided.convertToCooordinateType(JointStateCoordinateType.FLOAT_ABS_RANGE_OF_MOTION);
			myCenterPosition = jpAbsROM;
		}
		return myCenterPosition;
	}

	public double convertLopsidedFloatToROM(double lopsidedPos) throws Throwable {
		return convertLopsidedFloatToROM(lopsidedPos, oldMinServoPos, oldMaxServoPos, 
					oldDefServoPos, oldInvertedFlag);
	}
	public double convertROMtoLopsidedFloat(double romPos) throws Throwable {
		return convertROMtoLopsidedFloat(romPos, oldMinServoPos, oldMaxServoPos, 
					oldDefServoPos, oldInvertedFlag);
	}
	// -1.0 in lopsided   <->   0.0 in ROM (always, regardless of "inverted")
	// +1.0 in lopsided   <->   1.0 in ROM (always, regardless of "inverted")
	public static double convertLopsidedFloatToROM(double lopsidedPos, 
			int minP, int maxP, int defP, boolean inverted) throws Throwable {
				
		if ((lopsidedPos < -1.0) || (lopsidedPos > 1.0)) {
			throw new Exception("Cannot convert invalid lopsidedPos: " + lopsidedPos);
		}
		double lowSideRange = (double) defP - minP;  //  >= 0.0
		double highSideRange = (double) maxP - defP;  // >= 0.0
		
		double totalRange = (double) maxP - minP;    //  >= 0.0
		
		// lowRate (after possible inversion-correction) is also the default-pos in AbsROM.
		// lowRate + highRate = 1.0

		double lowRate = lowSideRange / totalRange;    // 0.0 < lowRate  < 1.0
		double highRate = highSideRange / totalRange;  // 0.0 < highRate < 1.0
		
		
		double nominalLP = lopsidedPos;
		if (inverted) {
			// Note the re-inversion below.
			nominalLP = -1.0 * lopsidedPos;
		}
		double nominalRP;
		if (nominalLP < 0.0) {
			// We are on the "low side" of the range, between minP and defP.
			// Multiply our distance from the low boundary (which is nominalROM 0.0) by the lowRate.
			nominalRP  = lowRate * (nominalLP + 1.0);  // == lp - (-1.0)
		} else {
			// We are on the high side.  
			nominalRP = lowRate + highRate * nominalLP;
		}
		double romPos = nominalRP;
		if (inverted) {
			romPos = 1.0 - nominalRP;
		}
		if ((romPos < -0.00001) || (romPos > 1.00001)) {
			throw new Exception("Calculation went awry, romPos computed to be: " + romPos);
		}
		if (romPos < 0.0) {
			romPos = 0.0;
		}
		if (romPos > 1.0) {
			romPos = 1.0;
		}
		return romPos;
	}
	public double convertROMtoLopsidedFloat(double romPos, 
				int minP, int maxP, int defP, boolean inverted) throws Throwable {
		if ((romPos < 0.0) || (romPos > 1.0)) {
			throw new Exception("Cannot convert invalid romPos: " + romPos);
		}
		double lowSideRange = (double) defP - minP;  // always a positive int
		double highSideRange = (double) maxP - defP;  // always a positive int
		
		double totalRange = (double) maxP - minP;    // always a positive int
		
		// lowRate + highRate = 1.0
		// lowRate (after possible inversion-correction) is also the default-pos in AbsROM.
		double lowRate = lowSideRange / totalRange;    // 0.0 < lowRate  < 1.0
		double highRate = highSideRange / totalRange;  // 0.0 < highRate < 1.0

		double nominalRP = romPos;
		if (inverted) {
			nominalRP = 1.0 - romPos;
		}
		double nominalLP;
		if (nominalRP < lowRate) {
			nominalLP = -1.0 + nominalRP / lowRate;
		} else {
			nominalLP = (nominalRP - lowRate) / highRate;
		}
		double lopsidedPos = (inverted) ? (-1.0 * nominalLP) : nominalLP;

		if ((lopsidedPos < -1.00001) || (lopsidedPos > 1.00001)) {
			throw new Exception("Calculation went awry, lopsidedPos computed to be illegal value: " + lopsidedPos);
		}
		if (lopsidedPos < -1.0) {
			lopsidedPos = -1.0;
		}
		if (lopsidedPos > 1.0) {
			lopsidedPos = 1.0;
		}
		return lopsidedPos;
	}
	public JointPosition makeJointPosForROM_value(double romValue) {
		JointPosition posJP = new JointPosition(this);
		posJP.setCoordinateFloat(JointStateCoordinateType.FLOAT_ABS_RANGE_OF_MOTION, romValue);
		return posJP;
	}
	public JointVelocityAROMPS makeJointVelForROM_speedValue(double velRomPS) {
		return new JointVelocityAROMPS(this, velRomPS);
	}
	public String toString() {
		return "Joint[name=" + getJointName()
				+ ", channelID=" + getDeviceChannelID()
				+ ", centerPos=" + getCenterPosition().getCoordinateFloat(JointStateCoordinateType.FLOAT_ABS_RANGE_OF_MOTION)
				+ ", " + getDeviceAndRobotDesc() + "]";
	}
}
