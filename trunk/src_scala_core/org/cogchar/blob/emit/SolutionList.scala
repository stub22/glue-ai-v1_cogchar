/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.blob.emit

import com.hp.hpl.jena.query.QuerySolution
import scala.collection.JavaConversions._

/**
 * @author Ryan Biggs
 */

// A very simple class so the SolutionList can be handed to external classes without Scala-Java conversion concerns
class SolutionList {
  var list: scala.collection.mutable.Buffer[Solution] = new scala.collection.mutable.ArrayBuffer[Solution]
  lazy val javaList: java.util.List[Solution] = list
}
