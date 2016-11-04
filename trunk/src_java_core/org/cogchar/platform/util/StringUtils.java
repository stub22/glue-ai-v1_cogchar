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

import java.text.DecimalFormat;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class StringUtils {
	private static DecimalFormat theFormatter = new DecimalFormat("0.0#");
	public static String joinArray(String[] arr, String separator) {
		StringBuffer result = new StringBuffer();
		if (arr.length > 0) {
			result.append(arr[0]);
			for (int i=1; i<arr.length; i++) {
				result.append(separator);
				result.append(arr[i]);
			}
		}
		return result.toString();
	}

	public static String formatDecimal(double d){
		return theFormatter.format(d);
	}
}
