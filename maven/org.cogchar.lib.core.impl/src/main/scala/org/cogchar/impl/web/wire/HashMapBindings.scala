/*
 *  Copyright 2014 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.impl.web.wire
import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable.ConcurrentMap

/**

 */

object HashMapBindings {
  // Determines how many controls are "cleared out" from state upon LiftConfig change and initial capacity of ConcurrentHashMaps
  // May be increased as necessary
  final val MAX_CONTROL_QUANTITY = 20;
  
  // Default parameters for ConcurrentHashMap configuration
  // For more info on these parameters, see http://ria101.wordpress.com/2011/12/12/concurrenthashmap-avoid-a-common-misuse/
  final val DEFAULT_INITIAL_CAPACITY = 8
  final val DEFAULT_LOAD_FACTOR = 0.9f
  final val DEFAULT_CONCURRENCY_LEVEL = 1
}

  
  
  class ConcHashMapWithCapacity[T,U](initialCapacity:Int) extends 
		  ConcurrentHashMap[T,U](initialCapacity, HashMapBindings.DEFAULT_LOAD_FACTOR, HashMapBindings.DEFAULT_CONCURRENCY_LEVEL)
		  
  class DfltConcHashMap[T,U] extends ConcHashMapWithCapacity[T,U](HashMapBindings.DEFAULT_INITIAL_CAPACITY)

