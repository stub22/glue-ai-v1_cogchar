/*
 *  Copyright 2008 Hanson Robotics Inc.
 *  All Rights Reserved.
 */

package org.cogchar.platform.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Stu Baurmann
 */
public class TimeUtils {
    private static DateFormat theTimestampFormat=new SimpleDateFormat("HH:mm:ss");
    private static DateFormat theDatestampFormat=new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");

	/**
	 * 
	 * @return
	 */
	public static long		currentTimeMillis() {
		return System.currentTimeMillis();
	}
	public static double msecToSec(long msec) {
		return ((double) msec) / 1000.0;
	}
	public static long getStampAgeMillisec(long stampMsec) {
		return currentTimeMillis() - stampMsec;
	}
	public static double getStampAgeSec(long stampMsec) {
		return msecToSec(getStampAgeMillisec(stampMsec));
	}
	public static Double msecObjToSecObj(Long msec) {
		if (msec != null) {
			return msecToSec(msec);
		} else {
			return null;
		}
	}
	public static Long msecStampObjToMsecAgeObj(Long stampMsec) {
		if (stampMsec != null) {
			return getStampAgeMillisec(stampMsec);
		} else {
			return null;
		}
	}
	public static Double msecStampObjToSecAgeObj(Long stampMsec) {
		if (stampMsec != null) {
			return getStampAgeSec(stampMsec);
		} else {
			return null;
		}
	}

    public static String getTimestamp(){
        return theTimestampFormat.format(new Date());
    }
    public static String getDatestamp(){
        return theDatestampFormat.format(new Date());
    }
}
