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
