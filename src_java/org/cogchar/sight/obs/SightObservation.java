/*
 *  Copyright 2008 Hanson Robotics Inc.
 *  All Rights Reserved.
 */

package org.cogchar.sight.obs;

import java.awt.Rectangle;
import java.io.Serializable;
import org.cogchar.animoid.calc.estimate.TargetObjectStateEstimate;
import org.cogchar.animoid.config.ViewPort;
import org.cogchar.animoid.protocol.EgocentricDirection;
import org.cogchar.platform.util.TimeUtils;

/**
 * @author Stu Baurmann
 */
public class SightObservation implements Serializable {
	private	long						myTimeStampMsec;
	private	EgocentricDirection			myCenterDirection;   // computed from boundRect + servoSnapshot + kinematics
	public	TargetObjectStateEstimate	myTOSE;
	private	Rectangle					myBoundRect;
	
	public EgocentricDirection getCenterDirection() {
		return myCenterDirection;
	}

	public long getTimeStampMsec() {
		return myTimeStampMsec;
	}

	public void setCenterDirection(EgocentricDirection centerDirection) {
		myCenterDirection = centerDirection;
	}

	public void setTimeStampMsec(long timeStampMsec) {
		myTimeStampMsec = timeStampMsec;
	}
	
	public Rectangle getBoundRect() {
		return myBoundRect;
	}

	public void setBoundRect(Rectangle boundRect) {
		myBoundRect = boundRect;
	}
	public Double getPixelArea() {
		return myBoundRect.getWidth() * myBoundRect.getHeight();
	}

	public Double getDiameterPixels() {
		// Pretend the enclosed area is a circle.  Yeah, that's the ticket!
		double area = getPixelArea();
		double radius = Math.sqrt(area/Math.PI);
		return 2.0 * radius;
	}
	public double getDiameterDeg(ViewPort vp) {
		return getDiameterPixels() * vp.getGeometricMeanDegPerPixel();
	}
	public double getAgeSec() {
		return TimeUtils.getStampAgeSec(myTimeStampMsec);
	}

}
