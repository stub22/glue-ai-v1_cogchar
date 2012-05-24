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

import org.cogchar.blob.emit.{BonyConfigEmitter, NVParam};


/**
 * @author Stu B. <www.texpedient.com>
 */

object Greeter {
	def main(args: Array[String]): Unit = {
		println(this.toString() + " says 'Hello!'");
		
		val bce = new BonyConfigEmitter();
		
		val nvp1 = new NVParam("color", "sienna");
		val nvp2 = new NVParam("shape", "moon");
		val ps1 = bce.makeParamString(List(nvp1, nvp2));
		println("Encoded paramString:  " + ps1);
		val urn1 = bce.makeCogcharURN("grocery", List(nvp1, nvp2));
		println("Made:   " + urn1);
	}
}
