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

package org.cogchar.integroid.jmxwrap;

import java.util.Map;

/**
 *
 * @author Stu Baurmann
 */
public interface IntegroidWrapperMXBean {
	public static String	INTEGROID_JMX_OBJNAME = "com.hansonrobotics:integroid=sincere";
	
	public static final String	ATTRIB_CUE_POSTED = "cuePosted";
	public static final String	ATTRIB_CUE_UPDATED = "cueUpdated";
	public static final String	ATTRIB_CUE_CLEARED = "cueCleared";
	public static final String	ATTRIB_JOB_POSTED = "jobPosted";
	public static final String	ATTRIB_JOB_UPDATED = "jobUpdated";
	public static final String	ATTRIB_JOB_CLEARED = "jobCleared";

	public void postVerbalCue(Map<String, Double> meanings, double strength);
	public void postThoughtCue(String thoughtName, double strength);
	public void postTextCue(String channel, String textData, double strength);
	public void postVariableCue(String name, String value, double strength);
	public void postHeardCue(String text);
}
