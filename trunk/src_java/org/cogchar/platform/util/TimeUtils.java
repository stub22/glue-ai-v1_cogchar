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

package org.cogchar.platform.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Stu B. <www.texpedient.com>
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
