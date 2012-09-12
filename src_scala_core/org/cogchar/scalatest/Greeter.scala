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

package org.cogchar.scalatest

import org.cogchar.blob.emit.RenderConfigEmitter;

/**
 * @author Stu B. <www.texpedient.com>
 */

object Greeter {
	// Used by org.cogchar.scalatest.Greeter
	def makeParamString(bindingList : List[NVParam]) : String = {
		val len = bindingList.length;
		if (len == 0) {
			return "";
		} else { 
			val firstPairString = bindingList.head.urlEncoding;
			if (len == 1) {
				return firstPairString;
			} else {
				return firstPairString + "&" + makeParamString(bindingList.tail);
			}
		}
	}
	
	def makeCogcharURN(urnPrefix : String, item : String, bindingList : List[NVParam]) : String = {
		val paramsEncoded = makeParamString(bindingList);
		val marker = if (paramsEncoded.length() > 0) "?" else "";
		urnPrefix + item + marker + paramsEncoded;
	}	
	def main(args: Array[String]): Unit = {
		println(this.toString() + " says 'Hello!'");
		
		val rce = new RenderConfigEmitter(None);
		val urnPrefix = rce.COGCHAR_URN_PREFIX;
		val nvp1 = new NVParam("color", "sienna");
		val nvp2 = new NVParam("shape", "moon");
		val ps1 = makeParamString(List(nvp1, nvp2));
		println("Encoded paramString:  " + ps1);
		val urn1 = makeCogcharURN(urnPrefix, "grocery", List(nvp1, nvp2));
		println("Made:   " + urn1);
	}
}
// Used by org.cogchar.scalatest.Greeter
case class NVParam(val name: String, val value: String) {
	def urlEncoding : String = {
		name + "=" + value;
	}
}