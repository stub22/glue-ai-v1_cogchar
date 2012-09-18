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

package org.cogchar.lifter.model

// A resource of codes used to trigger various Actors
object ActorCodes {
  final val SPEECH_REQUEST_CODE = 201
  final val LOAD_PAGE_CODE = 202
  final val SPEECH_OUT_CODE = 203
  final val CONTINUOUS_SPEECH_REQUEST_START_CODE = 204
  final val CONTINUOUS_SPEECH_REQUEST_STOP_CODE = 205
  final val REFRESH_PAGE_CODE = 206
  
  final val TEMPLATE_CODE = 301
}
