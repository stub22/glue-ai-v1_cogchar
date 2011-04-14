/*
 * BoundsAssertions.java
 *
 * Created on Aug 29, 2007, 9:40:41 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.cogchar.animoid.config;

/**
 *
 * @author humankind
 */
public class BoundsAssertions
{
	public static void checkInclusiveBounds(int value, int lower, int upper, String valueDesc) 
				throws Throwable {
		if ((value < lower) || (value > upper)) {
			String mesg = valueDesc + " is " + value + ", but was expected to be within [" + lower + "," + upper +"]";
			throw new Exception(mesg);
		}
	} 

}
