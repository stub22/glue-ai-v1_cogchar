/*
 *  Copyright 2012 by The Appdapter Project (www.appdapter.org).
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
package org.cogchar.render.opengl.bony.model;

import org.appdapter.module.basic.EmptyTimedModule;

/**
 * @author Stu B. <www.texpedient.com>
 */
public abstract class RenderModule extends EmptyTimedModule<RenderModulator> {

	protected abstract void doRenderCycle(long runSeqNum, float timePerFrame);

	@Override public synchronized void doRunOnce(RenderModulator rm, long runSeqNum) {		
		float currentTPF = rm.getCurrentTimePerFrame();
		doRenderCycle(runSeqNum, currentTPF);
	}

}
