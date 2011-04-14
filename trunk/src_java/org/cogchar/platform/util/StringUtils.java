/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.platform.util;

import java.text.DecimalFormat;

/**
 *
 * @author Stu Baurmann
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
