/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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
import org.cogchar.gen.oname.{BehavChanAct_owl2, HominoidBodySchema_owl2, AnimMotivMapBlend_owl2, WebTier_owl2}
/**
 * @author Stu B. <www.texpedient.com>
 */

object OntoTest {
	def main(args: Array[String]): Unit = {
		println(this.toString() + " says 'Hello!'");
		
		println("BCA-ANIM_FRAME_MEDIA=" + BehavChanAct_owl2.ANIM_FRAME_MEDIA)
		
		println ("WebTier_owl2.SENDER.localName: " + WebTier_owl2.SENDER.getLocalName())
	}
}
