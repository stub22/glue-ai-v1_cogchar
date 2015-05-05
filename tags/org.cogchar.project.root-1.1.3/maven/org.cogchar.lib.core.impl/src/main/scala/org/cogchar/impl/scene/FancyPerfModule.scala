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

package org.cogchar.impl.scene

import org.appdapter.module.basic.EmptyTimedModule
import org.cogchar.api.perform.{FancyPerformance, Performance}


/**
==
 * @author Stu B. <www.texpedient.com>
 */


class FancyPerfMonitorModule(val myPerf : FancyPerformance) extends EmptyTimedModule[BScene] {
	private var		myCachedPerfState = Performance.State.INITING

	def getPerfState : Performance.State = myCachedPerfState

	override protected def doStart(scn : BScene) {
		myRunDebugModulus = 25;
	}
	override protected def doRunOnce(scn : BScene,  runSeqNum : Long) {
		// This outer match on FancyBScene is now superfluous and may be removed if it gets in the way.
		scn match {
			case fbs : FancyBScene => {
				myPerf.syncWithFancyPerfChanNow
				myCachedPerfState = myPerf.getFancyPerfState
				myCachedPerfState match {
					case Performance.State.STOPPING => {
						getLogger.info("Performance state is STOPPING, so now stopping the perf-monitor-module on {}", Array[Object](myPerf))
						markStopRequested
					}
					case Performance.State.PLAYING => {
						getLogger.debug("Performance state is {} (=PLAYING!), so doing nothing on perf {}.", Array[Object]( myCachedPerfState, myPerf))
					}
					case _ => {
						getLogger.debug("Performance state is {}, so doing nothing on perf {}", Array[Object]( myCachedPerfState, myPerf))
					}
				}
			}
			case _ => {
				getLogger().warn("Cannot monitor fancy perf on behalf of un-fancy scene [{}], now stopping the monitor-module for {}", Array[Object]( scn, myPerf))
				markStopRequested
			}
		}
	}
}

/*
 * 		INITING,
		CUEING,
		PLAYING,
		PAUSING,
		STOPPING
 */